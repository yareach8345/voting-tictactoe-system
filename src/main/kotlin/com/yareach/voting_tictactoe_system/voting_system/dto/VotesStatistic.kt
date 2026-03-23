package com.yareach.voting_tictactoe_system.voting_system.dto

import java.time.LocalDateTime

data class VoteStatisticCount(
    val item: String,
    val voteCount: Int
)

data class VoteStatistic(
    val voteCounts: List<VoteStatisticCount>,
    val aggregatedAt: LocalDateTime,
)