package com.yareach.voting_tictactoe_system.game_group_info.grpc_service

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.common.extension.toDomain
import com.yareach.voting_tictactoe_system.game_group_info.extension.toGameStateForGrpc
import com.yareach.voting_tictactoe_system.game_group_info.service.GameGroupInfoService
import com.yareach.voting_tictactoe_system.game_group_info.proto.AddGameInfoMessage
import com.yareach.voting_tictactoe_system.game_group_info.proto.CheckGameInfoExistenceResponseMessage
import com.yareach.voting_tictactoe_system.game_group_info.proto.GameGroupInfoServiceGrpcKt
import com.yareach.voting_tictactoe_system.game_group_info.proto.GroupIdMessage
import com.yareach.voting_tictactoe_system.game_group_info.proto.GroupInfoStatePair
import io.grpc.Status
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class GameGroupInfoGrpcService(
    private val gameGroupInfoService: GameGroupInfoService,
): GameGroupInfoServiceGrpcKt.GameGroupInfoServiceCoroutineImplBase() {

    override suspend fun addGameInfo(request: AddGameInfoMessage): GroupIdMessage {
        val groupId = request.groupId
        val gameType = request.gameType.toDomain()

        val createResult = try {
            gameGroupInfoService.createNewGameGroupInfo(groupId, gameType)
        } catch(e: ApiException) {
            when(e.errorCode) {
                ErrorCode.GROUP_ID_DUPLICATE -> throw Status.ALREADY_EXISTS.withDescription(e.message).asRuntimeException()
                else -> throw Status.INTERNAL.asRuntimeException()
            }
        }

        return GroupIdMessage.newBuilder()
            .setGroupId(createResult.groupId)
            .build()
    }

    override suspend fun deleteGameInfo(request: GroupIdMessage): GroupIdMessage {
        val groupId = request.groupId

        try {
            gameGroupInfoService.deleteGameGroupInfo(groupId)
        } catch (e: ApiException) {
            when(e.errorCode) {
                ErrorCode.GAME_INFO_NOTFOUND -> throw Status.NOT_FOUND.withDescription(e.message).asRuntimeException()
                else -> throw Status.INTERNAL.asRuntimeException()
            }
        }

        return GroupIdMessage.newBuilder()
            .setGroupId(groupId)
            .build()
    }

    override suspend fun getGameState(request: GroupIdMessage): GroupInfoStatePair {
        val groupId = request.groupId

        val gameGroupInfo = try {
            gameGroupInfoService.getGameGroupInfo(groupId)
        } catch (e: ApiException) {
            when(e.errorCode) {
                ErrorCode.GAME_INFO_NOTFOUND -> throw Status.NOT_FOUND.withDescription(e.message).asRuntimeException()
                else -> throw Status.INTERNAL.asRuntimeException()
            }
        }

        return GroupInfoStatePair.newBuilder()
            .setGroupId(gameGroupInfo.groupId)
            .setState(gameGroupInfo.state.toGameStateForGrpc())
            .build()
    }

    override suspend fun isExistsByGroupId(request: GroupIdMessage): CheckGameInfoExistenceResponseMessage {
        val groupId = request.groupId

        val checkResult = gameGroupInfoService.checkGameGroupIsExists(groupId)

        return CheckGameInfoExistenceResponseMessage.newBuilder()
            .setGroupId(groupId)
            .setIsExist(checkResult)
            .build()
    }
}