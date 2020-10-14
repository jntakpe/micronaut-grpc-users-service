package com.github.jntakpe.users.config

import com.github.jntakpe.commons.grpc.GrpcValidator
import com.github.jntakpe.users.Users
import com.github.jntakpe.users.UsersValidator
import io.micronaut.context.annotation.Factory
import javax.inject.Singleton

@Factory
class GrpcValidatorConfig {

    @Singleton
    fun grpcValidators(): Iterable<GrpcValidator<*>> {
        return listOf(
            GrpcValidator(Users.UserRequest::class, UsersValidator.UserRequestValidator()),
            GrpcValidator(Users.UserResponse::class, UsersValidator.UserResponseValidator())
        )
    }
}
