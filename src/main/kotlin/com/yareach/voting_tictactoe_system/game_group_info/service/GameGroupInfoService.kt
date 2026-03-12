package com.yareach.voting_tictactoe_system.game_group_info.service

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.game_group_info.enum.GameState
import com.yareach.voting_tictactoe_system.game_group_info.model.GameGroupInfo
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoRepository
import com.yareach.voting_tictactoe_system.common.enum.GameType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface GameGroupInfoService {
    suspend fun createNewGameGroupInfo(groupId: String, gameType: GameType): GameGroupInfo

    suspend fun checkGameGroupIsExists(groupId: String): Boolean

    suspend fun getGameGroupInfo(groupId: String): GameGroupInfo

    suspend fun deleteGameGroupInfo(groupId: String)

    suspend fun updateGameState(groupId: String, newState: GameState): GameGroupInfo

    suspend fun updateLastUpdated(groupId: String): GameGroupInfo
}

@Service
@Transactional
class GameGroupInfoServiceImpl(
    private val gameGroupInfoRepository: GameGroupInfoRepository,
): GameGroupInfoService {

    override suspend fun createNewGameGroupInfo(
        groupId: String,
        gameType: GameType
    ): GameGroupInfo {
        if(gameGroupInfoRepository.existsByGroupId(groupId)) {
            throw ApiException(ErrorCode.GROUP_ID_DUPLICATE, "The GameGroupInfo for groupId $groupId, already exists.")
        }
        val gameGroupInfo = GameGroupInfo.new(groupId, gameType)

        return gameGroupInfoRepository.save(gameGroupInfo)
    }

    override suspend fun checkGameGroupIsExists(groupId: String): Boolean {
        return gameGroupInfoRepository.existsByGroupId(groupId)
    }

    override suspend fun getGameGroupInfo(groupId: String): GameGroupInfo {
        return gameGroupInfoRepository.findByGroupId(groupId)
            ?: throw ApiException(ErrorCode.GAME_INFO_NOTFOUND, "Game info not found with groupId: $groupId")
    }

    override suspend fun deleteGameGroupInfo(groupId: String) {
        if(!gameGroupInfoRepository.existsByGroupId(groupId)) {
            throw ApiException(ErrorCode.GAME_INFO_NOTFOUND, "Game info not found with groupId: $groupId")
        }
        gameGroupInfoRepository.deleteByGroupId(groupId)
    }

    override suspend fun updateGameState(
        groupId: String,
        newState: GameState
    ): GameGroupInfo {
        val gameInfo = gameGroupInfoRepository.findByGroupId(groupId)
            ?: throw ApiException(ErrorCode.GAME_INFO_NOTFOUND, "Game info not found with groupId: $groupId")

        gameInfo.changeState(newState)

        return gameGroupInfoRepository.save(gameInfo)
    }

    override suspend fun updateLastUpdated(groupId: String): GameGroupInfo {
        val gameInfo = gameGroupInfoRepository.findByGroupId(groupId)
            ?: throw ApiException(ErrorCode.GAME_INFO_NOTFOUND, "Game info not found with groupId: $groupId")
        return gameGroupInfoRepository.save(gameInfo)
    }
}