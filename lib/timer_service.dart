import 'dart:async';
import 'dart:convert';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:timer_service/supporter.dart';
import 'timer_service_platform_interface.dart';
export 'package:timer_service/supporter.dart';

class TimerService {
  StreamSubscription<dynamic>? _subscription;
  void initialize()=> TimerServicePlatform.instance.initialize();
  void startTimer({
    required TimerData data,
    required void Function() callbackFunction,
    void Function(RunningData)? streaam,
  }) async {
    final raw =
        PluginUtilities.getCallbackHandle(callbackFunction)?.toRawHandle();
    Map<String, dynamic> map = {
      'title': data.notificationTitle,
      'data': json.encode(data.data ?? {}),
      "info": data.notificationBody,
      "stopseconds":data.stopTime,
    };
    if (raw != null) {
      map.update(
        'callback',
        (value) => raw,
        ifAbsent: () => raw,
      );
    }

    final s = await TimerServicePlatform.instance.startTimer(map);
    _subscription = s
        ?.transform<RunningData>(
          StreamTransformer.fromHandlers(
            handleData: (value, sink) {
              sink.add(
                RunningData(
                  data: value["data"] == "null"
                      ? null
                      : json.decode(
                          value["data"] ?? "",
                        ),
                  seconds: value["time"],
                  status: value["status"],
                ),
              );
            },
            handleDone: (sink) {
              sink.close();
            },
          ),
        )
        .listen(streaam);
  }

  void stream({
    required void Function(RunningData)? streaam,
  }) async {
    final s = await TimerServicePlatform.instance.getStreamer();
    s
        ?.transform<RunningData>(
          StreamTransformer.fromHandlers(
            handleData: (value, sink) {
              sink.add(
                RunningData(
                  data: value["data"] == "null"
                      ? null
                      : json.decode(
                          value["data"] ?? "",
                        ),
                  seconds: value["time"],
                  status: value["status"],
                ),
              );
            },
            handleDone: (sink) {
              sink.close();
            },
          ),
        )
        .listen(streaam);
  }

  void stopTimer() async {
    await TimerServicePlatform.instance.stopTimer();
  }

  void pauseTimer() => TimerServicePlatform.instance.pauseTimer();
  void updateUserData(Map<String, dynamic> data) =>
      TimerServicePlatform.instance.updateUserData(data);
  void resumeTimer() => TimerServicePlatform.instance.resumeTimer();
  void excecuteBackground(
    Future<bool> Function(RunningData data) excecutor,
  ) async {
    WidgetsFlutterBinding.ensureInitialized();
    final data = await TimerServicePlatform.instance.getFinalTimerData();
    await excecutor(data);
    _subscription?.cancel();
    TimerServicePlatform.instance.endBackgroundOperation();
  }
}
