package com.yareach.voting_tictactoe_system.common.error

import io.grpc.StatusRuntimeException

data class ApiException(
    val errorCode: ErrorCode,
    val detail: String? = null,
) : RuntimeException()

fun ApiException.toGrpcRuntimeError(): StatusRuntimeException = errorCode.state.withDescription(detail).asRuntimeException()