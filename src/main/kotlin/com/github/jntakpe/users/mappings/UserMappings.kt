package com.github.jntakpe.users.mappings

import com.github.jntakpe.users.Users
import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.shared.CommonException
import com.github.jntakpe.users.shared.ScriptLogger
import io.grpc.Status
import java.util.*

private val log = ScriptLogger.log
private val isoCodes = Locale.getISOCountries().toList()

fun Users.UserRequest.toEntity() =
    User(username, email, countryCode.resolveCountry(), firstName.orNull(), lastName.orNull(), phoneNumber.orNull().removeWhitespaces())

fun User.toResponse(): Users.UserResponse = Users.UserResponse.newBuilder().let {
    it.username = username
    it.email = email
    it.firstName = firstName.orEmpty()
    it.lastName = lastName.orEmpty()
    it.phoneNumber = phoneNumber.orEmpty()
    it.countryCode = countryCode
    it.id = id.toString()
    it.build()
}

private fun String.orNull() = ifEmpty { null }

private fun String?.removeWhitespaces() = this?.filter { !it.isWhitespace() }

private fun String.resolveCountry(): String {
    return takeIf { isoCodes.contains(this) }
        ?: throw CommonException("Country field should an ISO3166-1 alpha-2 code", log::debug, Status.Code.INVALID_ARGUMENT)
}
