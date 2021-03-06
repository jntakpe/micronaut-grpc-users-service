package com.github.jntakpe.users.mapping

import com.github.jntakpe.commons.context.CommonException
import com.github.jntakpe.commons.context.ScriptLogger
import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.proto.UserResponse
import com.github.jntakpe.users.proto.Users
import io.grpc.Status
import java.util.*

private val log = ScriptLogger.log
private val isoCodes = Locale.getISOCountries().toList()

fun Users.UserRequest.toEntity() = User(
    username,
    email,
    countryCode.resolveCountry(),
    firstName.orNull(),
    lastName.orNull(),
    phoneNumber.orNull().removeWhitespaces()
)

fun User.toResponse() = UserResponse {
    val entity = this@toResponse
    username = entity.username
    email = entity.email
    firstName = entity.firstName.orEmpty()
    lastName = entity.lastName.orEmpty()
    phoneNumber = entity.phoneNumber.orEmpty()
    countryCode = entity.countryCode
    id = entity.id.toString()
}

private fun String.orNull() = ifEmpty { null }

private fun String?.removeWhitespaces() = this?.filter { !it.isWhitespace() }

private fun String.resolveCountry(): String {
    return takeIf { isoCodes.contains(this) }
        ?: throw CommonException("Country field should an ISO3166-1 alpha-2 code", log::debug, Status.Code.INVALID_ARGUMENT)
}
