package com.github.jntakpe.users.service

import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.repository.UserRepository
import com.github.jntakpe.users.shared.insertError
import com.github.jntakpe.users.shared.logger
import reactor.core.publisher.Mono
import javax.inject.Singleton

@Singleton
class UserService(private val repository: UserRepository) {

    private val log = logger()

    fun findByUsername(username: String): Mono<User> {
        return repository.findByUsername(username)
            .doOnSubscribe { log.debug("Searching user by username {}", username) }
            .doOnNext { log.debug("{} retrieved using it's username", it) }
            .switchIfEmpty(Mono.empty<User>().doOnSubscribe { log.info("No user found for username {}", username) })
    }

    fun create(user: User): Mono<User> {
        return repository.create(user)
            .doOnSubscribe { log.debug("Creating {}", user) }
            .doOnNext { log.info("{} created", it) }
            .onErrorMap { it.insertError(user, log) }
    }
}
