package com.yareach.voting_tictactoe_system.tictactoe.entity

import com.yareach.voting_tictactoe_system.common.enum.GameType
import com.yareach.voting_tictactoe_system.tictactoe.model.TicTacToeGameInfo
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tictactoe_game_info")
class TicTacToeGameInfoR2dbcEntity(
    @Id @Column("id")
    val id: Long? = null,

    @Column("group_id")
    val groupId: String,

    @Column("type")
    val type: GameType,
) {
    companion object {
        fun fromModel(model: TicTacToeGameInfo) = TicTacToeGameInfoR2dbcEntity(
            id = model.id,
            groupId = model.groupId,
            type = model.type,
        )
    }

    fun toModel() = TicTacToeGameInfo(
        id = id,
        groupId = groupId,
        type = type,
    )
}