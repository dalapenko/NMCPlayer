package com.dalapenko.laba

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.platform.app.InstrumentationRegistry

internal fun saveUiEvidence(image: ImageBitmap, name: String) {
    if (InstrumentationRegistry.getArguments().getString("saveUiEvidence") != "true") return

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/LabaTestEvidence")
    }
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ?: error("Could not create UI evidence image")
    context.contentResolver.openOutputStream(uri).use { output ->
        checkNotNull(output) { "Could not open UI evidence output stream" }
        image.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, output)
    }
}
