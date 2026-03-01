package com.yareach.voting_tictactoe_system.player.service

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.player.dto.PlayersByTeamDto
import com.yareach.voting_tictactoe_system.player.model.Player
import com.yareach.voting_tictactoe_system.player.repository.PlayerRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import kotlin.random.Random

interface PlayerService {
    suspend fun finalizeRecruitment(groupId: String, userIds: Set<String>): PlayersByTeamDto
    
    suspend fun getPlayersByGroupId(groupId: String): PlayersByTeamDto

    suspend fun deleteAllPlayersByGroupId(groupId: String): Long
}

@Service
class PlayerServiceImpl(
    private val playerRepository: PlayerRepository
): PlayerService {
    override suspend fun finalizeRecruitment(groupId: String, userIds: Set<String>): PlayersByTeamDto {
        if (userIds.size < 2) {
            throw ApiException(ErrorCode.NOT_ENOUGH_PLAYERS, "At least two users are required")
        }

        val divideTeamResult = divideTeam(userIds)

        val players =
            divideTeamResult.userIdsInTeamX.map { userIds -> Player.new(groupId, userIds, Team.X) } +
            divideTeamResult.userIdsInTeamO.map { userIds -> Player.new(groupId, userIds, Team.O) }
        playerRepository.saveAll(players).collect()

        return divideTeamResult
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
        if(!playerRepository.existsByGroupId(groupId)) {
            throw ApiException(ErrorCode.GROUP_NOT_FOUND, groupId)
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
        if(!playerRepository.existsByGroupId(groupId)) {
            throw ApiException(ErrorCode.GROUP_NOT_FOUND, groupId)
        }

        return playerRepository.deleteByGroupId(groupId)
    }
}