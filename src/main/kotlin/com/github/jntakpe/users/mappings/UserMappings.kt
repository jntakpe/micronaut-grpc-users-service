package com.github.jntakpe.users.mappings

import com.github.jntakpe.users.Users
import com.github.jntakpe.users.model.entity.User
import java.util.*

fun Users.UserRequest.toEntity() = User(username, email, countryCode.resolveCountry(), firstName, lastName, phoneNumber)

fun User.toResponse(): Users.UserResponse = Users.UserResponse.newBuilder().let {
    it.username = username
    it.email = email
    it.firstName = firstName
    it.lastName = lastName
    it.phoneNumber = phoneNumber
    it.countryCode = countryCode
    it.id = id.toString()
    it.build()
}

private fun String?.resolveCountry() = let { Locale.forLanguageTag(this)?.country } ?: Locale.FRANCE.country
