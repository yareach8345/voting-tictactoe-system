package com.yareach.voting_tictactoe_system.player.service

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.common.extension.doFirst
import com.yareach.voting_tictactoe_system.game_group_info.service.GameGroupInfoService
import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.player.dto.AddNewPlayerDto
import com.yareach.voting_tictactoe_system.player.dto.InitRecruitDto
import com.yareach.voting_tictactoe_system.player.dto.PlayersByTeamDto
import com.yareach.voting_tictactoe_system.player.dto.RecruitAcceptedDto
import com.yareach.voting_tictactoe_system.player.dto.RecruitCompleted
import com.yareach.voting_tictactoe_system.player.dto.RecruitRejectedDto
import com.yareach.voting_tictactoe_system.player.dto.RecruitRequestDto
import com.yareach.voting_tictactoe_system.player.dto.RecruitResponseDto
import com.yareach.voting_tictactoe_system.player.model.Player
import com.yareach.voting_tictactoe_system.player.repository.PlayerRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

interface PlayerService {
    fun processRecruitMessage(recruitMessage: Flow<RecruitRequestDto>): Flow<RecruitResponseDto>

    suspend fun getPlayersByGroupId(groupId: String): PlayersByTeamDto

    suspend fun deleteAllPlayersByGroupId(groupId: String): Long
}

@Service
@Transactional
class PlayerServiceImpl(
    private val playerRepository: PlayerRepository,
    private val gameGroupInfoService: GameGroupInfoService,
): PlayerService {

    override fun processRecruitMessage(recruitMessage: Flow<RecruitRequestDto>): Flow<RecruitResponseDto> = flow {

        // クライアントの全てのＩＤを取得
        // 以後の作業の前提がクライアントからのユーザーＩＤ伝送の終わり
        val (groupId, setOfUserId) = try {
            withTimeout(60.seconds) {
                var groupId: String? = null
                val setOfUserId = mutableSetOf<String>()

                recruitMessage.doFirst {
                    // 最初のメッセージはグループのＩＤであること
                    if (it !is InitRecruitDto) {
                        throw ApiException(ErrorCode.WRONG_MESSAGE_ORDER, "The first message must be groupId")
                    }

                    if(!gameGroupInfoService.checkGameGroupIsExists(it.groupId)) {
                        throw ApiException(ErrorCode.INVALID_GROUP_ID, "can't find group with the id ${it.groupId}")
                    }

                    groupId = it.groupId
                }.collect {
                    // 二番目からのメッセージは全てゲームに参加するユーザーのＩＤ
                    if(it !is AddNewPlayerDto) {
                        throw ApiException(ErrorCode.WRONG_MESSAGE_ORDER, "All data after the first must be userId")
                    }

                    val userId = it.userId

                    // リクエストメッセージのユーザーＩＤがもう存在すれば処理をしない。
                    if(userId in setOfUserId) {
                        emit(RecruitRejectedDto(userId))
                    } else {
                        setOfUserId += userId

                        emit(RecruitAcceptedDto(userId))
                    }
                }

                if(groupId == null) {
                    throw ApiException(ErrorCode.INTERNAL_ERROR, "INTERNAL ERROR. groupId is null.")
                }

                Pair(groupId, setOfUserId)
            }
        } catch (_: TimeoutCancellationException) {
            throw ApiException(ErrorCode.PLAYER_RECRUIT_TIMEOUT, "Recruitment was not completed on time")
        }

        if (setOfUserId.size < 2) {
            throw ApiException(ErrorCode.NOT_ENOUGH_PLAYERS, "At least two users are required")
        }

        val divideTeamResult = divideTeam(setOfUserId)

        val players =
            divideTeamResult.userIdsInTeamX.map { userIds -> Player.new(groupId, userIds, Team.X) } +
            divideTeamResult.userIdsInTeamO.map { userIds -> Player.new(groupId, userIds, Team.O) }
        playerRepository.saveAll(players).collect()

        emit(RecruitCompleted(divideTeamResult))
    }

    private fun divideTeam(userIds: Set<String>): PlayersByTeamDto {
        val shuffledUserId = userIds.shuffled()

        val half = userIds.size / 2
        val boundary = half * 2

        val playersInTeamX = mutableSetOf<String>()
        val playersInTeamO = mutableSetOf<String>()

        // リストの前の半はXチームへ、その後の半はOチームへ
        // ユーザー数が奇数の場合、halfは整数除算により切り捨てられるため1人が残る
        // 残った一人はランダムなチームに配置
        shuffledUserId.forEachIndexed { index, userId ->
            when {
                index < half -> playersInTeamX += userId
                index < boundary -> playersInTeamO += userId
                else -> if(Random.nextBoolean()) playersInTeamO += userId else playersInTeamX += userId
            }
        }

        return PlayersByTeamDto(
            playersInTeamX,
            playersInTeamO
        )
    }

    override suspend fun getPlayersByGroupId(groupId: String): PlayersByTeamDto {
        if(!gameGroupInfoService.checkGameGroupIsExists(groupId)) {
            throw ApiException(ErrorCode.INVALID_GROUP_ID, "can't find group with id: $groupId")
        }

        val playersByGroup = playerRepository.findByGroupId(groupId).toList()
            .groupBy { it.team }

        val userIdsInTeamX = playersByGroup.getOrDefault(Team.X, listOf()).map { it.userId }.toSet()
        val userIdsInTeamO = playersByGroup.getOrDefault(Team.O, listOf()).map { it.userId }.toSet()

        return PlayersByTeamDto(
            userIdsInTeamX,
            userIdsInTeamO
        )
    }

    override suspend fun deleteAllPlayersByGroupId(groupId: String): Long {
        if(!gameGroupInfoService.checkGameGroupIsExists(groupId)) {
            throw ApiException(ErrorCode.INVALID_GROUP_ID, "can't find group with id: $groupId")
        }

        return playerRepository.deleteByGroupId(groupId)
    }
}