package com.dalapenko.laba.core.media

import android.content.ContentResolver
import android.content.Context
import com.dalapenko.laba.testTrack
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class SafPlaybackCacheTest {

    private val context = mockk<Context>()
    private val contentResolver = mockk<ContentResolver>()
    private lateinit var filesDir: File
    private lateinit var cache: SafPlaybackCache

    @Before
    fun setUp() {
        filesDir = File.createTempFile("laba-cache-test", "").apply {
            delete()
            mkdirs()
        }
        every { context.filesDir } returns filesDir
        every { context.contentResolver } returns contentResolver
        cache = SafPlaybackCache(context)
    }

    @After
    fun tearDown() {
        filesDir.deleteRecursively()
    }

    @Test
    fun givenLocalTrack_whenCached_thenKeepsOriginalUriWithoutOpeningResolver() = runTest {
        val sourceUri = "file:///data/user/0/com.dalapenko.laba/files/track.mp3"

        val result = cache.cacheTracks(
            bookId = 42L,
            tracks = listOf(testTrack(id = 7L, fileUri = sourceUri)),
        )

        assertEquals(sourceUri, result.single())
        verify(exactly = 0) { contentResolver.openInputStream(any()) }
    }

    @Test
    fun givenBookCache_whenCleared_thenRemovesCachedBytes() = runTest {
        val cacheDirectory = File(filesDir, "playback-cache/42").apply {
            mkdirs()
        }
        File(cacheDirectory, "track.audio").writeText("audio")

        cache.clearBook(42L)

        assertTrue(!cacheDirectory.exists())
    }
}
