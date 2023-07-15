/**
 * # Copyright
 *
 * @author : DENVER(hajun)<fe.dev.denver@gmail.com>
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
import androidx.appcompat.app.AppCompatActivity
import com.samsung.android.sdk.healthdata.HealthConstants.StepCount
import com.samsung.android.sdk.healthdata.HealthDataStore
import com.samsung.android.sdk.healthdata.HealthDataStore.ConnectionListener
import com.samsung.android.sdk.healthdata.HealthPermissionManager
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType
import java.lang.Boolean
import kotlin.Exception
import kotlin.String


// Season 4th Kotlin challenge _ I think I might be a silly...
class MainActivity: AppCompatActivity() {
    var APP_TAG: String = "SamsungHealthTest"

    override fun onCraete(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mStore: HealthDataStore? = null

        /**
         * In official documentation, Samsung developers are using the Java. like...
         * private final HealthDataStore.ConnectionListener mConnectionListener = new HealthDataStore.ConnectionListener() { ... }
         * After new HealthDataStore.ConnectionListener(), we got an anonymous function
         * An anonymous function on Java is equal to an Object on Kotlin
         */

        val mConnectionListener: ConnectionListener = object : ConnectionListener {
            override fun onConnected() {
                Log.d(APP_TAG, "Health data service is connected.")
                val pmsManager = HealthPermissionManager(mStore)
                try {
                    println("Connected to the Samsung Health with using SDK for SH")

                    // Handle the permission, first
                    requestPermission() // Define permission requester explicitly
                } catch (e: Exception) {
                    Log.e(APP_TAG, e.javaClass.name + " - " + e.message)
                    Log.e(APP_TAG, "Permission setting fails.")
                }
            }
        }
    }

    private fun requestPermission() {
        println("Requesting permission...")

        // Permission list that I need
        val stepPermissionKey = PermissionKey(StepCount.HEALTH_DATA_TYPE, PermissionType.READ)
        // val heartRatePermissionKey = PermissionKey(HeartRate.HEALTH_DATA_TYPE, PermissionType.READ)

        // Permission Manager
        val pmsManager = HealthPermissionManager(mStore)

        // Try to request the permissions
        try {
            // Show user permission UI for allowing user to change options
            pmsManager.requestPermissions(mutableSetOf(stepPermissionKey), this)
            // pmsManager.requestPermissions(mutableSetOf(stepPermissionKey, heartRatePermissionKey), this@MainActivity)
            // ⬆️ If I got another permissions need to request
                .setResultListener { result: HealthPermissionManager.PermissionResult ->
                    println("Permission has been requested. setResultListener callback is called.")
                    val resultMap = result.resultMap
                    println(resultMap.entries) // print all of resultMap

                    if (resultMap.containsValue(Boolean.FALSE)) {
                        /**
                         * This if-statement means there are rejected permissions
                         */
//                        updateStepCountView("")
//                        showPermissionAlarmDialog()
                    } else {
                        // Get the current step count and display it
//                        mReporter.start()
                    }
                }
        } catch (e: java.lang.Exception) {
            Log.e(APP_TAG, "Permission setting fails.", e)
            println("Permission setting has been failed")
            println(e)
        }
    }
}
