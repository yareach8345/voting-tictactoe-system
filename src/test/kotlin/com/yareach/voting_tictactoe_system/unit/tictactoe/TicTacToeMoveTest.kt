package com.yareach.voting_tictactoe_system.unit.tictactoe

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeMove
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class TicTacToeMoveTest {

    @Nested
    @DisplayName("Moveドメインモデル生成")
    inner class GenerateTest {

        @Test
        @DisplayName("[Success case] TicTacToeMoveオブジェクトを生成")
        fun generateSuccessFully() {
            repeat(3) { y ->
                repeat(3) { x ->
                    val move = TicTacToeMove(y, x, Team.O)

                    assertEquals(y, move.y)
                    assertEquals(x, move.x)
                    assertEquals(Team.O, move.team)
                }
            }
        }

        @Test
        @DisplayName("[Fail case] xとyは0〜2の範囲内でなければならない")
        fun outOfBoardTest() {
            val team = if(Random.nextBoolean()) Team.X else Team.O

            // y < 0 の場合
            assertThrows<ApiException>{ TicTacToeMove(-1, 0, team) }
                .also { assertEquals(ErrorCode.OUT_OF_BOARD, it.errorCode) }

            // y > 2 の場合
            assertThrows<ApiException>{ TicTacToeMove(3, 0, team) }
                .also { assertEquals(ErrorCode.OUT_OF_BOARD, it.errorCode) }

            // x < 0 の場合
            assertThrows<ApiException>{ TicTacToeMove(0, -1, team) }
                .also { assertEquals(ErrorCode.OUT_OF_BOARD, it.errorCode) }

            // x > 2 の場合
            assertThrows<ApiException>{ TicTacToeMove(0, 3, team) }
                .also { assertEquals(ErrorCode.OUT_OF_BOARD, it.errorCode) }
        }
    }
}