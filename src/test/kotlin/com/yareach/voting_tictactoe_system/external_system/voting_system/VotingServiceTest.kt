package com.yareach.voting_tictactoe_system.external_system.voting_system

import com.yareach.voting_tictactoe_system.voting_system.dto.ChangeElectionStateRequestDto
import com.yareach.voting_tictactoe_system.voting_system.dto.ChangeElectionStateResponseDto
import com.yareach.voting_tictactoe_system.voting_system.dto.ElectionGenerateResponseDto
import com.yareach.voting_tictactoe_system.voting_system.dto.VoteRequestDto
import com.yareach.voting_tictactoe_system.voting_system.dto.VoteResponseDto
import com.yareach.voting_tictactoe_system.voting_system.dto.VoteStatistic
import com.yareach.voting_tictactoe_system.voting_system.dto.VoteStatisticCount
import com.yareach.voting_tictactoe_system.voting_system.service.VotingService
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
class VotingServiceTest {

    @Autowired
    lateinit var votingService: VotingService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var mockBackServer: MockWebServer

    val testElectionId = "testElectionId"
    val testTime: LocalDateTime = LocalDateTime.of(2020, 1, 1, 0, 0)

    @BeforeEach
    fun setUpMockWebServer() {
        mockBackServer = MockWebServer()
        mockBackServer.start(8081)
    }

    @AfterEach
    fun tearDownMockWebServer() {
        mockBackServer.shutdown()
    }

    @Nested
    inner class GenerateElectionTest {

        @BeforeEach
        fun enqueueResponse() {
            mockBackServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(objectMapper.writeValueAsString(ElectionGenerateResponseDto(testElectionId)))
                    .addHeader("Content-Type", "application/json")
            )
        }

        @Test
        fun shouldSendGenerateRequest() = runTest {
            val result = votingService.generateElection()

            assertEquals(testElectionId, result.electionId)

            val recordedRequest = mockBackServer.takeRequest()
            assertEquals("/elections", recordedRequest.path)
            assertEquals("POST", recordedRequest.method)
        }
    }

    @Nested
    inner class DeleteElectionTest {

        @BeforeEach
        fun enqueueResponse() {
            mockBackServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
            )
        }

        @Test
        fun shouldSendDeleteRequest() = runTest {
            votingService.deleteElection(testElectionId)

            val recordedRequest = mockBackServer.takeRequest()
            assertEquals("/elections/$testElectionId", recordedRequest.path)
            assertEquals("DELETE", recordedRequest.method)
        }
    }

    @Nested
    inner class StartElectionTest {

        val openState = "open"

        @BeforeEach
        fun enqueueResponse() {

            val response = ChangeElectionStateResponseDto(
                state = openState,
                electionId = testElectionId,
                updatedAt = testTime
            )

            mockBackServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(objectMapper.writeValueAsString(response))
                    .addHeader("Content-Type", "application/json")
            )
        }

        @Test
        fun shouldSendElectionStartRequest() = runTest {
            votingService.startElection(testElectionId)

            val recordedRequest = mockBackServer.takeRequest()
            assertEquals("/elections/$testElectionId/state", recordedRequest.path)
            assertEquals("PATCH", recordedRequest.method)
        }

        @Test
        fun shouldSendRequestWithNewStateOpen() = runTest {
            votingService.startElection(testElectionId)

            val recordedRequest = mockBackServer.takeRequest()
            val requestBody = objectMapper.readValue<ChangeElectionStateRequestDto>(recordedRequest.body.readUtf8())

            assertEquals(openState, requestBody.state)
        }

        @Test
        fun shouldReceiveResponseDtoFromServer() = runTest {
            val result = votingService.startElection(testElectionId)

            assertEquals(testElectionId, result.electionId)
            assertEquals(openState, result.state)
            assertEquals(testTime, result.updatedAt)
        }
    }

    @Nested
    inner class EndElectionTest {

        val closeState = "close"

        @BeforeEach
        fun enqueueResponse() {

            val response = ChangeElectionStateResponseDto(
                state = closeState,
                electionId = testElectionId,
                updatedAt = testTime
            )

            mockBackServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(objectMapper.writeValueAsString(response))
                    .addHeader("Content-Type", "application/json")
            )
        }

        @Test
        fun shouldSendElectionStartRequest() = runTest {
            votingService.endElection(testElectionId)

            val recordedRequest = mockBackServer.takeRequest()
            assertEquals("/elections/$testElectionId/state", recordedRequest.path)
            assertEquals("PATCH", recordedRequest.method)
        }

        @Test
        fun shouldSendRequestWithNewStateClose() = runTest {
            votingService.endElection(testElectionId)

            val recordedRequest = mockBackServer.takeRequest()
            val requestBody = objectMapper.readValue<ChangeElectionStateRequestDto>(recordedRequest.body.readUtf8())

            assertEquals(closeState, requestBody.state)
        }

        @Test
        fun shouldReceiveResponseDtoFromServer() = runTest {
            val result = votingService.endElection(testElectionId)

            assertEquals(testElectionId, result.electionId)
            assertEquals(closeState, result.state)
            assertEquals(testTime, result.updatedAt)
        }
    }

    @Nested
    inner class VoteTest {

        val testUserId = "testUser"
        val testItem = "testItem"

        @BeforeEach
        fun setUpEnqueueResponse() {

            val response = VoteResponseDto(
                electionId = testElectionId,
                userId = testUserId,
                item = testItem,
                votedAt = testTime
            )

            mockBackServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(objectMapper.writeValueAsString(response))
                    .addHeader("Content-Type", "application/json")
            )
        }

        @Test
        fun shouldSendVoteRequest() = runTest {

            val voteRequest = VoteRequestDto(
                userId = testUserId,
                item = testItem
            )

            votingService.vote(testElectionId, voteRequest)

            val recordedRequest = mockBackServer.takeRequest()
            assertEquals("/elections/$testElectionId/votes", recordedRequest.path)
            assertEquals("POST", recordedRequest.method)
        }

        @Test
        fun shouldRequestBodyHaveUserIdAndItem() = runTest {

            val voteRequest = VoteRequestDto(userId = testUserId, item = testItem)

            votingService.vote(testElectionId, voteRequest)

            val recordedRequest = mockBackServer.takeRequest()
            val requestBody = objectMapper.readValue<VoteRequestDto>(recordedRequest.body.readUtf8())

            assertEquals(testUserId, requestBody.userId)
            assertEquals(testItem, requestBody.item)
        }

        @Test
        fun shouldReturnResponse() = runTest {

            val request = VoteRequestDto(
                userId = testUserId,
                item = testItem
            )

            val response = votingService.vote(testElectionId, request)

            assertEquals(testElectionId, response.electionId)
            assertEquals(testUserId, response.userId)
            assertEquals(testItem, response.item)
            assertEquals(testTime, response.votedAt)
        }
    }

    @Nested
    inner class GetStatisticTest {

        val counts: List<VoteStatisticCount> = listOf(
            VoteStatisticCount(item = "item1", voteCount = 10),
            VoteStatisticCount(item = "item2", voteCount = 1),
            VoteStatisticCount(item = "item3", voteCount = 8),
            VoteStatisticCount(item = "item4", voteCount = 3),
        )

        @BeforeEach
        fun enqueueResponse() {

            val response = VoteStatistic(
                voteCounts = counts.sortedByDescending { it.voteCount },
                aggregatedAt = testTime,
            )

            mockBackServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(objectMapper.writeValueAsString(response))
                    .addHeader("Content-Type", "application/json")
            )
        }

        @Test
        fun shouldSendVoteRequest() = runTest {

            votingService.getStatistic(testElectionId)

            val recordedRequest = mockBackServer.takeRequest()

            assertEquals("/elections/$testElectionId/votes/statistic", recordedRequest.path)
            assertEquals("GET", recordedRequest.method)
        }

        @Test
        fun shouldReceiveResponseDtoFromServer() = runTest {

            val result = votingService.getStatistic(testElectionId)

            assertEquals(testTime, result.aggregatedAt)

            result.voteCounts.zip(counts.sortedByDescending { it.voteCount }).forEach { (actual, expected) -> assertEquals(expected, actual) }
        }
    }
}