package com.downnotice.mobile

import android.app.Application
import com.downnotice.mobile.notification.NotificationHelper
import com.downnotice.mobile.service.BootReceiver

class DownNoticeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper(this).createChannels()
        BootReceiver.schedulePollWork(this)
    }
}
