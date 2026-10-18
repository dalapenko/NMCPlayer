package com.dalapenko.laba.feature.library

import com.dalapenko.laba.core.data.BookWithProgress

enum class LibraryFilter {
    ALL,
    IN_PROGRESS,
    NOT_STARTED,
    COMPLETED,
}

fun filterLibraryBooks(
    books: List<BookWithProgress>,
    query: String,
    filter: LibraryFilter,
): List<BookWithProgress> {
    val normalizedQuery = query.trim()
    return books.filter { book ->
        val matchesQuery = normalizedQuery.isEmpty() ||
            book.book.title.contains(normalizedQuery, ignoreCase = true) ||
            book.book.author?.contains(normalizedQuery, ignoreCase = true) == true
        val isCompleted = book.progress?.isCompleted == true || book.progressFraction >= 1f
        val matchesFilter = when (filter) {
            LibraryFilter.ALL -> true
            LibraryFilter.IN_PROGRESS -> !isCompleted && book.progressFraction > 0f
            LibraryFilter.NOT_STARTED -> !isCompleted && !(book.progressFraction > 0f)
            LibraryFilter.COMPLETED -> isCompleted
        }

        matchesQuery && matchesFilter
    }
}
