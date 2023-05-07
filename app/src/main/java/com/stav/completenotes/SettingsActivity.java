package com.stav.completenotes;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.stav.completenotes.db.SQLiteHelper;

public class SettingsActivity extends AppCompatActivity {

    private TextView suggestions, logout, userSettings;
    private SQLiteHelper sqLiteHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sqLiteHelper = new SQLiteHelper(getApplicationContext());

        //link for the user to send suggestions to the owner
        suggestions = findViewById(R.id.suggestions);
        // Links the logout btn
        logout = findViewById(R.id.logout);
        // Links to the user settings btn
        userSettings = findViewById(R.id.userSettings);

        logout.setOnClickListener(view -> {
            sqLiteHelper.setCurrentUser(null);

            // Removing user from last user who logged in in the device
            SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putBoolean("hasLoggedIn", false);
            editor.putString("emailLoggedIn", "");
            editor.commit();

            Intent loginActivity = new Intent(SettingsActivity.this, LoginActivity.class);
            loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginActivity);
        });

        suggestions.setOnClickListener(v -> {
            Intent mailIntent = new Intent(Intent.ACTION_VIEW);

            //initialize the values for the mail
            Uri data = Uri.parse("mailto:?subject=" + "ToDo App Suggestion"+ "&body=" + "Your Suggestions" + "&to=" + "shahamstav@gmail.com");

            mailIntent.setData(data);
            startActivity(Intent.createChooser(mailIntent, "Send mail..."));
        });

        userSettings.setOnClickListener(v -> {
            Intent settings = new Intent(SettingsActivity.this, UserSettingsActivity.class);
            startActivity(settings);
        });
    }
}