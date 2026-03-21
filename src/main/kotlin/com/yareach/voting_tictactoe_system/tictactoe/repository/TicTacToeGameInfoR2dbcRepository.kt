package com.yareach.voting_tictactoe_system.tictactoe.repository

import com.yareach.voting_tictactoe_system.tictactoe.entity.TicTacToeGameInfoR2dbcEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TicTacToeGameInfoR2dbcRepository: CoroutineCrudRepository<TicTacToeGameInfoR2dbcEntity, Long> {
    suspend fun findByGroupId(groupId: String): TicTacToeGameInfoR2dbcEntity?

    suspend fun deleteByGroupId(groupId: String): Long

    suspend fun existsByGroupId(groupId: String): Boolean
}