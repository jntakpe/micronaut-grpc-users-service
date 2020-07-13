package com.github.jntakpe.users.service

import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.repository.UserRepository
import com.github.jntakpe.users.shared.logger
import reactor.core.publisher.Mono
import javax.inject.Singleton

@Singleton
class UserService(private val repository: UserRepository) {

    private val logger = logger()

    fun create(user: User): Mono<User> {
        return repository.create(user)
                .doOnSubscribe { logger.debug("Creating user {}", user) }
                .doOnNext { logger.info("User {} created", it) }
    }
}
