package com.yareach.voting_tictactoe_system.tictactoe.repository

import com.yareach.voting_tictactoe_system.tictactoe.entity.TicTacToeGameInfoR2dbcEntity
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeGameInfo
import org.springframework.stereotype.Repository

interface TicTacToeGameInfoRepository {
    suspend fun save(gameInfo: TicTacToeGameInfo): TicTacToeGameInfo

    suspend fun findGameByGroupId(groupId: String): TicTacToeGameInfo?

    suspend fun deleteByGroupId(groupId: String): Long

    suspend fun existsByGroupId(groupId: String): Boolean
}

@Repository
class TicTacToeGameInfoRepositoryR2dbcImpl(
    private val ticTacToeGameInfoR2dbcRepository: TicTacToeGameInfoR2dbcRepository
): TicTacToeGameInfoRepository {
    override suspend fun save(gameInfo: TicTacToeGameInfo): TicTacToeGameInfo {
        val entity = TicTacToeGameInfoR2dbcEntity.fromModel(gameInfo)

        return ticTacToeGameInfoR2dbcRepository.save(entity).toModel()
    }

    override suspend fun findGameByGroupId(groupId: String): TicTacToeGameInfo? {
        return ticTacToeGameInfoR2dbcRepository.findByGroupId(groupId)?.toModel()
    }

    override suspend fun deleteByGroupId(groupId: String): Long{
        return ticTacToeGameInfoR2dbcRepository.deleteByGroupId(groupId)
    }

    override suspend fun existsByGroupId(groupId: String): Boolean {
        return ticTacToeGameInfoR2dbcRepository.existsByGroupId(groupId)
    }

}