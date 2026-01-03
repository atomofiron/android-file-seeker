package app.atomofiron.searchboxapp.screens.main.util

import android.icu.util.Calendar

object EasterEggPeriods {

    val halloween = period(Calendar.OCTOBER, 31, Calendar.NOVEMBER, 1)
    val clown = period(Calendar.APRIL, 1, Calendar.APRIL, 1)
    val oldYear = period(Calendar.DECEMBER, 31, Calendar.DECEMBER, 31)
    val newYear = period(Calendar.JANUARY, 1, Calendar.JANUARY, 1)

    private fun period(fromMonth: Int, fromDate: Int, toMonth: Int, toDate: Int, yearDif: Int = 0): ClosedRange<Calendar> {
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, get(Calendar.YEAR) + yearDif)
            set(Calendar.MONTH, fromMonth)
            set(Calendar.DAY_OF_MONTH, fromDate)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            set(Calendar.YEAR, get(Calendar.YEAR) + yearDif)
            set(Calendar.MONTH, toMonth)
            set(Calendar.DAY_OF_MONTH, toDate)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        return start..end
    }

    fun updateYear(now: Calendar) {
        val current = now.get(Calendar.YEAR)
        halloween.start.set(Calendar.YEAR, current)
        halloween.endInclusive.set(Calendar.YEAR, current)
        clown.start.set(Calendar.YEAR, current)
        clown.endInclusive.set(Calendar.YEAR, current)
        oldYear.start.set(Calendar.YEAR, current)
        oldYear.endInclusive.set(Calendar.YEAR, current)
        newYear.start.set(Calendar.YEAR, current)
        newYear.endInclusive.set(Calendar.YEAR, current)
    }
}