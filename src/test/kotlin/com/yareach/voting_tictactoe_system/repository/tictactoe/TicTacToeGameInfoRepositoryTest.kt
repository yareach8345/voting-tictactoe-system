package com.yareach.voting_tictactoe_system.repository.tictactoe

import com.yareach.voting_tictactoe_system.common.enum.GameType
import com.yareach.voting_tictactoe_system.game_group_info.model.GameGroupInfo
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoR2dbcRepository
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoRepository
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoRepositoryR2dbcImpl
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeGameInfo
import com.yareach.voting_tictactoe_system.tictactoe.repository.TicTacToeGameInfoR2dbcRepository
import com.yareach.voting_tictactoe_system.tictactoe.repository.TicTacToeGameInfoRepository
import com.yareach.voting_tictactoe_system.tictactoe.repository.TicTacToeGameInfoRepositoryR2dbcImpl
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataR2dbcTest
class TicTacToeGameInfoRepositoryTest {

    @Autowired
    lateinit var gameGroupInfoR2dbcRepository: GameGroupInfoR2dbcRepository

    lateinit var gameGroupInfoRepository: GameGroupInfoRepository

    @Autowired
    lateinit var gameInfoR2dbcRepository: TicTacToeGameInfoR2dbcRepository

    lateinit var gameInfoRepository: TicTacToeGameInfoRepository

    val testGroupId = "testGroupId"

    @BeforeEach
    suspend fun setUp() {
        gameGroupInfoRepository = GameGroupInfoRepositoryR2dbcImpl(gameGroupInfoR2dbcRepository)

        gameInfoRepository = TicTacToeGameInfoRepositoryR2dbcImpl(gameInfoR2dbcRepository)

        gameGroupInfoRepository.save(GameGroupInfo.new(testGroupId, GameType.NORMAL))
    }

    @AfterEach
    suspend fun clean() {
        gameInfoR2dbcRepository.deleteAll()
        gameGroupInfoR2dbcRepository.deleteAll()
    }

    @Nested
    inner class SaveTest {

        @Test
        @DisplayName("ゲームの情報を保存する")
        fun saveSuccess() = runTest {

            val gameInfo = TicTacToeGameInfo(
                groupId = testGroupId,
                type = GameType.NORMAL,
            )

            val saved = gameInfoRepository.save(gameInfo)

            assertEquals(GameType.NORMAL, saved.type)
            assertEquals(testGroupId, testGroupId)
        }

        @Test
        @DisplayName("GroupIdの制約によって失敗する")
        fun whenGroupIdWrongShouldFail() = runTest {

            val gameInfo = TicTacToeGameInfo(
                groupId = "wrongGroupId",
                type = GameType.NORMAL,
            )

            assertFails { gameInfoRepository.save(gameInfo) }
        }
    }

    @Nested
    inner class  GetGameByGroupIdTest {

        @Test
        @DisplayName("GroupIdに該当するテータを取得")
        fun shouldFindGameInfoByGroupId() = runTest {
            val gameInfo = TicTacToeGameInfo(groupId = testGroupId, type = GameType.NORMAL)

            gameInfoRepository.save(gameInfo)

            val result = gameInfoRepository.findGameByGroupId(testGroupId)

            assertNotNull(result)
            assertEquals(testGroupId, result.groupId)
            assertEquals(GameType.NORMAL, result.type)
        }

        @Test
        @DisplayName("GroupIdに該当するデータがない場合、nullを返す")
        fun whenGameInfoNotExistsReturnNull() = runTest {
            val result = gameInfoRepository.findGameByGroupId("unexist game id")

            assertNull(result)
        }
    }

    @Nested
    inner class DeleteByGroupIdTest {

        @Test
        @DisplayName("GroupIdに該当するテータを削除する")
        fun shouldDeleteGameInfoByGroupId() = runTest {
            val gameInfo = TicTacToeGameInfo(groupId = testGroupId, type = GameType.NORMAL)

            gameInfoRepository.save(gameInfo)

            val result = gameInfoRepository.deleteByGroupId(testGroupId)

            assertEquals(1, result)
        }

        @Test
        @DisplayName("GroupIdに該当するデータがない場合、0を返す")
        fun whenNotExistsGameInfoShouldReturn0() = runTest {
            val result = gameInfoRepository.deleteByGroupId(testGroupId)

            assertEquals(0, result)
        }
    }

    @Nested
    inner class ExistsByGroupId {

        @Test
        @DisplayName("GroupIdに一致するデータの存在する場合、trueを返す")
        fun whenExistsGameInfoShouldReturnTrue() = runTest {
            val gameInfo = TicTacToeGameInfo(groupId = testGroupId, type = GameType.NORMAL)

            gameInfoRepository.save(gameInfo)

            val result = gameInfoRepository.existsByGroupId(testGroupId)

            assertTrue(result)
        }

        @Test
        @DisplayName("GroupIdに一致するデータのない場合、falseを返す")
        fun whenNotExistsGameInfoShouldReturnFalse() = runTest {
            val result = gameInfoRepository.existsByGroupId(testGroupId)

            assertFalse(result)
        }
    }
}