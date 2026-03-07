package com.yareach.voting_tictactoe_system.tictactoe.model

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.player.common.Team

enum class TicTacToeGameState{
    X_WON, O_WON, DRAW, IN_PROGRESS;

    companion object {
        fun winOf(team: Team) = when(team){
            Team.X -> X_WON
            Team.O -> O_WON
        }
    }
}

fun List<TicTacToeMove>.hasDuplicateCell() = distinctBy { it.y to it.x }.size != size

fun List<TicTacToeMove>.hasAlternatingTeams() = zipWithNext().all { it.first.team != it.second.team }

abstract class TicTacToeGame(
    initMoves: List<TicTacToeMove>,
) {
    init {
        if(initMoves.isNotEmpty()) { require(initMoves.first().team == Team.X ) { "First team must be X" } }
        require(initMoves.hasAlternatingTeams()) { "Team must alternate turns" }
    }

    private val _moves = initMoves.toMutableList()

    val moves: List<TicTacToeMove>
        get() = _moves.toList()

    val gameState: TicTacToeGameState
        get() = calcGameState()

    val currentTeam: Team
        get() = if(_moves.size % 2 == 0) Team.X else Team.O

    val currentTurnNumber: Int
        get() = _moves.size + 1

    abstract val effectiveMoves: List<TicTacToeMove>

    private fun calcGameState():  TicTacToeGameState{
        val lastMove = effectiveMoves.lastOrNull() ?: return TicTacToeGameState.IN_PROGRESS

        val board = TicTacToeBoard.fromMoves(effectiveMoves)

        // coordinateCalculatorで指定された座標上のマークがすべて同じかを判定する
        fun isLineCompleted(coordinateCalculator: (Int) -> Pair<Int, Int>) = (0 until 3)
            .map { coordinateCalculator(it) }
            .map { board.getAt(it.first, it.second) }
            .all { it == lastMove.team }

        //Check row
        if(isLineCompleted { it to lastMove.y }) return TicTacToeGameState.winOf(lastMove.team)

        //Check column
        if(isLineCompleted {  lastMove.y to it }) return TicTacToeGameState.winOf(lastMove.team)

        //Check main diagonal
        if(lastMove.x == lastMove.y && isLineCompleted{it to it}) return TicTacToeGameState.winOf(lastMove.team)

        //Check anti diagonal
        if(lastMove.x == 2 - lastMove.y && isLineCompleted{2 - it to it}) return TicTacToeGameState.winOf(lastMove.team)

        // 盤面がすべて埋まっている場合は引き分け
        if(effectiveMoves.size  == 9) return TicTacToeGameState.DRAW

        return TicTacToeGameState.IN_PROGRESS
    }

    fun move(x: Int, y: Int) {
        move(Cell(x, y))
    }

    fun move(cell: Cell) {
        if(gameState != TicTacToeGameState.IN_PROGRESS) throw ApiException(ErrorCode.GAME_IS_ENDED, "The game is already ended")
        if(effectiveMoves.any { it.x == cell.x && it.y == cell.y }) throw ApiException(ErrorCode.ALREADY_OCCUPIED_CELL, "cell (x=${cell.x}, y=${cell.y}) is already occupied")

        _moves.add(TicTacToeMove(cell, currentTeam))
    }
}

class TicTacToeGameNormalModeImpl(
    moves: List<TicTacToeMove> = listOf()
): TicTacToeGame(moves) {

    init {
        require(moves.size <= 9) { "There must be at least 9 moves" }
        require(!moves.hasDuplicateCell()) { "Moves must not contain duplicate cells" }
    }

    override val effectiveMoves: List<TicTacToeMove>
        get() = moves.toList()
}