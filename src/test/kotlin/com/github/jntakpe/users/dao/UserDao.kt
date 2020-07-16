package com.github.jntakpe.users.dao

import com.github.jntakpe.users.model.entity.User
import com.github.jntakpe.users.shared.MongoDao
import com.github.jntakpe.users.shared.TestDataProvider
import com.mongodb.reactivestreams.client.MongoDatabase
import org.litote.kmongo.reactivestreams.getCollection
import java.util.*
import javax.inject.Singleton

@Singleton
class UserDao(database: MongoDatabase) : MongoDao<User>(database.getCollection(), PersistedData) {

    object PersistedData : TestDataProvider<User> {
        const val JDOE_USERNAME = "jdoe"
        const val JDOE_MAIL = "jdoe@mail.com"
        const val MMOE_USERNAME = "mmoe"
        const val MMOE_MAIL = "mmoe@mail.com"

        override fun data() = listOf(jdoe(), mmoe())

        private fun jdoe() = User(JDOE_USERNAME, JDOE_MAIL, Locale.FRANCE.country, "John", "Doe", "+33123456789")

        private fun mmoe() = User(MMOE_USERNAME, MMOE_MAIL, Locale.ENGLISH.country)
    }

    object TransientData : TestDataProvider<User> {
        const val RROE_USERNAME = "rroe"
        const val RROE_MAIL = "rroe@mail.com"
        const val JOHN_SMITH = "jsmith"
        const val JOHN_MAIL = "jsmith@mail.com"

        override fun data() = listOf(rroe(), jsmith())

        private fun rroe() = User(RROE_USERNAME, RROE_MAIL, Locale.FRANCE.country, "Richard", "Roe", "+339877654321")

        private fun jsmith() = User(JOHN_SMITH, JOHN_MAIL, Locale.ENGLISH.country)
    }
}
