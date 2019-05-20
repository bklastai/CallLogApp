package com.bklastai.calllogapp.data

import android.database.Cursor
import android.os.Build
import android.provider.CallLog
import android.telephony.PhoneNumberUtils
import android.text.format.DateFormat
import java.util.Locale
import java.util.Date

val callTypes = arrayOf("Incoming", "Outgoing", "Missed")

data class CallLogEntry(val number: String, val timestamp: String, val type: String)

fun callLogEntryFromCursor(cursor: Cursor): CallLogEntry {
    val unformattedNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
    val number = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        PhoneNumberUtils.formatNumber(unformattedNumber, Locale.getDefault().country)
    } else {
        PhoneNumberUtils.formatNumber(unformattedNumber)
    }

    val nonFormattedDate = Date(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)))
    val date = DateFormat.format("h:mm a MM/dd/yy", nonFormattedDate).toString()

    val typeInt = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))
    val type = if (typeInt in 1..3) callTypes[typeInt -1] else "N/A"

    return CallLogEntry(number, date, type)
}
