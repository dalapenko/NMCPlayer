package com.dalapenko.laba.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Library : NavKey

@Serializable
data class Player(val bookId: Long, val autoPlay: Boolean = true) : NavKey

@Serializable
data class PlayerFiles(val bookId: Long) : NavKey

@Serializable
data object Settings : NavKey
