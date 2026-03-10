package com.yareach.voting_tictactoe_system.game_group_info.repository

import com.yareach.voting_tictactoe_system.game_group_info.entity.GameGroupInfoR2dbcEntity
import com.yareach.voting_tictactoe_system.game_group_info.model.GameGroupInfo
import org.springframework.stereotype.Repository

interface GameGroupInfoRepository {
    suspend fun save(gameGroupInfo: GameGroupInfo): GameGroupInfo

    suspend fun findByGroupId(groupId: String): GameGroupInfo?

    suspend fun existsByGroupId(groupId: String): Boolean

    suspend fun deleteByGroupId(groupId: String): Long
}

@Repository
class GameGroupInfoRepositoryR2dbcImpl(
    private val r2dbcRepository: GameGroupInfoR2dbcRepository,
): GameGroupInfoRepository {
    override suspend fun save(gameGroupInfo: GameGroupInfo): GameGroupInfo {
        val entity = GameGroupInfoR2dbcEntity.fromModel(gameGroupInfo)
        return r2dbcRepository.save(entity).toModel()
    }

    override suspend fun findByGroupId(groupId: String): GameGroupInfo? {
        return r2dbcRepository.findByGroupId(groupId)?.toModel()
    }

    override suspend fun existsByGroupId(groupId: String): Boolean {
        return r2dbcRepository.existsByGroupId(groupId)
    }

    override suspend fun deleteByGroupId(groupId: String): Long {
        return r2dbcRepository.deleteByGroupId(groupId)
    }

}