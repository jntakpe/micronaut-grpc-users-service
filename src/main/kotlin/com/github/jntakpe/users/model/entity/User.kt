package com.github.jntakpe.users.model.entity

import org.bson.types.ObjectId

data class User(val username: String,
                val email: String,
                val firstName: String? = null,
                val lastName: String? = null,
                val phoneNumber: Long? = null,
                override val id: ObjectId = ObjectId()
) : Identifiable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (username != other.username) return false

        return true
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }

    override fun toString(): String {
        return "User(username='$username')"
    }
}
