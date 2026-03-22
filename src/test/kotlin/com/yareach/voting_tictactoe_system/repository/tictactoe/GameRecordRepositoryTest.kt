package com.yareach.voting_tictactoe_system.repository.tictactoe

import com.yareach.voting_tictactoe_system.common.enum.GameType
import com.yareach.voting_tictactoe_system.game_group_info.model.GameGroupInfo
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoR2dbcRepository
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoRepository
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoRepositoryR2dbcImpl
import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.tictactoe.entity.GameRecordR2dbcEntity
import com.yareach.voting_tictactoe_system.tictactoe.model.Cell
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeMove
import com.yareach.voting_tictactoe_system.tictactoe.repository.GameRecordR2dbcRepository
import com.yareach.voting_tictactoe_system.tictactoe.repository.GameRecordRepository
import com.yareach.voting_tictactoe_system.tictactoe.repository.GameRecordRepositoryR2dbcImpl
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DataR2dbcTest
class GameRecordRepositoryTest {

    @Autowired
    private lateinit var gameGroupInfoR2dbcRepository: GameGroupInfoR2dbcRepository

    private lateinit var gameGroupInfoRepository: GameGroupInfoRepository

    @Autowired
    private lateinit var gameRecordR2dbcRepository: GameRecordR2dbcRepository

    private lateinit var gameRecordRepository: GameRecordRepository

    val testGroupId = "testGroup"

    @BeforeEach
    suspend fun setUp() {
        gameGroupInfoRepository = GameGroupInfoRepositoryR2dbcImpl(gameGroupInfoR2dbcRepository)

        gameRecordRepository = GameRecordRepositoryR2dbcImpl(gameRecordR2dbcRepository)

        val gameGroupInfo = GameGroupInfo.new(testGroupId, GameType.NORMAL)
        gameGroupInfoRepository.save(gameGroupInfo)
    }

    @AfterEach
    suspend fun tearDown() {
        gameRecordR2dbcRepository.deleteAll()
        gameGroupInfoR2dbcRepository.deleteAll()
    }

    @Nested
    inner class SaveTest {

        @Test
        @DisplayName("着手記録を保存する")
        fun shouldSave() = runTest {
            val newMove = TicTacToeMove.withCoordinate(0, 2, Team.X)

            val result = gameRecordRepository.save(testGroupId, newMove)
            val findResult = gameRecordRepository.findByGroupId(testGroupId).toList()

            assertEquals(0, result.x)
            assertEquals(2, result.y)
            assertEquals(Team.X, result.team)

            assertNotNull(findResult)
            assertEquals(1, findResult.size)
            assertEquals(result ,findResult.first())
        }

        @Test
        @DisplayName("間違ったgroupIdで保存とすると、失敗する")
        fun whenGroupIdNotExistsShouldFail() = runTest {
            val newMove = TicTacToeMove.withCoordinate(0, 2, Team.X)

            assertFails { gameRecordRepository.save("testGroupId", newMove) }
        }
    }

    @Nested
    inner class FindByGroupIdTest {

        @Test
        @DisplayName("GroupIdが一致するデータを取得する")
        fun whenOneMoveSavedShouldFindTheMove() = runTest {
            val newMove = TicTacToeMove.withCoordinate(0, 2, Team.X)

            gameRecordRepository.save(testGroupId, newMove)

            val findResult = gameRecordRepository.findByGroupId(testGroupId).toList()

            assertEquals(1, findResult.size)
            assertEquals(Cell(0, 2), findResult[0].cell)
            assertEquals(Team.X, findResult[0].team)
        }

        @Test
        @DisplayName("GroupIdが一致するデータが２つ以上であっても取得できる")
        fun whenMovesSavedShouldFindTheMove() = runTest {
            val moves = listOf(
                TicTacToeMove.withCoordinate(0, 2, Team.X),
                TicTacToeMove.withCoordinate(0, 0, Team.O),
                TicTacToeMove.withCoordinate(1, 1, Team.X),
            )

            moves.forEach { gameRecordRepository.save(testGroupId, it) }

            val findResult = gameRecordRepository.findByGroupId(testGroupId).toList()

            assertEquals(3, findResult.size)
            moves.forEachIndexed { index, move -> assertEquals(move, findResult[index]) }
        }

        @Test
        @DisplayName("GroupIdが一致するデータがない場合、空のFlowを返す")
        fun whenNothingSavedShouldFindEmptyFlow() = runTest {

            val findResult = gameRecordRepository.findByGroupId(testGroupId).toList()
            assertTrue(findResult.isEmpty())
        }
    }

    @Nested
    inner class ExistsByGroupIdTest {

        @Test
        @DisplayName("GroupIdに該当するデータの存在すると、trueを返す")
        fun whenDataExistReturnTrue() = runTest {

            val moves = listOf(
                TicTacToeMove.withCoordinate(0, 2, Team.X),
                TicTacToeMove.withCoordinate(0, 0, Team.O),
                TicTacToeMove.withCoordinate(1, 1, Team.X),
            )

            moves.forEach { gameRecordRepository.save(testGroupId, it) }

            val result = gameRecordRepository.existsByGroupId(testGroupId)

            assertTrue(result)
        }

        @Test
        @DisplayName("GroupIdに該当するデータがなければ、falseを返す")
        fun whenDataDoNotExistReturnFalse() = runTest {

            val result = gameRecordRepository.existsByGroupId(testGroupId)

            assertFalse(result)
        }
    }

    @Nested
    inner class CountByGroupTest {

        @Test
        @DisplayName("GroupIdに該当するデータが一つだけ保存されている場合、1を返す")
        fun whenOneMoveSavedShouldReturn1() = runTest {

            val newMove = TicTacToeMove.withCoordinate(0, 2, Team.X)

            gameRecordRepository.save(testGroupId, newMove)

            val result = gameRecordRepository.countMoveByGroup(testGroupId)

            assertEquals(1, result)
        }

        @Test
        @DisplayName("GroupIdが一致するデータが２つ以上有る場合、データの数を返す")
        fun whenNMoveSavedShouldReturnN() = runTest {

            val newMoves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(1, 0, Team.O),
                TicTacToeMove.withCoordinate(2, 2, Team.X),
                TicTacToeMove.withCoordinate(1, 1, Team.O),
            )

            newMoves.forEach { gameRecordRepository.save(testGroupId, it) }

            val result = gameRecordRepository.countMoveByGroup(testGroupId)

            assertEquals(newMoves.size.toLong(), result)
        }

        @Test
        @DisplayName("GroupIdに該当するデータがない場合、0を返す")
        fun whenNothingDataSavedShouldReturn0() = runTest {

            val result = gameRecordRepository.countMoveByGroup(testGroupId)

            assertEquals(0, result)
        }
    }

    @Nested
    inner class FindByGroupIdOrderByTimeStampTest {

        @Test
        @DisplayName("")
        fun shouldOrderByTimeStamp() = runTest {

            val startTime = LocalDateTime.of(2020, 1, 1, 0, 0, 0)

            val moves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(0, 2, Team.O),
                TicTacToeMove.withCoordinate(1, 1, Team.X),
                TicTacToeMove.withCoordinate(2, 2, Team.O),
                TicTacToeMove.withCoordinate(2, 0, Team.X),
            )

            moves.forEachIndexed { index, move ->
                val time = startTime.plusSeconds((index * 10).toLong())

                val entity = GameRecordR2dbcEntity(
                    id = null,
                    groupId = testGroupId,
                    team = move.team,
                    x = move.x,
                    y = move.y,
                    timeStamp = time
                )

                gameRecordR2dbcRepository.save(entity)
            }

            val result = gameRecordRepository.findByGroupIdOrderByTimeStampAsc(testGroupId).toList()

            result.zip(moves)
                .forEach { (actual, expected) ->
                    assertEquals(expected.cell, actual.cell)
                    assertEquals(expected.team, actual.team)
                }
        }
    }
}