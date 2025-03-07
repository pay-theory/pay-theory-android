package com.paytheory.lib.compose.inputs

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.text.TextRange
import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper

fun createSecureStringWrapperSaver(): Saver<SecureStringWrapper, *> = listSaver(
    save = { wrapper ->
        listOf(
            wrapper.secureValue.revealForUi(), // Save SecureString value
            wrapper.secureState.text,          // Save text content
            wrapper.secureState.selection.start,
            wrapper.secureState.selection.end,
            wrapper.secureState.composition?.start ?: -1,
            wrapper.secureState.composition?.end ?: -1
        )
    },
    restore = { list ->
        SecureStringWrapper(
            initialValue = SecureString(list[0] as String),
            selection = TextRange(
                start = list[1] as Int,
                end = list[2] as Int
            )
        )
    }
)

fun createSecureStringSaver(): Saver<SecureString, *> = listSaver(
    save = { secureString ->
        listOf(
            secureString.revealForUi()
        )
    },
    restore = { list ->
        SecureString(
            list[0]
        )
    }
)