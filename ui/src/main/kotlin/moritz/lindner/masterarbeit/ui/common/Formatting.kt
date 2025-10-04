package moritz.lindner.masterarbeit.ui.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Formatting {
    fun Long.asFormattedLocalDateTime(): String {
        val localDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(this),
            ZoneId.systemDefault()
        )
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        val formattedDate = localDateTime.format(formatter)

        return formattedDate
    }

    fun formatDuration(minutes: Double): String {
        return when {
            minutes < 1 -> "${(minutes * 60).toInt()}s"
            minutes < 60 -> "${minutes.toInt()}m"
            else -> {
                val hours = (minutes / 60).toInt()
                val mins = (minutes % 60).toInt()
                "${hours}h ${mins}m"
            }
        }
    }

    fun formatTimeSpan(start: Long, end: Long): String {
        val diff = end - start
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
}