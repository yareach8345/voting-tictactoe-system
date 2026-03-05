package com.yareach.voting_tictactoe_system.tictactoe.model

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.player.common.Team

data class TicTacToeMove(
    val y: Int,
    val x: Int,
    val team: Team,
) {
    init {
        if(x !in 0 until 3) throw ApiException(ErrorCode.OUT_OF_BOARD, "x must be between 0 and 2")
        if(y !in 0 until 3) throw ApiException(ErrorCode.OUT_OF_BOARD, "y must be between 0 and 2")
    }
}
