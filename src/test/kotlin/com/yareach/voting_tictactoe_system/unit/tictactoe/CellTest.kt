package com.yareach.voting_tictactoe_system.unit.tictactoe

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.tictactoe.model.Cell
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CellTest {

    @Nested
    @DisplayName("Cellオブジェクト生成")
    inner class GenerateCellTest {

        @Test
        @DisplayName("[Success Case] (0,0)~(3,3)　範囲のセルに対するオブジェクト生成")
        fun successCase() {
            repeat(3) { x ->
                repeat(3) { y ->
                    val move = Cell(x, y)

                    assertEquals(x, move.x)
                    assertEquals(y, move.y)
                }
            }
        }

        @Test
        @DisplayName("[Fail case] xとyは0〜2の範囲内でなければならない")
        fun outOfBoardTest() {

            listOf(-1, 0, 3)
                .flatMap { x -> listOf(-1, 0, 3).map { y -> x to y } }
                .filterNot { (x, y) -> x == 0 && y == 0 }
                .forEach { coord ->
                    assertThrows<ApiException>{ Cell(coord.first, coord.second) }
                        .also { assertEquals(ErrorCode.OUT_OF_BOARD, it.errorCode) }
                }
        }
    }

    @Nested
    @DisplayName("xとyの値検証")
    inner class ValidateTest {

        @Test
        @DisplayName("[Success case] xとyは0〜2の範囲内でなければfalseを返す")
        fun isEnableReturnTrueTest() {

            repeat(3) { y ->
                repeat(3) { x ->
                    assertTrue(Cell.isEnable(y, x))
                }
            }
        }

        @Test
        @DisplayName("[Success case] xとyは0〜2の範囲内でなければfalseを返す")
        fun isEnableReturnFalseTest() {

            listOf(-1, 0, 3)
                .flatMap { x -> listOf(-1, 0, 3).map { y -> x to y } }
                .filterNot { (x, y) -> x == 0 && y == 0 }
                .forEach { coord -> assertFalse(Cell.isEnable(coord.first, coord.second)) }
        }
    }
}