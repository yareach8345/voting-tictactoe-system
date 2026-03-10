package com.yareach.voting_tictactoe_system.game_group_info.repository

import com.yareach.voting_tictactoe_system.game_group_info.entity.GameGroupInfoR2dbcEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GameGroupInfoR2dbcRepository: CoroutineCrudRepository<GameGroupInfoR2dbcEntity, Long> {
    suspend fun findByGroupId(groupId: String): GameGroupInfoR2dbcEntity?

    suspend fun existsByGroupId(groupId: String?): Boolean

    suspend fun deleteByGroupId(groupId: String): Long
}