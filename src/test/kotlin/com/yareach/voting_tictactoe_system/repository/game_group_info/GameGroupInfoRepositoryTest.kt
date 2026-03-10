package com.yareach.voting_tictactoe_system.repository.game_group_info

import com.yareach.voting_tictactoe_system.game_group_info.enum.GameState
import com.yareach.voting_tictactoe_system.game_group_info.model.GameGroupInfo
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoR2dbcRepository
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoRepository
import com.yareach.voting_tictactoe_system.game_group_info.repository.GameGroupInfoRepositoryR2dbcImpl
import com.yareach.voting_tictactoe_system.tictactoe.enum.GameType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest
import org.springframework.dao.DuplicateKeyException
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataR2dbcTest
class GameGroupInfoRepositoryTest {

    @Autowired
    private lateinit var gameGroupInfoR2dbcRepository: GameGroupInfoR2dbcRepository

    private lateinit var gameGroupInfoRepository: GameGroupInfoRepository

    fun assertGameGroupInfo(beforeSave: GameGroupInfo, saveResult: GameGroupInfo) {
        assertNotNull(saveResult.id)
        assertEquals(beforeSave.groupId, saveResult.groupId)
        assertEquals(beforeSave.gameType, saveResult.gameType)
        assertEquals(beforeSave.state, saveResult.state)
    }

    @BeforeEach
    fun setUpRepository() {
        gameGroupInfoRepository = GameGroupInfoRepositoryR2dbcImpl(gameGroupInfoR2dbcRepository)
    }

    @AfterEach
    suspend fun tearDownRepository() {
        gameGroupInfoR2dbcRepository.deleteAll()
    }

    @Nested
    @DisplayName("save")
    inner class SaveGroupInfoTest {

        @Test
        @DisplayName("[Success case] game_group_info データ保存")
        fun saveSuccessfully() = runTest {
            val gameGroupInfo = GameGroupInfo.new("group1", GameType.NORMAL)

            val result = gameGroupInfoRepository.save(gameGroupInfo)

            val readResult = gameGroupInfoRepository.findByGroupId("group1")

            assertGameGroupInfo(gameGroupInfo, result)

            assertNotNull(readResult)
            assertGameGroupInfo(gameGroupInfo, readResult)
        }

        @Test
        @DisplayName("[Fail case] 指定したgroupIdに対するデータがもう存在する場合、エラー発生")
        fun whenGroupIdDuplicated() = runTest {
            val gameGroupInfo1 = GameGroupInfo.new("group1", GameType.NORMAL)
            val gameGroupInfo2 = GameGroupInfo.new("group1", GameType.INFINITY)

            gameGroupInfoRepository.save(gameGroupInfo1)
            assertThrows<DuplicateKeyException>{ gameGroupInfoRepository.save(gameGroupInfo2) }
        }
    }

    @Nested
    @DisplayName("findByGroupId")
    inner class FindByGroupIdTest {

        @Test
        @DisplayName("[Success case] GroupIdで検索")
        fun findByGroupIdSuccess() = runTest {
            val gameGroupInfo = GameGroupInfo.new("group1", GameType.NORMAL)

            gameGroupInfoRepository.save(gameGroupInfo)

            val findResult = gameGroupInfoRepository.findByGroupId("group1")

            assertNotNull(findResult)
            assertGameGroupInfo(gameGroupInfo, findResult)
        }

        @Test
        @DisplayName("[Success case] GroupIdに該当するデータが存在しない場合、nullを返す")
        fun findByGroupIdResultIsNull() = runTest {
            val findResult = gameGroupInfoRepository.findByGroupId("unexists_groupId")

            assertNull(findResult)
        }
    }

    @Nested
    @DisplayName("existsByGroupId")
    inner class ExistsByGroupIdTest {

        @Test
        @DisplayName("[Success case] 該当するデータが存在する場合、trueを返す")
        fun whenDataIsExistsReturnTrue() = runTest {
            val gameGroupInfo = GameGroupInfo.new("group1", GameType.NORMAL)

            gameGroupInfoRepository.save(gameGroupInfo)

            val result = gameGroupInfoRepository.existsByGroupId("group1")

            assertTrue(result)
        }

        @Test
        @DisplayName("[Success case] 該当するデータが存在する場合、falseを返す")
        fun whenDataIsNotExistsResultFalse() = runTest {
            val findResult = gameGroupInfoRepository.existsByGroupId("unexists_groupId")

            assertFalse(findResult)
        }
    }

    @Nested
    @DisplayName("deleteByGroupId")
    inner class DeleteByGroupIdTest {

        @Test
        @DisplayName("[Success case] GroupIdに該当するデータを削除")
        fun deleteByGroupIdSuccess() = runTest {

            val gameGroupInfo = GameGroupInfo.new("group1", GameType.NORMAL)
            gameGroupInfoRepository.save(gameGroupInfo)

            val result = gameGroupInfoRepository.deleteByGroupId("group1")

            val findResult = gameGroupInfoRepository.findByGroupId("group1")

            assertNull(findResult)
            assertEquals(1, result)
        }

        @Test
        @DisplayName("[Success case] GroupIdに該当するデータがない場合もエラー発生しない")
        fun whenMatchingDataIsNotExists() = runTest {

            val result = gameGroupInfoRepository.deleteByGroupId("unexists_groupId")

            assertEquals(0, result)
        }
    }

    @Nested
    @DisplayName("updateGameState")
    inner class UpdateGameStateTest {

        @Test
        @DisplayName("[Success case] stateを変更")
        fun updateGameStateSuccess() = runTest {
            val gameGroupInfo = GameGroupInfo.new("group1", GameType.NORMAL)
            gameGroupInfoRepository.save(gameGroupInfo)

            val beforeTime = LocalDateTime.now()
            gameGroupInfoRepository.updateGameState("group1", GameState.FINISHED)
            val afterTime = LocalDateTime.now()

            val readResult = gameGroupInfoRepository.findByGroupId("group1")

            assertNotNull(readResult)
            assertEquals(GameState.FINISHED, readResult.state)
            assertTrue { readResult.lastUpdated.isAfter(beforeTime) }
            assertTrue { readResult.lastUpdated.isBefore(afterTime) }
        }
    }

    @Nested
    @DisplayName("updateLastUpdated")
    inner class UpdateLastUpdatedTest {

        @Test
        @DisplayName("[Success case] lastUpdatedをアップデート")
        fun updateGameStateSuccess() = runTest {
            val gameGroupInfo = GameGroupInfo.new("group1", GameType.NORMAL)
            gameGroupInfoRepository.save(gameGroupInfo)

            val beforeTime = LocalDateTime.now()
            gameGroupInfoRepository.updateLastUpdatedToNow("group1")
            val afterTime = LocalDateTime.now()

            val readResult = gameGroupInfoRepository.findByGroupId("group1")

            assertNotNull(readResult)
            assertTrue { readResult.lastUpdated.isAfter(beforeTime) }
            assertTrue { readResult.lastUpdated.isBefore(afterTime) }
        }
    }
}