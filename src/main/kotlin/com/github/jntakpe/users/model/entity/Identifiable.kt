package com.github.jntakpe.users.model.entity

import org.bson.types.ObjectId

interface Identifiable {
    val id: ObjectId
}
