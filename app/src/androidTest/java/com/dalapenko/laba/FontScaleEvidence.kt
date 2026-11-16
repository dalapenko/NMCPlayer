package com.dalapenko.laba

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.math.abs

internal fun assertSystemFontScale(context: Context, source: String): Float {
    val actual = context.resources.configuration.fontScale
    val expected = InstrumentationRegistry.getArguments().getString("expectedFontScale")?.toFloat()
        ?: error("Pass expectedFontScale with the instrumentation arguments")
    check(abs(actual - expected) <= 0.01f) {
        "$source expected system fontScale=$expected, actual=$actual"
    }
    Log.i(source, "systemFontScale=$actual")
    return actual
}

internal fun assertTextHeightGrows(
    fontScale: Float,
    textHeightPx: Float,
    baselineArgument: String,
    source: String,
) {
    if (fontScale <= 1f) return
    val baseline = InstrumentationRegistry.getArguments().getString(baselineArgument)?.toFloat()
        ?: error("Pass $baselineArgument from the matching fontScale=1.0 run")
    check(textHeightPx > baseline + 1f) {
        "$source text height did not grow: actual=$textHeightPx px baseline=$baseline px"
    }
    Log.i(source, "fontScale=$fontScale textHeight=$textHeightPx px baseline=$baseline px")
}

internal fun saveDeviceScreenshot(name: String) {
    val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
    saveUiEvidence(bitmap.asImageBitmap(), name)
}
