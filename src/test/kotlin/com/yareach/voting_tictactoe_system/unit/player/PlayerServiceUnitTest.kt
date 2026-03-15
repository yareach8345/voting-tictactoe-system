package com.yareach.voting_tictactoe_system.unit.player

import com.yareach.voting_tictactoe_system.common.error.ApiException
import com.yareach.voting_tictactoe_system.common.error.ErrorCode
import com.yareach.voting_tictactoe_system.game_group_info.service.GameGroupInfoService
import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.player.dto.AddNewPlayerDto
import com.yareach.voting_tictactoe_system.player.dto.InitRecruitDto
import com.yareach.voting_tictactoe_system.player.dto.RecruitAcceptedDto
import com.yareach.voting_tictactoe_system.player.dto.RecruitCompleted
import com.yareach.voting_tictactoe_system.player.dto.RecruitRejectedDto
import com.yareach.voting_tictactoe_system.player.model.Player
import com.yareach.voting_tictactoe_system.player.repository.PlayerRepository
import com.yareach.voting_tictactoe_system.player.service.PlayerServiceImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertThrows
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@Suppress("Unused")
class PlayerServiceUnitTest {
    val playerRepositoryMock = mockk<PlayerRepository>()
    val gameGroupInfoServiceMock = mockk<GameGroupInfoService>()

    val playerService = PlayerServiceImpl(playerRepositoryMock, gameGroupInfoServiceMock)

    val groupId = "testGroupId"

    @BeforeEach
    fun setGameGroupInfoData() {
        coEvery { gameGroupInfoServiceMock.checkGameGroupIsExists(any()) } answers { args[0] == groupId }
    }

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

            val inputFlow = flow {
                emit(InitRecruitDto(groupId))
                userIds.forEach { emit(AddNewPlayerDto(userId = it)) }
            }

            val result = playerService.processRecruitMessage(inputFlow).toList()

            coVerify { val _unused = playerRepositoryMock.saveAll(any()) }

            result.dropLast(1)
                .filterIsInstance<RecruitAcceptedDto>()
                .also { assertEquals(userIds.size, it.size) }
                .zip(userIds)
                .forEach { (acceptedDto, userId) ->
                    assertEquals(userId, acceptedDto.userId)
                }

            result.takeLast(1)[0]
                .also { lastResponseDto ->
                    assertInstanceOf<RecruitCompleted>(lastResponseDto)
                    assertEquals(3, lastResponseDto.playersByTeam.userIdsInTeamX.size)
                    assertEquals(3, lastResponseDto.playersByTeam.userIdsInTeamO.size)

                    assertTrue { isMatchPlayerAndUserId(saveAllSlot.captured, userIds) }
                }
        }

        @Test
        @DisplayName("[success case] ユーザーの数が奇数である場合、余った一人はランダムなチームに配置")
        fun divideTeamWhenNumberOfPlayersIsOdd() = runTest {
            val userIds = List(7) { "testUser$it" }

            val inputFlow = flow {
                emit(InitRecruitDto(groupId))
                userIds.forEach { emit(AddNewPlayerDto(userId = it)) }
            }

            val result = playerService.processRecruitMessage(inputFlow).toList()

            coVerify { val _unused = playerRepositoryMock.saveAll(any()) }

            result.dropLast(1)
                .filterIsInstance<RecruitAcceptedDto>()
                .also { assertEquals(userIds.size, it.size) }
                .zip(userIds)
                .forEach { (acceptedDto, userId) ->
                    assertEquals(userId, acceptedDto.userId)
                }

            result.takeLast(1)[0]
                .also { lastResponseDto ->
                    assertInstanceOf<RecruitCompleted>(lastResponseDto)
                    assertEquals(1, abs(lastResponseDto.playersByTeam.userIdsInTeamO.size - lastResponseDto.playersByTeam.userIdsInTeamX.size))

                    assertTrue { isMatchPlayerAndUserId(saveAllSlot.captured, userIds) }
                }

            assertTrue { isMatchPlayerAndUserId(saveAllSlot.captured, userIds) }
        }

        @Test
        @DisplayName("[fail case] 人数が2未満である場合、エラー発生")
        fun divideTeamWhenNumberOfPlayersUnder2() = runTest {
            val userIds = List(1) { "testUser$it" }

            val inputFlow = flow {
                emit(InitRecruitDto(groupId))
                userIds.forEach { emit(AddNewPlayerDto(userId = it)) }
            }

            val exception = assertThrows<ApiException> { playerService.processRecruitMessage(inputFlow).collect() }

            assertEquals(ErrorCode.NOT_ENOUGH_PLAYERS, exception.errorCode)
        }

        @Test
        @DisplayName("[fail case] 存在しないgroupIdで募集を始めたら失敗する")
        fun whenGroupIdIsNotExistsShouldFail() = runTest {
            val userIds = List(6) { "testUser$it" }
            val inputFlow = flow {
                emit(InitRecruitDto("wrong-group-id"))
                userIds.forEach { emit(AddNewPlayerDto(userId = it)) }
            }

            val exception = assertThrows<ApiException> { playerService.processRecruitMessage(inputFlow).collect() }

            assertEquals(ErrorCode.INVALID_GROUP_ID, exception.errorCode)
        }

        @Test
        @DisplayName("[fail case] 最初のメッセージがInitメッセージではない場合、失敗する")
        fun whenFirstMessageIsNotInitMessageShouldFail() = runTest {
            val userIds = List(6) { "testUser$it" }
            val inputFlow = flow { userIds.forEach { emit(AddNewPlayerDto(userId = it)) } }

            val exception = assertThrows<ApiException> { playerService.processRecruitMessage(inputFlow).collect() }

            assertEquals(ErrorCode.WRONG_MESSAGE_ORDER, exception.errorCode)
        }

        @Test
        @DisplayName("[fail case] 二番目のメッセージの以後、Initメッセージをもらう場合、失敗する")
        fun whenReceiveInitMessageAfterReceiveTheFirstMessageShouldFail() = runTest {
            val userIds = List(6) { "testUser$it" }
            val inputFlow = flow {
                emit(InitRecruitDto(groupId))
                userIds.take(3).forEach { emit(AddNewPlayerDto(userId = it)) }
                emit(InitRecruitDto(groupId))
                userIds.drop(3).forEach { emit(AddNewPlayerDto(userId = it)) }
            }

            val exception = assertThrows<ApiException> { playerService.processRecruitMessage(inputFlow).collect() }

            assertEquals(ErrorCode.WRONG_MESSAGE_ORDER, exception.errorCode)
        }

        @Test
        @DisplayName("[Success case] もう追加したユーザーのＩＤをまた受けると、Rejectedメッセージを伝送")
        fun whenUserIdDuplicatedShouldReceiveRejectedMessage() = runTest {
            val userIds = List(6) { "testUser$it" }
            val inputFlow = flow {
                emit(InitRecruitDto(groupId))
                userIds.take(3).forEach { emit(AddNewPlayerDto(userId = it)) }
                userIds[2].also { emit(AddNewPlayerDto(userId = it)) } // "testUser2"を二度目伝送
                userIds.drop(3).forEach { emit(AddNewPlayerDto(userId = it)) }
            }

            val result = playerService.processRecruitMessage(inputFlow).toList()

            result[2].also {
                assertInstanceOf<RecruitAcceptedDto>(it)
                assertEquals("testUser2", it.userId)
            }

            result[3].also {
                assertInstanceOf<RecruitRejectedDto>(it)
                assertEquals("testUser2", it.userId)
            }
        }

        @Test
        @DisplayName("[Fail case] タイムアウト発生")
        fun timeTest() = runTest {
            val userIds = List(10) { "testUser$it" }
            val inputFlow = flow {
                emit(InitRecruitDto(groupId))
                userIds.forEach {
                    delay(10.seconds)
                    emit(AddNewPlayerDto(userId = it))
                }
            }

            val exception = assertThrows<ApiException> { playerService.processRecruitMessage(inputFlow).collect() }

            assertEquals(ErrorCode.PLAYER_RECRUIT_TIMEOUT, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("特定のグループのプレイヤーのユーザーＩＤをチーム別に取得")
    inner class GetPlayersByGroupTest {

        @Test
        @DisplayName("[Success case] プレイヤーのユーザーＩＤを修得")
        fun getPlayersByGroupSuccessfully() = runTest {

            val players = List(6) { Player.new(groupId, "user-$it", if (it % 2 == 0) Team.X else Team.O) }

            coEvery { playerRepositoryMock.findByGroupId(groupId) } returns players.asFlow()

            val result = playerService.getPlayersByGroupId(groupId)

            coVerify { gameGroupInfoServiceMock.checkGameGroupIsExists(groupId) }

            coVerify { val _unused = playerRepositoryMock.findByGroupId(groupId) }

            assertEquals(listOf("user-0", "user-2", "user-4"), result.userIdsInTeamX.toList().sorted())
            assertEquals(listOf("user-1", "user-3", "user-5"), result.userIdsInTeamO.toList().sorted())
        }

        @Test
        @DisplayName("[Success case] プレイヤーが存在しないチームは空リストで返す")
        fun whenInATeamThereIsNotUsers() = runTest {

            val players = List(6) { Player.new(groupId, "user-$it", Team.X) }

            coEvery { playerRepositoryMock.findByGroupId(groupId) } returns players.asFlow()

            val result = playerService.getPlayersByGroupId(groupId)

            coVerify { gameGroupInfoServiceMock.checkGameGroupIsExists(groupId) }

            coVerify { val _unused = playerRepositoryMock.findByGroupId(groupId) }

            assertEquals(6, result.userIdsInTeamX.size)
            assertEquals(0, result.userIdsInTeamO.size)
        }

        @Test
        @DisplayName("[Fail case] 指定したGroupIdのプレイヤーが存在しない場合、エラー発生")
        fun whereNobodyExistInGroup() = runTest {

            val wrongGroupId = "wrong-group-id"

            coEvery { playerRepositoryMock.existsByGroupId(wrongGroupId) }.returns(false)

            val exception = assertThrows<ApiException> {
                playerService.getPlayersByGroupId(wrongGroupId)
            }

            coVerify { gameGroupInfoServiceMock.checkGameGroupIsExists(wrongGroupId) }

            coVerify(exactly = 0) { val _unused = playerRepositoryMock.findByGroupId(wrongGroupId) }

            assertEquals(ErrorCode.INVALID_GROUP_ID, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("GroupIdでプレイヤーのデータ削除")
    inner class DeleteAllPlayersByGroupIdTest {

        @Test
        @DisplayName("[success case] グループの所属のプレイヤーを削除")
        fun deleteAllPlayerSuccessfully() = runTest {

            coEvery { playerRepositoryMock.deleteByGroupId(groupId) } returns 6

            val result = playerService.deleteAllPlayersByGroupId(groupId)

            assertEquals(6, result)
        }

        @Test
        @DisplayName("[fail case] 指定したＩＤのグループが存在しない場合、エラー発生")
        fun whenMatchingGroupIsNotExists() = runTest {
            val wrongGroupId = "wrongGroupId"

            val exception = assertThrows<ApiException> { playerService.deleteAllPlayersByGroupId(wrongGroupId) }

            assertEquals(ErrorCode.INVALID_GROUP_ID, exception.errorCode)
        }
    }
}