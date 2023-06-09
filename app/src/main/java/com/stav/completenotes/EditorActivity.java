package com.stav.completenotes;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.stav.completenotes.db.SQLiteHelper;
import com.stav.completenotes.utils.Item;
import com.stav.completenotes.utils.MyAlarm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditorActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private EditText title_text, detail_text;
    private TextView dateTime_text;
    private Button addButton;

    //if the request has come to edit a particular item,
    private int itemId;

    private SQLiteHelper sqlHelper;
    private Item i;

    //----------------------------------------------------------date Time
    public static String alarm_title;

    private AlarmManager alarmManager;
    private Intent alarm_intent;
    private PendingIntent pendingIntent;

    //set time to this Calender
    private Calendar c;

    //get current time from this Calender
    private Calendar calendar;

    private int day, month, year, hour, minute;
    private long set_Time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        c = Calendar.getInstance();

        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm_intent = new Intent(getApplicationContext(), MyAlarm.class);
        //to create a notification channel
        createNotificationChannel();

        sqlHelper = new SQLiteHelper(getApplicationContext());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        title_text = findViewById(R.id.title_text);
        detail_text = findViewById(R.id.detail_text);
        dateTime_text = findViewById(R.id.dateTime_text);

        //dateTime_text set Time OnClick...
        dateTime_text.setOnClickListener(v -> set_DateTime());

        addButton = findViewById(R.id.addButton);

        Intent intent   = getIntent();
        itemId = intent.getIntExtra("itemId", -1);
        Log.i("itemId", String.valueOf(itemId));

        //Editing an existing item in the list
        if(itemId != -1) {
            i = MainActivity.itemAdapter.getItem(itemId);

            //initialising values from the item
            title_text.setText(i.getTitle());
            detail_text.setText(i.getDetail());
            dateTime_text.setText(i.getDateTime());

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // we add only those items with titleText
                    if(!(title_text.getText().toString().replaceAll(" ", "").length() == 0)){
                        i.setTitle(title_text.getText().toString());
                        i.setDetail(detail_text.getText().toString());

                        //check if any alarm was associated with it
                        if(i.getDateTime() != null)
                        {
                            //dateTime/alarm is already set and must be deleted
                            cancelAlarm(i.getId());
                        }

                        //new dateTime for the item if provided by the user
                        i.setDateTime(dateTime_text.getText().toString());

                        //if dateTime is set we need to set the alarm for this item
                        if(!dateTime_text.getText().toString().isEmpty())
                        {
                            //we generate unique id for each alarm and pass it as reqCode,
                            // to avoid clash with others
                            int id = generate_ID();
                            i.setId(id);

                            // we pass the time, uniqueId as reqCode,
                            // and the title for the alarm notification
                            setAlarm(c, id, title_text.getText().toString());
                        }
                        saveData();
                    } else {
                        //if the titleText is empty
                        //we need to delete the item, and cancel the alarm associated
                        MainActivity.itemAdapter.remove(i);
                        cancelAlarm(i.getId());
                        saveData();
                    }
                    finish();
                }

            });

        } else {
            //request has come to add a new item to the adapter (list)
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check if the title exists
                    if(!(title_text.getText().toString().replaceAll(" ", "").length() == 0)){
                        //generate a unique id which is used as reqCode for alarms
                        int reqCode = generate_ID();
                        Item newItem = new Item(title_text.getText().toString(), detail_text.getText().toString(), false,dateTime_text.getText().toString(), reqCode);
                        //add item to the adapter
                        MainActivity.itemAdapter.add(newItem);

                        //if dateTime_text is not empty
                        //user wants to create a remainder(alarm)
                        if(!dateTime_text.getText().toString().isEmpty())
                        {
                            //dateTime is not empty, is set
                            setAlarm(c, reqCode, title_text.getText().toString());
                        }

                        Log.i("Item added ", String.valueOf(reqCode));
                        saveData();
                    }
                    finish();
                }

            });
        }

    }
    //method to save data to the sqlite
    public void saveData() {
        Gson gson = new Gson();

        String json = gson.toJson(MainActivity.itemArrayList);
        Log.i("Items: ", json + " User: " + sqlHelper.getCurrentUser().getUsername());
        sqlHelper.updateBoard(sqlHelper.getCurrentUser().getUsername(), json);
    }

    //method to generate uniqueId
    public int generate_ID()
    {
        //get previous id from sqlite,
        //if no id stored, returns 1

        int id = sqlHelper.getLastId(sqlHelper.getCurrentUser().getUsername());

        Log.i("id", String.valueOf(id));

        //if id == -1,  there was no id stored
        //so we need to initialise it and store
        if(id == -1)
        {
            //return the generated ID
            return 1;
        }
        else
        {
            //we need to increment the previous id and store and return the same
            return id+1;
        }
    }

    //-----------------------------------------------------------DATE TIME EFFECTS...
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            CharSequence name = "RemainderChannel";
            String description = "Channel for lem";

            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel("notifyLemubit", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // set alarm
    public void setAlarm(Calendar c, int req_code, String title){
        long timeNow = System.currentTimeMillis();
        Log.i("TimeLeft", String.valueOf(c.getTimeInMillis() - timeNow));
        alarm_intent = new Intent(EditorActivity.this, MyAlarm.class);

        //by this we share the title and id for the alarm notification,  to AlarmReceiver
        alarm_intent.putExtra("title", title);
        alarm_intent.putExtra("Id", req_code);

        pendingIntent = PendingIntent.getBroadcast(EditorActivity.this, req_code, alarm_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarm_title = title;

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis() - 2000, pendingIntent);
        Log.i("Note", "Alarm SET " + req_code);


    }
    //cancel alarm
    public void cancelAlarm(int req_code){
        PendingIntent pen = PendingIntent.getBroadcast(EditorActivity.this,
                req_code, alarm_intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pen);
        Log.i("Note", "Alarm CANCELLED " + req_code);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        //after date is set we need to open timePicker to select time
        TimePickerDialog timePickerDialog = new TimePickerDialog(EditorActivity.this, EditorActivity.this, hour, minute, DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE,minute);
        c.set(Calendar.SECOND, 0);

        set_Time = c.getTimeInMillis();
        Log.i("TIME SET TO",  c.getTime().toString());

        Date date = c.getTime();

        //we format the date as below
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
        //fill the dateTime text with the set date time
        dateTime_text.setText(simpleDateFormat.format(date).toString());

    }

    //Set DateTime
    public void set_DateTime()
    {
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(EditorActivity.this, EditorActivity.this,year, month,day);
        datePickerDialog.show();

    }
    //if clearTime textView is clicked,  we need to clear the set Date time
    public void clearTime(View v)
    {
        dateTime_text.setText("");
    }
}