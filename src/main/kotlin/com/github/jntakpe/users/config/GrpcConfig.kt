package com.github.jntakpe.users.config

import com.github.jntakpe.commons.ReactorExceptionLogger
import com.github.jntakpe.users.Users
import com.github.jntakpe.users.UsersValidator
import io.envoyproxy.pgv.ExplicitValidatorIndex
import io.envoyproxy.pgv.grpc.ValidatingServerInterceptor
import io.grpc.BindableService
import io.grpc.ServerInterceptor
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.util.TransmitStatusRuntimeExceptionInterceptor
import io.micronaut.context.annotation.Factory
import javax.inject.Singleton

@Factory
class GrpcConfig {

    @Singleton
    fun validatorInterceptor(): ValidatingServerInterceptor {
        val index = ExplicitValidatorIndex()
            .add(Users.UserRequest::class.java, UsersValidator.UserRequestValidator())
        return ValidatingServerInterceptor(index)
    }

    @Singleton
    fun reflectionService(): BindableService = ProtoReflectionService.newInstance()

    @Singleton
    fun errorInterceptor(): ServerInterceptor = ReactorExceptionLogger.run { TransmitStatusRuntimeExceptionInterceptor.instance() }
}
