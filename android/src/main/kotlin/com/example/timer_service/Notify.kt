package com.example.timer_service

import android.app.Notification
import android.util.Log
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import android.graphics.BitmapFactory
import android.app.NotificationChannel

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationService.COUNTER_CHANNEL_ID,
                "Timer",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Used for task timer foreground Updates"
            notificationManager.createNotificationChannel(channel)
        }
    }
    //TODO:change.setSmallIcon(R.drawable.)

    fun getTimerNotification(title: String,info:String,isPaused:Boolean=false): Notification {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val activityPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val pauseIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, NotificationReceiver::class.java).apply {
                putExtra("ACTION", "PAUSE")}
            ,
            PendingIntent.FLAG_IMMUTABLE
        )
         val stopIntent = PendingIntent.getBroadcast(
            context,
            3,
            Intent(context, NotificationReceiver::class.java).apply {
                putExtra("ACTION", "STOP")}
            ,
            PendingIntent.FLAG_IMMUTABLE
        )
         val resumeIntent = PendingIntent.getBroadcast(
            context,
            4,
            Intent(context, NotificationReceiver::class.java).apply {
                putExtra("ACTION", "RESUME")}
            ,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = NotificationCompat.Builder(context, COUNTER_CHANNEL_ID)
        //TODO change
            .setSmallIcon(R.drawable.play)
            .setContentTitle(title)
            .setContentText(info)
            .setContentIntent(activityPendingIntent)
           // .setContent(RemoteViews("com.DreamTech.prome", R.layout.notification))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            //.setStyle()
            if (isPaused) {
                notificationBuilder.addAction(
                    R.drawable.play,
                    "Resume",
                    resumeIntent
                )
            }else 
            {
                notificationBuilder.addAction(
                    R.drawable.pause,
                    "Pause",
                    pauseIntent
                )
            } 
            notificationBuilder.addAction(
                R.drawable.stop,
                "Stop",
                stopIntent,
                // showsUserInterface: true,
            )     
       val notification =  notificationBuilder.build()
       return notification
    }
    
    fun showNotification(notId:Int,notification: Notification) {
        notificationManager.notify(notId,notification)
    }
    
    fun cancelNotification() {
        notificationManager.cancelAll()
    } 
    
    companion object {
        const val COUNTER_CHANNEL_ID = "timer_channel"
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.getStringExtra("ACTION")
        when (action) {
            "PAUSE" -> {
                val serviceIntent = Intent(context, TimerService::class.java)
                serviceIntent.action = "PAUSE"
                 context.startService(serviceIntent)
            }
            "STOP" -> {
                context.stopService(Intent(context, TimerService::class.java))
                NotificationService(context).cancelNotification()
            }
            "RESUME" -> {
                val serviceIntent = Intent(context, TimerService::class.java)
                serviceIntent.action = "RESUME"
                context.startService(serviceIntent)
            }
            else ->{
                Log.d("NotificationReceiver","Unknown action")
            }
        }
       
        
    }
}
