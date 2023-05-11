package com.stav.completenotes.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.stav.completenotes.MainActivity;
import com.stav.completenotes.R;

public class MyAlarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder  builder = new NotificationCompat.Builder(context, "notifyLemubit")
                .setSmallIcon(R.drawable.ic_done_black_24dp)
                .setContentText("Remainder from CompleteNotes")
                //title of the alarm will be title of the item itself,
                // which we receive  through the intent alarm_intent in "EditorActivity.java"
                .setContentTitle(intent.getExtras().getString("title"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);



        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Log.i("INFO", "NOTIFICATION OCCURS");

        //id will be same as item_id,
        notificationManager.notify(intent.getExtras().getInt("Id"), builder.build());
    }
}
