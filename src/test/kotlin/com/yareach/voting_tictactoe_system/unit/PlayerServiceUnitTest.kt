package com.yareach.voting_tictactoe_system.unit

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.player.model.Player
import com.yareach.voting_tictactoe_system.player.repository.PlayerRepository
import com.yareach.voting_tictactoe_system.player.service.PlayerServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("Unused")
class PlayerServiceUnitTest {
    val playerRepositoryMock = mockk<PlayerRepository>()
    val playerService = PlayerServiceImpl(playerRepositoryMock)

    @Nested
    @DisplayName("プレイヤー募集結果処理")
    inner class FinalizeRecruitmentTest {

        val saveAllSlot = slot<List<Player>>()

        fun isMatchPlayerAndUserId(players: List<Player>, userIds: List<String>): Boolean {
            return players
                .sortedBy { it.userId }
                .zip(userIds.sorted())
                .all{ (player, userId) -> player.userId == userId }
        }

        @BeforeEach
        fun setUpRepositoryMock() {
            coEvery { playerRepositoryMock.saveAll(capture(saveAllSlot)) } answers { saveAllSlot.captured.asFlow() }
        }

        @Test
        @DisplayName("[success case] チームを分けって結果を戻す")
        fun divideTeamSuccessfully() = runTest {
            val userIds = List(6) { "testUser$it" }

            val groupId = "testGroup"

            val result = playerService.finalizeRecruitment(groupId, userIds.toSet())

            coVerify { val _unused = playerRepositoryMock.saveAll(any()) }

            assertEquals(3, result.userIdsInTeamO.size)
            assertEquals(3, result.userIdsInTeamX.size)

            assertTrue { isMatchPlayerAndUserId(saveAllSlot.captured, userIds) }
        }

        @Test
        @DisplayName("[success case] ユーザーの数が奇数である場合、余った一人はランダムなチームに配置")
        fun divideTeamWhenNumberOfPlayersIsOdd() = runTest {
            val userIds = List(7) { "testUser$it" }

            val groupId = "testGroup"

            val result = playerService.finalizeRecruitment(groupId, userIds.toSet())

            coVerify { val _unused = playerRepositoryMock.saveAll(any()) }

            assertEquals(1, abs(result.userIdsInTeamO.size - result.userIdsInTeamX.size))

            assertTrue { isMatchPlayerAndUserId(saveAllSlot.captured, userIds) }
        }

        @Test
        @DisplayName("[fail case] 人数が2未満である場合、エラー発生")
        fun divideTeamWhenNumberOfPlayersUnder2() = runTest {
            val userIds = List(1) { "testUser$it" }

            val groupId = "testGroup"

            val exception = assertThrows<ApiException> {
                playerService.finalizeRecruitment(groupId, userIds.toSet())
            }

            assertEquals(ErrorCode.NOT_ENOUGH_PLAYERS, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("特定のグループのプレイヤーのユーザーＩＤをチーム別に取得")
    inner class GetPlayersByGroupTest {

        @Test
        @DisplayName("[Success case] プレイヤーのユーザーＩＤを修得")
        fun getPlayersByGroupSuccessfully() = runTest {
            val groupId = "testGroup"

            val players = List(6) { Player.new(groupId, "user-$it", if(it % 2 == 0) Team.X else Team.O ) }

            coEvery { playerRepositoryMock.existsByGroupId(groupId) }.returns(true)

            coEvery { playerRepositoryMock.findByGroupId(groupId) } returns players.asFlow()

            val result = playerService.getPlayersByGroupId(groupId)

            coVerify { playerRepositoryMock.existsByGroupId(groupId) }

            coVerify { val _unused = playerRepositoryMock.findByGroupId(groupId) }

            assertEquals(listOf("user-0", "user-2", "user-4"), result.userIdsInTeamX.toList().sorted())
            assertEquals(listOf("user-1", "user-3", "user-5"), result.userIdsInTeamO.toList().sorted())
        }

        @Test
        @DisplayName("[Success case] プレイヤーが存在しないチームは空リストで返す")
        fun whenInATeamThereIsNotUsers() = runTest {
            val groupId = "testGroup"

            val players = List(6) { Player.new(groupId, "user-$it", Team.X) }

            coEvery { playerRepositoryMock.existsByGroupId(groupId) }.returns(true)

            coEvery { playerRepositoryMock.findByGroupId(groupId) } returns players.asFlow()

            val result = playerService.getPlayersByGroupId(groupId)

            coVerify { playerRepositoryMock.existsByGroupId(groupId) }

            coVerify { val _unused = playerRepositoryMock.findByGroupId(groupId) }

            assertEquals(6, result.userIdsInTeamX.size)
            assertEquals(0, result.userIdsInTeamO.size)
        }

        @Test
        @DisplayName("[Fail case] 指定したGroupIdのプレイヤーが存在しない場合、エラー発生")
        fun whereNobodyExistInGroup() = runTest {
            val groupId = "testGroup"

            coEvery { playerRepositoryMock.existsByGroupId(groupId) }.returns(false)

            val exception = assertThrows<ApiException> {
                playerService.getPlayersByGroupId(groupId)
            }

            coVerify { playerRepositoryMock.existsByGroupId(groupId) }

            coVerify(exactly = 0) { val _unused = playerRepositoryMock.findByGroupId(groupId) }

            assertEquals(ErrorCode.GROUP_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("GroupIdでプレイヤーのデータ削除")
    inner class DeleteAllPlayersByGroupIdTest {

        @Test
        @DisplayName("[success case] グループの所属のプレイヤーを削除")
        fun deleteAllPlayerSuccessfully() = runTest {
            val groupId = "testGroup"

            coEvery { playerRepositoryMock.existsByGroupId(groupId) } returns true

            coEvery { playerRepositoryMock.deleteByGroupId(groupId) } returns 6

            val result = playerService.deleteAllPlayersByGroupId(groupId)

            assertEquals(6, result)
        }

        @Test
        @DisplayName("[fail case] 指定したＩＤのグループが存在しない場合、エラー発生")
        fun whenMatchingGroupIsNotExists() = runTest {
            val groupId = "testGroup"

            coEvery { playerRepositoryMock.existsByGroupId(groupId) } returns false

            val exception = assertThrows<ApiException>{ playerService.deleteAllPlayersByGroupId(groupId) }

            assertEquals(ErrorCode.GROUP_NOT_FOUND, exception.errorCode)
        }
    }
}