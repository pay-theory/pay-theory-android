package com.paytheory.lib

import android.content.Context

/**
 * Interface that defines a provider of Android Context.
 * This decouples components from requiring a direct Context instance.
 */
interface ContextProvider {
    /**
     * Returns an Android Context if available, or null for test environments.
     * @return The Android Context or null
     */
    fun getContext(): Context?
} 