package com.yareach.voting_tictactoe_system.repository.player

import com.yareach.voting_tictactoe_system.common.enum.GameType
import com.yareach.voting_tictactoe_system.game_group_info.model.GameGroupInfo
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoR2dbcRepository
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoRepository
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoRepositoryR2dbcImpl
import com.yareach.voting_tictactoe_system.player.common.Team
import com.yareach.voting_tictactoe_system.player.model.Player
import com.yareach.voting_tictactoe_system.player.repository.PlayerR2dbcRepository
import com.yareach.voting_tictactoe_system.player.repository.PlayerRepository
import com.yareach.voting_tictactoe_system.player.repository.PlayerRepositoryR2dbcImpl
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DataR2dbcTest
class PlayerRepositoryTest
{
    @Autowired
    private lateinit var playerR2dbcRepository: PlayerR2dbcRepository

    private lateinit var playerRepository: PlayerRepository

    @Autowired
    private lateinit var gameGroupInfoR2dbcRepository: GameGroupInfoR2dbcRepository

    private lateinit var gameGroupInfoRepository: GameGroupInfoRepository

    val testGroupId = "testGroupId"

    @BeforeEach
    suspend fun initRepository() {
        playerRepository = PlayerRepositoryR2dbcImpl(playerR2dbcRepository)

        gameGroupInfoRepository = GameGroupInfoRepositoryR2dbcImpl(gameGroupInfoR2dbcRepository)
        gameGroupInfoRepository.save(GameGroupInfo.new(testGroupId, GameType.NORMAL))
    }

    @AfterEach
    suspend fun tearDown() {
        playerR2dbcRepository.deleteAll()
        gameGroupInfoR2dbcRepository.deleteAll()
    }

    @AfterEach
    suspend fun clearDatabase() {
        playerR2dbcRepository.deleteAll()
    }

    @Nested
    @DisplayName("プレイヤーデータ保存")
    inner class SaveDataTest {

        @Nested
        @DisplayName("単一データ保存")
        inner class SaveTest {

            @Test
            @DisplayName("[Success Case] エンティティーのＩＤフィールドが空いている場合、新しいデータを保存")
            fun saveDataSuccessfully() = runTest {
                val newPlayer = Player.new(testGroupId, "user1", Team.O)

                val saveResult = playerRepository.save(newPlayer)

                val allPlayerInDb = playerR2dbcRepository.findAll().toList()

                assertNotNull(saveResult.id)

                Assertions.assertEquals(1, allPlayerInDb.size)
                Assertions.assertEquals(testGroupId, allPlayerInDb.first().groupId)
                Assertions.assertEquals("user1", allPlayerInDb.first().userId)
            }

            @Test
            @DisplayName("[Success Case] エンティティーのＩＤフィールドが空いていない場合、データを更新")
            fun updateDataSuccessfully() = runTest {
                val newPlayer = Player.new(testGroupId, "user1", Team.O)

                val saveResult = playerRepository.save(newPlayer)

                val updateResult = playerRepository.save(saveResult)

                Assertions.assertEquals(testGroupId, updateResult.groupId)
                Assertions.assertEquals("user1", updateResult.userId)
                Assertions.assertEquals(Team.O, updateResult.team)
            }

            @Test
            @DisplayName("[Fail case] 存在しないgroupIdで保存しようとした場合、失敗する")
            fun whenGroupIdIsNotExistsShouldFail() = runTest {

                val newPlayer = Player.new("unexist-group-id", "user1", Team.O)

                val exception = assertThrows<DataAccessException>{ playerRepository.save(newPlayer) }

                assertInstanceOf<DataIntegrityViolationException>(exception)
            }
        }

        @Nested
        @DisplayName("複数のプレイヤーデータを保存")
        inner class SaveAllTest {

            @Test
            @DisplayName("[Success Case] 複数のプレイヤーデータを保存")
            fun saveDataSuccessfully() = runTest {
                val players = List(5) { i -> Player.new(testGroupId, "user$i", Team.O) }

                val savedResult = playerRepository.saveAll(players)

                val findResult = playerRepository.findByGroupId(testGroupId)

                Assertions.assertEquals(5, savedResult.count())
                Assertions.assertEquals(5, findResult.count())
            }

            @Test
            @DisplayName("[Fail case] 存在しないgroupIdで保存しようとした場合、失敗する")
            fun whenGroupIdIsNotExistsShouldFail() = runTest {

                val newPlayers = List(5) { i -> Player.new("unexist-group-id", "user$i", Team.O) }

                val exception = assertThrows<DataAccessException>{ playerRepository.saveAll(newPlayers).collect() }

                assertInstanceOf<DataIntegrityViolationException>(exception)
            }
        }
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
                repeat(3) { i -> playerRepository.save(Player.new(testGroupId, "user$i", Team.X)) }

                val result = playerRepository.findByGroupId(testGroupId).toList()

                Assertions.assertEquals(3, result.size)
            }

            @Test
            @DisplayName("[Success Case] グループに属したメンバーがない時、空のFlowを返す")
            fun whenNoOneInTheGroup() = runTest {
                val result = playerRepository.findByGroupId(testGroupId).toList()

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
                    val team = if (i % 2 == 0) Team.O else Team.X
                    playerRepository.save(Player(groupId = testGroupId, userId = "user$i", team = team))
                }

                val result = playerRepository.findByGroupIdAndTeam(testGroupId, Team.O).toList()

                Assertions.assertEquals(5, result.size)
            }

            @Test
            @DisplayName("[Success Case] 該当するデータがなければ空のFlowを返す")
            fun whenMatchingDataIsNotExist() = runTest {
                repeat(10) { i -> playerRepository.save(Player(groupId = testGroupId, userId = "user$i", team = Team.X)) }

                val result = playerRepository.findByGroupIdAndTeam(testGroupId, Team.O).toList()

                assert(result.isEmpty())
            }
        }

        @Nested
        @DisplayName("with group id and user id (find a special player)")
        inner class FindByGroupIdAndUserTest {

            @Test
            @DisplayName("[Success Case] グループＩＤとユーザーＩＤで特定なプレイヤーを取得")
            fun successCase() = runTest {
                playerRepository.save(Player(groupId = testGroupId, userId = "user1", team = Team.X))

                val result = playerRepository.findByGroupIdAndUserId(testGroupId, "user1")

                assertNotNull(result)
                Assertions.assertEquals(testGroupId, result.groupId)
                Assertions.assertEquals("user1", result.userId)
            }

            @Test
            @DisplayName("[Fail Case] 該当するデータがない場合、nullを返す")
            fun whenMatchingDataIsNotExist() = runTest {
                val result = playerRepository.findByGroupIdAndUserId(testGroupId, "user1")

                Assertions.assertNull(result)
            }
        }
    }

    @Nested
    @DisplayName("データの存在確認")
    inner class CheckDataExist {

        @Nested
        @DisplayName("特定のグループの所属のユーザーの存在確認")
        inner class ExistsByGroupIdTest {

            @Test
            @DisplayName("[success case] グループに属するユーザーがあればtrueを返す")
            fun existMatchingUser() = runTest {
                playerRepository.save(Player.new(groupId = testGroupId, userId = "user1", team = Team.X))

                val result = playerRepository.existsByGroupId(testGroupId)

                assertTrue(result)
            }

            @Test
            @DisplayName("[success case] グループに属するユーザーが存在しない場合、falseを返す")
            fun notExistMatchingUser() = runTest {
                val result = playerRepository.existsByGroupId(testGroupId)

                assertFalse(result)
            }
        }

        @Nested
        @DisplayName("特定のプレイヤーの存在確認")
        inner class ExistsByGroupAndUserIdTest {

            @Test
            @DisplayName("[Success Case] 該当するデータが存在する場合、trueを返す")
            fun whenMatchingDataIsExists() = runTest {
                playerRepository.save(Player.new(groupId = testGroupId, userId = "user1", team = Team.X))

                val result = playerRepository.existsByGroupIdAndUserId(testGroupId, "user1")

                assertTrue(result)
            }

            @Test
            @DisplayName("[Success Case] 該当するデータが存在しない場合、falseを返す")
            fun whenMatchingDataIsNotExists() = runTest {
                val result = playerRepository.existsByGroupIdAndUserId(testGroupId, "user1")

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
                repeat(3) { i ->
                    playerRepository.save(
                        Player.new(
                            groupId = testGroupId,
                            userId = "user$i",
                            team = Team.O
                        )
                    )
                }

                val result = playerRepository.deleteByGroupId(testGroupId)

                Assertions.assertEquals(3, result)
            }

            @Test
            @DisplayName("[Success Case] 該当するプレイヤーが存在しない場合、０を返す")
            fun matchingPlayerIsNotExist() = runTest {
                val result = playerRepository.deleteByGroupId(testGroupId)

                Assertions.assertEquals(0, result)
            }
        }

        @Nested
        @DisplayName("groupIdとuserIdによるプレイヤー削除")
        inner class DeleteByGroupIdAndUserIdTest {

            @Test
            @DisplayName("[Success Case] 該当するデータを削除し、1を返す")
            fun whenMatchingDataIsExist() = runTest {
                playerRepository.save(Player.new(groupId = testGroupId, userId = "user1", team = Team.O))

                val result = playerRepository.deleteByGroupIdAndUserId(testGroupId, "user1")

                Assertions.assertEquals(1, result)
            }

            @Test
            @DisplayName("[Success Case] 該当データが存在しない場合は0を返す")
            fun whenMatchingDataIsNotExist() = runTest {
                val result = playerRepository.deleteByGroupIdAndUserId(testGroupId, "user1")

                Assertions.assertEquals(0, result)
            }
        }
    }
}