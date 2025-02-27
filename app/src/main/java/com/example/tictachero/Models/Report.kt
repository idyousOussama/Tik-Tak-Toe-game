package com.example.tictachero.Models

class Report (var reportId: String , var reportMessage : String , sender  : OnlinePlayer , reportType : ReportType  ) {
}


enum class ReportType {
    NOTIFICATION
}