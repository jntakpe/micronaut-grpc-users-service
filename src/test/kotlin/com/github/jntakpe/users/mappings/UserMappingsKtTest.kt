package com.github.jntakpe.users.mappings

import com.github.jntakpe.commons.test.assertStatusException
import com.github.jntakpe.users.dao.UserDao.PersistedData.JDOE_MAIL
import com.github.jntakpe.users.dao.UserDao.PersistedData.JDOE_USERNAME
import com.github.jntakpe.users.dao.UserDao.PersistedData.jdoe
import com.github.jntakpe.users.dao.UserDao.PersistedData.mmoe
import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.proto.UserRequest
import com.github.jntakpe.users.proto.UserResponse
import io.grpc.Status
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import java.util.*

internal class UserMappingsKtTest {

    @Test
    fun `to entity should map partial request`() {
        val request = UserRequest {
            username = JDOE_USERNAME
            email = JDOE_MAIL
            countryCode = Locale.FRANCE.country
        }
        val entity = request.toEntity()
        val expected = User(JDOE_USERNAME, JDOE_MAIL, Locale.FRANCE.country)
        assertThat(entity).usingRecursiveComparison().ignoringFields(User::id.name).isEqualTo(expected)
    }

    @Test
    fun `to entity should map full request`() {
        val expected = jdoe
        val request = UserRequest {
            username = JDOE_USERNAME
            email = JDOE_MAIL
            countryCode = Locale.FRANCE.country
            firstName = expected.firstName
            lastName = expected.lastName
            phoneNumber = expected.phoneNumber
        }
        val entity = request.toEntity()
        assertThat(entity).usingRecursiveComparison().ignoringFields(User::id.name).isEqualTo(expected)
    }

    @Test
    fun `to entity should remove whitespace from phone number`() {
        val request = UserRequest {
            username = JDOE_USERNAME
            email = JDOE_MAIL
            countryCode = Locale.FRANCE.country
            phoneNumber = "+33 1 23 45 67 89"
        }
        assertThat(request.toEntity().phoneNumber).isEqualTo("+33123456789")
    }

    @Test
    fun `to entity should fail when country code not iso`() {
        val request = UserRequest {
            username = JDOE_USERNAME
            email = JDOE_MAIL
            countryCode = "ZY"
        }
        catchThrowable { request.toEntity() }.assertStatusException(Status.INVALID_ARGUMENT)
    }

    @Test
    fun `to response should map partial`() {
        val entity = mmoe
        val expected = UserResponse {
            username = entity.username
            email = entity.email
            countryCode = entity.countryCode
            firstName = ""
            lastName = ""
            phoneNumber = ""
            id = entity.id.toString()
        }
        assertThat(entity.toResponse()).usingRecursiveComparison().isEqualTo(expected)
    }

    @Test
    fun `to response should map full`() {
        val entity = jdoe
        val expected = UserResponse {
            username = entity.username
            email = entity.email
            countryCode = entity.countryCode
            firstName = entity.firstName
            lastName = entity.lastName
            phoneNumber = entity.phoneNumber
            id = entity.id.toString()
        }
        assertThat(entity.toResponse()).usingRecursiveComparison().isEqualTo(expected)
    }
}
