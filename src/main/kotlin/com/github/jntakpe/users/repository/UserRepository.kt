package com.github.jntakpe.users.repository

import com.github.jntakpe.users.model.entity.User
import com.mongodb.reactivestreams.client.MongoClient
import reactor.core.publisher.Mono
import javax.inject.Singleton

@Singleton
class UserRepository(client: MongoClient) {

    private val collection = client.getDatabase("micronaut_users").getCollection("users", User::class.java)

    fun create(user: User): Mono<User> {
        return Mono.from(collection.insertOne(user)).thenReturn(user)
    }
}
