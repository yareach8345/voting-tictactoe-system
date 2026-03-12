package com.yareach.voting_tictactoe_system.game_group_info.extension

import com.yareach.voting_tictactoe_system.game_group_info.enum.GameState
import com.yareach.voting_tictactoe_system.game_group_info.enum.GameState.*
import com.yareach.voting_tictactoe_system.game_group_info.proto.GameState as GameStateForGrpc

fun GameState.toGameStateForGrpc(): GameStateForGrpc {
    return when(this) {
        GENERATED -> GameStateForGrpc.GENERATED
        RECRUITING -> GameStateForGrpc.RECRUITING
        BEFORE_START -> GameStateForGrpc.BEFORE_START
        PLAYING -> GameStateForGrpc.PLAYING
        FINISHED -> GameStateForGrpc.FINISHED
    }
}

fun GameStateForGrpc.toDomain(): GameState {
    return when(this) {
        GameStateForGrpc.GENERATED -> GENERATED
        GameStateForGrpc.RECRUITING -> RECRUITING
        GameStateForGrpc.BEFORE_START -> BEFORE_START
        GameStateForGrpc.PLAYING -> PLAYING
        GameStateForGrpc.FINISHED -> FINISHED
        else -> throw IllegalStateException("Unsupported game state: ${this.name}")
    }
}