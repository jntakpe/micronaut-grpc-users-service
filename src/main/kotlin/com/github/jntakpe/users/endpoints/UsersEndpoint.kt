package com.github.jntakpe.users.endpoints

import com.github.jntakpe.users.Users
import com.github.jntakpe.users.UsersServiceGrpc
import com.github.jntakpe.users.mappings.toEntity
import com.github.jntakpe.users.mappings.toResponse
import com.github.jntakpe.users.service.UserService
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class UsersEndpoint(private val userService: UserService) : UsersServiceGrpc.UsersServiceImplBase() {

    override fun findByUsername(request: Users.UsersByUsernameRequest, responseObserver: StreamObserver<Users.UserResponse>) {
        val user = with(Users.UserResponse.newBuilder()) {
            username = "jntakpe"
            email = "jntakpe@mail.com"
            lastName = "N'takpé"
            build()
        }
        responseObserver.onNext(user)
        responseObserver.onCompleted()
    }

    override fun create(request: Users.UserRequest, responseObserver: StreamObserver<Users.UserResponse>) {
        userService.create(request.toEntity())
                .doOnNext {
                    responseObserver.onNext(it.toResponse())
                    responseObserver.onCompleted()
                }
                .subscribe()
    }
}
