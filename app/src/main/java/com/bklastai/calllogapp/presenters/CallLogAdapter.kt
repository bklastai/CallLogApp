package com.bklastai.calllogapp.presenters

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bklastai.calllogapp.R
import com.bklastai.calllogapp.data.callLogEntryFromCursor

class CallLogAdapter(mContext: Context, mCursor: Cursor?) :
    CursorRecyclerViewAdapter<CallLogAdapter.MyViewHolder>(mContext, mCursor) {

    override fun onBindViewHolder(viewHolder: MyViewHolder, cursor: Cursor) {
        val myListItem = callLogEntryFromCursor(cursor)
        viewHolder.numTV.text = myListItem.number
        viewHolder.timeTV.text = myListItem.timestamp
        viewHolder.directionTV.text = myListItem.type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.call_log_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numTV: TextView = itemView.findViewById(R.id.number)
        val timeTV: TextView = itemView.findViewById(R.id.call_timestamp)
        val directionTV: TextView = itemView.findViewById(R.id.call_direction)
    }

    override fun getItemCount(): Int {
        return Math.min(super.getItemCount(), 50)
    }
}