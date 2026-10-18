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
    return books.filter { item ->
        val matchesQuery = normalizedQuery.isEmpty() ||
            item.book.title.containsRussianEquivalent(normalizedQuery) ||
            item.book.author?.containsRussianEquivalent(normalizedQuery) == true
        val isCompleted = item.progress?.isCompleted == true || item.progressFraction >= 1f
        val matchesFilter = when (filter) {
            LibraryFilter.ALL -> true
            LibraryFilter.IN_PROGRESS -> !isCompleted && item.progressFraction > 0f
            LibraryFilter.NOT_STARTED -> !isCompleted && item.progressFraction <= 0f
            LibraryFilter.COMPLETED -> isCompleted
        }
        matchesQuery && matchesFilter
    }
}

private fun String.containsRussianEquivalent(query: String): Boolean =
    query.length <= length &&
        (0..(length - query.length)).any { start ->
            query.indices.all { index ->
                get(start + index).matchesRussianEquivalent(query[index])
            }
        }

private fun Char.matchesRussianEquivalent(other: Char): Boolean {
    if (equals(other, ignoreCase = true)) return true
    val lower = lowercaseChar()
    val otherLower = other.lowercaseChar()
    return (lower == 'е' || lower == 'ё') && (otherLower == 'е' || otherLower == 'ё')
}
