package com.yareach.voting_tictactoe_system.player.dto

sealed interface RecruitRequestDto

data class InitRecruitDto(
    val groupId: String
): RecruitRequestDto

data class AddNewPlayerDto(
    val userId: String
): RecruitRequestDto
