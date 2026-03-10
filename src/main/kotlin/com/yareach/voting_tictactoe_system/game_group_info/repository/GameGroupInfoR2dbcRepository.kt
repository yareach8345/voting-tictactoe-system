package com.yareach.voting_tictactoe_system.game_group_info.repository

import com.yareach.voting_tictactoe_system.game_group_info.entity.GameGroupInfoR2dbcEntity
import com.yareach.voting_tictactoe_system.game_group_info.enum.GameState
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GameGroupInfoR2dbcRepository: CoroutineCrudRepository<GameGroupInfoR2dbcEntity, Long> {
    suspend fun findByGroupId(groupId: String): GameGroupInfoR2dbcEntity?

    suspend fun existsByGroupId(groupId: String?): Boolean

    suspend fun deleteByGroupId(groupId: String): Long

    @Query("update game_group_info set state = $2 where group_id = $1")
    suspend fun updateState(groupId: String, newState: GameState)

    @Query("update game_group_info set last_updated = current_timestamp where group_id = $1")
    suspend fun updateLastUpdatedToNow(groupId: String)
}