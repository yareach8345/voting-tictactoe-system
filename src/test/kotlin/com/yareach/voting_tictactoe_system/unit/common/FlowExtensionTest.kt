package com.yareach.voting_tictactoe_system.unit.common

import com.yareach.voting_tictactoe_system.common.extension.doFirst
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FlowExtensionTest {

    @Nested
    @DisplayName("doFirst")
    inner class DoFirstTest {
        @Test
        @DisplayName("一番目のデータを別に処理")
        fun flowDoFirst() = runTest {
            val flow = (0 .. 11).asFlow()

            flow.doFirst {
                assertEquals(0, it)
            }.collectIndexed { index, value ->
                assertEquals(index + 1, value)
            }
        }

        @Test
        @DisplayName("データがない場合、どの関数も実行されない")
        fun whenFlowHasZeroElements() = runTest {
            val flow = flowOf<Int>()

            val functionSpyForFirst = mockk<(Int) -> Unit>()

            val functionSpyForCollect = mockk<(Int) -> Unit>()

            flow
                .doFirst(functionSpyForFirst)
                .collect(functionSpyForCollect)

            coVerify(exactly = 0) { functionSpyForFirst(any()) }
            coVerify(exactly = 0) { functionSpyForCollect(any()) }
        }

        @Test
        @DisplayName("データが一つだけある場合、doFirstの引数の関数だけが実行される")
        fun whenFlowHasOnlyOneElement() = runTest {
            val flow = flowOf(1)

            val functionSpyForFirst = mockk<(Int) -> Unit>()
            coEvery { functionSpyForFirst(any()) } returns Unit

            val functionSpyForCollect = mockk<(Int) -> Unit>()

            flow
                .doFirst(functionSpyForFirst)
                .collect(functionSpyForCollect)

            coVerify(exactly = 1) { functionSpyForFirst(any()) }
            coVerify(exactly = 0) { functionSpyForCollect(any()) }
        }
    }
}