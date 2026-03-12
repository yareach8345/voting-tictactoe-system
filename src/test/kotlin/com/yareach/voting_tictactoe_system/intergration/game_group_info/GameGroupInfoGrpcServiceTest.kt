package com.yareach.voting_tictactoe_system.intergration.game_group_info

import com.yareach.voting_tictactoe_system.common.enum.GameType
import com.yareach.voting_tictactoe_system.common.extension.toGameTypeForProto
import com.yareach.voting_tictactoe_system.game_group_info.enum.GameState
import com.yareach.voting_tictactoe_system.game_group_info.extension.toDomain
import com.yareach.voting_tictactoe_system.game_group_info.grpc_service.GameGroupInfoGrpcService
import com.yareach.voting_tictactoe_system.game_group_info.proto.AddGameInfoMessage
import com.yareach.voting_tictactoe_system.game_group_info.proto.GameGroupInfoServiceGrpcKt
import com.yareach.voting_tictactoe_system.game_group_info.proto.GroupIdMessage
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoR2dbcRepository
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoRepository
import com.yareach.voting_tictactoe_system.game_group_info.service.GameGroupInfoService
import io.grpc.ManagedChannel
import io.grpc.Server
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
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
import kotlin.test.assertTrue

@SpringBootTest
class GameGroupInfoGrpcServiceTest {

    @Autowired
    private lateinit var gameGroupInfoRepository: GameGroupInfoRepository

    @Autowired
    private lateinit var gameGroupInfoService: GameGroupInfoService

    private lateinit var gameGroupInfoGrpcService: GameGroupInfoGrpcService

    private val serverName = InProcessServerBuilder.generateName()

    private lateinit var server: Server
    private lateinit var channel: ManagedChannel

    private lateinit var stub: GameGroupInfoServiceGrpcKt.GameGroupInfoServiceCoroutineStub

    @BeforeEach
    fun setup() {

        gameGroupInfoGrpcService = GameGroupInfoGrpcService(gameGroupInfoService)

        server = InProcessServerBuilder.forName(serverName)
            .addService(gameGroupInfoGrpcService)
            .build()
        server.start()

        channel = InProcessChannelBuilder
            .forName(serverName)
            .build()

        stub = GameGroupInfoServiceGrpcKt.GameGroupInfoServiceCoroutineStub(channel)
    }

    @AfterEach
    suspend fun tearDown(
        @Autowired gameGroupInfoR2dbcRepository: GameGroupInfoR2dbcRepository
    ) {
        channel.shutdownNow()
        server.shutdownNow()
        gameGroupInfoR2dbcRepository.deleteAll()
    }

    @Nested
    @DisplayName("addGameInfo")
    inner class AddGameInfoTest {

        @Test
        @DisplayName("[Success case] 新しいデータを保存")
        fun shouldNewGameGroupInfoSaved() = runTest {
            val groupId = "group1"
            val gameType = GameType.NORMAL

            val addGameInfoMessage = AddGameInfoMessage.newBuilder()
                .setGroupId(groupId)
                .setGameType(gameType.toGameTypeForProto())
                .build()

            val result = stub.addGameInfo(addGameInfoMessage)

            assertEquals(groupId, result.groupId)

            val findResult = gameGroupInfoRepository.findByGroupId(groupId)

            assertNotNull(findResult)
            assertEquals(groupId, findResult.groupId)
            assertEquals(gameType, findResult.gameType)
            assertEquals(GameState.GENERATED, findResult.state)
        }

        @Test
        @DisplayName("[Fail case] GroupIdが一致するデータがもう存在する場合、エラーが発生")
        fun whenGameGroupInfoForGroupIdIsAlreadyExistShouldFail() = runTest {
            val groupId = "group1"
            val gameType = GameType.NORMAL

            gameGroupInfoService.createNewGameGroupInfo(groupId, gameType)

            val message = AddGameInfoMessage.newBuilder()
                .setGroupId(groupId)
                .setGameType(gameType.toGameTypeForProto())
                .build()

            val exception = assertThrows<StatusRuntimeException> { gameGroupInfoGrpcService.addGameInfo(message) }

            assertEquals(Status.ALREADY_EXISTS.code, exception.status.code)
        }
    }

    @Nested
    @DisplayName("deleteGameInfo")
    inner class DeleteGameInfoTest {

        @Test
        @DisplayName("[Success case] データベースからデータを削除")
        fun shouldDeleteGameInfoSaved() = runTest {
            val groupId = "group1"
            val gameType = GameType.NORMAL

            gameGroupInfoService.createNewGameGroupInfo(groupId, gameType)

            val message = GroupIdMessage.newBuilder()
                .setGroupId(groupId)
                .build()

            val result = gameGroupInfoGrpcService.deleteGameInfo(message)

            assertEquals(groupId, result.groupId)

            val findResult = gameGroupInfoRepository.existsByGroupId(groupId)

            assertFalse(findResult)
        }

        @Test
        @DisplayName("[Fail case] データが存在しない場合、エラー発生")
        fun whenMatchingDataNotExistShouldFail() = runTest {

            val message = GroupIdMessage.newBuilder()
                .setGroupId("unexist-group-id")
                .build()

            val exception = assertThrows<StatusRuntimeException> { gameGroupInfoGrpcService.deleteGameInfo(message) }

            assertEquals(Status.NOT_FOUND.code, exception.status.code)
        }
    }

    @Nested
    @DisplayName("getGameState")
    inner class GetGameStateTest {

        @Test
        @DisplayName("[Success case] GameGroupInfoの状態を取得")
        fun shouldGetGameStateSaved() = runTest {

            val groupId = "group1"
            val gameType = GameType.NORMAL
            val gameState = GameState.RECRUITING

            gameGroupInfoService.createNewGameGroupInfo(groupId, gameType)
            gameGroupInfoService.updateGameState(groupId, gameState)

            val message = GroupIdMessage.newBuilder()
                .setGroupId(groupId)
                .build()

            val result = gameGroupInfoGrpcService.getGameState(message)

            assertEquals(groupId, result.groupId)
            assertEquals(gameState, result.state.toDomain())
        }

        @Test
        @DisplayName("[Fail case] 該当するデータが存在しない場合、エラー発生")
        fun whenMatchingDataNotExistShouldFail() = runTest {
            val message = GroupIdMessage.newBuilder()
                .setGroupId("unexist-group-id")
                .build()

            val exception = assertThrows<StatusRuntimeException> { gameGroupInfoGrpcService.getGameState(message) }

            assertEquals(Status.NOT_FOUND.code, exception.status.code)
        }
    }

    @Nested
    @DisplayName("isExistsByGroupId")
    inner class IsExistsByGroupIdTest {

        @Test
        @DisplayName("[Success case] 該当するデータが存在すれば、trueをもらう")
        fun shouldIsExistsByGroupIdTrue() = runTest {

            val groupId = "group1"
            val gameType = GameType.NORMAL

            gameGroupInfoService.createNewGameGroupInfo(groupId, gameType)

            val message = GroupIdMessage.newBuilder()
                .setGroupId(groupId)
                .build()

            val result = gameGroupInfoGrpcService.isExistsByGroupId(message)

            assertEquals(groupId, result.groupId)
            assertTrue(result.isExist)
        }

        @Test
        @DisplayName("[Fail case] 該当するデータがない場合、falseをもらう")
        fun whenMatchingDataNotExistShouldFail() = runTest {
            val groupId = "unexist-group-id"

            val message = GroupIdMessage.newBuilder()
                .setGroupId(groupId)
                .build()

            val result = gameGroupInfoGrpcService.isExistsByGroupId(message)

            assertEquals(groupId, result.groupId)
            assertFalse(result.isExist)
        }
    }
}