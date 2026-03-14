package com.yareach.voting_tictactoe_system.common.error

fun catchApiException(throwable: Throwable) {
    if(throwable is ApiException) {
        throw throwable.errorCode.state.withDescription(throwable.detail).asRuntimeException()
    }
}

suspend fun<R> withCatchingApiException(run: suspend () -> R): R {
    try {
        return run()
    } catch (apiException: ApiException) {
        throw apiException.errorCode.state.withDescription(apiException.detail).asRuntimeException()
    }
}