package com.yareach.voting_tictactoe_system.unit.tictactoe.tictactoe_game

import com.yareach.voting_tictactoe_system.common.enum.GameType
import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeGame
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeGameInfinityModeImpl
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeGameNormalModeImpl
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeMove
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertInstanceOf
import kotlin.test.Test

class TicTacToeGameFactory {

    val sampleMoves = listOf(
        TicTacToeMove.withCoordinate(0, 0, Team.X),
        TicTacToeMove.withCoordinate(2, 0, Team.O),
        TicTacToeMove.withCoordinate(1, 1, Team.X),
        TicTacToeMove.withCoordinate(2, 2, Team.O),
    )

    @Test
    @DisplayName("movesないでNormalGameオブジェクトを生成する")
    fun generateNormalGameWithoutMoves() {
        val game = TicTacToeGame.from(GameType.NORMAL)

        assertEquals(GameType.NORMAL, game.gameType)
        assertEquals(0, game.moves.size)
        assertInstanceOf<TicTacToeGameNormalModeImpl>(game)
    }

    @Test
    @DisplayName("movesを持ってNormalGameオブジェクトを生成する")
    fun generateNormalGameWithMoves() {
        val game = TicTacToeGame.from(GameType.NORMAL, sampleMoves)

        assertEquals(GameType.NORMAL, game.gameType)
        assertEquals(sampleMoves.size, game.moves.size)
        game.moves.forEachIndexed { i, move -> assertEquals(sampleMoves[i].cell, move.cell) }
        assertInstanceOf<TicTacToeGameNormalModeImpl>(game)
    }

    @Test
    @DisplayName("movesないでInfinityGameオブジェクトを生成する")
    fun generateInfinityGameWithoutMoves() {
        val game = TicTacToeGame.from(GameType.INFINITY)

        assertEquals(GameType.INFINITY, game.gameType)
        assertEquals(0, game.moves.size)
        assertInstanceOf<TicTacToeGameInfinityModeImpl>(game)
    }

    @Test
    @DisplayName("movesを持ってInfinityGameオブジェクトを生成する")
    fun generateInfinityGameWithMoves() {
        val game = TicTacToeGame.from(GameType.INFINITY, sampleMoves)

        assertEquals(GameType.INFINITY, game.gameType)
        assertEquals(sampleMoves.size, game.moves.size)
        game.moves.forEachIndexed { i, move -> assertEquals(sampleMoves[i].cell, move.cell) }
        assertInstanceOf<TicTacToeGameInfinityModeImpl>(game)
    }
}