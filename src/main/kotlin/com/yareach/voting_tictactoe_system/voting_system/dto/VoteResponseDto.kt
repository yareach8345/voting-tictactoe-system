package com.yareach.voting_tictactoe_system.voting_system.dto

import java.time.LocalDateTime

data class VoteResponseDto(
    val electionId: String,
    val userId: String,
    val item: String,
    val votedAt: LocalDateTime
)
