package com.github.jntakpe.users.service

import com.github.jntakpe.commons.cache.RedisReactiveCache
import com.github.jntakpe.commons.context.CommonException
import com.github.jntakpe.commons.context.logger
import com.github.jntakpe.commons.mongo.insertError
import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.repository.UserRepository
import io.grpc.Status.Code.NOT_FOUND
import org.bson.types.ObjectId
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserService(private val repository: UserRepository, @Named("users") private val usersCacheRedis: RedisReactiveCache) {

    private val log = logger()

    fun findById(id: ObjectId): Mono<User> {
        return usersCacheRedis.orPutOnCacheMiss(id) { repository.findById(id) }
            .doOnSubscribe { log.debug("Searching user by id {}", id) }
            .doOnNext { log.debug("{} retrieved using it's id", it) }
            .switchIfEmpty(missingIdError(id).toMono())
    }

    fun findByUsername(username: String): Mono<User> {
        return usersCacheRedis.orPutOnCacheMiss(username) { repository.findByUsername(username) }
            .doOnSubscribe { log.debug("Searching user by username {}", username) }
            .doOnNext { log.debug("{} retrieved using it's username", it) }
            .switchIfEmpty(missingUsernameError(username).toMono())
    }

    fun create(user: User): Mono<User> {
        return repository.create(user)
            .doOnSubscribe { log.debug("Creating {}", user) }
            .doOnNext { log.info("{} created", it) }
            .onErrorMap { it.insertError(user, log) }
            .doOnNext {
                usersCacheRedis.putAndForget(user.id, user)
                usersCacheRedis.putAndForget(user.username, user)
            }
    }

    private fun missingUsernameError(username: String) = CommonException("No user found for username $username", log::debug, NOT_FOUND)

    private fun missingIdError(id: ObjectId) = CommonException("No user found for id $id", log::debug, NOT_FOUND)
}
