package com.github.jntakpe.users.endpoints

import com.github.jntakpe.commons.test.assertStatusException
import com.github.jntakpe.users.ByIdRequest
import com.github.jntakpe.users.UserRequest
import com.github.jntakpe.users.UsersByUsernameRequest
import com.github.jntakpe.users.UsersServiceGrpc
import com.github.jntakpe.users.dao.UserDao
import com.github.jntakpe.users.model.entity.User
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

@MicronautTest
internal class UsersEndpointTest(private val dao: UserDao, private val stub: UsersServiceGrpc.UsersServiceBlockingStub) {

    @BeforeEach
    fun setup() {
        dao.init()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by id should return ok response`(user: User) {
        val request = ByIdRequest { id = user.id.toString() }
        val response = stub.findById(request)
        assertThat(response.id).isNotEmpty().isEqualTo(user.id.toString())
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.TransientData::class)
    fun `find by id should fail when user does not exist`(user: User) {
        val request = ByIdRequest { id = user.id.toString() }
        val error = catchThrowable { stub.findById(request) }
        assertThat(error).isInstanceOf(StatusRuntimeException::class.java)
        error as StatusRuntimeException
        assertThat(error.status.code).isEqualTo(Status.NOT_FOUND.code)
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by username should return ok response`(user: User) {
        val request = UsersByUsernameRequest { username = user.username }
        val response = stub.findByUsername(request)
        assertThat(response.id).isNotEmpty()
        assertThat(response.username).isEqualTo(user.username)
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.TransientData::class)
    fun `find by username should fail when user does not exist`(user: User) {
        val request = UsersByUsernameRequest { username = user.username }
        val error = catchThrowable { stub.findByUsername(request) }
        assertThat(error).isInstanceOf(StatusRuntimeException::class.java)
        error as StatusRuntimeException
        assertThat(error.status.code).isEqualTo(Status.NOT_FOUND.code)
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.TransientData::class)
    fun `create should return ok response`(user: User) {
        val initSize = dao.count()
        val response = stub.create(userRequestMapping(user))
        assertThat(response.id).isNotEmpty()
        assertThat(dao.count()).isEqualTo(initSize + 1)
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `create should fail when user already exists`(user: User) {
        val initSize = dao.count()
        catchThrowable { stub.create(userRequestMapping(user)) }.assertStatusException(Status.ALREADY_EXISTS)
        assertThat(dao.count()).isEqualTo(initSize)
    }

    @Test
    fun `create should fail when invalid request`() {
        val request = UserRequest {
            username = "invalid"
            email = "wrong.mail"
            countryCode = "FR"
        }
        catchThrowable { stub.create(request) }.assertStatusException(Status.INVALID_ARGUMENT)
    }

    private fun userRequestMapping(user: User) = UserRequest {
        username = user.username
        email = user.email
        countryCode = user.countryCode
        firstName = user.firstName.orEmpty()
        lastName = user.lastName.orEmpty()
        phoneNumber = user.phoneNumber.orEmpty()
    }
}
