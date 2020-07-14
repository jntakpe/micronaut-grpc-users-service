package com.github.jntakpe.users.config

import com.mongodb.WriteConcern.W1
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoDatabase
import io.micronaut.context.annotation.Factory
import org.litote.kmongo.reactivestreams.withKMongo
import javax.inject.Singleton

@Factory
class MongoConfig {

    @Singleton
    fun databaseClient(client: MongoClient): MongoDatabase = client.getDatabase("micronaut_users").withWriteConcern(W1).withKMongo()
}
