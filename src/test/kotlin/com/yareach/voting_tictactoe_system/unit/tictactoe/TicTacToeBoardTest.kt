package com.yareach.voting_tictactoe_system.unit.tictactoe

import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.tictactoe.model.Cell
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeBoard
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeMove
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class TicTacToeBoardTest {

    @Nested
    @DisplayName("オブジェクト生成テスト")
    inner class GenerateTicTacToeBoardTest {

        @Nested
        @DisplayName("コンストラクタ")
        inner class WithConstructorTest {

            @Test
            @DisplayName("[Success case] 3x3のボードオブジェクトを生成")
            fun generateTicTacToeBoardSuccess() {

                val inputList = listOf(
                    listOf(Team.X, null, null),
                    listOf(Team.O, Team.X, null),
                    listOf(null, null, Team.O),
                )
                val board = TicTacToeBoard(inputList)

                assertEquals(3, board.board.size)

                board.board.forEach { row -> assertEquals(3, row.size) }

                repeat(3) { y ->
                    repeat(3) { x ->

                        val fromBoard = board.getAt(x, y)
                        val fromInputList = inputList[y][x]

                        assertEquals(fromInputList, fromBoard)
                    }
                }
            }

            @Test
            @DisplayName("[Fail case] 入力値のリストのサイズが3×3でなければ失敗する")
            fun whenListSizeIsWrong() {

                val inputList = listOf(
                    listOf(Team.X, null),
                    listOf(Team.O, Team.X, null),
                    listOf(null, null, Team.O),
                )

                assertThrows<IllegalArgumentException> { TicTacToeBoard(inputList) }
            }
        }

        @Nested
        @DisplayName("fromMoves Factoryメソッド")
        inner class WithFromMovesFactoryMethodTest {

            @Test
            @DisplayName("[Success Case] Factoryメソッドを用いてTicTacToeBoard生成")
            fun generateMoveSuccess() {

                val moves = listOf(
                    Cell(0, 0),
                    Cell(1, 1),
                    Cell(2, 2),
                    Cell(0, 1),
                ).mapIndexed { index, cell ->
                    val team = if(index == 0) Team.X else Team.O
                    TicTacToeMove(cell, team)
                }

                val board = TicTacToeBoard.fromMoves(moves)

                // nullではないセルの値を検証
                moves.forEach { move -> assertEquals(move.team, board.getAt(move.x, move.y)) }

                // nullであるセルを検証
                (0 .. 2).flatMap { y -> (0 .. 2) // (0,0) ~ (2,2)　範囲のセルを生成
                    .map { x -> Cell(x, y) } }
                    .filterNot { cell -> moves.any { move -> move.x == cell.x && move.y == cell.y } } // nullではないセルを排除
                    .forEach { assertNull(board.getAt(it.x, it.y)) }
            }

            @Test
            @DisplayName("[Fail case] 同じセルのデータが２つ以上存在すればエラー発生")
            fun whenCellDuplicated() {

                val moves = listOf(
                    Cell(0, 0),
                    Cell(1, 1), // (1,1) duplicated
                    Cell(1, 1), // (1,1) duplicated
                    Cell(2, 2),
                    Cell(0, 1),
                ).mapIndexed { index, cell ->
                    val team = if(index == 0) Team.X else Team.O
                    TicTacToeMove(cell, team)
                }

                assertThrows<IllegalArgumentException> { TicTacToeBoard.fromMoves(moves) }
            }
        }
    }

    @Nested
    @DisplayName("getAtメソッドで特定のセルの値を得る")
    inner class GetAtMethodTest {

        @Test
        @DisplayName("[Success case] (0,0) ~ (2,2) 範囲のセルのデータ修得")
        fun getDataWithGetAtMethod() {
            val moves = listOf(
                Cell(0, 0),
                Cell(1, 1),
                Cell(2, 2),
                Cell(0, 1),
            ).mapIndexed { index, cell ->
                val team = if (index == 0) Team.X else Team.O
                TicTacToeMove(cell, team)
            }

            val board = TicTacToeBoard.fromMoves(moves)

            repeat(3) { y ->
                repeat(3) { x ->
                    val fromBoard = board.getAt(x, y)

                    val expectedValue = moves
                        .firstOrNull { move -> move.x == x && move.y == y }
                        ?.team

                    assertEquals(expectedValue, fromBoard)
                }
            }
        }

        @Test
        @DisplayName("[Fail case] getAtの引数、xとyは0〜2の範囲内でなければならない")
        fun outOfBoardTest() {

            val moves = listOf(
                Cell(0, 0),
            ).mapIndexed { index, cell ->
                val team = if (index == 0) Team.X else Team.O
                TicTacToeMove(cell, team)
            }

            val board = TicTacToeBoard.fromMoves(moves)

            listOf(-1, 0, 3).flatMap { x -> listOf(-1, 0, 3).map { y -> x to y } }
                .filterNot { (x, y) -> x == 0 && y == 0 }
                .forEach { (x, y) -> assertThrows<IllegalArgumentException> { board.getAt(x, y) } }
        }
    }
}