package com.dalapenko.laba.core.media

import android.content.Context
import android.net.Uri
import android.util.Log
import com.dalapenko.laba.core.database.entity.TrackEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/**
 * Copies SAF-backed audio into app-private storage before playback.
 *
 * The persisted SAF URI remains the track identity. Cache files are only playback sources and are
 * written atomically so Media3 never sees a partially copied file.
 */
class SafPlaybackCache(private val context: Context) {

    suspend fun cacheTracks(bookId: Long, tracks: List<TrackEntity>): List<String> =
        withContext(Dispatchers.IO) {
            tracks.map { track ->
                cacheTrack(bookId, track.id, track.fileUri) ?: track.fileUri
            }
        }

    suspend fun clearBook(bookId: Long) = withContext(Dispatchers.IO) {
        synchronized(cacheLock) {
            File(cacheDirectory, bookId.toString()).deleteRecursively()
        }
    }

    private fun cacheTrack(bookId: Long, trackId: Long, sourceUri: String): String? {
        if (sourceUri.substringBefore(':') != CONTENT_SCHEME) return sourceUri
        val uri = Uri.parse(sourceUri)
        val bookDirectory = File(cacheDirectory, bookId.toString())
        val cacheFile = File(bookDirectory, "$trackId-${sourceKey(sourceUri)}$CACHE_EXTENSION")

        return synchronized(cacheLock) {
            if (cacheFile.isFile && cacheFile.length() > 0L) {
                Uri.fromFile(cacheFile).toString()
            } else {
                val temporaryFile = File(bookDirectory, ".${cacheFile.name}$PART_SUFFIX")
                try {
                    bookDirectory.mkdirs()
                    val source = context.contentResolver.openInputStream(uri)
                    if (source == null) {
                        null
                    } else {
                        source.use { input ->
                            temporaryFile.outputStream().use { output ->
                                input.copyTo(output, COPY_BUFFER_SIZE)
                                output.flush()
                                output.fd.sync()
                            }
                        }
                        if (temporaryFile.renameTo(cacheFile)) {
                            Uri.fromFile(cacheFile).toString()
                        } else {
                            null
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: IOException) {
                    Log.w(TAG, "Unable to cache SAF track book=$bookId track=$trackId", error)
                    null
                } catch (error: SecurityException) {
                    Log.w(TAG, "Unable to cache SAF track book=$bookId track=$trackId", error)
                    null
                } finally {
                    temporaryFile.delete()
                }
            }
        }
    }
    private fun sourceKey(sourceUri: String): String = MessageDigest
        .getInstance(SHA_256)
        .digest(sourceUri.toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private val cacheDirectory: File
        get() = File(context.filesDir, CACHE_DIRECTORY)

    private companion object {
        const val CACHE_DIRECTORY = "playback-cache"
        const val CACHE_EXTENSION = ".audio"
        const val CONTENT_SCHEME = "content"
        const val PART_SUFFIX = ".part"
        const val SHA_256 = "SHA-256"
        const val COPY_BUFFER_SIZE = 64 * 1024
        const val TAG = "SafPlaybackCache"
        val cacheLock = Any()
    }
}
