package com.udc.apptfg.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Util {
    companion object{
        fun getTodayDate(): String {
            val currentDate = Date()
            val formatter = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
            return formatter.format(currentDate)
        }
    }
}