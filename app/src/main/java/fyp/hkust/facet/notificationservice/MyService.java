package fyp.hkust.facet.notificationservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fyp.hkust.facet.R;
import fyp.hkust.facet.activity.LoginActivity;
import fyp.hkust.facet.activity.MainActivity;
import fyp.hkust.facet.activity.ProfileActivity;
import fyp.hkust.facet.activity.ProfileEditActivity;
import fyp.hkust.facet.model.Notification;
import fyp.hkust.facet.model.User;
import fyp.hkust.facet.skincolordetection.CaptureActivity;

import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;

/**
 * Created by ClementNg on 8/3/2017.
 */

public class MyService extends Service {

    private final static String TAG = MyService.class.getSimpleName();
    private DatabaseReference mDatabaseNotifications;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean firstTime = true;
    private int count = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // START YOUR TASKS
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(getApplicationContext(), startId + " task is running", Toast.LENGTH_SHORT).show();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    // User is signed in
                    Log.d("Service", "onAuthStateChanged:signed_in user_id:" + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);

        mDatabaseNotifications = FirebaseDatabase.getInstance().getReference().child("Notifications");
        Log.d("Service" + "mDatabaseNotification", mDatabaseNotifications.toString());

        mDatabaseNotifications.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    Log.i("dataSnapshot.getValue()", dataSnapshot.getValue().toString());
                    final Notification notification_data = dataSnapshot.getValue(Notification.class);
                    Map<String, Notification> td = (HashMap<String, Notification>) dataSnapshot.getValue();
                    List<Notification> values = new ArrayList<>(td.values());
                    Log.d(TAG + "  arraylist", values.toString());
                    if (firstTime == true) {
                        count = values.size();
                    }
                    if (values.size() > count) {
                        createNotification(getApplicationContext(), " new notification");
                        count = values.size();
                    }
                }
                firstTime = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return START_STICKY;

    }

    private void createNotification(Context context, String message) {
        Intent notificationIntent = new Intent(context, ProfileActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentIntent(intent)
                .setPriority(PRIORITY_HIGH) //private static final PRIORITY_HIGH = 5;
                .setContentText(message)
                .setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        // STOP YOUR TASKS
        Toast.makeText(getApplicationContext(), " task is stopped", Toast.LENGTH_SHORT).show();
        Log.d("My Service", " is stopped");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}