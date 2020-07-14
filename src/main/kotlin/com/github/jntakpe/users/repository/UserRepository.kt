package com.github.jntakpe.users.repository

import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.model.entity.User_.Companion.Email
import com.github.jntakpe.users.model.entity.User_.Companion.Username
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.reactivestreams.client.MongoDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.getCollection
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import javax.inject.Singleton

@Singleton
class UserRepository(database: MongoDatabase) {

    private val collection = database.getCollection<User>()

    init {
        collection.createIndex(Indexes.ascending(Username.name), IndexOptions().unique(true)).toMono().subscribe()
        collection.createIndex(Indexes.ascending(Email.name), IndexOptions().unique(true)).toMono().subscribe()
    }

    fun findByUsername(username: String): Mono<User> = collection.find(Username eq username).toMono()

    fun create(user: User): Mono<User> = collection.insertOne(user).toMono().thenReturn(user)
}
