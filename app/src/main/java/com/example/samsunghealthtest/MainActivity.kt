package com.example.samsunghealthtest

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.samsung.android.sdk.healthdata.*
import com.samsung.android.sdk.healthdata.HealthDataStore.ConnectionListener
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType
import com.samsung.android.sdk.healthdata.HealthResultHolder.ResultListener
import java.lang.Boolean
import kotlin.Any
import kotlin.Exception


class MainActivity : AppCompatActivity() {
    val APP_TAG = "SimpleHealth"

    private var mInstance: MainActivity? = null
    private var mStore: HealthDataStore? = null
    private var mConnError: HealthConnectionErrorResult? = null
    private var mKeySet: Set<PermissionKey>? = null


    // Connect to the health service when the app start
    override fun onCreate(savedInstanceState: Bundle?) {
        // ...
        super.onCreate(savedInstanceState)
        mInstance = this
        mKeySet = HashSet()
        (mKeySet as HashSet<PermissionKey>).add(PermissionKey(HealthConstants.StepCount.HEALTH_DATA_TYPE, PermissionType.READ))
        // Create a HealthDataStore instance and set its listener
        mStore = HealthDataStore(this, mConnectionListener)
        // Request the connection to the health data store
        mStore!!.connectService()
    }

    // End the health data store connection when the activity is destroyed
    override fun onDestroy() {
        mStore!!.disconnectService()
        super.onDestroy()
    }


    private val mConnectionListener: ConnectionListener = object : ConnectionListener {
        override fun onConnected() {
            Log.d(APP_TAG, "Health data service is connected.")
            val pmsManager = HealthPermissionManager(mStore)
            try {
                // Check whether the permissions that this application needs are acquired
                // Request the permission for reading step counts if it is not acquired
                val resultMap = pmsManager.isPermissionAcquired(mKeySet)

                if (resultMap.containsValue(Boolean.FALSE)) {
                    // Request the permission for reading step counts if it is not acquired
                    pmsManager.requestPermissions(mKeySet, this@MainActivity)
                        .setResultListener(mPermissionListener)
                } else {
                    // Get the current step count and display it
                    // ...
                }
                // Get the current step count and display it if data permission is required
                // ...
            } catch (e: Exception) {
                Log.e(APP_TAG, e.javaClass.name + " - " + e.message)
                Log.e(APP_TAG, "Permission setting fails.")
            }
        }

        override fun onConnectionFailed(error: HealthConnectionErrorResult) {
            Log.d(APP_TAG, "Health data service is not available.")
            showConnectionFailureDialog(error)
        }

        override fun onDisconnected() {
            Log.d(APP_TAG, "Health data service is disconnected.")
        }
    }

    private fun showConnectionFailureDialog(error: HealthConnectionErrorResult) {
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        mConnError = error
        var message = "Connection with Samsung Health is not available"
        if (mConnError!!.hasResolution()) {
            message = when (error.errorCode) {
                HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED -> "Please install Samsung Health"
                HealthConnectionErrorResult.OLD_VERSION_PLATFORM -> "Please upgrade Samsung Health"
                HealthConnectionErrorResult.PLATFORM_DISABLED -> "Please enable Samsung Health"
                HealthConnectionErrorResult.USER_AGREEMENT_NEEDED -> "Please agree with Samsung Health policy"
                else -> "Please make Samsung Health available"
            }
        }
        alert.setMessage(message)
        alert.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
            if (mConnError!!.hasResolution()) {
                mConnError!!.resolve(mInstance)
            }
        })
        if (error.hasResolution()) {
            alert.setNegativeButton("Cancel", null)
        }
        alert.show()
    }

    private val mPermissionListener: ResultListener<HealthPermissionManager.PermissionResult> =
        ResultListener<HealthPermissionManager.PermissionResult> { result ->
            Log.d(APP_TAG, "Permission callback is received.")
            val resultMap: Map<PermissionKey, kotlin.Boolean> = result.resultMap
            if (resultMap.containsValue(Boolean.FALSE)) {
                // Requesting permission fails
            } else {
                // Get the current step count and display it
            }
        }
}

