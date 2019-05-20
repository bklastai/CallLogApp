package com.bklastai.calllogapp.presenters

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.bklastai.calllogapp.R

class MessageViewController(private val errorTV: TextView, private val callback: MessageViewClickListener) {
    private val emptyCallLogMessage = errorTV.context.resources.getString(R.string.no_call_logs_detected)
    private val permissionRationale = errorTV.context.resources.getString(R.string.call_logs_permission_rational)
    private val errorText = errorTV.context.resources.getString(R.string.error_swipe_to_refresh)

    enum class MesssageState {
        EmptyList,
        PermissionNeeded,
        Error,
        Invisible
    }

    interface MessageViewClickListener {
        fun onMessageViewClicked(state: MesssageState)
    }

    fun showEmptyListMessage() {
        errorTV.visibility = VISIBLE
        errorTV.text = emptyCallLogMessage
        errorTV.setOnClickListener(null)
    }

    fun showPermissionRationale() {
        errorTV.visibility = VISIBLE
        errorTV.text = permissionRationale
        errorTV.setOnClickListener { callback.onMessageViewClicked(MesssageState.PermissionNeeded) }
    }

    fun showErrorText() {
        errorTV.visibility = VISIBLE
        errorTV.text = errorText
        errorTV.setOnClickListener(null)
    }

    fun clear() {
        errorTV.visibility = GONE
        errorTV.setOnClickListener(null)
    }
}