package com.example.workplan.pushnotification;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.workplan.HomeActivity;
import com.example.workplan.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SendNotificationService extends IntentService {



    final private static String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private  static String serverKey = "key=" + "AAAAZsPspSc:APA91bGK0pLLhaXi64TDq2wxRnkm6qptJfPHVO8MfahSR_xJlPpITxZlthUxmDpt3vGbaZrGLtf8cYoiCk0VwhOFiCLAMVpRimrsxCuFlxarX-ZyvnKVO4x_lQLUaxwI8m5aBC59v8Ia";
    final private  static String contentType = "application/json";

    public SendNotificationService() {
        super("SendNotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.e("Seervice ","Service is called know");
        double lat = intent.getDoubleExtra("lat", 0);
        double longitude=intent.getDoubleExtra("long",0);
        String userid=intent.getStringExtra("Userid");



        sendNotification(this,"Reached the Office","Employee received the office","manger");

        /// this is only for testing
        sendNotification("Reached the Office","You Reached the office");




    }



    void sendNotification(Context context, String NOTIFICATION_TITLE, String NOTIFICATION_MESSAGE, String Senderid) {

        String TOPIC = "/topics/" + Senderid;
        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", NOTIFICATION_TITLE);
            notifcationBody.put("message", NOTIFICATION_MESSAGE);

            notification.put("to", TOPIC);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {
            Log.e("TAG", "onCreate: " + e.getMessage());
        }

        Volley.newRequestQueue(context).add( new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("TAG", "onResponse: " + response.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //  Toast.makeText(MainActivity.this, "Request error", Toast.LENGTH_LONG).show();
                        Log.i("TAG", "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        });

    }




/////  for testing purpose only /////////////////////////


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title,String messageBody) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }







}
