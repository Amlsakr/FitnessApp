package com.aml_sakr.fitlife.core.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseMviViewModel<State : UIState, Event : UIEvent, Action : OneTimeAction>(
    initialState: State
) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<State> = mutableState.asStateFlow()

    private val actionChannel = Channel<Action>(Channel.BUFFERED)
    val actions: Flow<Action> = actionChannel.receiveAsFlow()

    fun onEvent(event: Event) {
        handleEvent(event)
    }

    protected abstract fun handleEvent(event: Event)

    protected fun setState(reducer: State.() -> State) {
        mutableState.value = mutableState.value.reducer()
    }

    protected fun sendAction(action: Action) {
        viewModelScope.launch {
            actionChannel.send(action)
        }
    }
}
