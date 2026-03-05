package com.yareach.voting_tictactoe_system.unit.tictactoe

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.tictactoe.model.Cell
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeMove
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class TicTacToeMoveTest {

    @Nested
    @DisplayName("オブジェクト生成")
    inner class GenerateMoveTest {

        @Test
        @DisplayName("[Success case] CellとTeamで特定のセルへの着手を表すオブジェクトを生成")
        fun generateSuccessfully() {
            repeat(3) { x ->
                repeat(3) { y ->
                    val cell = Cell(x, y)
                    val team = if(Random.nextBoolean()) Team.X else Team.O

                    val move = TicTacToeMove(cell, team)

                    assertEquals(x, move.x)
                    assertEquals(y, move.y)
                    assertEquals(team, move.team)
                }
            }
        }
    }

    @Nested
    @DisplayName("withCoordinate Factoryメソッドを用いてオブジェクトを生成")
    inner class GenerateWithFactoryMethodTest {

        @Test
        @DisplayName("[Success case] オブジェクト生成成功")
        fun generateSuccessFully() {
            repeat(3) { x ->
                repeat(3) { y ->
                    val team = if(Random.nextBoolean()) Team.X else Team.O

                    val move = TicTacToeMove.withCoordinate(x, y, team)

                    assertEquals(x, move.x)
                    assertEquals(y, move.y)
                    assertEquals(team, move.team)
                }
            }
        }

        @Test
        @DisplayName("[Fail case] xとyは0〜2の範囲内でなければならない")
        fun outOfBoardTest() {
            listOf(-1, 0, 3).flatMap { x ->
                listOf(-1, 0, 3).map { y-> x to y }
            }.filterNot {
                Cell.isEnable(it.first, it.second)
            }.forEach { (x, y) ->
                val team = if(Random.nextBoolean()) Team.X else Team.O

                assertThrows<ApiException>{ TicTacToeMove.withCoordinate(x, y, team) }
                    .also { assertEquals(ErrorCode.OUT_OF_BOARD, it.errorCode) }
            }
        }
    }
}