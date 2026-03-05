package com.yareach.voting_tictactoe_system.tictactoe.model

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode

data class Cell(
    val x: Int,
    val y: Int,
) {
    init {
        if(!isEnable(x, y)) {
            throw ApiException(ErrorCode.OUT_OF_BOARD, "x and y must be between 0 and 2")
        }
    }

    companion object {
        fun isEnable(x: Int, y: Int): Boolean {
            return x in 0..2 && y in 0..2
        }
    }
}
