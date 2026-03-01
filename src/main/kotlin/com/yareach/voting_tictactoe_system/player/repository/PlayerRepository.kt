package com.yareach.voting_tictactoe_system.player.repository

import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.player.entity.PlayerR2dbcEntity
import com.yareach.voting_tictactoe_system.player.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Repository

interface PlayerRepository {
    suspend fun save(player: Player): Player

    suspend fun saveAll(players: List<Player>): Flow<Player>

    suspend fun findByGroupId(groupId: String): Flow<Player>

    suspend fun findByGroupIdAndTeam(groupId: String, team: Team): Flow<Player>

    suspend fun findByGroupIdAndUserId(groupId: String, userId: String): Player?

    suspend fun existsByGroupId(groupId: String): Boolean

    suspend fun existsByGroupIdAndUserId(groupId: String, userId: String): Boolean

    suspend fun deleteByGroupId(groupId: String): Long

    suspend fun deleteByGroupIdAndUserId(groupId: String, userId: String): Long
}

@Repository
class PlayerRepositoryR2dbcImpl(
    private val playerR2dbcRepository: PlayerR2dbcRepository
) : PlayerRepository {
    override suspend fun save(player: Player): Player {
        val entity = PlayerR2dbcEntity.fromModel(player)
        return playerR2dbcRepository.save(entity).toModel()
    }

    override suspend fun saveAll(players: List<Player>): Flow<Player> {
        val entities = players.map{ PlayerR2dbcEntity.fromModel(it) }

        return playerR2dbcRepository.saveAll(entities).map { it.toModel() }
    }

    override suspend fun findByGroupId(groupId: String): Flow<Player> {
        return playerR2dbcRepository.findByGroupId(groupId).map { it.toModel() }
    }

    override suspend fun findByGroupIdAndTeam(
        groupId: String,
        team: Team
    ): Flow<Player> {
        return playerR2dbcRepository.findByGroupIdAndTeam(groupId, team).map { it.toModel() }
    }

    override suspend fun findByGroupIdAndUserId(
        groupId: String,
        userId: String
    ): Player? {
        return playerR2dbcRepository.findByGroupIdAndUserId(groupId, userId)?.toModel()
    }

    override suspend fun existsByGroupId(groupId: String): Boolean {
        return playerR2dbcRepository.existsByGroupId(groupId)
    }

    override suspend fun existsByGroupIdAndUserId(groupId: String, userId: String): Boolean {
        return playerR2dbcRepository.existsByGroupIdAndUserId(groupId, userId)
    }

    override suspend fun deleteByGroupId(groupId: String): Long {
        return playerR2dbcRepository.deleteByGroupId(groupId)
    }

    override suspend fun deleteByGroupIdAndUserId(groupId: String, userId: String): Long {
        return playerR2dbcRepository.deleteByGroupIdAndUserId(groupId, userId)
    }
}