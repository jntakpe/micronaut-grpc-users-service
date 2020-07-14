package com.github.jntakpe.users.service

import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.repository.UserRepository
import com.github.jntakpe.users.shared.logger
import reactor.core.publisher.Mono
import javax.inject.Singleton

@Singleton
class UserService(private val repository: UserRepository) {

    private val logger = logger()

    fun findByUsername(username: String): Mono<User> {
        return repository.findByUsername(username)
            .doOnSubscribe { logger.info("Finding user by username {}", username) }
            .doOnNext { logger.info("User {} retrieved using it's username", it) }
            .switchIfEmpty(Mono.empty<User>().doOnSubscribe { logger.info("No user found for username {}", username) })
    }

    fun create(user: User): Mono<User> {
        return repository.create(user)
            .doOnSubscribe { logger.info("Creating user {}", user) }
            .doOnNext { logger.info("User {} created", it) }
    }
}
