package com.yareach.voting_tictactoe_system.player.grpc_service

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.player.dto.toResult
import com.yareach.voting_tictactoe_system.player.message.RecruitStreamMessageFactory
import com.yareach.voting_tictactoe_system.player.proto.AllPlayerDeleteResult
import com.yareach.voting_tictactoe_system.player.proto.GroupId
import com.yareach.voting_tictactoe_system.player.proto.PlayerServiceGrpcKt
import com.yareach.voting_tictactoe_system.player.proto.PlayersByTeam
import com.yareach.voting_tictactoe_system.player.proto.RecruitRequestMessage
import com.yareach.voting_tictactoe_system.player.proto.RecruitStreamMessage
import com.yareach.voting_tictactoe_system.player.service.PlayerService
import io.grpc.Status
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.springframework.grpc.server.service.GrpcService
import kotlin.time.Duration.Companion.seconds

@GrpcService
class PlayerGrpcService(
    private val playerService: PlayerService,
): PlayerServiceGrpcKt.PlayerServiceCoroutineImplBase() {

    override fun recruitPlayer(requests: Flow<RecruitRequestMessage>): Flow<RecruitStreamMessage> = flow {
        // クライアントの全てのＩＤを取得
        // 以後の作業の前提がクライアントからのユーザーＩＤ伝送の終わり
        val (groupId, userIds) = try {
            withTimeout(60.seconds) {
                var groupId: String? = null
                val setOfUserId = mutableSetOf<String>()

                requests.collectIndexed { index, request ->
                    if (index == 0) {
                        // 最初のメッセージはグループのＩＤであること
                        if (request.payloadCase != RecruitRequestMessage.PayloadCase.INIT) {
                            throw Status.INVALID_ARGUMENT.withDescription("The first message must be groupId")
                                .asRuntimeException()
                        }
                        groupId = request.init.groupId
                    } else {
                        // 二番目からのメッセージは全てゲームに参加するユーザーのＩＤ
                        if (request.payloadCase != RecruitRequestMessage.PayloadCase.ADDUSER) {
                            throw Status.INVALID_ARGUMENT.withDescription("All data after the first must be userId")
                                .asRuntimeException()
                        }

                        val userId = request.addUser.userId

                        // リクエストメッセージのユーザーＩＤがもう存在すれば処理をしない。
                        if(userId !in setOfUserId) {
                            setOfUserId += userId

                            emit(RecruitStreamMessageFactory.buildAcceptMessage(userId))
                        }
                    }
                }

                val finalGroupId = groupId ?: throw Status.INTERNAL.withDescription("INTERNAL ERROR. groupId is null.")
                    .asRuntimeException()

                finalGroupId to setOfUserId
            }
        } catch (e: TimeoutCancellationException) {
            throw Status.DEADLINE_EXCEEDED
                .withDescription("Request timeout")
                .withCause(e)
                .asRuntimeException()
        }

        val divideTeamResult = try{
            playerService.finalizeRecruitment(groupId, userIds)
        } catch (apiException: ApiException) {
            when(apiException.errorCode) {
                ErrorCode.NOT_ENOUGH_PLAYERS ->
                    throw Status.FAILED_PRECONDITION
                        .withDescription("At least two users are required")
                        .withCause(apiException)
                        .asRuntimeException()
                else ->
                    throw Status.INTERNAL
                        .withDescription("Unknown Exception")
                        .withCause(apiException)
                        .asRuntimeException()
            }
        }

        emit(RecruitStreamMessageFactory.buildCompletedMessage(divideTeamResult.toResult()))
    }

    override suspend fun getUserIdsInGroup(request: GroupId): PlayersByTeam {
        try {
            return playerService.getPlayersByGroupId(request.groupId).toResult()
        } catch (apiException: ApiException) {
            when(apiException.errorCode) {
                ErrorCode.GROUP_NOT_FOUND ->
                    throw Status.NOT_FOUND.withDescription("Group is not found").asRuntimeException()
                else ->
                    throw Status.INTERNAL
                        .withDescription("Unknown Exception")
                        .withCause(apiException)
                        .asRuntimeException()
            }
        }
    }

    override suspend fun deleteAllUserInGroupId(request: GroupId): AllPlayerDeleteResult {
        val groupId = request.groupId

        try {
            playerService.deleteAllPlayersByGroupId(groupId)
        } catch (apiException: ApiException) {
            when(apiException.errorCode) {
                ErrorCode.GROUP_NOT_FOUND ->
                    throw Status.NOT_FOUND.withDescription("Group is not found").asRuntimeException()
                else ->
                    throw Status.INTERNAL
                        .withDescription("Unknown Exception")
                        .withCause(apiException)
                        .asRuntimeException()
            }
        }

        return AllPlayerDeleteResult.newBuilder()
            .setGroupId(groupId)
            .build()
    }
}