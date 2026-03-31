package com.yareach.voting_tictactoe_system.tictactoe.dto

import com.yareach.voting_tictactoe_system.tictactoe.model.Cell

sealed interface TakeTurnRequestDto

data class InitTakeTurnDto(
    val groupId: String
): TakeTurnRequestDto

data class VoteDto(
    val userId: String,
    val cell: Cell
): TakeTurnRequestDto
