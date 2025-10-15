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
}