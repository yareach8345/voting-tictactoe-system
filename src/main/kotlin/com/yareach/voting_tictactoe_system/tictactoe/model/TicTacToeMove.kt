package com.yareach.voting_tictactoe_system.tictactoe.model

import com.yareach.voting_tictactoe_system.player.common.Team

data class TicTacToeMove(
    val cell: Cell,
    val team: Team
) {

    companion object {
        fun withCoordinate(x: Int, y: Int, team: Team):  TicTacToeMove {
            val cell = Cell(x, y)
            return TicTacToeMove(cell, team)
        }
    }

    val x: Int
        get() = cell.x

    val y: Int
        get() = cell.y
}
