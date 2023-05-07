package com.stav.completenotes;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stav.completenotes.db.SQLiteHelper;
import com.stav.completenotes.db.User;
import com.stav.completenotes.utils.Item;
import com.stav.completenotes.utils.MyAlarm;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static ArrayList<Item> itemArrayList;
    public static ItemAdapter itemAdapter;

    private SQLiteHelper sqlHelper;

    private ListView listView;
    private AlarmManager alarmManager;
    private Intent alarm_intent;

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private int currentTaskIndex = 0;
    private static final String CHANNEL_ID = "my_channel_01";
    private static final CharSequence CHANNEL_NAME = "My Channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setting the sql helper
        sqlHelper = new SQLiteHelper(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        //Loading the saved data in data base
        loadData(sqlHelper.getCurrentUser());

        //Initialising adapter for listView
        itemAdapter = new ItemAdapter(this, itemArrayList);
        listView = findViewById(R.id.list1);

        listView.setAdapter(itemAdapter);

        //initialising alarmManager and alarm_intent, which we use it later
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm_intent = new Intent(getApplicationContext(), MyAlarm.class);

        // Adding sensor that detects when the user's hand is near the device.
        // When the sensor detects that the user's hand is close to the device,
        // it could trigger the application to display a pop-up notification with the next item on the list.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Get a reference to the proximity sensor
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    // Detects when the proximity sensor changes
    @Override
    public void onSensorChanged(SensorEvent event) {
        float proximityValue = event.values[0];
        if (proximityValue < proximitySensor.getMaximumRange()) {
            currentTaskIndex++;
            if (currentTaskIndex >= itemArrayList.size()) {
                currentTaskIndex = 0;
            }

            showNotification(itemArrayList.get(currentTaskIndex).getTitle());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    //method for loading data from database
    private void loadData(User user) {
        Gson gson = new Gson();
        //json contains the stored data if any, else null
        String json = sqlHelper.getItems(user);
        Log.i("items: ", json);
        Type type = new TypeToken<ArrayList<Item>>(){}.getType();
        itemArrayList = gson.fromJson(json, type);

        if(itemArrayList == null){
            itemArrayList = new ArrayList<>();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "My Notification Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void showNotification(String task) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_done_black_24dp)
                    .setContentTitle("Task Reminder")
                    .setContentText(task)
                    .setPriority(NotificationCompat.PRIORITY_MAX);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (NullPointerException e) {
            Log.e(TAG, "Notification error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Notification error: " + e.getMessage());
        }
    }

    // method to save data in sqlite
    public void saveData() {

        Gson gson = new Gson();

        String json = gson.toJson(MainActivity.itemArrayList);
        Log.i("Items: ", json + " User: " + sqlHelper.getCurrentUser().getUsername());
        sqlHelper.updateBoard(sqlHelper.getCurrentUser(), json);
    }

    // ItemAdapter as ArrayAdapter of type item which we have created as a separate class.
    // Used nested classes because we use ItemAdapter only on this class
    public class ItemAdapter extends ArrayAdapter<Item> {
        public ItemAdapter(Context context, ArrayList<Item> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Item user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
            }
            // Lookup view for data population
            // holds the TITLE of the item
            final TextView tvtitle = convertView.findViewById(R.id.item_title);

            // Setting up the feature to edit any item by clicking on that item
            tvtitle.setOnClickListener(v -> {
                // onClick Editor opens where the user can edit the item
                Intent in = new Intent(getApplicationContext(), EditorActivity.class);

                // we pass this itemId so the editor knows if it
                // it should create another item or edit the item's details
                in.putExtra("itemId", position);
                startActivity(in);
            });
            // feature to delete the item by click and hold
            tvtitle.setOnLongClickListener(new View.OnLongClickListener() {
                Item i = getItem(position);
                @Override
                public boolean onLongClick(View v) {
                    // alert dialog to confirm with the user before deleting the selected item
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Do you want to delete this item?")
                            .setMessage("This item will no longer exist")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // check if any dateTime(alarm) is associated with this item
                                    // delete if any
                                    if(i.getDateTime() != null)
                                    {
                                        // alarm exists, so cancel
                                        cancelAlarm(i.getId());
                                    }

                                    // remove the item from the adapter
                                    itemAdapter.remove(i);

                                    saveData();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();

                    return true;
                }
            });
            // holds the details of the item
            final TextView tvdetails = convertView.findViewById(R.id.item_details);

            // holds the state of the item, if checked(completed) or not
            CheckBox tvdone = convertView.findViewById(R.id.item_done);

            // on changing the state of the item
            tvdone.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (buttonView.isChecked()) {
                    Log.i("INFo", "Checked "+String.valueOf(position));

                    Item currentItem = getItem(position);
                    currentItem.setIs_done(true);

                    // feature to strike through the text if the item is checked(completed)
                    tvtitle.setPaintFlags(tvtitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    tvdetails.setPaintFlags(tvtitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    saveData();

                }
                else {
                    Item currentItem = getItem(position);
                    currentItem.setIs_done(false);

                    // removing the strikeThrough
                    tvtitle.setPaintFlags(tvtitle.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    tvdetails.setPaintFlags(tvtitle.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

                    saveData();
                }
            });

            // Populate the data into the template view using the data object
            tvtitle.setText(user.getTitle());
            tvdetails.setText(user.getDetail());

            // item_details will be formatted as bullet list
            String[] lines = user.getDetail().split("\n");
            tvdetails.setText("");

            // only if item_details exist
            if(!user.getDetail().isEmpty()){
                for (int i = 0; i < lines.length; i++) {
                    //"\u2022" -> bullet point
                    tvdetails.append("\u2022  " + lines[i]+"\n");
                }
            }

            tvdone.setChecked(user.getIs_done());

            // Return the completed view to render on screen
            return convertView;

        }

        // method to cancel alarm of particular item_id (reqCode)
        public void cancelAlarm(int req_code){
            PendingIntent pen = PendingIntent.getBroadcast(MainActivity.this,
                    req_code, alarm_intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.cancel(pen);
            Log.i("Note", "Alarm CANCELLED " + req_code);
        }
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            //menu to add item to the list
            case R.id.add_item:
                startActivity(new Intent(MainActivity.this, EditorActivity.class));

                return true;

            case R.id.settings:
                openSettings();
                return  true;

            case R.id.clear_all:
                clearAll();
                return true;

            // clear only those items which are being checked
            case R.id.clear_completed:
                clearCompleted();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //    OPEN SETTINGS
    public void openSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    public void clearAll(){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Are you sure?")
                .setMessage("All the items will be cleared")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // adapter.clear() also could have been used to remove the items
                        // but I also need to cancel the alarms associated with it
                        Log.i("No of items", String.valueOf(itemAdapter.getCount()));

                        while(itemAdapter.getCount() > 0)
                        {
                            Item it = itemAdapter.getItem(itemAdapter.getCount() - 1);

                            //clear the alarms associated with this item
                            if(it.getDateTime() != null)
                            {
                                //alarm must be set
                                cancelAlarm(it.getId());
                            }
                            Log.i("Removed", String.valueOf(itemAdapter.getCount()));
                            itemAdapter.remove(it);
                        }



                        saveData();
                        Toast.makeText(getApplicationContext(), "Cleared all Tasks", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();


    }
    // removing checked items
    public void clearCompleted(){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Are you sure?")
                .setMessage("All the Completed will be cleared")
                .setPositiveButton("Yes", (dialog, which) -> {

                    Log.i("No of items", String.valueOf(itemAdapter.getCount()));
                    int ind = itemAdapter.getCount();
                    while(ind > 0)
                    {
                        Item it = itemAdapter.getItem(ind - 1);
                        if(it.getIs_done())
                        {
                            // clear the alarms associated with this item
                            if(it.getDateTime() != null)
                            {
                                // alarm must be set
                                cancelAlarm(it.getId());
                            }
                            Log.i("Removed", String.valueOf(itemAdapter.getCount()));
                            itemAdapter.remove(it);
                        }
                        ind--;

                    }

                    saveData();
                    Toast.makeText(getApplicationContext(), "Cleared all Tasks", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();

    }
    //    method to cancel alarm of particular item_id (reqCode)
    public void cancelAlarm(int req_code){
        PendingIntent pen = PendingIntent.getBroadcast(MainActivity.this,
                req_code, alarm_intent,
                PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pen);
        Log.i("Note", "Alarm CANCELLED " + req_code);
    }

}