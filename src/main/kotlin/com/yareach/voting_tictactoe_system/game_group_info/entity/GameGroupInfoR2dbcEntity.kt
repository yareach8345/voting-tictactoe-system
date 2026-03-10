package com.yareach.voting_tictactoe_system.game_group_info.entity

import com.yareach.voting_tictactoe_system.game_group_info.enum.GameState
import com.yareach.voting_tictactoe_system.game_group_info.model.GameGroupInfo
import com.yareach.voting_tictactoe_system.tictactoe.enum.GameType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "game_group_info")
class GameGroupInfoR2dbcEntity(
    @Id @Column("id")
    val id: Int? = null,

    @Column("group_id")
    val groupId: String,

    @Column("game_type")
    val gameType: GameType,

    @Column("state")
    var state: GameState,

    @Column("last_updated")
    var lastUpdated: LocalDateTime,
) {
    companion object {
        fun fromModel(model: GameGroupInfo) = GameGroupInfoR2dbcEntity(
            id = model.id,
            groupId = model.groupId,
            gameType = model.gameType,
            state = model.state,
            lastUpdated = model.lastUpdated,
        )
    }

    fun toModel() = GameGroupInfo(
        id = this.id,
        groupId = this.groupId,
        gameType = this.gameType,
        state = this.state,
        lastUpdated = this.lastUpdated,
    )
}