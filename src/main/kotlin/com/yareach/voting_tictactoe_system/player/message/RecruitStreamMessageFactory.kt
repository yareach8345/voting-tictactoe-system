package com.yareach.voting_tictactoe_system.player.message

import com.yareach.voting_tictactoe_system.player.proto.PlayersByTeam
import com.yareach.voting_tictactoe_system.player.proto.RecruitAccepted
import com.yareach.voting_tictactoe_system.player.proto.RecruitCompleted
import com.yareach.voting_tictactoe_system.player.proto.RecruitStreamMessage

object RecruitStreamMessageFactory {
    fun buildAcceptMessage(userId: String): RecruitStreamMessage {
        val accepted = RecruitAccepted.newBuilder()
            .setUserId(userId)
            .build()

        val streamMessage = RecruitStreamMessage.newBuilder()
            .setAccepted(accepted)
            .build()

        return streamMessage
    }

    fun buildCompletedMessage(playersByTeam: PlayersByTeam): RecruitStreamMessage {
        val completed = RecruitCompleted.newBuilder()
            .setPlayersByTeam(playersByTeam)
            .build()

        val streamMessage = RecruitStreamMessage.newBuilder()
            .setCompleted(completed)
            .build()

        return streamMessage
    }
}