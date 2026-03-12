package com.yareach.voting_tictactoe_system.common.extension

import com.yareach.voting_tictactoe_system.common.enum.GameType
import com.yareach.voting_tictactoe_system.common.enum.GameType.*
import com.yareach.voting_tictactoe_system.game_group_info.proto.GameType as GameTypeForProto

fun GameType.toGameTypeForProto(): GameTypeForProto {
    return when (this) {
        NORMAL -> GameTypeForProto.NORMAL
        INFINITY -> GameTypeForProto.INFINITY
    }
}

fun GameTypeForProto.toDomain(): GameType {
    return when (this) {
        GameTypeForProto.NORMAL -> NORMAL
        GameTypeForProto.INFINITY -> INFINITY
        else -> throw IllegalStateException("Unsupported game type: ${this.name}")
    }
}