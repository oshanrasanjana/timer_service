package com.example.timer_service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.*
import android.content.Context
import io.flutter.plugin.common.EventChannel
import android.os.Handler
import android.os.Looper


class TimerService : Service() {
    private var job: Job? = null
    private var startTime: Long = 0
    private var stopSeconds: Int = 0
    private var secondGlob: Int = 0
    private var elapsedPausedTime: Long = 0 // Time when paused
    private var isPaused: Boolean = false
    private var title: String = "Task Started"
    private var info: String = ""
    private var userdata: String = ""
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        Log.d("TimerService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START"-> {
            title = if( intent.getStringExtra("title")?:"null" == "null") "Task Started" else intent.getStringExtra("title")?:"Task Started"
            info = if( intent.getStringExtra("info")?:"null" == "null") "" else intent.getStringExtra("info")?:""
            userdata = if( intent.getStringExtra("userdata")?:"null" == "null") "" else intent.getStringExtra("userdata")?:""
            stopSeconds = intent.getIntExtra("stopseconds", 0)
            startService() 
            } 
            "PAUSE" -> pauseService()
            "RESUME" -> resumeService()
            "UPDATE" -> {
            userdata = if( intent.getStringExtra("userdata")?:"null" == "null") "" else intent.getStringExtra("userdata")?:""
            }
        }
        return START_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()
        stopCounting()
        sendUpdateToFlutter("ENDED")
        NativeCallbackInvoker.invokeCallback(this, Holder.callback)
        Log.d("TimerService", "Service destroyed")
    }

    private fun startService() {
        val notification =NotificationService(this).getTimerNotification(title,"Timer Started $info")
        startForeground(1, notification)
        startTime = SystemClock.elapsedRealtime()
        startCounting()
    }
    private fun checkCondition() {
        if (secondGlob >= stopSeconds + 5) {
            stopService()
        }
    }

    private fun stopService() {
        NotificationService(this).cancelNotification()
        stopForeground(true)
        stopSelf()
    }


    private fun startCounting() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                if (!isPaused) {
                    val elapsedTime = SystemClock.elapsedRealtime() - startTime
                    val seconds = (elapsedTime / 1000).toInt()
                    secondGlob=seconds
                    updateNotification()
                    sendUpdateToFlutter("RUNNING") 
                    checkCondition()
                    delay(1000)

                }
            }
        }
    }

    private fun stopCounting() {
        job?.cancel()
    }

    private fun pauseService() {
        
        isPaused = true
        elapsedPausedTime = SystemClock.elapsedRealtime() - startTime
        sendUpdateToFlutter("PAUSED")
        updateNotification(true)
    }

    private fun resumeService() {
        sendUpdateToFlutter("RESUMED")
        isPaused = false
        startTime = SystemClock.elapsedRealtime() - elapsedPausedTime
        
    }

    private fun updateNotification(isPaused:Boolean =false) {
        val notificationService = NotificationService(this)
        val formattedTime = formatTime(secondGlob)
        notificationService.showNotification(1,notificationService.getTimerNotification(title,formattedTime,isPaused))
    }

    private fun sendUpdateToFlutter(status:String) {
      mainHandler.post {
          val data = mapOf(
                "data" to userdata,
                "time" to secondGlob,
                "status" to status
            )
        if(status=="ENDED"){
            Holder.data=data
        }
        Holder.eventSink?.success(data)
        }
    }
    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
