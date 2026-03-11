package com.yareach.voting_tictactoe_system.unit.game_group_info

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.game_group_info.enum.GameState
import com.yareach.voting_tictactoe_system.game_group_info.model.GameGroupInfo
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoRepository
import com.yareach.voting_tictactoe_system.game_group_info.service.GameGroupInfoService
import com.yareach.voting_tictactoe_system.game_group_info.service.GameGroupInfoServiceImpl
import com.yareach.voting_tictactoe_system.tictactoe.enum.GameType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameGroupInfoServiceImplTest {
    val gameGroupInfoRepository = mockk<GameGroupInfoRepository>()

    val gameGroupInfoService: GameGroupInfoService = GameGroupInfoServiceImpl(gameGroupInfoRepository)

    @Nested
    @DisplayName("createNewGameGroupInfo")
    inner class CreateNewGameGroupInfoTests {

        @Test
        @DisplayName("[Success case] GameGroupInfoを生成して保存")
        fun shouldNewGameGroupInfoSaved() = runTest {

            val groupId = "group1"
            val gameType = GameType.NORMAL

            val slot = slot<GameGroupInfo>()

            coEvery { gameGroupInfoRepository.existsByGroupId(groupId) } returns false

            coEvery {
                gameGroupInfoRepository.save(capture(slot))
            } answers {
                GameGroupInfo(
                    id = 1,
                    groupId = slot.captured.groupId,
                    gameType = slot.captured.gameType,
                    state = slot.captured.state,
                    lastUpdated = slot.captured.lastUpdated,
                )
            }

            val beforeSave = LocalDateTime.now()

            val result = gameGroupInfoService.createNewGameGroupInfo(groupId, gameType)

            val afterSave = LocalDateTime.now()

            coVerify(exactly = 1) { gameGroupInfoRepository.existsByGroupId(groupId) }
            coVerify(exactly = 1) { gameGroupInfoRepository.save(slot.captured) }

            assertTrue { beforeSave.isBefore(result.lastUpdated) }
            assertTrue { afterSave.isAfter(result.lastUpdated) }

            assertNull(slot.captured.id)
            assertEquals(groupId, slot.captured.groupId)
            assertEquals(gameType, slot.captured.gameType)
            assertEquals(GameState.GENERATED, slot.captured.state)

            assertNotNull(result.id)
            assertEquals(groupId, result.groupId)
            assertEquals(gameType, result.gameType)
            assertEquals(GameState.GENERATED, result.state)
        }

        @Test
        @DisplayName("[Fail case] GroupIdに対するGameGroupInfoがもう存在する場合、エラー発生")
        fun shouldFailGameGroupInfoSavedWhenGameGroupInfoForGroupIdIsExists() = runTest {
            val groupId = "group1"
            coEvery { gameGroupInfoRepository.existsByGroupId(groupId) } returns true

            val exception = assertThrows<ApiException> { gameGroupInfoService.createNewGameGroupInfo(groupId, GameType.NORMAL) }

            assertEquals(ErrorCode.GROUP_ID_DUPLICATE, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("checkGameGroupIsExists")
    inner class CheckGameGroupIsExistsTests {

        @Test
        @DisplayName("[Success case] GroupIdが一致するデータが存在する場合、trueを返す")
        fun whenDataExistsShouldReturnTrue() = runTest {
            val groupId = "group1"

            coEvery { gameGroupInfoRepository.existsByGroupId(groupId) } returns true

            val result = gameGroupInfoService.checkGameGroupIsExists(groupId)

            assertTrue(result)
        }

        @Test
        @DisplayName("[Success case] GroupIdが一致するデータが存在しない場合、trueを返す")
        fun whenDataIsNotExistShouldReturnFalse() = runTest {
            coEvery { gameGroupInfoRepository.existsByGroupId(any()) } returns false

            val result = gameGroupInfoService.checkGameGroupIsExists("unexist-group-id")

            assertFalse(result)
        }
    }

    @Nested
    @DisplayName("getGameGroupInfo")
    inner class GetGameGroupInfoTests {

        @Test
        @DisplayName("[Success case] GroupIdが一致するデータが存在する場合、そのデータを返す")
        fun shouldGetGameGroupInfo() = runTest {
            val groupId = "group1"

            coEvery { gameGroupInfoRepository.findByGroupId(groupId) } answers {
                GameGroupInfo(
                    id = 0,
                    groupId = groupId,
                    gameType = GameType.INFINITY,
                    state = GameState.BEFORE_START,
                    lastUpdated = LocalDateTime.now(),
                )
            }

            val result = gameGroupInfoService.getGameGroupInfo(groupId)

            assertEquals(groupId, result.groupId)
        }

        @Test
        @DisplayName("[Success case] GroupIdが一致するデータが存在しない場合、エラーが発生")
        fun whenMatchingDataIsNotExistsShouldGetGameGroupInfo() = runTest {
            val groupId = "group1"

            coEvery { gameGroupInfoRepository.findByGroupId(any()) } returns null

            val exception = assertThrows<ApiException> { gameGroupInfoService.getGameGroupInfo(groupId) }

            assertEquals(ErrorCode.GAME_INFO_NOTFOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("deleteGameGroupInfo")
    inner class DeleteGameGroupInfoTests {

        @Test
        @DisplayName("[Success case] GroupIdが一致するデータを削除する")
        fun shouldDeleteGameGroupInfo() = runTest {
            val groupId = "group1"

            coEvery { gameGroupInfoRepository.existsByGroupId(groupId) } returns true
            coEvery { gameGroupInfoRepository.deleteByGroupId(groupId) } returns 1

            gameGroupInfoService.deleteGameGroupInfo(groupId)

            coVerify(exactly = 1) { gameGroupInfoRepository.existsByGroupId(groupId) }
            coVerify(exactly = 1) { gameGroupInfoRepository.deleteByGroupId(groupId) }
        }

        @Test
        @DisplayName("[Fail case] GroupIdが一致するデータがない場合、エラーが発生")
        fun whenMatchingDataNotExistsShouldFail() = runTest {
            val groupId = "group1"

            coEvery { gameGroupInfoRepository.existsByGroupId(groupId) } returns false

            val exception = assertThrows<ApiException>{ gameGroupInfoService.deleteGameGroupInfo(groupId) }

            coVerify(exactly = 1) { gameGroupInfoRepository.existsByGroupId(groupId) }
            coVerify(exactly = 0) { gameGroupInfoRepository.deleteByGroupId(groupId) }

            assertEquals(ErrorCode.GAME_INFO_NOTFOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("updateGameState")
    inner class UpdateGameStateTests {

        @Test
        @DisplayName("[Success case] ゲームの状態変更")
        fun shouldUpdateGameState() = runTest {

            val groupId = "group1"
            val gameGroupInfo = GameGroupInfo(
                id = 0,
                groupId = groupId,
                gameType = GameType.NORMAL,
                state = GameState.GENERATED,
                lastUpdated = LocalDateTime.now(),
            )

            val slot = slot<GameGroupInfo>()
            lateinit var timeBeforeUpdate: LocalDateTime

            coEvery { gameGroupInfoRepository.findByGroupId(groupId) } returns gameGroupInfo
            coEvery { gameGroupInfoRepository.save(capture(slot)) } answers {
                timeBeforeUpdate = slot.captured.lastUpdated
                slot.captured.apply { setLastUpdatedToCurrentTime() }
            }

            val result = gameGroupInfoService.updateGameState(groupId, GameState.RECRUITING)

            coVerify(exactly = 1) { gameGroupInfoRepository.findByGroupId(groupId) }
            coVerify(exactly = 1) { gameGroupInfoRepository.save(slot.captured) }

            assertEquals(groupId, result.groupId)
            assertEquals(GameState.RECRUITING, result.state)
            println(result.lastUpdated)
            println(slot.captured.lastUpdated)
            assertTrue { result.lastUpdated.isAfter(timeBeforeUpdate) }
        }

        @Test
        @DisplayName("[Success case] データが存在しない場合、エラーが発生")
        fun whenMatchingDataNotExistsShouldFail() = runTest {
            val groupId = "group1"

            coEvery { gameGroupInfoRepository.findByGroupId(groupId) } returns null

            val exception = assertThrows<ApiException>{ gameGroupInfoService.updateGameState(groupId, GameState.RECRUITING) }

            assertEquals(ErrorCode.GAME_INFO_NOTFOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("updateLastUpdated")
    inner class UpdateLastUpdatedTests {

        @Test
        @DisplayName("[Success case] 最終更新時刻をアップデート")
        fun shouldUpdateLastUpdated() = runTest {
            val groupId = "group1"

            val gameGroupInfo = GameGroupInfo(
                id = 0,
                groupId = groupId,
                gameType = GameType.NORMAL,
                state = GameState.GENERATED,
                lastUpdated = LocalDateTime.now()
            )

            lateinit var timeBeforeUpdate: LocalDateTime

            coEvery { gameGroupInfoRepository.findByGroupId(groupId) } returns gameGroupInfo

            coEvery { gameGroupInfoRepository.save(gameGroupInfo) } answers {
                timeBeforeUpdate = gameGroupInfo.lastUpdated
                gameGroupInfo.apply { setLastUpdatedToCurrentTime() }
            }

            val result = gameGroupInfoService.updateLastUpdated(groupId)

            assertEquals(gameGroupInfo.id, result.id)
            assertEquals(gameGroupInfo.groupId, result.groupId)
            assertEquals(gameGroupInfo.gameType, result.gameType)
            assertEquals(gameGroupInfo.state, result.state)
            assertTrue { result.lastUpdated.isAfter(timeBeforeUpdate) }
        }

        @Test
        @DisplayName("[Fail case] GroupIdが一致するデータが存在しない場合、エラー発生")
        fun whenMatchingDataNotExistsShouldFail() = runTest {
            val groupId = "group1"

            coEvery { gameGroupInfoRepository.findByGroupId(groupId) } returns null

            val exception = assertThrows<ApiException>{ gameGroupInfoService.updateLastUpdated(groupId) }

            assertEquals(ErrorCode.GAME_INFO_NOTFOUND, exception.errorCode)
        }
    }
}