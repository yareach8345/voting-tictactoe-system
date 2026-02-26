package com.yareach.voting_tictactoe_system.player.entity

import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.player.model.Player
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "player")
class PlayerR2dbcEntity(
    @Id @Column("id")
    val id: Long?,

    @Column("group_id")
    val groupId: String,

    @Column("user_id")
    val userId: String,

    @Column("team")
    val team: Team?
) {
    companion object {
        fun fromModel(model: Player): PlayerR2dbcEntity = PlayerR2dbcEntity(
            id = model.id,
            groupId = model.groupId,
            userId = model.userId,
            team = model.team
        )
    }

    fun toModel(): Player = Player(
        id = id,
        groupId = groupId,
        userId = userId,
        team = team
    )
}