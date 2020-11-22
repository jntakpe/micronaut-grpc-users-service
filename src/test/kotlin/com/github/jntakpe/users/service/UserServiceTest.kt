package com.github.jntakpe.users.service

import com.github.jntakpe.commons.cache.RedisReactiveCache
import com.github.jntakpe.commons.test.expectStatusException
import com.github.jntakpe.users.dao.UserDao
import com.github.jntakpe.users.dao.UserDao.TransientData
import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.repository.UserRepository
import com.mongodb.MongoWriteException
import com.mongodb.ServerAddress
import com.mongodb.WriteError
import io.grpc.Status
import io.micronaut.configuration.lettuce.cache.RedisCache
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.BsonDocument
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import javax.inject.Named

@MicronautTest
internal class UserServiceTest(
    private val service: UserService,
    private val dao: UserDao,
    private val userRepository: UserRepository,
    @Named("users") private val usersCache: RedisReactiveCache,
    @Named("users") private val rawCache: RedisCache,
) {

    @BeforeEach
    fun setup() {
        dao.init()
        rawCache.invalidateAll()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by id should return user`(user: User) {
        service.findById(user.id).test()
            .expectNext(user)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by id should call repository since cache miss`(user: User) {
        val repoSpy = spyk(userRepository)
        UserService(repoSpy, usersCache).findById(user.id).test()
            .expectNext(user)
            .then {
                verify { repoSpy.findById(user.id) }
                confirmVerified(repoSpy)
                assertThat(rawCache.get(user.id, User::class.java)).isPresent.get().isEqualTo(user)
            }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by id should not call repository since retrieved from cache`(user: User) {
        rawCache.put(user.id, user)
        val repoSpy = spyk(userRepository)
        UserService(repoSpy, usersCache).findById(user.id).test()
            .expectNext(user)
            .then {
                verify { repoSpy.findById(user.id) wasNot Called }
                confirmVerified(repoSpy)
            }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(TransientData::class)
    fun `find by id fail when user does not exists`(user: User) {
        service.findById(user.id).test()
            .expectStatusException(Status.NOT_FOUND)
            .verify()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by username should return user`(user: User) {
        service.findByUsername(user.username).test()
            .expectNext(user)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by username should call repository since cache miss`(user: User) {
        val repoSpy = spyk(userRepository)
        UserService(repoSpy, usersCache).findByUsername(user.username).test()
            .expectNext(user)
            .then {
                verify { repoSpy.findByUsername(user.username) }
                confirmVerified(repoSpy)
                assertThat(rawCache.get(user.username, User::class.java)).isPresent.get().isEqualTo(user)
            }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by username should not call repository since retrieved from cache`(user: User) {
        rawCache.put(user.username, user)
        val repoSpy = spyk(userRepository)
        UserService(repoSpy, usersCache).findByUsername(user.username).test()
            .expectNext(user)
            .then {
                verify { repoSpy.findByUsername(user.username) wasNot Called }
                confirmVerified(repoSpy)
            }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(TransientData::class)
    fun `find by username fail when user does not exists`(user: User) {
        service.findByUsername(user.username).test()
            .expectStatusException(Status.NOT_FOUND)
            .verify()
    }

    @ParameterizedTest
    @ArgumentsSource(TransientData::class)
    fun `create should return created document`(user: User) {
        service.create(user).test()
            .expectNext(user)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(TransientData::class)
    fun `create should put item in cache`(user: User) {
        val retrieveWithId = { rawCache.get(user.id, User::class.java) }
        val retrieveWithUsername = { rawCache.get(user.username, User::class.java) }
        assertThat(retrieveWithId()).isEmpty
        assertThat(retrieveWithUsername()).isEmpty
        service.create(user).test()
            .expectNext(user)
            .then {
                assertThat(retrieveWithId()).isPresent.get().isEqualTo(user)
                assertThat(retrieveWithUsername()).isPresent.get().isEqualTo(user)
            }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `create should fail with already exists code when integrity constraint violated`(user: User) {
        service.create(user).test()
            .expectStatusException(Status.ALREADY_EXISTS)
            .verify()
    }

    @Test
    fun `create should fail with internal code when unexpected mongo exception occurs`() {
        val mockedRepository = mockkClass(UserRepository::class)
        val exception = MongoWriteException(WriteError(999, "", BsonDocument.parse("{}")), ServerAddress("localhost"))
        every { mockedRepository.create(any()) } returns exception.toMono()
        every { mockedRepository.create(any()) } returns exception.toMono()
        UserService(mockedRepository, mockkClass(RedisReactiveCache::class, relaxed = true)).create(TransientData.data().first()).test()
            .expectStatusException(Status.INTERNAL)
            .verify()
    }

    @Test
    fun `create should fail with internal code when exception differs from mongo exception`() {
        val mockedRepository = mockkClass(UserRepository::class)
        every { mockedRepository.create(any()) } returns NullPointerException("Oops").toMono()
        UserService(mockedRepository, mockkClass(RedisReactiveCache::class, relaxed = true)).create(TransientData.data().first()).test()
            .expectStatusException(Status.INTERNAL)
            .verify()
    }
}
