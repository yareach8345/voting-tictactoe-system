package com.yareach.voting_tictactoe_system.voting_system.dto

data class VoteRequestDto(
    val userId: String,
    val item: String
)