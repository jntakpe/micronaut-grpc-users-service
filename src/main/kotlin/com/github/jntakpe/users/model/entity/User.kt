package com.github.jntakpe.users.model.entity

import com.github.jershell.kbson.ObjectIdSerializer
import com.github.jntakpe.commons.mongo.Identifiable
import com.github.jntakpe.commons.mongo.Identifiable.Companion.DB_ID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import org.litote.kmongo.Data

@Data
@Serializable
data class User(
    val username: String,
    val email: String,
    val countryCode: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    @SerialName(DB_ID) @Serializable(ObjectIdSerializer::class) override val id: ObjectId = ObjectId()
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
