package allinontech.anonyma.elements;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import allinontech.anonyma.Anonyma;
import allinontech.anonyma.HomeScreen;
import allinontech.anonyma.Login;
import allinontech.anonyma.R;
import allinontech.anonyma.backend.SharedPreference;
import allinontech.anonyma.backend.Util;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "ABBA";

    private SharedPreference sharedPreferenceObj;

    private FirebaseFirestore firestore;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d( "ABBA", "notification received");

        sharedPreferenceObj = new SharedPreference( Anonyma.getAppContext());
        if( sharedPreferenceObj != null){
            String currentUser = sharedPreferenceObj.getUid();

            Map<String, String> data = remoteMessage.getData();
            if( !data.isEmpty()){
                String uid = data.get("uid");
                if( uid.equals( currentUser)){
                    Log.d(TAG, "onMessageReceived: new incoming message.");
                    String title = data.get( "title");
                    String message = data.get( "message");
                    sendNotification(title, message);
                }
            }
        }



    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        sharedPreferenceObj = new SharedPreference( Anonyma.getAppContext());
        if( sharedPreferenceObj != null){
            sharedPreferenceObj.setFCM( token.trim());
        }
        else{
            Toast.makeText( Anonyma.getAppContext(), "Failed to update token", Toast.LENGTH_SHORT).show();
        }
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        if( firestore == null){
            firestore = Util.getFirestore();
        }

        firestore.collection("users").document( HomeScreen.UserId)
                .update( "fcm_token", token.trim())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d( TAG, "FCM token updated");
                    }
                });
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stars_black_24dp)
                        .setColor( getResources().getColor( R.color.colorAccent))
                        .setContentTitle( title.toUpperCase())
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Anoyma notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 , notificationBuilder.build());
    }
}