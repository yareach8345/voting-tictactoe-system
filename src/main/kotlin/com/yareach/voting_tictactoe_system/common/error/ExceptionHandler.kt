package com.yareach.voting_tictactoe_system.common.error

suspend fun<R> withCatchingApiException(run: suspend () -> R): R {
    try {
        return run()
    } catch (apiException: ApiException) {
        throw apiException.errorCode.state.withDescription(apiException.detail).asRuntimeException()
    }
}