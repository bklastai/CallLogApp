package com.bklastai.calllogapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bklastai.calllogapp.presenters.CallLogAdapter
import com.bklastai.calllogapp.presenters.MessageViewController
import com.bklastai.calllogapp.data.CallLogLoader
import android.net.Uri.fromParts
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.content.Intent
import android.database.Cursor
import android.widget.Toast
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity(), MessageViewController.MessageViewClickListener {
    // request codes
    private val PERMISSIONS_REQUEST_READ_CALL_LOG: Int = 2345

    // views
    private lateinit var swipeToreRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var errorText: TextView

    // presenters
    val listAdapter: CallLogAdapter = CallLogAdapter(this, null)
    private val callLogsLoader = CallLogLoader(this)
    lateinit var messageViewController: MessageViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        messageViewController = MessageViewController(errorText, this)
    }

    private fun initViews() {
        swipeToreRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeToreRefreshLayout.setOnRefreshListener {
            Handler().postDelayed({
                swipeToreRefreshLayout.isRefreshing = false
            }, 2000)

            if (hasCallLogReadPermissions()) {
                callLogsLoader.initLoader()
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_CALL_LOG)) {
                swipeToreRefreshLayout.isRefreshing = false
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CALL_LOG),
                    PERMISSIONS_REQUEST_READ_CALL_LOG)
            }
        }

        val viewManager = LinearLayoutManager(this)
        recyclerView = findViewById<RecyclerView>(R.id.call_log_list).apply {
            setHasFixedSize(true)
            this.layoutManager = viewManager
            this.adapter = listAdapter
        }

        errorText = findViewById(R.id.error_text)
    }

    override fun onStart() {
        super.onStart()
        if (!hasCallLogReadPermissions()) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG)
                && hasAskedForPermission()) {
                messageViewController.showPermissionRationale()
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CALL_LOG),
                    PERMISSIONS_REQUEST_READ_CALL_LOG)
            }
        } else {
            callLogsLoader.initLoader()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_CALL_LOG -> {
                if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    messageViewController.clear()
                    callLogsLoader.initLoader()
                } else {
                    messageViewController.showPermissionRationale()
                }
                getPreferences(Context.MODE_PRIVATE).edit().putBoolean(
                    resources.getString(R.string.has_asked_for_permissions), true).apply()
            }
        }
    }

    override fun onMessageViewClicked(state: MessageViewController.MesssageState) {
        if (state == MessageViewController.MesssageState.PermissionNeeded) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG) &&
                hasAskedForPermission()) {
                val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = fromParts("package", packageName, null)
                startActivityForResult(intent, PERMISSIONS_REQUEST_READ_CALL_LOG)
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_CALL_LOG),
                    PERMISSIONS_REQUEST_READ_CALL_LOG)
            }
        } else {
            throw IllegalStateException("message callback triggered with message state: $state")
        }
    }

    private fun hasCallLogReadPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasAskedForPermission(): Boolean {
        /*
        * We need `hasAskedForPermission()` to differentiate between two scenarios when
        * shouldShowRequestPermissionRationale returns false (i.e. user has never been asked about permissions and user
        * refused and checked the 'Don't ask again' box). In the latter case, requestPermissions stops functioning (as
        * per system design), so the only way to display call logs is to open app prefs and grant the permission through
        * there but we don't want to do that before we asked the user to grant the permission for the very first time.
        * */
        return getPreferences(Context.MODE_PRIVATE).getBoolean(
            resources.getString(R.string.has_asked_for_permissions), false)
    }

    fun onLoadFinished(data: Cursor?) {
        swipeToreRefreshLayout.isRefreshing = false
        when {
            data == null -> messageViewController.showErrorText()
            data.count == 0 -> messageViewController.showEmptyListMessage()
            else -> {
                messageViewController.clear()
                val wasEmpty = listAdapter.itemCount == 0
                if (!wasEmpty) {
                    Toast.makeText(this, resources.getString(R.string.updated), Toast.LENGTH_SHORT).show()
                }
            }
        }
        listAdapter.changeCursor(data)
    }
}
