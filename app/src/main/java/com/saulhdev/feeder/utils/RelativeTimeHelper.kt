package com.saulhdev.feeder.utils

import android.content.Context
import com.saulhdev.feeder.R
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

object RelativeTimeHelper {
    private val calendar: Calendar
        get() = Calendar.getInstance()

    private val currentTime: Long
        get() = System.currentTimeMillis() / 1000

    private fun getAbsoluteDate(context: Context, i: Long): String {
        val resources = context.resources
        val j = i * 1000
        val calendar = calendar
        val i2 = calendar[Calendar.YEAR]
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        val timeInMillis = calendar.timeInMillis
        var j2 = 86400000.toLong()
        var j3 = timeInMillis + j2
        var j4 = j3 + j2
        j2 = timeInMillis - j2
        calendar.timeInMillis = j
        j4--
        val locale: Locale
        var format: String
        if (j in j3..j4) {
            locale = Locale.ENGLISH
            val atFormat = resources.getString(R.string.date_at_time_FORMAT)
            val tomorrow = resources.getString(R.string.tomorrow)
            val timeOfDay = "${calendar[Calendar.HOUR_OF_DAY]}:${calendar[Calendar.MINUTE]}"
            format = String.format(locale, atFormat, tomorrow, timeOfDay)
            return format
        }
        j3--
        return when {
            j in timeInMillis..j3       -> {
                locale = Locale.ENGLISH
                val atFormat = resources.getString(R.string.date_at_time_FORMAT)
                val today = resources.getString(R.string.today)
                val timeOfDay = "${calendar[Calendar.HOUR_OF_DAY]}:${calendar[Calendar.MINUTE]}"
                format = String.format(locale, atFormat, today, timeOfDay)
                format
            }

            j < j2 || j >= timeInMillis -> {
                val string: String = if (calendar[Calendar.YEAR] != i2) {
                    resources.getString(
                        R.string.date_format_day_month_year,
                        calendar[Calendar.DAY_OF_MONTH],
                        resources.getStringArray(R.array.date_shortmonths)[min(
                            calendar[2],
                            Calendar.HOUR_OF_DAY
                        )],
                        calendar[Calendar.YEAR]
                    )
                } else {
                    resources.getString(
                        R.string.date_format_day_month,
                        calendar[Calendar.DAY_OF_MONTH],
                        resources.getStringArray(R.array.date_shortmonths)[min(
                            calendar[2],
                            Calendar.HOUR_OF_DAY
                        )]
                    )
                }
                locale = Locale.ENGLISH
                val atFormat = resources.getString(R.string.date_at_time_FORMAT)
                val timeOfDay = "${calendar[Calendar.HOUR_OF_DAY]}:${calendar[Calendar.MINUTE]}"
                format = String.format(locale, atFormat, string, timeOfDay)
                format
            }

            else                        -> {
                locale = Locale.ENGLISH
                val atFormat = resources.getString(R.string.date_at_time_FORMAT)
                val yesterday = resources.getString(R.string.yesterday)
                val timeOfDay = "${calendar[Calendar.HOUR_OF_DAY]}:${calendar[Calendar.MINUTE]}"
                format = String.format(locale, atFormat, yesterday, timeOfDay)
                format
            }
        }
    }

    /**
     * @param context - Context to use with resources
     * @param i - Time in seconds (Unix timestamp)
     * @return Time relative to this
     */
    fun getDateFormattedRelative(context: Context, i: Long): String {
        val resources = context.resources
        val currentTime = currentTime - i
        if (currentTime >= 14400 || currentTime < 0) {
            return getAbsoluteDate(context, i)
        }
        return when {
            currentTime >= 10800 -> {
                resources.getStringArray(R.array.date_ago_hrs)[2]
            }

            currentTime >= 7200  -> {
                resources.getStringArray(R.array.date_ago_hrs)[1]
            }

            currentTime >= 3600  -> {
                resources.getStringArray(R.array.date_ago_hrs)[0]
            }

            currentTime >= 60    -> {
                resources.getQuantityString(
                    R.plurals.date_ago_mins,
                    (currentTime / 60).toInt(),
                    (currentTime / 60).toInt()
                )
            }

            currentTime <= 10    -> {
                resources.getString(R.string.date_ago_now)
            }

            else                 -> {
                resources.getQuantityString(
                    R.plurals.date_ago_secs,
                    currentTime.toInt(),
                    currentTime.toInt()
                )
            }
        }
    }
}