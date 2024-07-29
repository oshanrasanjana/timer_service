import 'dart:async';

import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:timer_service/supporter.dart';

import 'timer_service_method_channel.dart';

abstract class TimerServicePlatform extends PlatformInterface {
  TimerServicePlatform() : super(token: _token);

  static final Object _token = Object();

  static TimerServicePlatform _instance = MethodChannelTimerService();

  static TimerServicePlatform get instance => _instance;

  static set instance(TimerServicePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<Stream<dynamic>?> startTimer(Map<String, dynamic> args);
  Future<Stream<dynamic>?> getStreamer();
  Future<void> stopTimer();
  Future<void> pauseTimer();
  Future<void> resumeTimer();
  Future<void>endBackgroundOperation();
  Future<RunningData> getFinalTimerData();
}
