package com.yareach.voting_tictactoe_system.player.entity

import com.yareach.voting_tictactoe_system.player.common.Team
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
    override fun toString(): String {
        return "$id: $userId, $groupId, $team"
    }
}