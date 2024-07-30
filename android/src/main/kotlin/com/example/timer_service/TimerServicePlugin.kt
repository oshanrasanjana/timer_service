package com.example.timer_service

import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.EventChannel
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.view.FlutterCallbackInformation
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
//import io.flutter.plugin.GeneratedPluginRegistrant

class TimerServicePlugin: FlutterPlugin, MethodCallHandler {

  private lateinit var channel: MethodChannel
  private lateinit var eventChannel: EventChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "timer_service")
    channel.setMethodCallHandler(this)

    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "timer_service_events")
    eventChannel.setStreamHandler(object : EventChannel.StreamHandler {
      override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        Holder.eventSink = events
      }

      override fun onCancel(arguments: Any?) {
        Holder.eventSink = null
      }
    })
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "startTimer" -> {
        try {
          val arg = JSONObject(call.arguments as Map<String, String>)
          val title = arg.getString("title")
          val callback = arg.getLong("callback")?:null
          val info = arg.getString("info")
          val userdata = arg.getString("data")
          val serviceIntent = Intent(context, TimerService::class.java)
          serviceIntent.action = "START"
          serviceIntent.putExtra("title", title)
          serviceIntent.putExtra("info", info)
          serviceIntent.putExtra("userdata", userdata)
          Holder.callback = callback
          context.startService(serviceIntent)
          result.success(null)
        } catch (e: Exception) {
          val error = e.toString()
          result.success("Something went wrong: $error")
        }
      }
      "updateData" -> {
        val arg = JSONObject(call.arguments as Map<String, String>)
        val userdata = arg.getString("data")
        val serviceIntent = Intent(context, TimerService::class.java)
        serviceIntent.action = "UPDATE"
        serviceIntent.putExtra("userdata", userdata)
        context.startService(serviceIntent)
        result.success(null)
      }
      "stopTimer" -> {
        context.stopService(Intent(context, TimerService::class.java))
        NotificationService(context).cancelNotification()
        result.success(null)
      }
      "pauseTimer" -> {
        val serviceIntent = Intent(context, TimerService::class.java)
        serviceIntent.action = "PAUSE"
        context.startService(serviceIntent)
        result.success(null)
      }
      "resumeTimer" -> {
        val serviceIntent = Intent(context, TimerService::class.java)
        serviceIntent.action = "RESUME"
        context.startService(serviceIntent)
        result.success(null)
      }
      "final_data" -> {
        result.success(Holder.data)
      }
      "closeCHN" -> {
        context.stopService(Intent(context, TimerService::class.java))
        Holder.engine?.destroy()
        Holder.engine = null
        result.success(null)
      }
      "initialize" -> {
        NotificationService(context).createNotificationChannel()
        result.success(null)
      }
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    eventChannel.setStreamHandler(null)
  }
}


object Holder {
  var eventSink: EventChannel.EventSink? = null
  var callback: Long? =null
  var engine : FlutterEngine?=null
  var data :Map<String, Any>? =null
}

object NativeCallbackInvoker {
  private const val CHANNEL = "taskTimer"
  fun invokeCallback(context: Context, callbackHandle: Long?) {
      if (callbackHandle != null) {
          synchronized(this) {
              Holder.engine?.destroy()
              Holder.engine = FlutterEngine(context)
          }
          var flutterLoader = FlutterLoader()

          if (!flutterLoader.initialized()) {
          flutterLoader.startInitialization(context)
          }
        
          flutterLoader.ensureInitializationCompleteAsync(
              context,
              null,
              Handler(Looper.getMainLooper())
              ) {
                  var engine =Holder.engine
                  val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
                  engine?.let { engine ->  
                     // GeneratedPluginRegistrant.registerWith(engine)
                      engine.dartExecutor.executeDartCallback(
                      DartExecutor.DartCallback(
                          context.assets,
                          flutterLoader.findAppBundlePath(),
                          callbackInfo
                          )
                      )
                   }
              }
          }
  }
}

