package com.github.jntakpe.users.endpoints

import com.github.jntakpe.users.ReactorUsersServiceGrpc
import com.github.jntakpe.users.Users
import com.github.jntakpe.users.mappings.toEntity
import com.github.jntakpe.users.mappings.toResponse
import com.github.jntakpe.users.service.UserService
import reactor.core.publisher.Mono
import javax.inject.Singleton

@Singleton
class UsersEndpoint(private val userService: UserService) : ReactorUsersServiceGrpc.UsersServiceImplBase() {

    override fun findByUsername(request: Mono<Users.UsersByUsernameRequest>): Mono<Users.UserResponse> {
        return request
                .map {
                    with(Users.UserResponse.newBuilder()) {
                        username = "jntakpe"
                        email = "jntakpe@mail.com"
                        lastName = "N'takpé"
                        build()
                    }
                }
    }

    override fun create(request: Mono<Users.UserRequest>): Mono<Users.UserResponse> {
        return request
                .flatMap { userService.create(it.toEntity()) }
                .map { it.toResponse() }
    }
}
