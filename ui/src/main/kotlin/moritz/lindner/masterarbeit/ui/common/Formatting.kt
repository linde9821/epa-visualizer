package moritz.lindner.masterarbeit.ui.common

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

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

    fun Double.roundToLongSafe(): Long {
        return if (this.isNaN()) 0L
        else this.roundToLong()
    }

    fun Duration.toContextual(): String {
        return when {
            this.toDays() > 365 -> "${this.toDays() / 365}y ${(this.toDays() % 365) / 30}mo"
            this.toDays() > 30 -> "${this.toDays() / 30}mo ${this.toDays() % 30}d"
            this.toDays() > 0 -> "${this.toDays()}d ${this.toHours() % 24}h"
            this.toHours() > 0 -> "${this.toHours()}h ${this.toMinutes() % 60}m"
            this.toMinutes() > 0 -> "${this.toMinutes()}m"
            else -> "${this.seconds}s"
        }
    }
}