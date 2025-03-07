package com.paytheory.lib.compose.inputs

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.text.TextRange
import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper

/**
 * Creates a Saver for SecureStringWrapper objects.
 *
 * This function provides a mechanism to save and restore the state of a
 * [SecureStringWrapper] object, which represents a string value that
 * needs to be handled securely, along with its associated state (like text selection
 * and composition).
 *
 * The saver handles the following properties of the [SecureStringWrapper]:
 *   - `secureValue`: The underlying secure string value. This is saved after revealing it for UI.
 *   - `text`: The current text content of the wrapper.
 *   - `selection`: The current text selection range.
 *   - `composition`: The current composition range (if any).
 *
 * The saved state is represented as a list of the following:
 *   1. The revealed `secureValue` (String).
 *   2. The `text` (String).
 *   3. The `selection.start` (Int).
 *   4. The `selection.end` (Int).
 *   5. The `composition.start` (Int, or -1 if null).
 *   6. The `composition.end` (Int, or -1 if null).
 *
 * When restoring, only the initial `secureValue` and the `selection` are used from the saved state.
 * `text` and `composition` are not restored, meaning that the restored `SecureStringWrapper` will only keep the saved sensitive data and selection.
 *
 * @return A Saver that can save and restore SecureStringWrapper objects.
 */
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


/**
 * Creates a [Saver] for [SecureString] objects.
 *
 * This saver is responsible for saving and restoring [SecureString] objects
 * within the Compose state saving mechanism.  Since [SecureString] is designed
 * to protect sensitive string data, the saving process temporarily reveals the
 * underlying string for the duration of the save, then it is discarded.
 *
 * **Security Considerations:**
 * - While this function allows saving and restoring [SecureString] data across
 *   configuration changes or process death, that
 *   the revealed string is not exposed during the save operation.
 *
 * **How it works:**
 * - `save`: When saving a [SecureString], it temporarily exposes the underlying
 *   data using `revealForProcessing()` and stores it as a single-element list
 *   of byte arrays.
 * - `restore`: When restoring, it retrieves the single byte array from the list and
 *   creates a new [SecureString] instance.
 *
 * @return A [Saver] capable of saving and restoring [SecureString] objects.
 */
fun createSecureStringSaver(): Saver<SecureString, *> = listSaver(
    save = { secureString ->
        listOf(
            secureString.revealForProcessing()
        )
    },
    restore = { list ->
        SecureString(
            list[0]
        )
    }
)