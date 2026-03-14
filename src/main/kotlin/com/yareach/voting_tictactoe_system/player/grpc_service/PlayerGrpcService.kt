package com.yareach.voting_tictactoe_system.player.grpc_service

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.catchApiException
import com.yareach.voting_tictactoe_system.common.error.withCatchingApiException
import com.yareach.voting_tictactoe_system.player.dto.toResult
import com.yareach.voting_tictactoe_system.player.message.RequestGrpcMessageTransformer
import com.yareach.voting_tictactoe_system.player.message.ResponseGrpcMessageTransformer
import com.yareach.voting_tictactoe_system.player.proto.AllPlayerDeleteResult
import com.yareach.voting_tictactoe_system.player.proto.GroupId
import com.yareach.voting_tictactoe_system.player.proto.PlayerServiceGrpcKt
import com.yareach.voting_tictactoe_system.player.proto.RecruitRequestMessage
import com.yareach.voting_tictactoe_system.player.service.PlayerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class PlayerGrpcService(
    private val playerService: PlayerService,
): PlayerServiceGrpcKt.PlayerServiceCoroutineImplBase() {

    override fun recruitPlayer(requests: Flow<RecruitRequestMessage>) =
        playerService
            .processRecruitMessage(requests.map { RequestGrpcMessageTransformer.grpcMessageToDto(it) })
            .map { ResponseGrpcMessageTransformer.responseDtoToProtoMessage(it) }
            .catch{ catchApiException(it) }

    override suspend fun getUserIdsInGroup(request: GroupId) = withCatchingApiException {
        playerService.getPlayersByGroupId(request.groupId).toResult()
    }

    override suspend fun deleteAllUserInGroupId(request: GroupId): AllPlayerDeleteResult = withCatchingApiException {
        val groupId = request.groupId

        playerService.deleteAllPlayersByGroupId(groupId)

        AllPlayerDeleteResult.newBuilder()
            .setGroupId(groupId)
            .build()
    }
}