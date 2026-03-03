package com.yareach.voting_tictactoe_system.intergration

import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.player.model.Player
import com.yareach.voting_tictactoe_system.player.proto.GroupId
import com.yareach.voting_tictactoe_system.player.proto.PlayerServiceGrpcKt
import com.yareach.voting_tictactoe_system.player.repository.PlayerR2dbcRepository
import com.yareach.voting_tictactoe_system.player.repository.PlayerRepository
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusException
import kotlinx.coroutines.flow.collect
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

@SpringBootTest
class PlayerGrpcServiceTest {
    val port = 9090

    private lateinit var channel: ManagedChannel
    private lateinit var stub: PlayerServiceGrpcKt.PlayerServiceCoroutineStub

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @Autowired
    private lateinit var playerR2dbcRepository: PlayerR2dbcRepository

    @BeforeEach
    fun setup() {
        channel = ManagedChannelBuilder.forAddress("localhost", port)
            .usePlaintext()
            .build()

        stub = PlayerServiceGrpcKt.PlayerServiceCoroutineStub(channel)
    }

    @AfterEach
    suspend fun cleanup() {
        playerR2dbcRepository.deleteAll()
    }

    @Nested
    @DisplayName("getUserIdsInGroup")
    inner class GetUserIdsInGroupTest {

        @Test
        @DisplayName("[success case] groupIdを指定して特定のグループのプレイヤーのユーザーＩＤを取得")
        fun getUserIdSuccessfully() = runTest {
            List(6) {
                Player.new("group1", "user-${it}", if(it % 2 == 0) Team.X else Team.O)
            }.also { playerRepository.saveAll(it).collect() }

            val groupId = GroupId.newBuilder().setGroupId("group1").build()

            val result = stub.getUserIdsInGroup(groupId)
            val userIdsInTeamO = result.userIdsInTeamOList.toList()
            val userIdsInTeamX = result.userIdsInTeamXList.toList()

            assertEquals(listOf("user-0", "user-2", "user-4"), userIdsInTeamX.sorted())
            assertEquals(listOf("user-1", "user-3", "user-5"), userIdsInTeamO.sorted())
        }

        @Test
        @DisplayName("[success case] 片方のチームにプレイヤーが存在しない場合、そのチームは空のリストで返される")
        fun returnsEmptyListWhenOneTeamHasNoPlayers() = runTest {
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
        fun throwExceptionWhenPlayerInGroupIsNotExist() = runTest {
            val groupId = GroupId.newBuilder().setGroupId("group1").build()

            val exception: StatusException = assertThrows{ stub.getUserIdsInGroup(groupId) }
            assertEquals(Status.NOT_FOUND.code, exception.status.code)
        }
    }

    @Nested
    @DisplayName("deleteAllUserInGroup")
    inner class DeleteAllUserInGroupTest {

        @Test
        @DisplayName("[success case] groupIdで特定のグループのプレイヤーのデータを削除")
        fun deleteAllUsersSuccessfully() = runTest {
            List(6) {
                Player.new("group1", "user-${it}", if(it % 2 == 0) Team.X else Team.O)
            }.also { playerRepository.saveAll(it).collect() }

            val groupId = GroupId.newBuilder().setGroupId("group1").build()

            val result = stub.deleteAllUserInGroupId(groupId)

            assertEquals("group1", result.groupId)
            assertFalse(playerRepository.existsByGroupId("group1"))
        }

        @Test
        @DisplayName("[fail case] 指定してグループにプレイヤーがない場合、エラー発生")
        fun throwExceptionWhenPlayerInGroupIsNotExist() = runTest {
            val groupId = GroupId.newBuilder().setGroupId("group1").build()

            val exception: StatusException = assertThrows{ stub.getUserIdsInGroup(groupId) }
            assertEquals(Status.NOT_FOUND.code, exception.status.code)
        }
    }
}