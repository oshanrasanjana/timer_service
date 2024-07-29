import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:timer_service/supporter.dart';

import 'timer_service_platform_interface.dart';

class MethodChannelTimerService extends TimerServicePlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('timer_service');
  final eventChannel = const EventChannel('timer_service_events');

  @override
  Future<Stream<dynamic>?> startTimer(Map<String, dynamic> args) async {
    await methodChannel.invokeMethod('startTimer', args);
    return eventChannel.receiveBroadcastStream();
  }

  @override
  Future<void> stopTimer() async {
    await methodChannel.invokeMethod('stopTimer');
  }

  @override
  Future<Stream<dynamic>?> getStreamer() async {
    return eventChannel.receiveBroadcastStream();
  }

  @override
  Future<void> pauseTimer() async {
    await methodChannel.invokeMethod('pauseTimer');
  }

  @override
  Future<void> resumeTimer() async {
    await methodChannel.invokeMethod('resumeTimer');
  }

  @override
  Future<void> endBackgroundOperation() async {
    await methodChannel.invokeMethod('closeCHN');
  }

  @override
  Future<RunningData> getFinalTimerData() async {
    final completer = Completer<RunningData>();
    methodChannel.invokeMethod('final_data', {}).then((value) {
      completer.complete(RunningData(
        data: value["data"] == "null" ? null : json.decode(value["data"] ?? ""),
        seconds: value["time"] as int,
        status: "ENDED",
      ));
    });
    return completer.future;
  }
}
