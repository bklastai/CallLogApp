package com.bklastai.calllogapp.data

import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.bklastai.calllogapp.MainActivity

class CallLogLoader(private val mainActivity: MainActivity): LoaderManager.LoaderCallbacks<Cursor> {
    private val LOADER_ID = 123
    private val projection = arrayOf(CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE)

    fun initLoader() {
        LoaderManager.getInstance(mainActivity).restartLoader(LOADER_ID, null,this)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        when (id) {
            LOADER_ID -> {
                // most recent calls at the top
                val filterQuery = CallLog.Calls.DATE + " DESC"

                // at most 50 call logs, CallLogAdapter limits its size to 50 as well but this speeds up the query
                val uri = CallLog.Calls.CONTENT_URI.buildUpon().appendQueryParameter("limit", "50").build()

                return CursorLoader(mainActivity, uri, projection, null, null, filterQuery)
            } else -> {
                throw IllegalAccessException("Unrecognized loader id $id")
            }
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        mainActivity.onLoadFinished(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}
}