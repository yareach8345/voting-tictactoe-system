package com.yareach.voting_tictactoe_system.voting_system.dto

import java.time.LocalDateTime

data class ChangeElectionStateResponseDto(
    val electionId: String,
    val state: String,
    val updatedAt: LocalDateTime,
)