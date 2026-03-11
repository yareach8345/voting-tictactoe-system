package com.yareach.voting_tictactoe_system.common.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val state: HttpStatus,
    val message: String,
    val errorCode: String
) {
    GAME_INFO_NOTFOUND(HttpStatus.NOT_FOUND, "Game info not found", "Game info not found"),
    GROUP_ID_DUPLICATE(HttpStatus.BAD_REQUEST, "Duplicate group id", "Duplicate group id"),

    NOT_ENOUGH_PLAYERS(HttpStatus.UNPROCESSABLE_ENTITY, "Not enough players to start the game.", "NOT_ENOUGH_PLAYERS"),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "Group not found.", "GROUP_NOT_FOUND"),

    OUT_OF_BOARD(HttpStatus.CONFLICT, "Coordinates must be between 0 and 2.", "OUT_OF_BOARD"),
    ALREADY_OCCUPIED_CELL(HttpStatus.CONFLICT, "The cell is already occupied.", "ALREADY_OCCUPIED_CELL"),
    GAME_IS_ENDED(HttpStatus.CONFLICT, "Game is ended", "GAME_IS_ENDED"),

    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error.", "INTERNAL"),
}