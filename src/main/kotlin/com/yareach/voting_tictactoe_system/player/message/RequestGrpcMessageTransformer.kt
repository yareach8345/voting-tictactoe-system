package com.yareach.voting_tictactoe_system.player.message

import com.yareach.voting_tictactoe_system.player.dto.AddNewPlayerDto
import com.yareach.voting_tictactoe_system.player.dto.InitRecruitDto
import com.yareach.voting_tictactoe_system.player.proto.RecruitRequestMessage
import io.grpc.Status

object RequestGrpcMessageTransformer {
    fun grpcMessageToDto(
        grpcMessage: RecruitRequestMessage,
    ) = when(grpcMessage.payloadCase) {
        RecruitRequestMessage.PayloadCase.INIT -> InitRecruitDto(grpcMessage.init.groupId)
        RecruitRequestMessage.PayloadCase.ADDUSER -> AddNewPlayerDto(grpcMessage.addUser.userId)
        else -> throw Status.INVALID_ARGUMENT.withDescription("illegal payload case ${grpcMessage.payloadCase}").asRuntimeException()
    }
}