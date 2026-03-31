package com.yareach.voting_tictactoe_system.tictactoe.service

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.game_group_info.enum.GameState
import com.yareach.voting_tictactoe_system.game_group_info.service.GameGroupInfoService
import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.tictactoe.dto.TakeTurnRequestDto
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeGameInfo
import com.yareach.voting_tictactoe_system.tictactoe.repository.GameRecordRepository
import com.yareach.voting_tictactoe_system.tictactoe.repository.TicTacToeGameInfoRepository
import com.yareach.voting_tictactoe_system.voting_system.service.VotingService
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service

interface TicTacToeService {
    suspend fun startGame(groupId: String): TicTacToeGameInfo

    suspend fun endGame(groupId: String)

    suspend fun getCurrentField(groupId: String): List<List<Team?>>

    suspend fun takeTurn(inputFlow: Flow<TakeTurnRequestDto>)
}

@Service
class TicTacToeServiceImpl(
    private val tictactoeGameInfoRepository: TicTacToeGameInfoRepository,
    private val gameRecordRepository: GameRecordRepository,
    private val gameGroupInfoService: GameGroupInfoService,
    private val votingService: VotingService
) : TicTacToeService {

    override suspend fun startGame(groupId: String): TicTacToeGameInfo {

        if(!gameGroupInfoService.checkGameGroupIsExists(groupId)) { throw ApiException(ErrorCode.GAME_NOT_EXISTS, "Game not found error.") }

        val gameGroupInfo = gameGroupInfoService.getGameGroupInfo(groupId)

        if(gameGroupInfo.state != GameState.BEFORE_START) {
            throw ApiException(ErrorCode.GAME_STATE_ERROR, "Game state error. Game state must be BEFORE_START")
        }

        val gameInfo = TicTacToeGameInfo.of(groupId, gameGroupInfo.gameType)
            .let { gameInfo -> tictactoeGameInfoRepository.save(gameInfo) }

        gameGroupInfoService.updateGameState(groupId, GameState.PLAYING)

        return gameInfo
    }

    override suspend fun endGame(groupId: String) {

        if(!gameGroupInfoService.checkGameGroupIsExists(groupId)) {
            throw ApiException(ErrorCode.GAME_NOT_EXISTS)
        }

        val gameGroupInfo = gameGroupInfoService.getGameGroupInfo(groupId)

        if(gameGroupInfo.state != GameState.PLAYING) {
            throw ApiException(ErrorCode.GAME_STATE_ERROR, "Game state error. Game state must be BEFORE_START")
        }


        tictactoeGameInfoRepository.deleteByGroupId(groupId)

        gameGroupInfoService.updateGameState(groupId, GameState.FINISHED)
    }

    override suspend fun getCurrentField(groupId: String): List<List<Team?>> {
        TODO("Not yet implemented")
    }

    override suspend fun takeTurn(inputFlow: Flow<TakeTurnRequestDto>) {
        TODO("Not yet implemented")
    }
}