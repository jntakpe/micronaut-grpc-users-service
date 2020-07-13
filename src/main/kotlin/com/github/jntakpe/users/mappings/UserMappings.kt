package com.github.jntakpe.users.mappings

import com.github.jntakpe.users.Users
import com.github.jntakpe.users.model.entity.User

fun Users.UserRequest.toEntity() = User(username, email, firstName, lastName, phoneNumber)

fun User.toResponse(): Users.UserResponse = Users.UserResponse.newBuilder().let {
    it.username = username
    it.email = email
    it.firstName = firstName
    it.lastName = lastName
    it.phoneNumber = phoneNumber ?: 0
    it.id = id.toString()
    it.build()
}
