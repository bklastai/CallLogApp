package com.bklastai.calllogapp.presenters

import android.content.Context
import android.database.Cursor
import android.database.DataSetObserver
import android.provider.CallLog
import androidx.recyclerview.widget.RecyclerView

/* Attribution: https://gist.github.com/skyfishjy/443b7448f59be978bc59 */

abstract class CursorRecyclerViewAdapter<VH : RecyclerView.ViewHolder>(val mContext: Context,
                   private var cursor: Cursor?) : RecyclerView.Adapter<VH>() {
    var mDataValid: Boolean = false

    private var mRowIdColumn: Int = 0

    private val mDataSetObserver: DataSetObserver?

    init {
        mDataValid = cursor != null
        mRowIdColumn = if (mDataValid) this.cursor!!.getColumnIndex(CallLog.Calls._ID) else -1
        mDataSetObserver = NotifyingDataSetObserver()
        cursor?.registerDataSetObserver(mDataSetObserver)
        apply {
            setHasStableIds(true)
        }
    }

    override fun getItemCount(): Int {
        return if (mDataValid && cursor != null) cursor!!.count else 0
    }

    override fun getItemId(position: Int): Long {
        return if (mDataValid && cursor != null && cursor!!.moveToPosition(position))
            cursor!!.getLong(mRowIdColumn) else 0
    }

    abstract fun onBindViewHolder(viewHolder: VH, cursor: Cursor)

    override fun onBindViewHolder(viewHolder: VH, position: Int) {
        if (!mDataValid || cursor == null) {
            throw IllegalStateException("this should only be called when the cursor is valid")
        }
        if (!cursor!!.moveToPosition(position)) {
            throw IllegalStateException("couldn't move cursor to position $position")
        }
        onBindViewHolder(viewHolder, cursor!!)
    }

    fun changeCursor(cursor: Cursor?) {
        val old = swapCursor(cursor)
        old?.close()
    }

    private fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor === cursor) {
            return null
        }
        val oldCursor = cursor
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver)
        }
        cursor = newCursor
        if (cursor != null) {
            if (mDataSetObserver != null) {
                cursor!!.registerDataSetObserver(mDataSetObserver)
            }
            mRowIdColumn = newCursor!!.getColumnIndexOrThrow(CallLog.Calls._ID)
            mDataValid = true
            notifyDataSetChanged()
        } else {
            mRowIdColumn = -1
            mDataValid = false
            notifyDataSetChanged()
        }
        return oldCursor
    }

    private inner class NotifyingDataSetObserver : DataSetObserver() {
        override fun onChanged() {
            super.onChanged()
            mDataValid = true
            notifyDataSetChanged()
        }

        override fun onInvalidated() {
            super.onInvalidated()
            mDataValid = false
            notifyDataSetChanged()
        }
    }
}