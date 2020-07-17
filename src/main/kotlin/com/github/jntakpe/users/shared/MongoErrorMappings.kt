package com.github.jntakpe.users.shared

import com.mongodb.ErrorCategory.DUPLICATE_KEY
import com.mongodb.ErrorCategory.fromErrorCode
import com.mongodb.MongoWriteException
import io.grpc.Status
import org.slf4j.Logger

fun <T : Identifiable> Throwable.insertError(entity: T, log: Logger): CommonException {
    return if (isDuplicateKey()) {
        CommonException(Status.ALREADY_EXISTS.withDescription("$entity already exists").withCause(this), log::info)
    } else {
        CommonException(Status.INTERNAL.withDescription("Unable to store $entity").withCause(this), log::warn)
    }
}

private fun Throwable.isDuplicateKey() = this is MongoWriteException && fromErrorCode(this.error.code) === DUPLICATE_KEY
