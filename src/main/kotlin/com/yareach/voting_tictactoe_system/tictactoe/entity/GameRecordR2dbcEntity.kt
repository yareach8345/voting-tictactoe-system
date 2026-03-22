package com.yareach.voting_tictactoe_system.tictactoe.entity

import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.tictactoe.model.Cell
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeMove
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("game_record")
class GameRecordR2dbcEntity(
    @Id @Column("id")
    val id: Long? = null,

    @Column("group_id")
    val groupId: String,

    @Column("team")
    val team: Team,

    @Column("x")
    val x: Int,
    @Column("y")
    val y: Int,

    @Column("timestamp")
    val timeStamp: LocalDateTime? = null,
) {
    companion object {
        fun new(groupId: String, move: TicTacToeMove) = GameRecordR2dbcEntity(
            groupId = groupId,
            team = move.team,
            x = move.x,
            y = move.y
        )
    }

    fun toModel() = TicTacToeMove(
        team = team,
        cell = Cell(x, y)
    )
}