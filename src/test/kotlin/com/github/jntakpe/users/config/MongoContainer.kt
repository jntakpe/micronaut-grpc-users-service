package com.github.jntakpe.users.config

import org.testcontainers.containers.MongoDBContainer

object MongoContainer {

    val instance: MongoDBContainer by lazy {
        MongoDBContainer("mongo:4.0.20").apply { start() }
    }
}
