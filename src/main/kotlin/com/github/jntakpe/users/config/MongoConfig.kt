package com.github.jntakpe.users.config

import com.mongodb.reactivestreams.client.MongoDatabase
import io.micronaut.configuration.mongo.core.DefaultMongoConfiguration
import io.micronaut.configuration.mongo.reactive.DefaultReactiveMongoClientFactory
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import org.litote.kmongo.reactivestreams.KMongo
import javax.inject.Singleton

@Factory
@Replaces(factory = DefaultReactiveMongoClientFactory::class)
class MongoConfig {

    @Singleton
    fun databaseClient(config: DefaultMongoConfiguration): MongoDatabase {
        return KMongo.createClient(config.buildSettings()).getDatabase("micronaut_users")
    }
}
