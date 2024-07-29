import 'dart:async';
import 'dart:ui';

import 'package:flutter/material.dart';

import 'timer_service_platform_interface.dart';

class TimerService {
  StreamSubscription<dynamic>? _subscription;
  void startTimer({
    required String title,
    required String id,
    required String body,
    required Function function,
    required void Function(dynamic)? streaam,
  }) async {
    final raw = PluginUtilities.getCallbackHandle(function)?.toRawHandle();
    Map<String, dynamic> map = {
      'title': title,
      'id': id,
      'callback': raw!,
      "info": body,
    };

    final s = await TimerServicePlatform.instance.startTimer(map);
    _subscription = s?.listen(streaam);
  }

  void stream({
    required void Function(dynamic)? streaam,
  }) async {
    final s = await TimerServicePlatform.instance.getStreamer();
    s?.listen(streaam);
  }

  void stopTimer() async {
    await TimerServicePlatform.instance.stopTimer();
  }

  void pauseTimer() => TimerServicePlatform.instance.pauseTimer();

  void resumeTimer() => TimerServicePlatform.instance.resumeTimer();
  void excecuteBackground(
    Future<bool> Function(String id, int seconds) excecutor,
  ) async {
    WidgetsFlutterBinding.ensureInitialized();
    final data = await TimerServicePlatform.instance.getFinalTimerData();
    await excecutor(data["id"], data["time"]);
    _subscription?.cancel();
    TimerServicePlatform.instance.endBackgroundOperation();
  }
}
