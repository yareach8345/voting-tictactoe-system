package com.yareach.voting_tictactoe_system.unit.tictactoe

import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.tictactoe.model.Cell
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeGameNormalModeImpl
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeMove
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicTacToeGameNormalModeImplTest {

    @Nested
    @DisplayName("オブジェクト生成テスト")
    inner class ConstructTest {

        @Test
        @DisplayName("movesを用いてオブジェクト生成")
        fun generateTicTacToeGame() {
            val moves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(1, 1, Team.O),
                TicTacToeMove.withCoordinate(2, 2, Team.X),
                TicTacToeMove.withCoordinate(0, 1, Team.O),
            )

            val game = TicTacToeGameNormalModeImpl(moves)

            assertEquals(Team.X, game.currentTeam)
            assertEquals(5, game.currentTurnNumber)
            assertTrue(moves.zip(game.moves).any { it.first == it.second })
        }

        @Test
        @DisplayName("空いているリストを用いてオブジェクト生成")
        fun generateTicTacToeGameWithEmptyList() {
            val game = TicTacToeGameNormalModeImpl(listOf())

            assertEquals(Team.X, game.currentTeam)
            assertEquals(1, game.currentTurnNumber)
            assertTrue(game.moves.isEmpty())
        }

        @Test
        @DisplayName("movesのサイズが9以上であれば、エラー発生")
        fun containMoreThen10Moves() {
            val moves = List(10) { index ->
                val x = Random.nextInt(0, 3)
                val y = Random.nextInt(0, 3)
                val team = if(index % 2 == 0) Team.X else Team.O
                TicTacToeMove.withCoordinate(x, y, team)
            }

            val exception = assertThrows<IllegalArgumentException> { TicTacToeGameNormalModeImpl(moves) }

            assertEquals(
                "There must be at least 9 moves",
                exception.message
            )
        }

        @Test
        @DisplayName("movesのcellが重複するとエラー発生")
        fun containDuplicatedCells() {

            val moves = listOf(
                TicTacToeMove.withCoordinate(2, 0, Team.X), // (2, 0) duplicated
                TicTacToeMove.withCoordinate(1, 1, Team.O),
                TicTacToeMove.withCoordinate(2, 2, Team.X),
                TicTacToeMove.withCoordinate(2, 0, Team.O), // (2, 0) duplicated
            )

            val exception = assertThrows<IllegalArgumentException> { TicTacToeGameNormalModeImpl(moves) }

            assertEquals(
                "Moves must not contain duplicate cells",
                exception.message
            )
        }

        @Test
        @DisplayName("movesのteamがX→Oの順で交互にならない場合はエラー")
        fun teamNotAlternate() {

            val moves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(1, 1, Team.O),
                TicTacToeMove.withCoordinate(2, 2, Team.X), // Error! consecutive X moves
                TicTacToeMove.withCoordinate(0, 1, Team.X), // Error! consecutive X moves
            )

            val exception = assertThrows<IllegalArgumentException> { TicTacToeGameNormalModeImpl(moves) }

            assertEquals(
                "Team must alternate turns",
                exception.message
            )
        }

        @Test
        @DisplayName("最初のmoveはXではなければならない")
        fun teamOfFirstTeamIsMustX() {

            val moves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.O),
                TicTacToeMove.withCoordinate(1, 1, Team.X),
                TicTacToeMove.withCoordinate(2, 2, Team.O),
            )

            val exception = assertThrows<IllegalArgumentException> { TicTacToeGameNormalModeImpl(moves) }

            assertEquals(
                "First team must be X",
                exception.message
            )
        }
    }

    @Nested
    @DisplayName("moveテスト")
    inner class MoveTest {
        val sampleMoves = listOf(
            TicTacToeMove.withCoordinate(0, 0, Team.X),
            TicTacToeMove.withCoordinate(1, 1, Team.O),
        )

        val game = TicTacToeGameNormalModeImpl(sampleMoves)

        @Test
        @DisplayName("moveメソッドで着手を記録する")
        fun moveSuccessful() {

            game.move(Cell(2, 2))
            game.move(Cell(0, 1))

            val lastTwoMoves = game.moves.takeLast(2)

            assertEquals(4, game.moves.size)
            assertEquals(sampleMoves, game.moves.dropLast(2))
            assertEquals(TicTacToeMove.withCoordinate(2, 2, Team.X), lastTwoMoves[0])
            assertEquals(TicTacToeMove.withCoordinate(0, 1, Team.O), lastTwoMoves[1])
        }

        @Test
        @DisplayName("xとyの値を持って着手を記録する")
        fun moveSuccessfulWithXAndY() {

            game.move(2, 2)
            game.move(0, 1)

            val lastTwoMoves = game.moves.takeLast(2)

            assertEquals(4, game.moves.size)
            assertEquals(sampleMoves, game.moves.dropLast(2))
            assertEquals(TicTacToeMove.withCoordinate(2, 2, Team.X), lastTwoMoves[0])
            assertEquals(TicTacToeMove.withCoordinate(0, 1, Team.O), lastTwoMoves[1])
        }
    }
}