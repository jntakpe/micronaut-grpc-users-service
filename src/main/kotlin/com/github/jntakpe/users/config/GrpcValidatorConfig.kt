package com.github.jntakpe.users.config

import com.github.jntakpe.commons.grpc.GrpcValidator
import com.github.jntakpe.users.proto.Users
import com.github.jntakpe.users.proto.UsersValidator
import io.micronaut.context.annotation.Factory
import javax.inject.Singleton

@Factory
class GrpcValidatorConfig {

    @Singleton
    fun userRequestValidator() = GrpcValidator(Users.UserRequest::class, UsersValidator.UserRequestValidator())

    @Singleton
    fun userResponseValidator() = GrpcValidator(Users.UserResponse::class, UsersValidator.UserResponseValidator())
}
