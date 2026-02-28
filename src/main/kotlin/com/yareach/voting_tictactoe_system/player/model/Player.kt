package com.yareach.voting_tictactoe_system.player.model

import com.yareach.voting_tictactoe_system.player.common.Team

class Player(
    val id: Long? = null,
    val groupId: String,
    val userId: String,
    val team: Team
) {
    companion object {
        fun new(groupId: String, userId: String, team: Team) = Player(groupId = groupId, userId = userId, team = team)
    }
}