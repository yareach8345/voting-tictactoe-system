package com.yareach.voting_tictactoe_system.tictactoe.repository

import com.yareach.voting_tictactoe_system.tictactoe.entity.GameRecordR2dbcEntity
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeMove
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface GameRecordRepository {
    suspend fun save(groupId: String, move: TicTacToeMove): TicTacToeMove

    suspend fun findByGroupId(groupId: String): Flow<TicTacToeMove>

    suspend fun deleteByGroupId(groupId: String)

    suspend fun existsByGroupId(groupId: String): Boolean

    suspend fun countMoveByGroup(groupId: String): Long

    suspend fun findByGroupIdOrderByTimeStampAsc(groupId: String): Flow<TicTacToeMove>
}

class GameRecordRepositoryR2dbcImpl(
    private val gameRecordR2dbcRepository: GameRecordR2dbcRepository
) : GameRecordRepository {

    override suspend fun save(
        groupId: String,
        move: TicTacToeMove
    ): TicTacToeMove {
        val entity = GameRecordR2dbcEntity.new(groupId, move)

        return gameRecordR2dbcRepository.save(entity).toModel()
    }

    override suspend fun findByGroupId(groupId: String): Flow<TicTacToeMove> {
        return gameRecordR2dbcRepository.findByGroupId(groupId).map { it.toModel() }
    }

    override suspend fun deleteByGroupId(groupId: String) {
        gameRecordR2dbcRepository.deleteByGroupId(groupId)
    }

    override suspend fun existsByGroupId(groupId: String): Boolean {
        return gameRecordR2dbcRepository.existsByGroupId(groupId)
    }

    override suspend fun countMoveByGroup(groupId: String): Long {
        return gameRecordR2dbcRepository.countByGroupId(groupId)
    }

    override suspend fun findByGroupIdOrderByTimeStampAsc(groupId: String): Flow<TicTacToeMove> {
        return gameRecordR2dbcRepository.findByGroupIdOrderByTimeStampAsc(groupId).map { it.toModel() }
    }
}