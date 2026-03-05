package com.yareach.voting_tictactoe_system.common.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val state: HttpStatus,
    val message: String,
    val errorCode: String
) {
    NOT_ENOUGH_PLAYERS(HttpStatus.UNPROCESSABLE_ENTITY, "Not enough players to start the game.", "NOT_ENOUGH_PLAYERS"),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "Group not found.", "GROUP_NOT_FOUND"),

    OUT_OF_BOARD(HttpStatus.CONFLICT, "Coordinates must be between 0 and 2.", "OUT_OF_BOARD"),

    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error.", "INTERNAL"),
}