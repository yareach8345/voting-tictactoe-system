package com.yareach.voting_tictactoe_system.common.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun<T> Flow<T>.doFirst(run: suspend (T) -> Unit) = flow {
    var isFirst = true

    collect { value ->
        if(isFirst) {
            run(value)
            isFirst = false
        } else {
            emit(value)
        }
    }
}