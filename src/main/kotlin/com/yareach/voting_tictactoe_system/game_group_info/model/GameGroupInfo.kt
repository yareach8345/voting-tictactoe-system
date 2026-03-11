package com.yareach.voting_tictactoe_system.game_group_info.model

import com.yareach.voting_tictactoe_system.game_group_info.enum.GameState
import com.yareach.voting_tictactoe_system.common.enum.GameType
import java.time.LocalDateTime

class GameGroupInfo(
    val id: Int? = null,
    val groupId: String,
    val gameType: GameType,
    var state: GameState,
    var lastUpdated: LocalDateTime,
) {
    companion object {
        fun new(groupId: String, gameType: GameType) = GameGroupInfo(
            groupId = groupId,
            gameType = gameType,
            state = GameState.GENERATED,
            lastUpdated = LocalDateTime.now()
        )
    }

    fun changeState(state: GameState) {
        this.state = state
    }

    fun setLastUpdatedToCurrentTime() {
        lastUpdated = LocalDateTime.now()
    }
}