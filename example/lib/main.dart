import 'dart:async';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:timer_service/supporter.dart';
import 'package:timer_service/timer_service.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Task Timer Plugin Example'),
        ),
        body: const TaskTimerExample(),
      ),
    );
  }
}

class TaskTimerExample extends StatefulWidget {
  const TaskTimerExample({super.key});

  @override
  State<TaskTimerExample> createState() => _TaskTimerExampleState();
}

class _TaskTimerExampleState extends State<TaskTimerExample> {
  final _taskTimer = TimerService();
  static StreamSubscription<dynamic>? _subscription;
  String text = "Welcome ";

  @override
  void initState() {
    super.initState();
  }

  void _startTimer() async {
    final ins = await SharedPreferences.getInstance();
    ins.setString("Test", "Hello World");
    _taskTimer.startTimer(
      callbackFunction: callback,
      streaam: (event) {
        setState(() {
          text = event.toString();
        });
      },
      data: const TimerData(
        notificationTitle: 'Hello world',
        data: {
          "id": 'sjhmashghjgsaghjdsajhg',
        },
      ),
    );
  }

  void _stopTimer() {
    _taskTimer.stopTimer();
    _subscription?.cancel();
  }

  void _pauseTimer() {
    _taskTimer.pauseTimer();
  }

  void _resumeTimer() {
    _taskTimer.resumeTimer();
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox.expand(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Text(
            text,
            textAlign: TextAlign.center,
            style: const TextStyle(fontSize: 25, color: Colors.black),
          ),
          ElevatedButton(
            onPressed: _startTimer,
            child: const Text('Start Timer'),
          ),
          ElevatedButton(
            onPressed: _stopTimer,
            child: const Text('Stop Timer'),
          ),
          ElevatedButton(
            onPressed: _pauseTimer,
            child: const Text('Pause Timer'),
          ),
          ElevatedButton(
            onPressed: _resumeTimer,
            child: const Text('Resume Timer'),
          ),
        ],
      ),
    );
  }
}

@pragma('vm:entry-point')
void callback() async {
  WidgetsFlutterBinding.ensureInitialized();
  debugPrint("came here");
  final ins = await SharedPreferences.getInstance();
  final s = ins.getString("Test");
  debugPrint("came here $s");
}
