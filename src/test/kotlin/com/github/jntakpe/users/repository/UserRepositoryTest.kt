package com.github.jntakpe.users.repository

import com.github.jntakpe.users.dao.UserDao
import com.github.jntakpe.users.model.entity.User
import com.mongodb.MongoWriteException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import reactor.kotlin.test.test
import java.util.*

@MicronautTest
internal class UserRepositoryTest(private val repository: UserRepository, private val dao: UserDao) {


    @BeforeEach
    fun setup() {
        dao.init()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by id should find one`(user: User) {
        val id = user.id
        repository.findById(id).test()
            .consumeNextWith { assertThat(it.id).isEqualTo(id) }
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.TransientData::class)
    fun `find by username should return empty`(user: User) {
        repository.findById(user.id).test()
            .expectNextCount(0)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by username should find one`(user: User) {
        val username = user.username
        repository.findByUsername(username).test()
            .consumeNextWith { assertThat(it.username).isEqualTo(username) }
            .verifyComplete()
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown", "", "*"])
    fun `find by username should return empty`(username: String) {
        repository.findByUsername(username).test()
            .expectNextCount(0)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.TransientData::class)
    fun `create should add document`(user: User) {
        val initSize = dao.count()
        repository.create(user).test()
            .consumeNextWith {
                assertThat(it).isEqualTo(user)
                assertThat(dao.count()).isNotZero().isEqualTo(initSize + 1)
            }
            .verifyComplete()
    }

    @Test
    fun `create should fail when username already exists`() {
        val initSize = dao.count()
        repository.create(User(UserDao.PersistedData.JDOE_USERNAME, UserDao.PersistedData.JDOE_MAIL, Locale.FRANCE.country)).test()
            .consumeErrorWith {
                assertThat(it).isInstanceOf(MongoWriteException::class.java)
                assertThat(dao.count()).isEqualTo(initSize)
            }
            .verify()
    }

    @Test
    fun `create should fail when email already exists`() {
        val initSize = dao.count()
        repository.create(User("new", UserDao.PersistedData.JDOE_MAIL, Locale.FRANCE.country)).test()
            .consumeErrorWith {
                assertThat(it).isInstanceOf(MongoWriteException::class.java)
                assertThat(dao.count()).isEqualTo(initSize)
            }
            .verify()
    }
}
