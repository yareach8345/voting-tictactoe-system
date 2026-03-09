package com.yareach.voting_tictactoe_system.unit.tictactoe

import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.tictactoe.model.Cell
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeGameInfinityModeImpl
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeGameState
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeMove
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicTacToeGameInfinityModeImplTest {

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

            val game = TicTacToeGameInfinityModeImpl(moves)

            assertEquals(Team.X, game.currentTeam)
            assertEquals(5, game.currentTurnNumber)
            assertTrue(moves.zip(game.moves).any { it.first == it.second })
        }

        @Test
        @DisplayName("空いているリストを用いてオブジェクト生成")
        fun generateTicTacToeGameWithEmptyList() {
            val game = TicTacToeGameInfinityModeImpl(listOf())

            assertEquals(Team.X, game.currentTeam)
            assertEquals(1, game.currentTurnNumber)
            assertTrue(game.moves.isEmpty())
        }

        @Test
        @DisplayName("movesのサイズが9以上でも、エラーは発生されない")
        fun containMoreThen10Moves() {
            val moves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(1, 0, Team.O),
                TicTacToeMove.withCoordinate(2, 2, Team.X),

                TicTacToeMove.withCoordinate(1, 1, Team.O),
                TicTacToeMove.withCoordinate(1, 2, Team.X),
                TicTacToeMove.withCoordinate(2, 0, Team.O),

                TicTacToeMove.withCoordinate(0, 2, Team.X),
                TicTacToeMove.withCoordinate(2, 1, Team.O),
                TicTacToeMove.withCoordinate(0, 1, Team.X),

                TicTacToeMove.withCoordinate(1, 0, Team.O),
            )

            val game = TicTacToeGameInfinityModeImpl(moves)

            assertEquals(Team.X, game.currentTeam)
            assertEquals(11, game.currentTurnNumber)
            assertTrue(moves.zip(game.moves).all { it.first == it.second })

            assertEquals(6, game.effectiveMoves.size)
            assertTrue(game.effectiveMoves.zip(moves.takeLast(6)).all { it.first == it.second })
        }

        @Test
        @DisplayName("6手以内（3巡以内）にセルの重複がある場合はエラーが発生")
        fun containDuplicatedCells() {

            val moves = listOf(
                TicTacToeMove.withCoordinate(2, 0, Team.X), // (2, 0) duplicated
                TicTacToeMove.withCoordinate(1, 1, Team.O),
                TicTacToeMove.withCoordinate(2, 2, Team.X),
                TicTacToeMove.withCoordinate(2, 0, Team.O), // (2, 0) duplicated
            )

            val exception = assertThrows<IllegalArgumentException> { TicTacToeGameInfinityModeImpl(moves) }

            assertEquals(
                "Moves must not contain duplicate cells within any 6 moves",
                exception.message
            )
        }

        @Test
        @DisplayName("3巡より前のセルであれば、重複して選択してもエラーが発生しない")
        fun legalDuplicatedCells() {

            val moves = listOf(
                TicTacToeMove.withCoordinate(2, 0, Team.X), // (2, 0) duplicated
                TicTacToeMove.withCoordinate(1, 1, Team.O),
                TicTacToeMove.withCoordinate(2, 2, Team.X),
                TicTacToeMove.withCoordinate(1, 0, Team.O),
                TicTacToeMove.withCoordinate(0, 1, Team.X),
                TicTacToeMove.withCoordinate(0, 2, Team.O),
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(2, 0, Team.O), // (2, 0) duplicated
            )

            val game = TicTacToeGameInfinityModeImpl(moves)

            assertEquals(6, game.effectiveMoves.size)
            assertEquals(6, game.effectiveMoves.map { it.cell }.distinct().size)
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

            val exception = assertThrows<IllegalArgumentException> { TicTacToeGameInfinityModeImpl(moves) }

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

            val exception = assertThrows<IllegalArgumentException> { TicTacToeGameInfinityModeImpl(moves) }

            assertEquals(
                "First team must be X",
                exception.message
            )
        }
    }

    @Nested
    @DisplayName("gameStateテスト")
    inner class GameStateTest {

        @Test
        @DisplayName("ゲームが進行中である場合、IN_PROGRESS状態になる")
        fun shouldBeInProgress() {
            val sampleMoves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(1, 1, Team.O),
            )

            val game = TicTacToeGameInfinityModeImpl(sampleMoves)

            assertEquals(TicTacToeGameState.IN_PROGRESS, game.gameState)
        }

        @Test
        @DisplayName("ゲームがXの勝利で終わった場合、X_WON状態になる")
        fun shouldBeXWon() {
            val sampleMoves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(0, 1, Team.O),
                TicTacToeMove.withCoordinate(1, 1, Team.X),
                TicTacToeMove.withCoordinate(0, 2, Team.O),
                TicTacToeMove.withCoordinate(2, 2, Team.X),
            )

            val game = TicTacToeGameInfinityModeImpl(sampleMoves)

            assertEquals(TicTacToeGameState.X_WON, game.gameState)
        }

        @Test
        @DisplayName("ゲームがOの勝利で終わった場合、O_WON状態になる")
        fun shouldBeOWon() {
            val sampleMoves = listOf(
                TicTacToeMove.withCoordinate(1, 0, Team.X),
                TicTacToeMove.withCoordinate(0, 0, Team.O),
                TicTacToeMove.withCoordinate(0, 1, Team.X),
                TicTacToeMove.withCoordinate(1, 1, Team.O),
                TicTacToeMove.withCoordinate(0, 2, Team.X),
                TicTacToeMove.withCoordinate(2, 2, Team.O),
            )

            val game = TicTacToeGameInfinityModeImpl(sampleMoves)

            assertEquals(TicTacToeGameState.O_WON, game.gameState)
        }

        @Test
        @DisplayName("有効な手は直近6手のみなので、9手目以降もDRAW状態に成らない")
        fun shouldBeDraw() {
            val sampleMoves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(1, 0, Team.O),
                TicTacToeMove.withCoordinate(2, 2, Team.X),

                TicTacToeMove.withCoordinate(1, 1, Team.O),
                TicTacToeMove.withCoordinate(1, 2, Team.X),
                TicTacToeMove.withCoordinate(2, 0, Team.O),

                TicTacToeMove.withCoordinate(0, 2, Team.X),
                TicTacToeMove.withCoordinate(2, 1, Team.O),
                TicTacToeMove.withCoordinate(0, 1, Team.X),

                TicTacToeMove.withCoordinate(1, 0, Team.O),
                TicTacToeMove.withCoordinate(2, 2, Team.X),
            )

            val game = TicTacToeGameInfinityModeImpl(sampleMoves)

            assertEquals(TicTacToeGameState.IN_PROGRESS, game.gameState)
        }
    }

    @Nested
    @DisplayName("moveテスト")
    inner class MoveTest {

        @Test
        @DisplayName("moveメソッドで着手を記録する")
        fun moveSuccessful() {
            val moves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(1, 1, Team.O),
            )

            val game = TicTacToeGameInfinityModeImpl(moves)

            game.move(Cell(2, 2))
            game.move(Cell(0, 1))

            val lastTwoMoves = game.moves.takeLast(2)

            assertEquals(4, game.moves.size)
            assertEquals(moves, game.moves.dropLast(2))
            assertEquals(TicTacToeMove.withCoordinate(2, 2, Team.X), lastTwoMoves[0])
            assertEquals(TicTacToeMove.withCoordinate(0, 1, Team.O), lastTwoMoves[1])
        }

        @Test
        @DisplayName("xとyの指定して着手を記録する")
        fun moveSuccessfulWithXAndY() {
            val moves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(1, 1, Team.O),
            )

            val game = TicTacToeGameInfinityModeImpl(moves)

            game.move(2, 2)
            game.move(0, 1)

            val lastTwoMoves = game.moves.takeLast(2)

            assertEquals(4, game.moves.size)
            assertEquals(moves, game.moves.dropLast(2))
            assertEquals(TicTacToeMove.withCoordinate(2, 2, Team.X), lastTwoMoves[0])
            assertEquals(TicTacToeMove.withCoordinate(0, 1, Team.O), lastTwoMoves[1])
        }

        @Test
        @DisplayName("もう着手したセルに再び着手すればエラー発生")
        fun occupiedTest() {
            val moves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(1, 1, Team.O),
            )

            val game = TicTacToeGameInfinityModeImpl(moves)

            val exception = assertThrows<IllegalArgumentException> { game.move(1, 1) }

            assertEquals("cell (x=1, y=1) is already occupied", exception.message)
        }

        @Test
        @DisplayName("ゲーム終了後に着手するとエラー発生")
        fun whenGameStateIsNotInProgress() {

            val sampleMoves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(0, 1, Team.O),
                TicTacToeMove.withCoordinate(1, 1, Team.X),
                TicTacToeMove.withCoordinate(0, 2, Team.O),
                TicTacToeMove.withCoordinate(2, 2, Team.X),
            )

            val game = TicTacToeGameInfinityModeImpl(sampleMoves)

            val exception = assertThrows<IllegalArgumentException> { game.move(1, 0) }

            assertEquals("The game is already finished", exception.message)
        }

        @Test
        @DisplayName("6手以後、着手すればもっとも古い手から無効になる")
        fun oldestMovesShouldBeExcludedFromEffectiveMoves() {

            val sampleMoves = listOf(
                TicTacToeMove.withCoordinate(0, 0, Team.X),
                TicTacToeMove.withCoordinate(0, 1, Team.O),
                TicTacToeMove.withCoordinate(1, 1, Team.X),
                TicTacToeMove.withCoordinate(0, 2, Team.O),
                TicTacToeMove.withCoordinate(2, 0, Team.X),
                TicTacToeMove.withCoordinate(2, 2, Team.O),
            )

            val game = TicTacToeGameInfinityModeImpl(sampleMoves)

            game.move(1, 0)

            assertEquals(6, game.effectiveMoves.size)
            assertEquals(sampleMoves[1], game.effectiveMoves[0]) // もっとも古いTicTacToeMoveが無効になってeffectiveMovesから除外される
        }
    }
}