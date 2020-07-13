package com.github.jntakpe.users.endpoints

import com.github.jntakpe.users.users.Users
import com.github.jntakpe.users.users.UsersServiceGrpc
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class UsersEndpoint : UsersServiceGrpc.UsersServiceImplBase() {
    override fun findByUsername(request: Users.UsersByUsernameRequest, responseObserver: StreamObserver<Users.UsersResponse>) {
        val user = with(Users.UsersResponse.newBuilder()) {
            username = "jntakpe"
            email = "jntakpe@mail.com"
            lastName = "N'takpé"
            build()
         }
        responseObserver.onNext(user)
        responseObserver.onCompleted()
    }
}
