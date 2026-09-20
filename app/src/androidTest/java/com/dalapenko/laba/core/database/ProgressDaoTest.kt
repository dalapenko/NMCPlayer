package com.dalapenko.laba.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dalapenko.laba.core.database.entity.BookEntity
import com.dalapenko.laba.core.database.entity.ProgressEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressDaoTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun olderSnapshotCannotOverwriteNewerProgress() = runBlocking {
        val bookId = database.bookDao().insert(
            BookEntity(
                title = "Test book",
                rootFolderUri = "content://test/book",
                totalDurationMs = 120_000L,
            ),
        )
        val dao = database.progressDao()

        dao.upsertIfNewer(
            ProgressEntity(
                bookId = bookId,
                lastTrackId = 2L,
                lastPositionMs = 20_000L,
                lastUpdated = 200L,
            ),
        )
        dao.upsertIfNewer(
            ProgressEntity(
                bookId = bookId,
                lastTrackId = 1L,
                lastPositionMs = 10_000L,
                lastUpdated = 100L,
            ),
        )

        val saved = checkNotNull(dao.getByBook(bookId))
        assertEquals(2L, saved.lastTrackId)
        assertEquals(20_000L, saved.lastPositionMs)
        assertEquals(200L, saved.lastUpdated)
    }
}
