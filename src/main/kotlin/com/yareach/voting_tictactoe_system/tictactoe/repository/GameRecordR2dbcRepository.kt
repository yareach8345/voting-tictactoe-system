package com.yareach.voting_tictactoe_system.tictactoe.repository

import com.yareach.voting_tictactoe_system.tictactoe.entity.GameRecordR2dbcEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GameRecordR2dbcRepository: CoroutineCrudRepository<GameRecordR2dbcEntity, Long> {
    suspend fun findByGroupId(groupId: String): Flow<GameRecordR2dbcEntity>

    suspend fun deleteByGroupId(groupId: String): Long

    suspend fun existsByGroupId(groupId: String): Boolean

    suspend fun countByGroupId(groupId: String): Long

    suspend fun findByGroupIdOrderByTimeStampAsc(groupId: String): Flow<GameRecordR2dbcEntity>
}