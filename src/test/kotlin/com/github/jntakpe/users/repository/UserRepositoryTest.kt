package com.github.jntakpe.users.repository

import com.github.jntakpe.users.dao.UserDao
import com.github.jntakpe.users.model.entity.User
import com.mongodb.MongoWriteException
import com.mongodb.reactivestreams.client.MongoDatabase
import io.micronaut.test.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import org.litote.kmongo.reactivestreams.getCollection
import reactor.kotlin.test.test
import java.util.*

@MicronautTest
internal class UserRepositoryTest(
    private val userRepository: UserRepository,
    private val userDao: UserDao,
    private val database: MongoDatabase
) {

    private val collection = database.getCollection<User>()

    @BeforeEach
    fun setup() {
        userDao.init()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.SavedData::class)
    fun `find by username should find one`(user: User) {
        val username = user.username
        userRepository.findByUsername(username).test()
            .consumeNextWith { assertThat(it.username).isEqualTo(username) }
            .verifyComplete()
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown", ""])
    fun `find by username should return empty`(username: String) {
        userRepository.findByUsername(username).test()
            .expectNextCount(0)
            .verifyComplete()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.TransientData::class)
    fun `create should add document`(user: User) {
        val initSize = userDao.count()
        userRepository.create(user).test()
            .consumeNextWith {
                assertThat(it).isEqualTo(user)
                assertThat(userDao.count()).isNotZero().isEqualTo(initSize + 1)
            }
            .verifyComplete()
    }

    @Test
    fun `create should fail when username already exists`() {
        val initSize = userDao.count()
        userRepository.create(User(UserDao.SavedData.JDOE_USERNAME, UserDao.SavedData.JDOE_MAIL, Locale.FRANCE.country)).test()
            .consumeErrorWith {
                assertThat(it).isInstanceOf(MongoWriteException::class.java)
                assertThat(userDao.count()).isEqualTo(initSize)
            }
            .verify()
    }

    @Test
    fun `create should fail when email already exists`() {
        val initSize = userDao.count()
        userRepository.create(User("new", UserDao.SavedData.JDOE_MAIL, Locale.FRANCE.country)).test()
            .consumeErrorWith {
                assertThat(it).isInstanceOf(MongoWriteException::class.java)
                assertThat(userDao.count()).isEqualTo(initSize)
            }
            .verify()
    }
}
