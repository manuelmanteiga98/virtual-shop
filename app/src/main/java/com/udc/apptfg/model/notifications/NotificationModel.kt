package com.udc.apptfg.model.notifications

data class NotificationModel(
    var id: String,
    var subject: String,
    var content: String,
    var read: Boolean
)
