package com.yareach.voting_tictactoe_system.tictactoe.model

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.player.common.Team

class TicTacToeBoard(
    val board: List<List<Team?>>
) {
    init {
        require(board.size == 3) { "A board must have 3 rows." }
        require(board.all { it.size == 3 }) { "A row must have 3 columns." }
    }

    fun getAt(x: Int, y: Int): Team? {
        require(x in 0 until 3) { "x must be between 0 and 3" }
        require(y in 0 until 3) { "y must be between 0 and 3" }

        return board[y][x]
    }

    companion object {
        fun fromMoves(moves: List<TicTacToeMove>): TicTacToeBoard {

            val temp = List(3) { MutableList<Team?>(3) { null } }

            moves.forEach { move ->
                if(temp[move.y][move.x] != null) { throw IllegalArgumentException("Duplicate cell (x=${move.x}, y=${move.y})") }

                temp[move.y][move.x] = move.team
            }

            val board = temp.map { it.toList() }
            return TicTacToeBoard(board)
        }
    }
}
