class ReportExportRequest {
  final String? reportType;

  ReportExportRequest({this.reportType});

  Map<String, dynamic> toJson() {
    return {
      'reportType': reportType,
    };
  }
}
