package com.github.jntakpe.users.config

import io.grpc.protobuf.services.ProtoReflectionService
import io.micronaut.context.annotation.Factory
import javax.inject.Singleton

@Factory
class GrpcServerConfiguration {

    @Singleton
    fun reflectionService(): ProtoReflectionService {
        return ProtoReflectionService.newInstance() as ProtoReflectionService
    }
}
