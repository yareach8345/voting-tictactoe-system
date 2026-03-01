package com.yareach.voting_tictactoe_system.player.repository

import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.player.entity.PlayerR2dbcEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PlayerR2dbcRepository: CoroutineCrudRepository<PlayerR2dbcEntity, Long> {
    suspend fun findByGroupId(groupId: String): Flow<PlayerR2dbcEntity>

    suspend fun findByGroupIdAndTeam(groupId: String, team: Team): Flow<PlayerR2dbcEntity>

    suspend fun findByGroupIdAndUserId(groupId: String, userId: String): PlayerR2dbcEntity?

    suspend fun existsByGroupId(groupId: String): Boolean

    suspend fun existsByGroupIdAndUserId(groupId: String, userId: String): Boolean

    suspend fun deleteByGroupId(groupId: String): Long

    suspend fun deleteByGroupIdAndUserId(groupId: String, userId: String): Long
}