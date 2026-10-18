package com.dalapenko.laba.feature.library

import com.dalapenko.laba.core.data.BookWithProgress
import com.dalapenko.laba.testBook
import com.dalapenko.laba.testProgress
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryFilteringTest {

    @Test
    fun givenBlankQueryAndAllFilter_whenFiltering_thenInputOrderIsPreserved() {
        val books = listOf(book(3L, "Third"), book(1L, "First"), book(2L, "Second"))

        assertEquals(books, filterLibraryBooks(books, "  ", LibraryFilter.ALL))
    }

    @Test
    fun givenQuery_whenFiltering_thenTitleAndAuthorMatchCaseInsensitively() {
        val books = listOf(
            book(1L, "The Amber Road", author = "Writer One"),
            book(2L, "Blue Sky", author = "MIRA STONE"),
            book(3L, "Green Field", author = null),
        )

        assertEquals(listOf(1L), ids(filterLibraryBooks(books, "aMbEr", LibraryFilter.ALL)))
        assertEquals(listOf(2L), ids(filterLibraryBooks(books, "mira stone", LibraryFilter.ALL)))
        assertEquals(listOf(3L), ids(filterLibraryBooks(books, "green", LibraryFilter.ALL)))
        assertEquals(emptyList<Long>(), ids(filterLibraryBooks(books, "unknown", LibraryFilter.ALL)))
    }

    @Test
    fun givenQueryWithSurroundingWhitespace_whenFiltering_thenWhitespaceIsIgnored() {
        assertEquals(
            listOf(1L),
            ids(filterLibraryBooks(listOf(book(1L, "Moonlight")), "  Moon  ", LibraryFilter.ALL)),
        )
    }

    @Test
    fun givenBooksAtProgressBoundaries_whenFilteringByStatus_thenStatusesMatchContract() {
        val books = listOf(
            book(1L, "Persisted complete", fraction = 0.4f, completed = true),
            book(2L, "Fraction complete", fraction = 1f),
            book(3L, "In progress", fraction = 0.5f),
            book(4L, "Zero progress", fraction = 0f),
            book(5L, "No progress row", fraction = 0f, includeProgress = false),
        )

        assertEquals(listOf(1L, 2L), ids(filterLibraryBooks(books, "", LibraryFilter.COMPLETED)))
        assertEquals(listOf(3L), ids(filterLibraryBooks(books, "", LibraryFilter.IN_PROGRESS)))
        assertEquals(listOf(4L, 5L), ids(filterLibraryBooks(books, "", LibraryFilter.NOT_STARTED)))
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), ids(filterLibraryBooks(books, "", LibraryFilter.ALL)))
    }

    @Test
    fun givenQueryAndStatus_whenFiltering_thenBothConditionsMustMatch() {
        val books = listOf(
            book(1L, "Moonlight", fraction = 0.2f),
            book(2L, "Moonlight", fraction = 0f),
            book(3L, "Sunrise", fraction = 0.7f),
        )

        assertEquals(listOf(1L), ids(filterLibraryBooks(books, "moon", LibraryFilter.IN_PROGRESS)))
        assertEquals(listOf(2L), ids(filterLibraryBooks(books, "MOON", LibraryFilter.NOT_STARTED)))
        assertEquals(emptyList<Long>(), ids(filterLibraryBooks(books, "sun", LibraryFilter.COMPLETED)))
    }

    @Test
    fun givenQueryWithRussianEVariants_whenFiltering_thenTitleAndAuthorMatchBothDirections() {
        val books = listOf(
            book(1L, "Ёлка", author = null),
            book(2L, "Other", author = "Ёжов"),
            book(3L, "Ель", author = null),
            book(4L, "Other", author = "Егоров"),
        )

        assertEquals(listOf(1L, 3L), ids(filterLibraryBooks(books, "ел", LibraryFilter.ALL)))
        assertEquals(listOf(2L), ids(filterLibraryBooks(books, "еж", LibraryFilter.ALL)))
        assertEquals(listOf(1L, 3L), ids(filterLibraryBooks(books, "ёл", LibraryFilter.ALL)))
        assertEquals(listOf(4L), ids(filterLibraryBooks(books, "ёг", LibraryFilter.ALL)))
    }

    private fun book(
        id: Long,
        title: String,
        author: String? = "Author",
        fraction: Float = 0f,
        completed: Boolean = false,
        includeProgress: Boolean = true,
    ): BookWithProgress = BookWithProgress(
        book = testBook(id = id, title = title, author = author),
        progress = if (includeProgress) testProgress(bookId = id, isCompleted = completed) else null,
        progressFraction = fraction,
    )

    private fun ids(books: List<BookWithProgress>): List<Long> = books.map { it.book.id }
}
