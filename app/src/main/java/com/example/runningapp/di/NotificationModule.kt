package com.example.runningapp.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.runningapp.R
import com.example.runningapp.activity.FeedActivity
import com.example.runningapp.util.constant.Constants.ACTION_NAVIGATE_TO_MAPS_FRAGMENT
import com.example.runningapp.util.constant.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningapp.util.constant.Constants.PENDING_INTENT_REQUEST_CODE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {

    @ServiceScoped
    @Provides
    fun providerPendingIntent(
            @ApplicationContext context : Context
    ) : PendingIntent{
        return PendingIntent.getActivity(
                context,
                PENDING_INTENT_REQUEST_CODE,
                Intent(context,FeedActivity::class.java).apply {
                    this.action = ACTION_NAVIGATE_TO_MAPS_FRAGMENT
                },
                PendingIntent.FLAG_UPDATE_CURRENT
        )

    }

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
            @ApplicationContext context : Context,pendingIntent : PendingIntent
    ):NotificationCompat.Builder{
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.account)
                .setColor(Color.RED)
                .setContentIntent(pendingIntent)
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
            @ApplicationContext context : Context
    ) : NotificationManager{
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

}