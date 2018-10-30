package com.chit.chat

import java.util.*

object DateUtil{

    fun dateFromMiliSecSinceEpoch(milliSecondsSinceEpoch: Long) : String{
        val mydate = Calendar.getInstance()
        mydate.timeInMillis = milliSecondsSinceEpoch
        val hourOfDay = mydate.get(Calendar.HOUR_OF_DAY)
        val hourString = if (hourOfDay < 10) "0$hourOfDay" else "" + hourOfDay
        val minute = mydate.get(Calendar.MINUTE)
        val minuteString = if (minute < 10) "0$minute" else "" + minute
        return  hourString + ":" + minuteString + "  (" + mydate.get(Calendar.DAY_OF_MONTH) + "." + (mydate.get(Calendar.MONTH) + 1) + ")"
    }

}

