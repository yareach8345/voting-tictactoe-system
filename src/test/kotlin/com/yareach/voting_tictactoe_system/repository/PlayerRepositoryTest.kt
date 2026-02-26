package com.yareach.voting_tictactoe_system.repository

import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.player.model.Player
import com.yareach.voting_tictactoe_system.player.repository.PlayerR2dbcRepository
import com.yareach.voting_tictactoe_system.player.repository.PlayerRepository
import com.yareach.voting_tictactoe_system.player.repository.PlayerRepositoryR2dbcImpl
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DataR2dbcTest
class PlayerRepositoryTest {

    @Autowired
    private lateinit var playerR2dbcRepository: PlayerR2dbcRepository

    private lateinit var playerRepository: PlayerRepository

    @BeforeEach
    fun initRepository() {
        playerRepository = PlayerRepositoryR2dbcImpl(playerR2dbcRepository)
    }

    @AfterEach
    suspend fun clearDatabase() {
        playerR2dbcRepository.deleteAll()
    }

    @Nested
    @DisplayName("データ取得")
    inner class FindDataTest {

        @Nested
        @DisplayName("with group id")
        inner class FindByGroupIdTest {

            @Test
            @DisplayName("[Success Case] グループＩＤでグループに所属するメンバーを取得")
            fun successCase() = runTest {
                repeat(3) { i -> playerRepository.save(Player.new("group1", "user$i")) }

                val result = playerRepository.findByGroupId("group1").toList()

                assertEquals(3, result.size)
            }

            @Test
            @DisplayName("[Success Case] グループに属したメンバーがない時、空のFlowを返す")
            fun whenNoOneInTheGroup() = runTest {
                val result = playerRepository.findByGroupId("group2").toList()

                assert(result.isEmpty())
            }
        }

        @Nested
        @DisplayName("with group id and team")
        inner class FindByGroupIdAndTeamTest {

            @Test
            @DisplayName("[Success Case] groupIdとteamを条件にプレイヤーデータ取得")
            fun successCase() = runTest {
                repeat(10) { i ->
                    val team = if(i % 2 == 0) Team.O else Team.X
                    playerRepository.save(Player(groupId = "group1", userId = "user$i", team = team))
                }

                val result = playerRepository.findByGroupIdAndTeam("group1", Team.O).toList()

                assertEquals(5, result.size)
            }

            @Test
            @DisplayName("[Success Case] 該当するデータがなければ空のFlowを返す")
            fun whenMatchingDataIsNotExist() = runTest {
                repeat(10) { i -> playerRepository.save(Player(groupId = "group1", userId = "user$i", team = Team.X)) }

                val result = playerRepository.findByGroupIdAndTeam("group1", Team.O).toList()

                assert(result.isEmpty())
            }
        }

        @Nested
        @DisplayName("with group id and user id (find a special player)")
        inner class FindByGroupIdAndUserTest {

            @Test
            @DisplayName("[Success Case] グループＩＤとユーザーＩＤで特定なプレイヤーを取得")
            fun successCase() = runTest {
                playerRepository.save(Player(groupId = "group1", userId = "user1", team = Team.X))

                val result = playerRepository.findByGroupIdAndUserId("group1", "user1")

                assertNotNull(result)
                assertEquals("group1", result.groupId)
                assertEquals("user1", result.userId)
            }

            @Test
            @DisplayName("[Fail Case] 該当するデータがない場合、nullを返す")
            fun whenMatchingDataIsNotExist() = runTest {
                val result = playerRepository.findByGroupIdAndUserId("group1", "user1")

                assertNull(result)
            }
        }
    }

    @Nested
    @DisplayName("データの存在確認")
    inner class CheckDataExist {

        @Nested
        @DisplayName("特定のプレイヤーの存在確認")
        inner class ExistsByGroupAndUserIdTest {

            @Test
            @DisplayName("[Success Case] 該当するデータが存在する場合、trueを返す")
            fun whenMatchingDataIsExists() = runTest {
                playerRepository.save(Player.new(groupId = "group1", userId = "user1"))

                val result = playerRepository.existsByGroupIdAndUserId("group1", "user1")

                assertTrue(result)
            }

            @Test
            @DisplayName("[Success Case] 該当するデータが存在しない場合、falseを返す")
            fun whenMatchingDataIsNotExists() = runTest {
                val result = playerRepository.existsByGroupIdAndUserId("group1", "user1")

                assertFalse(result)
            }
        }
    }

    @Nested
    @DisplayName("データ削除")
    inner class DeleteDataTest {

        @Nested
        @DisplayName("グループの全てのプレイヤーを削除")
        inner class DeleteByGroupIdTest {

            @Test
            @DisplayName("[Success Case] 該当するプレイヤーのデータを削除し、削除したプレイヤーの数を返す")
            fun deletePlayersSuccessfully() = runTest {
                repeat(3) { i -> playerRepository.save(Player.new(groupId = "group1", userId = "user$i")) }

                val result = playerRepository.deleteByGroupId("group1")

                assertEquals(3, result)
            }

            @Test
            @DisplayName("[Success Case] 該当するプレイヤーが存在しない場合、０を返す")
            fun matchingPlayerIsNotExist() = runTest {
                val result = playerRepository.deleteByGroupId("group1")

                assertEquals(0, result)
            }
        }

        @Nested
        @DisplayName("groupIdとuserIdによるプレイヤー削除")
        inner class DeleteByGroupIdAndUserIdTest {

            @Test
            @DisplayName("[Success Case] 該当するデータを削除し、1を返す")
            fun whenMatchingDataIsExist() = runTest {
                playerRepository.save(Player.new(groupId = "group1", userId = "user1"))

                val result = playerRepository.deleteByGroupIdAndUserId("group1", "user1")

                assertEquals(1, result)
            }

            @Test
            @DisplayName("[Success Case] 該当データが存在しない場合は0を返す")
            fun whenMatchingDataIsNotExist() = runTest {
                val result = playerRepository.deleteByGroupIdAndUserId("group1", "user1")

                assertEquals(0, result)
            }
        }
    }
}