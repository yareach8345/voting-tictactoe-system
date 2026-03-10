package com.yareach.voting_tictactoe_system.game_group_info.model

import com.yareach.voting_tictactoe_system.tictactoe.enum.GameType

class GameGroupInfo(
    val id: Int? = null,
    val groupId: String,
    val gameType: GameType,
) {
    companion object {
        fun of(groupId: String, gameType: GameType) = GameGroupInfo(
            groupId = groupId,
            gameType = gameType
        )
    }
}