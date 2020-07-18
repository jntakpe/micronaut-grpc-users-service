package com.github.jntakpe.users.config

import com.github.jntakpe.users.UsersServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import javax.inject.Singleton

@Factory
class ClientConfig {

    @Singleton
    fun serverStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): UsersServiceGrpc.UsersServiceBlockingStub {
        return UsersServiceGrpc.newBlockingStub(channel)
    }
}
