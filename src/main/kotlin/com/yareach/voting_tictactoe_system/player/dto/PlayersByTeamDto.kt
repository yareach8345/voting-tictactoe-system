package com.yareach.voting_tictactoe_system.player.dto

import com.yareach.voting_tictactoe_system.player.proto.PlayersByTeam

data class PlayersByTeamDto (
    val userIdsInTeamX: Set<String>,
    val userIdsInTeamO: Set<String>
)

fun PlayersByTeamDto.toResult(): PlayersByTeam = PlayersByTeam.newBuilder()
    .addAllUserIdsInTeamX(userIdsInTeamX)
    .addAllUserIdsInTeamO(userIdsInTeamO)
    .build()