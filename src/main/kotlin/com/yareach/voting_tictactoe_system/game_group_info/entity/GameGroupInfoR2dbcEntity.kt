package com.yareach.voting_tictactoe_system.game_group_info.entity

import com.yareach.voting_tictactoe_system.game_group_info.model.GameGroupInfo
import com.yareach.voting_tictactoe_system.tictactoe.enum.GameType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "game_group_info")
class GameGroupInfoR2dbcEntity(
    @Id @Column("id")
    val id: Int? = null,

    @Column("group_id")
    val groupId: String,

    @Column("game_type")
    val gameType: GameType,
) {
    companion object {
        fun fromModel(model: GameGroupInfo) = GameGroupInfoR2dbcEntity(
            id = model.id,
            groupId = model.groupId,
            gameType = model.gameType
        )
    }

    fun toModel() = GameGroupInfo(
        id = this.id,
        groupId = this.groupId,
        gameType = this.gameType
    )
}