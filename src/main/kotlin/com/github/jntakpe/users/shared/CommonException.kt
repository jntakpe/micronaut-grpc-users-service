package com.github.jntakpe.users.shared

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException

class CommonException(
    override val message: String,
    val log: ErrorLoggingFunction,
    code: Status.Code,
    cause: Throwable? = null,
    metadata: Metadata? = null
) : StatusRuntimeException(Status.fromCode(code).withDescription(message).withCause(cause), metadata)
