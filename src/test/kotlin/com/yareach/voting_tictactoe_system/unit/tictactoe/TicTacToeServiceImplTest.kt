package com.yareach.voting_tictactoe_system.unit.tictactoe

import com.yareach.voting_tictactoe_system.common.enum.GameType
import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.game_group_info.enum.GameState
import com.yareach.voting_tictactoe_system.game_group_info.model.GameGroupInfo
import com.yareach.voting_tictactoe_system.game_group_info.service.GameGroupInfoService
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeGameInfo
import com.yareach.voting_tictactoe_system.tictactoe.repository.GameRecordRepository
import com.yareach.voting_tictactoe_system.tictactoe.repository.TicTacToeGameInfoRepository
import com.yareach.voting_tictactoe_system.tictactoe.service.TicTacToeServiceImpl
import com.yareach.voting_tictactoe_system.voting_system.service.VotingService
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import java.time.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class TicTacToeServiceImplTest {

    private val tictactoeGameInfoRepositoryMock = mockk<TicTacToeGameInfoRepository>()

    private val gameRecordRepositoryMock = mockk<GameRecordRepository>()

    private val gameGroupInfoServiceMock = mockk<GameGroupInfoService>()

    private val votingServiceMock = mockk<VotingService>()

    private val ticTacToeService = TicTacToeServiceImpl(
        tictactoeGameInfoRepositoryMock,
        gameRecordRepositoryMock,
        gameGroupInfoServiceMock,
        votingServiceMock
    )

    @Nested
    inner class StartGameTest {

        @Nested
        inner class WhenGameIsExists {

            @BeforeEach
            fun setUpMock() {
                coEvery {
                    gameGroupInfoServiceMock.checkGameGroupIsExists(any<String>())
                } returns true
            }

            @Nested
            inner class WhenGameStateIsBeforeStart {
                val testGameType = GameType.NORMAL

                @BeforeTest
                fun setUpMock() {

                    coEvery {
                        gameGroupInfoServiceMock.getGameGroupInfo(any<String>())
                    } answers {
                        val groupId = arg<String>(0)

                        GameGroupInfo(
                            id = 0,
                            groupId = groupId,
                            gameType = testGameType,
                            state = GameState.BEFORE_START,
                            lastUpdated = LocalDateTime.now()
                        )
                    }

                    coEvery {
                        tictactoeGameInfoRepositoryMock.save(any())
                    } answers {
                        val gameInfo = arg<TicTacToeGameInfo>(0)

                        TicTacToeGameInfo(
                            id = 0,
                            groupId = gameInfo.groupId,
                            type = gameInfo.type
                        )
                    }

                    coJustRun { gameGroupInfoServiceMock.updateGameState(any(), any()) }
                }

                @Test
                fun shouldSaveNewGameInfo() = runTest {
                    val testGroupId = "groupId"
                    ticTacToeService.startGame(testGroupId)

                    coVerify(exactly = 1) {
                        tictactoeGameInfoRepositoryMock.save(match {
                            it.groupId == testGroupId
                        })
                    }
                }

                @Test
                fun shouldCallGameStateUpdateMethodWithPLAYING() = runTest {
                    val testGroupId = "groupId"
                    ticTacToeService.startGame(testGroupId)

                    coVerify(exactly = 1) { gameGroupInfoServiceMock.updateGameState(testGroupId, GameState.PLAYING) }
                }

                @Test
                fun shouldReturnNewGameInfo() = runTest {
                    val testGroupId = "groupId"
                    val result = ticTacToeService.startGame(testGroupId)

                    assertNotNull(result.id)
                    assertEquals(testGroupId, result.groupId)
                    assertEquals(testGameType, result.type)
                }
            }

            @Nested
            inner class WhenGameStateIsOtherState {

                @BeforeTest
                fun setUpMock() {

                    coEvery {
                        gameGroupInfoServiceMock.getGameGroupInfo(any<String>())
                    } answers {
                        val groupId = arg<String>(0)

                        GameGroupInfo(
                            id = 0,
                            groupId = groupId,
                            gameType = GameType.NORMAL,
                            state = GameState.GENERATED,
                            lastUpdated = LocalDateTime.now()
                        )
                    }
                }

                @Test
                fun shouldThrowException() = runTest {
                    val testGroupId = "groupId"

                    val exception = assertFailsWith<ApiException> { ticTacToeService.startGame(testGroupId) }

                    coVerify(exactly = 1) { gameGroupInfoServiceMock.getGameGroupInfo(testGroupId) }

                    assertEquals(ErrorCode.GAME_STATE_ERROR, exception.errorCode)
                }

                @Test
                fun shouldNotCalledSaveAndUpdateGameState() = runTest {
                    val testGroupId = "groupId"

                    assertFails { ticTacToeService.startGame(testGroupId) }

                    coVerify(exactly = 0) { tictactoeGameInfoRepositoryMock.save(any()) }
                    coVerify(exactly = 0) { gameGroupInfoServiceMock.updateGameState(any(), any()) }
                }
            }
        }

        @Nested
        inner class WhenGameIsNotExists {

            @BeforeEach
            fun setUpMock() {
                coEvery {
                    gameGroupInfoServiceMock.checkGameGroupIsExists(any<String>())
                } returns false
            }

            @Test
            fun shouldThrowException() = runTest {

                val exception = assertFailsWith<ApiException> { ticTacToeService.startGame("not-exists-group-id") }

                assertEquals(ErrorCode.GAME_NOT_EXISTS, exception.errorCode)
            }
        }
    }

    @Nested
    inner class EndGameTest {

        @Nested
        inner class WhenGameExists {

            @BeforeTest
            fun setUpMock() {
                coEvery {
                    gameGroupInfoServiceMock.checkGameGroupIsExists(any<String>())
                } returns true

                coJustRun { tictactoeGameInfoRepositoryMock.deleteByGroupId(any<String>()) }
                coJustRun { gameGroupInfoServiceMock.updateGameState(any(), any()) }
            }

            @Nested
            inner class WhenGameStateIsPlaying {

                @BeforeTest
                fun setUpMock() {
                    coEvery {
                        gameGroupInfoServiceMock.getGameGroupInfo(any<String>())
                    } answers {
                        val gameId = arg<String>(0)

                        GameGroupInfo(
                            id = 0,
                            groupId = gameId,
                            gameType = GameType.NORMAL,
                            state = GameState.PLAYING,
                            lastUpdated = LocalDateTime.now()
                        )
                    }
                }

                @Test
                fun shouldDeleteGameInfo() = runTest {

                    val testGameId = "game-id"

                    ticTacToeService.endGame(testGameId)

                    coVerify(exactly = 1) { tictactoeGameInfoRepositoryMock.deleteByGroupId(testGameId) }
                }

                @Test
                fun shouldChangeGameStateToFinished() = runTest {

                    val testGameId = "game-id"

                    ticTacToeService.endGame(testGameId)

                    coVerify(exactly = 1) { gameGroupInfoServiceMock.updateGameState(testGameId, GameState.FINISHED) }
                }
            }

            @Nested
            inner class WhenGameStateIsNotPlaying {

                @BeforeTest
                fun setUpMock() {
                    coEvery {
                        gameGroupInfoServiceMock.getGameGroupInfo(any<String>())
                    } answers {
                        val gameId = arg<String>(0)

                        GameGroupInfo(
                            id = 0,
                            groupId = gameId,
                            gameType = GameType.NORMAL,
                            state = GameState.GENERATED,
                            lastUpdated = LocalDateTime.now()
                        )
                    }
                }

                @Test
                fun shouldThrowException() = runTest {

                    val exception = assertFailsWith<ApiException> {
                        ticTacToeService.endGame("not-exists-group-id")
                    }

                    assertEquals(ErrorCode.GAME_STATE_ERROR, exception.errorCode)
                }

                @Test
                fun shouldNotDeletedGameInfo() = runTest {

                    assertFails {
                        ticTacToeService.endGame("not-exists-group-id")
                    }

                    coVerify(exactly = 0) { tictactoeGameInfoRepositoryMock.deleteByGroupId(any<String>()) }
                }

                @Test
                fun shouldNotCallChangeGameStateMethod() = runTest {

                    assertFails {
                        ticTacToeService.endGame("not-exists-group-id")
                    }

                    coVerify(exactly = 0) { gameGroupInfoServiceMock.updateGameState(any(), any()) }
                }
            }
        }

        @Nested
        inner class WhenGameIsNotExists {

            @BeforeTest
            fun setUpMock() {
                coEvery {
                    gameGroupInfoServiceMock.checkGameGroupIsExists(any<String>())
                } returns false
            }

            @Test
            fun shouldThrowException() = runTest {
                val exception = assertFailsWith<ApiException> { ticTacToeService.endGame("not-exists-group-id") }

                assertEquals(ErrorCode.GAME_NOT_EXISTS, exception.errorCode)
            }
        }
    }
}