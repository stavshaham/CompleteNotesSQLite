package com.stav.completenotes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private TextView suggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //link for the user to send suggestions to the owner
        suggestions = findViewById(R.id.suggestions);

        suggestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mailIntent = new Intent(Intent.ACTION_VIEW);

                //initialse the values for the mail
                Uri data = Uri.parse("mailto:?subject=" + "ToDo App Suggestion"+ "&body=" + "Your Suggestions" + "&to=" + "shahamstav@gmail.com");

                mailIntent.setData(data);
                startActivity(Intent.createChooser(mailIntent, "Send mail..."));
            }
        });
    }

    //if the AppTheme switch's state is changed, we need to restart the app
    // to load the set AppTheme
    public void restartApp(){
        Intent j = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(j);

        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(i);

        finish();
    }
}