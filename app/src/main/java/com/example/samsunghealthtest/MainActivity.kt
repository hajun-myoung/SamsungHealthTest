/**
 * # Copyright
 *
 * @author : DENVER(hajun)<fe.dev.denver@gmail.com> at AIT Studio
 * @message1 : Thanks to Samsung, but...could you please give me a official docus with Kotlin version...?
 * @message2 : Well, I know...I am a silly...
 * @message3 : But I beat you, SAMSUNG! Ha-hA!
 *
 * ## References
 *
 * 1. Official Documentation
 *    @author : Samsung Electronics Co., Ltd.
 *    @link : https://developer.samsung.com/health/android/data/guide/hello-health-data.html
 *    @feedback : It's wrote by Java. You need to convert it. But don't worry! Android Studio did that instead of you.
 *
 * 2. A personal repository
 *    @author : jgch88
 *    @repository : SamsungHealthSpike
 *    @feedback : Good to understand SDK for Samsung Health, specially about the Kotlin-version
 *                It's a quite simple and easy to read code
 */

package com.example.samsunghealthtest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.samsung.android.sdk.healthdata.*
//import com.samsung.android.sdk.healthdata.HealthConstants.HeartRate
import com.samsung.android.sdk.healthdata.HealthConstants.StepCount
import com.samsung.android.sdk.healthdata.HealthDataStore.ConnectionListener
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType
import java.lang.Boolean
import java.util.*
import kotlin.Exception
import kotlin.Long
import kotlin.Number
import kotlin.String
import kotlin.arrayOf

// Season 4th Kotlin challenge _ I think I might be a silly...
class MainActivity: AppCompatActivity() {
    var APP_TAG: String = "SamsungHealthTest"

    private var mStore: HealthDataStore? =  null // Data will be store here
    private var mResolver: HealthDataResolver? = null

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * In official documentation, Samsung developers are using the Java. like...
         * private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() { ... }
         * After new HealthDataStore.ConnectionListener(), we got an anonymous function
         * An anonymous function on Java is equal to an Object on Kotlin
         */

        // CAUTION! In the Kotlin, function() { ... } structure doesn't mean the scope itself.
        // It's an anonymous function { ... } like a lambda expression in Python or () => {} in JS

        mStore = HealthDataStore(this, object : ConnectionListener {
            override fun onConnectionFailed(p0: HealthConnectionErrorResult?) {
                Log.e(APP_TAG, "Connection Failed to Samsung Health")
                if (p0 != null) {
                    Log.e(APP_TAG, p0?.errorCode.toString())
                }
            }

            override fun onConnected() {
                Log.d(APP_TAG, "Health data service is connected.")
                // Handle the permission, first
                if(isPermissionAcquired()){
                    Log.d(APP_TAG, "Permission is acquired. Try to get some data.")
                    getStepCount()
                }
                else{
                    requestPermission() // Define permission requester explicitly
                }
            }

            override fun onDisconnected() {
                Log.d(APP_TAG, "Disconnected from the SH")
            }
        })

        mStore?.connectService()
    }

    private fun getStepCount(mode: Number? = 1) {
        if(mode == 1){
            //get today step count
            val startTime = getToday()
            val ONE_DAY_IN_MS = 24 * 60 * 60 * 1000L
            val endTime = startTime + ONE_DAY_IN_MS

            val properties = arrayOf(
                StepCount.COUNT,
                StepCount.CREATE_TIME,
                StepCount.DEVICE_UUID
            )

            val request = HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .setProperties(properties)
                .setLocalTimeRange(StepCount.START_TIME, StepCount.TIME_OFFSET, startTime, endTime)
                .build()

            mResolver = HealthDataResolver(mStore, null)

            try{
                mResolver?.read(request)?.setResultListener { result ->
                    try{
                        val iterator = result.iterator()
                        while(iterator.hasNext()){
                            val data = iterator.next()
                            val totalCount = data.getInt(StepCount.COUNT)
                            val uuid = data.getString(StepCount.DEVICE_UUID)
                            val time = data.getFloat(StepCount.CREATE_TIME)
                            Log.d(APP_TAG, "$time $totalCount $uuid")
                        }
                    }
                    finally{
                        result.close()
                    }
                }
            }catch(e: Exception){
                Log.e(APP_TAG, e.toString())
            }
        }
    }

    private fun getToday(): Long {
        val today = Calendar.getInstance(TimeZone.getTimeZone("GMT+9:00"))

        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val result: Long = today.timeInMillis
        val logString = "The time is $result"
        Log.e(APP_TAG, logString)

        return result
    }

    private fun isPermissionAcquired(): kotlin.Boolean {
        val stepPermKey = PermissionKey(StepCount.HEALTH_DATA_TYPE, PermissionType.READ)
        val permissionKeys = setOf(stepPermKey)

        val pmsManager = HealthPermissionManager(mStore)

        try{
            val resultMap = pmsManager.isPermissionAcquired(permissionKeys)
            return !resultMap.containsValue(Boolean.FALSE)
        }
        catch(e: Exception){
            Log.e(APP_TAG, "isPermissionAcquired fail", e)
        }

        return false
    }

    private fun requestPermission() {
        Log.d(APP_TAG, "Requesting permission...")

        // Permission list that I need
        val stepPermissionKey = PermissionKey(StepCount.HEALTH_DATA_TYPE, PermissionType.READ)
//        val heartRatePermissionKey = PermissionKey(HeartRate.HEALTH_DATA_TYPE, PermissionType.READ)

        // Show user permission UI for allowing user to change options
//        val permissionKeys = setOf(stepPermissionKey, heartRatePermissionKey)
        val permissionKeys = setOf(stepPermissionKey)

        // Permission Manager
        val pmsManager = HealthPermissionManager(mStore)

        // Try to request the permissions
        try {
            pmsManager.requestPermissions(permissionKeys, this)
                .setResultListener { result ->
                    Log.d(APP_TAG, "Permission has been requested. setResultListener callback is called.")
                    val resultMap = result.resultMap

                    val newAlert = AlertDialog.Builder(this)
                    newAlert.setTitle("Notice")
                        .setMessage(resultMap.toString())
                        .setPositiveButton("OK", null)
                        .show()

                    if (resultMap.containsValue(Boolean.FALSE)) {
                        // When be rejected some permissions
                        showPermissionAlarmDialog()
                    }
                }
        } catch (e: java.lang.Exception) {
            Log.e(APP_TAG, "Permission setting has been failed", e)
        }

    }

    private fun showPermissionAlarmDialog()  {
        if(isFinishing) {
            return
        }

        val newAlert = AlertDialog.Builder(this)
        newAlert.setTitle("Notice")
            .setMessage("steps should be acquired")
            .setPositiveButton("OK", null)
            .show()
    }
}
