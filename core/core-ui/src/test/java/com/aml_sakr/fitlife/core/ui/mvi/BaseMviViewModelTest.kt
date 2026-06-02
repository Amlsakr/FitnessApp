package com.aml_sakr.fitlife.core.ui.mvi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseMviViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onEvent_updatesStateAndEmitsOneTimeAction() = runTest(dispatcher) {
        val viewModel = CounterViewModel()

        viewModel.onEvent(CounterEvent.Increment)
        advanceUntilIdle()

        assertEquals(CounterState(count = 1), viewModel.state.value)
        assertEquals(CounterAction.CountChanged, viewModel.actions.first())
    }

    private data class CounterState(val count: Int = 0) : UIState

    private sealed interface CounterEvent : UIEvent {
        data object Increment : CounterEvent
    }

    private sealed interface CounterAction : OneTimeAction {
        data object CountChanged : CounterAction
    }

    private class CounterViewModel :
        BaseMviViewModel<CounterState, CounterEvent, CounterAction>(CounterState()) {
        override fun handleEvent(event: CounterEvent) {
            when (event) {
                CounterEvent.Increment -> {
                    setState { copy(count = count + 1) }
                    sendAction(CounterAction.CountChanged)
                }
            }
        }
    }
}
