package com.yareach.voting_tictactoe_system.player.message

import com.yareach.voting_tictactoe_system.player.dto.RecruitAcceptedDto
import com.yareach.voting_tictactoe_system.player.dto.RecruitCompleted
import com.yareach.voting_tictactoe_system.player.dto.RecruitRejectedDto
import com.yareach.voting_tictactoe_system.player.dto.RecruitResponseDto

object ResponseGrpcMessageTransformer {

    fun responseDtoToProtoMessage(
        responseDto: RecruitResponseDto
    ) = when(responseDto) {
        is RecruitAcceptedDto -> RecruitStreamMessageFactory.buildAcceptMessage(responseDto.userId)
        is RecruitRejectedDto -> RecruitStreamMessageFactory.buildRejectedMessage(responseDto.userId)
        is RecruitCompleted -> RecruitStreamMessageFactory.buildCompletedMessage(responseDto.playersByTeam)
    }
}