package com.yareach.voting_tictactoe_system.voting_system.service

import com.yareach.voting_tictactoe_system.voting_system.config.VoteServerConfig
import com.yareach.voting_tictactoe_system.voting_system.dto.ChangeElectionStateRequestDto
import com.yareach.voting_tictactoe_system.voting_system.dto.ChangeElectionStateResponseDto
import com.yareach.voting_tictactoe_system.voting_system.dto.ElectionGenerateResponseDto
import com.yareach.voting_tictactoe_system.voting_system.dto.VoteRequestDto
import com.yareach.voting_tictactoe_system.voting_system.dto.VoteResponseDto
import com.yareach.voting_tictactoe_system.voting_system.dto.VoteStatistic
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

interface VotingService {

    suspend fun generateElection(): ElectionGenerateResponseDto

    suspend fun deleteElection(electionId: String)

    suspend fun startElection(electionId: String): ChangeElectionStateResponseDto

    suspend fun endElection(electionId: String): ChangeElectionStateResponseDto

    suspend fun vote(electionId: String, voteRequestDto: VoteRequestDto): VoteResponseDto

    suspend fun getStatistic(electionId: String): VoteStatistic
}

@Service
class VotingServiceImpl(
    config: VoteServerConfig,
    webClientBuilder: WebClient.Builder
) : VotingService {
    private val webClient = webClientBuilder.baseUrl(config.url).build()

    /**
     * テストのため作成したコード。
     * 開発際、参考するため残す。
     * 後で必ず消すこと。
     */
//    init {
//        runBlocking {
//            try {
//                println("test!!!!")
//                val electionId = generateElection().electionId
//                println("election generated -  electionId :  $electionId")
//                startElection(electionId)
//                println("??")
//                vote(electionId, VoteRequestDto("userId-1", "v1"))
//                vote(electionId, VoteRequestDto("userId-2", "v1"))
//                vote(electionId, VoteRequestDto("userId-3", "v1"))
//                vote(electionId, VoteRequestDto("userId-4", "9999999"))
//                endElection(electionId)
//                val result = getStatistic(electionId)
//                println(result.toString())
//                deleteElection(electionId)
//                println("test!!!!")
//            } catch (e: Exception) {
//                println(e)
//            }
//        }
//    }

    override suspend fun generateElection(): ElectionGenerateResponseDto {
        return webClient.post().uri("/elections").retrieve().awaitBody<ElectionGenerateResponseDto>()
    }

    override suspend fun deleteElection(electionId: String) {
        webClient.delete().uri("/elections/$electionId").retrieve().awaitBody<Unit>()
    }

    override suspend fun startElection(electionId: String): ChangeElectionStateResponseDto {
        val changeElectionStateRequest = ChangeElectionStateRequestDto(state = "open")
        return webClient.patch().uri("/elections/$electionId/state")
            .bodyValue(changeElectionStateRequest)
            .retrieve()
            .awaitBody<ChangeElectionStateResponseDto>()
    }

    override suspend fun endElection(electionId: String): ChangeElectionStateResponseDto {
        val changeElectionStateRequest = ChangeElectionStateRequestDto(state = "close")
        return webClient.patch().uri("/elections/$electionId/state")
            .bodyValue(changeElectionStateRequest)
            .retrieve()
            .awaitBody<ChangeElectionStateResponseDto>()
    }

    override suspend fun vote(electionId: String, voteRequestDto: VoteRequestDto): VoteResponseDto {
        return webClient.post().uri("/elections/$electionId/votes")
            .bodyValue(voteRequestDto)
            .retrieve()
            .awaitBody<VoteResponseDto>()
    }

    override suspend fun getStatistic(electionId: String): VoteStatistic {
        return webClient.get().uri("/elections/$electionId/votes/statistic")
            .retrieve()
            .awaitBody<VoteStatistic>()
    }
}