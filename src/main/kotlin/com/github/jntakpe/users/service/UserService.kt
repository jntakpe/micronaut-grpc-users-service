package com.github.jntakpe.users.service

import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.repository.UserRepository
import com.github.jntakpe.users.shared.CommonException
import com.github.jntakpe.users.shared.insertError
import com.github.jntakpe.users.shared.logger
import io.grpc.Status.Code.NOT_FOUND
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import javax.inject.Singleton

@Singleton
class UserService(private val repository: UserRepository) {

    private val log = logger()

    fun findByUsername(username: String): Mono<User> {
        return repository.findByUsername(username)
            .doOnSubscribe { log.debug("Searching user by username {}", username) }
            .doOnNext { log.debug("{} retrieved using it's username", it) }
            .switchIfEmpty(missingUserError(username).toMono())
    }

    fun create(user: User): Mono<User> {
        return repository.create(user)
            .doOnSubscribe { log.debug("Creating {}", user) }
            .doOnNext { log.info("{} created", it) }
            .onErrorMap { it.insertError(user, log) }
    }

    private fun missingUserError(username: String) = CommonException("No user found for username $username", log::debug, NOT_FOUND)
}
