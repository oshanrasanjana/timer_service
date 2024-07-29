class TimerData {
  final String notificationTitle;
  final String? notificationBody;
  final Map<String, dynamic>? data;
  const TimerData({
    required this.notificationTitle,
    this.notificationBody,
    this.data,
  });
}

class RunningData {
  final Map<String, dynamic>? data;
  final int seconds;
  final String status;
  const RunningData({
    required this.data,
    required this.seconds,
    required this.status,
  });
}
