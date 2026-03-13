package com.yareach.voting_tictactoe_system.player.dto

sealed interface RecruitResponseDto

data class RecruitAcceptedDto(
    val userId: String
): RecruitResponseDto

data class RecruitRejectedDto(
    val userId: String
): RecruitResponseDto

data class RecruitCompleted (
    val playersByTeam: PlayersByTeamDto
): RecruitResponseDto
