package com.yareach.voting_tictactoe_system.intergration.player

import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.player.grpc_service.PlayerGrpcService
import com.yareach.voting_tictactoe_system.player.model.Player
import com.yareach.voting_tictactoe_system.player.proto.AddNewPlayer
import com.yareach.voting_tictactoe_system.player.proto.GroupId
import com.yareach.voting_tictactoe_system.player.proto.InitRecruiting
import com.yareach.voting_tictactoe_system.player.proto.PlayerServiceGrpcKt
import com.yareach.voting_tictactoe_system.player.proto.RecruitRequestMessage
import com.yareach.voting_tictactoe_system.player.proto.RecruitStreamMessage
import com.yareach.voting_tictactoe_system.player.repository.PlayerR2dbcRepository
import com.yareach.voting_tictactoe_system.player.repository.PlayerRepository
import com.yareach.voting_tictactoe_system.player.service.PlayerService
import io.grpc.ManagedChannel
import io.grpc.Server
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SpringBootTest
class PlayerGrpcServiceTest {

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    private val serverName = InProcessServerBuilder.generateName()

    private val testDispatcher = StandardTestDispatcher()

    @Autowired
    private lateinit var playerService: PlayerService
    private lateinit var playerGrpcService: PlayerGrpcService

    private lateinit var server: Server
    private lateinit var channel: ManagedChannel

    private lateinit var stub: PlayerServiceGrpcKt.PlayerServiceCoroutineStub

    @BeforeEach
    fun setup() {

        playerGrpcService = PlayerGrpcService(playerService, testDispatcher)

        server = InProcessServerBuilder.forName(serverName)
            .executor(testDispatcher.asExecutor())
            .addService(playerGrpcService)
            .build()
        server.start()

        channel = InProcessChannelBuilder
            .forName(serverName)
            .executor(testDispatcher.asExecutor())
            .build()

        stub = PlayerServiceGrpcKt.PlayerServiceCoroutineStub(channel)
    }

    @AfterEach
    fun tearDown() {
        channel.shutdownNow()
        server.shutdownNow()
    }

    @AfterEach
    suspend fun cleanup(
        @Autowired playerR2dbcRepository: PlayerR2dbcRepository,
    ) {
        playerR2dbcRepository.deleteAll()
    }

    @Nested
    @DisplayName("recruit player")
    inner class RecruitPlayerTest {

        fun generateRecruitInitMessage(groupId: String): RecruitRequestMessage =
            RecruitRequestMessage.newBuilder()
                .setInit(
                    InitRecruiting.newBuilder()
                        .setGroupId(groupId)
                        .build()
                ).build()

        fun generateAddNewPlayerMessage(userId: String): RecruitRequestMessage =
            RecruitRequestMessage.newBuilder()
                .setAddUser(
                    AddNewPlayer.newBuilder()
                        .setUserId(userId)
                        .build()
                ).build()

        @Test
        @DisplayName("[Success case] ユーザー募集")
        fun recruitPlayerSuccess() = runTest(testDispatcher) {

            val numberOfPlayers = 10

            val inputFlow = flow {
                emit(generateRecruitInitMessage("group1"))

                repeat(numberOfPlayers) { index ->
                    delay(500.milliseconds)
                    emit(generateAddNewPlayerMessage("user-$index"))
                }
            }

            val outputStream = stub.recruitPlayer(inputFlow)

            launch {
                val outputMessages = outputStream.toList()

                assertEquals(numberOfPlayers + 1, outputMessages.size)

                repeat(numberOfPlayers) { index ->
                    val message = outputMessages.getOrNull(index)

                    assertNotNull(message)
                    assertEquals(RecruitStreamMessage.PayloadCase.ACCEPTED, message.payloadCase)
                    assertEquals("user-$index", message.accepted.userId)
                }

                val lastMessage = outputMessages.lastOrNull()
                assertNotNull(lastMessage)
                assertEquals(RecruitStreamMessage.PayloadCase.COMPLETED, lastMessage.payloadCase)

                assertEquals(numberOfPlayers / 2, lastMessage.completed.playersByTeam.userIdsInTeamXList.size)
                assertEquals(numberOfPlayers / 2, lastMessage.completed.playersByTeam.userIdsInTeamOList.size)

                val allPlayers =
                    lastMessage.completed.playersByTeam.let { it.userIdsInTeamXList + it.userIdsInTeamOList }
                assertEquals(numberOfPlayers, allPlayers.size)
                assertEquals(List(numberOfPlayers) { "user-$it" }, allPlayers.sorted())
            }
        }

        @Test
        @DisplayName("[Fail case] タイムアウト発生")
        fun whenOccursTimeout() = runTest(testDispatcher) {

            val numberOfPlayers = 10

            val inputFlow = flow {
                emit(generateRecruitInitMessage("group1"))
                repeat(numberOfPlayers) { index ->
                    delay(10.seconds)
                    emit(generateAddNewPlayerMessage("user-$index"))
                }
            }

            val outputStream = stub.recruitPlayer(inputFlow)

            launch {
                val exception: StatusException = assertThrows { outputStream.collect() }

                assertEquals(Status.DEADLINE_EXCEEDED.code, exception.status.code)
            }
        }

        @Test
        @DisplayName("[Fail case] Message Sequence Error - Initデータ未送信")
        fun missingInitMessage() = runTest(testDispatcher) {

            val numberOfPlayers = 10

            val inputFlow = flow {
                repeat(numberOfPlayers) { index ->
                    delay(10.seconds)
                    emit(generateAddNewPlayerMessage("user-$index"))
                }
            }

            val outputStream = stub.recruitPlayer(inputFlow)

            launch {
                val exception: StatusException = assertThrows { outputStream.collect() }

                assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
            }
        }

        @Test
        @DisplayName("[Fail case] Message Sequence Error - Initメッセージ重複送信")
        fun initMessageIsDuplicated() = runTest(testDispatcher) {

            val numberOfPlayers = 10

            val inputFlow = flow {

                //重複送信
                emit(generateRecruitInitMessage("group1"))
                emit(generateRecruitInitMessage("group1"))

                repeat(numberOfPlayers) { index ->
                    delay(10.seconds)
                    emit(generateAddNewPlayerMessage("user-$index"))
                }
            }

            val outputStream = stub.recruitPlayer(inputFlow)

            launch {
                val exception: StatusException = assertThrows { outputStream.collect() }

                assertEquals(Status.INVALID_ARGUMENT.code, exception.status.code)
            }
        }
    }

    @Nested
    @DisplayName("getUserIdsInGroup")
    inner class GetUserIdsInGroupTest {

        @Test
        @DisplayName("[success case] groupIdを指定して特定のグループのプレイヤーのユーザーＩＤを取得")
        fun getUserIdSuccessfully() = runTest(testDispatcher) {
            List(6) {
                Player.new("group1", "user-${it}", if (it % 2 == 0) Team.X else Team.O)
            }.also { playerRepository.saveAll(it).collect() }

            val groupId = GroupId.newBuilder().setGroupId("group1").build()

            val result = stub.getUserIdsInGroup(groupId)
            val userIdsInTeamO = result.userIdsInTeamOList.toList()
            val userIdsInTeamX = result.userIdsInTeamXList.toList()

            assertEquals(listOf("user-0", "user-2", "user-4"), userIdsInTeamX.sorted())
            assertEquals(listOf("user-1", "user-3", "user-5"), userIdsInTeamO.sorted())
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @Test
        @DisplayName("[success case] 片方のチームにプレイヤーが存在しない場合、そのチームは空のリストで返される")
        fun returnsEmptyListWhenOneTeamHasNoPlayers() = runTest(testDispatcher) {
            List(6) {
                Player.new("group1", "user-${it}", Team.X)
            }.also { playerRepository.saveAll(it).collect() }

            val groupId = GroupId.newBuilder().setGroupId("group1").build()

            val result = stub.getUserIdsInGroup(groupId)
            val userIdsInTeamO = result.userIdsInTeamOList.toList()
            val userIdsInTeamX = result.userIdsInTeamXList.toList()

            assertEquals(6, userIdsInTeamX.size)
            assertEquals(0, userIdsInTeamO.size)
        }

        @Test
        @DisplayName("[fail case] 指定してグループにプレイヤーがない場合、エラー発生")
        fun throwExceptionWhenPlayerInGroupIsNotExist() = runTest(testDispatcher) {
            val groupId = GroupId.newBuilder().setGroupId("group1").build()

            val exception: StatusException = assertThrows { stub.getUserIdsInGroup(groupId) }
            assertEquals(Status.NOT_FOUND.code, exception.status.code)
        }
    }

    @Nested
    @DisplayName("deleteAllUserInGroup")
    inner class DeleteAllUserInGroupTest {

        @Test
        @DisplayName("[success case] groupIdで特定のグループのプレイヤーのデータを削除")
        fun deleteAllUsersSuccessfully() = runTest(testDispatcher) {
            List(6) {
                Player.new("group1", "user-${it}", if (it % 2 == 0) Team.X else Team.O)
            }.also { playerRepository.saveAll(it).collect() }

            val groupId = GroupId.newBuilder().setGroupId("group1").build()

            val result = stub.deleteAllUserInGroupId(groupId)

            assertEquals("group1", result.groupId)
            assertFalse(playerRepository.existsByGroupId("group1"))
        }

        @Test
        @DisplayName("[fail case] 指定してグループにプレイヤーがない場合、エラー発生")
        fun throwExceptionWhenPlayerInGroupIsNotExist() = runTest(testDispatcher) {
            val groupId = GroupId.newBuilder().setGroupId("group1").build()

            val exception: StatusException = assertThrows { stub.getUserIdsInGroup(groupId) }
            assertEquals(Status.NOT_FOUND.code, exception.status.code)
        }
    }
}