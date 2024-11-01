package ru.carport.app.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
//import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import ru.carport.app.MainActivity;
import ru.carport.app.R;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);
        generateNotification(remoteMessage);
    }

    private void generateNotification(RemoteMessage remoteMessage) {
        String body = remoteMessage.getNotification().getBody();
        String title = remoteMessage.getNotification().getTitle();
        String NOTIFICATION_CHANNEL_ID = remoteMessage.getNotification().getChannelId();

//        Map<String, String> data = remoteMessage.getData();
//        String longText = null;
//
//        if(data != null && data.containsKey("ftext")) {
//            longText = data.get("ftext");
//        }

        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        if(orderId != null) {
//            intent.putExtra("openUrl", getString(R.string.order_url) + orderId);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            //startActivity(intent);
//        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        long[] VIBRATE_PATTERN = {0, 500};

        //Uri NOTIFICATION_SOUND_URI = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.raw.telegraph);

        int NOTIFICATION_COLOR = getResources().getColor(R.color.colorPrimary);

        if(NOTIFICATION_CHANNEL_ID == null || NOTIFICATION_CHANNEL_ID == "") {
            NOTIFICATION_CHANNEL_ID = getString(R.string.default_notification_channel_id);
//        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION",
//                NotificationManager.IMPORTANCE_DEFAULT);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_foreground))
            .setColor(NOTIFICATION_COLOR)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(soundUri)
            //.setSound(NOTIFICATION_SOUND_URI)
            .setVibrate(VIBRATE_PATTERN)
            .setContentIntent(pendingIntent);

//        if(longText != null && longText != "") {
//            notificationBuilder = notificationBuilder
//                .setStyle(new NotificationCompat.InboxStyle()
//                        .addLine(longText));
//            notificationBuilder = notificationBuilder
//                .setStyle(new NotificationCompat.BigTextStyle()
//                    .bigText("2 " + longText));
//        }

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if(NOTIFICATION_ID > 1073741824) {
            NOTIFICATION_ID = 0;
        }

        notificationManager.notify(NOTIFICATION_ID++, notificationBuilder.build());
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        //.fbToken = s;
//        getCarPortToken();
        Log.d("TOKENFIREBASE", s);
    }

//    private String getCarPortToken() {
//        String value = localStorage.getItem("CarPortUserToken");
//        Log.d("", value);
//        return value;
//    }
}
