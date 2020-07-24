package com.github.jntakpe.users.service

import com.github.jntakpe.users.dao.UserDao
import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.repository.UserRepository
import com.github.jntakpe.users.shared.expectStatusException
import com.mongodb.MongoWriteException
import com.mongodb.ServerAddress
import com.mongodb.WriteError
import io.grpc.Status
import io.micronaut.test.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockkClass
import org.bson.BsonDocument
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test

@MicronautTest
internal class UserServiceTest(private val service: UserService, private val dao: UserDao) {

    @BeforeEach
    fun setup() {
        dao.init()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by username should return user`(user: User) {
        service.findByUsername(user.username).test()
            .expectNext(user)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.TransientData::class)
    fun `find by username return empty`(user: User) {
        service.findByUsername(user.username).test()
            .expectNextCount(0)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.TransientData::class)
    fun `create should return created document`(user: User) {
        service.create(user).test()
            .expectNext(user)
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
        UserService(mockedRepository).create(UserDao.TransientData.data().first()).test()
            .expectStatusException(Status.INTERNAL)
            .verify()
    }

    @Test
    fun `create should fail with internal code when exception differs from mongo exception`() {
        val mockedRepository = mockkClass(UserRepository::class)
        every { mockedRepository.create(any()) } returns NullPointerException("Oops").toMono()
        UserService(mockedRepository).create(UserDao.TransientData.data().first()).test()
            .expectStatusException(Status.INTERNAL)
            .verify()
    }
}
