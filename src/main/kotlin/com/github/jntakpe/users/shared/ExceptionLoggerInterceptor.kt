package com.github.jntakpe.users.shared

import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.util.TransmitStatusRuntimeExceptionInterceptor

class ExceptionLoggerInterceptor : ServerInterceptor {

    private val log = logger()
    private val delegate = TransmitStatusRuntimeExceptionInterceptor.instance()

    override fun <ReqT : Any, RespT : Any> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate.interceptCall(call, headers, next)) {
            override fun onMessage(message: ReqT) {
                forwardException { super.onMessage(message) }
            }

            override fun onComplete() {
                forwardException { super.onComplete() }
            }

            override fun onCancel() {
                forwardException { super.onCancel() }
            }

            override fun onReady() {
                forwardException { super.onReady() }
            }

            override fun onHalfClose() {
                forwardException { super.onHalfClose() }
            }

            private fun forwardException(function: () -> Unit) {
                try {
                    function()
                } catch (e: CommonException) {
                    e.log(e.message, e)
                    throw e
                } catch (e: Exception) {
                    log.warn("An unexpected error occurred", e)
                    throw e
                }
            }
        }
    }
}
