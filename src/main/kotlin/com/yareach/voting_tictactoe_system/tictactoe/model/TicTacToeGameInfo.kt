package com.yareach.voting_tictactoe_system.tictactoe.model

import com.yareach.voting_tictactoe_system.common.enum.GameType

data class TicTacToeGameInfo(
    val id: Long? = null,
    val groupId: String,
    val type: GameType,
) {
    companion object {
        fun of(
            groupId: String,
            gameType: GameType,
        ): TicTacToeGameInfo {
            return TicTacToeGameInfo(
                groupId = groupId,
                type = gameType,
            )
        }
    }
}