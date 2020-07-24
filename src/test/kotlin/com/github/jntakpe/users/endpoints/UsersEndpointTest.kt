package com.github.jntakpe.users.endpoints

import com.github.jntakpe.users.Users
import com.github.jntakpe.users.UsersServiceGrpc
import com.github.jntakpe.users.dao.UserDao
import com.github.jntakpe.users.model.entity.User
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

@MicronautTest
internal class UsersEndpointTest(private val dao: UserDao, private val serverStub: UsersServiceGrpc.UsersServiceBlockingStub) {

    @BeforeEach
    fun setup() {
        dao.init()
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.PersistedData::class)
    fun `find by username should return ok response`(user: User) {
        val request = Users.UsersByUsernameRequest.newBuilder().setUsername(user.username).build()
        val response = serverStub.findByUsername(request)
        assertThat(response.id).isNotNull()
        assertThat(response.username).isEqualTo(user.username)
    }

    @ParameterizedTest
    @ArgumentsSource(UserDao.TransientData::class)
    fun `find by username should fail when user does not exist`(user: User) {
        val request = Users.UsersByUsernameRequest.newBuilder().setUsername(user.username).build()
        val error = catchThrowable { serverStub.findByUsername(request) }
        assertThat(error).isInstanceOf(StatusRuntimeException::class.java)
        error as StatusRuntimeException
        assertThat(error.status.code).isEqualTo(Status.CANCELLED.code)
    }
}
