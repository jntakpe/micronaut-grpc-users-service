package com.github.jntakpe.users.service

import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.repository.UserRepository
import com.github.jntakpe.users.shared.CommonException
import com.github.jntakpe.users.shared.logger
import com.mongodb.ErrorCategory
import com.mongodb.ErrorCategory.DUPLICATE_KEY
import com.mongodb.MongoWriteException
import io.grpc.Status
import reactor.core.publisher.Mono
import javax.inject.Singleton

@Singleton
class UserService(private val repository: UserRepository) {

    private val log = logger()

    fun findByUsername(username: String): Mono<User> {
        return repository.findByUsername(username)
            .doOnSubscribe { log.debug("Finding user by username {}", username) }
            .doOnNext { log.debug("User {} retrieved using it's username", it) }
            .switchIfEmpty(Mono.empty<User>().doOnSubscribe { log.info("No user found for username {}", username) })
    }

    fun create(user: User): Mono<User> {
        return repository.create(user)
            .doOnSubscribe { log.debug("Creating user {}", user) }
            .doOnNext { log.info("User {} created", it) }
            .onErrorMap { it.mongoError(user) }
    }

    private fun Throwable.mongoError(user: User): CommonException {
        return if (isDuplicateKey()) {
            CommonException(Status.ALREADY_EXISTS.withDescription("$user already exists").withCause(this), log::info)
        } else {
            CommonException(Status.INTERNAL.withDescription("Unable to store user $user").withCause(this), log::warn)
        }
    }

    private fun Throwable.isDuplicateKey() = this is MongoWriteException && ErrorCategory.fromErrorCode(this.error.code) === DUPLICATE_KEY
}
