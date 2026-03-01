package com.yareach.voting_tictactoe_system.common.error

data class ApiException(
    val errorCode: ErrorCode,
    val detail: String? = null,
) : RuntimeException()