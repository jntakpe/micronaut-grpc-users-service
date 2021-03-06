package com.github.jntakpe.users.repository

import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.model.entity.User_.Companion.Email
import com.github.jntakpe.users.model.entity.User_.Companion.Username
import com.mongodb.client.model.IndexOptions
import com.mongodb.reactivestreams.client.MongoDatabase
import com.mongodb.reactor.client.toReactor
import org.bson.types.ObjectId
import org.litote.kmongo.ascending
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.getCollection
import org.litote.kmongo.reactor.findOne
import org.litote.kmongo.reactor.findOneById
import reactor.core.publisher.Mono
import javax.inject.Singleton

@Singleton
class UserRepository(database: MongoDatabase) {

    private val collection = database.getCollection<User>().toReactor()

    init {
        collection.createIndex(ascending(Username), IndexOptions().unique(true)).subscribe()
        collection.createIndex(ascending(Email), IndexOptions().unique(true)).subscribe()
    }

    fun findById(id: ObjectId): Mono<User> = collection.findOneById(id)

    fun findByUsername(username: String): Mono<User> = collection.findOne(Username eq username)

    fun create(user: User): Mono<User> = collection.insertOne(user).thenReturn(user)
}
