package com.yareach.voting_tictactoe_system.common.error

import io.grpc.Status

enum class ErrorCode(
    val state: Status,
    val message: String,
    val errorCode: String
) {
    GAME_INFO_NOTFOUND(Status.NOT_FOUND, "Game info not found", "GAME_INFO_NOT_FOUND"),
    GROUP_ID_DUPLICATE(Status.INVALID_ARGUMENT, "Duplicate group id", "DUPLICATE_GROUP_ID"),
    GAME_STATE_ERROR(Status.ABORTED, "Game state error", "GAME_STATE_ERROR"),

    NOT_ENOUGH_PLAYERS(Status.INTERNAL, "Not enough players to start the game.", "NOT_ENOUGH_PLAYERS"),
    INVALID_GROUP_ID(Status.NOT_FOUND, "Group not found.", "INVALID_GROUP_ID"),
    PLAYER_RECRUIT_TIMEOUT(Status.DEADLINE_EXCEEDED, "Player recruit timeout.", "PLAYER_RECRUIT_TIMEOUT"),

    OUT_OF_BOARD(Status.OUT_OF_RANGE, "Coordinates must be between 0 and 2.", "OUT_OF_BOARD"),
    GAME_NOT_EXISTS(Status.NOT_FOUND, "Game not exists.", "GAME_NOT_FOUND"),

    WRONG_MESSAGE_ORDER(Status.INVALID_ARGUMENT, "Wrong message order", "WRONG_MESSAGE_ORDER"),

    INTERNAL_ERROR(Status.INTERNAL, "Internal error", "INTERNAL ERROR"),
}