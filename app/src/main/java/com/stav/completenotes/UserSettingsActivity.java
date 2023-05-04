package com.stav.completenotes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.stav.completenotes.db.SQLiteHelper;
import com.stav.completenotes.db.User;

import java.util.Calendar;

public class UserSettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private SQLiteHelper sqLiteHelper;
    private EditText editTextName, editTextEmail, editTextDate, editTextGender, editTextPhone;
    private TextView textViewWelcome;
    private String name, email, dob, username, phone, gender;
    private DatePickerDialog picker;
    private ImageView genderImageView;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        // Initializing xml values
        saveBtn = findViewById(R.id.saveBtn);
        editTextName = findViewById(R.id.edit_text_name);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextDate = findViewById(R.id.edit_text_dob);
        editTextGender = findViewById(R.id.edit_text_gender);
        editTextPhone = findViewById(R.id.edit_text_phone);

        textViewWelcome = findViewById(R.id.text_show_welcome);
        genderImageView = findViewById(R.id.ic_gender);

        sqLiteHelper = new SQLiteHelper(getApplicationContext());

        User user = sqLiteHelper.getCurrentUser();
        if (user == null) {
            Toast.makeText(UserSettingsActivity.this, "Something went wrong! User's details are not available at this moment", Toast.LENGTH_SHORT).show();
        } else {
            updateUserData(user);
        }
        editTextName.setOnClickListener(v -> {
            alertDialogBuilder("Change username", "Enter your username", editTextName, "username");
        });

        editTextPhone.setOnClickListener(v -> {
            alertDialogBuilder("Change phone number", "Enter your phone number", editTextPhone, "phone");
        });

        editTextEmail.setOnClickListener(v -> {
            alertDialogBuilder("Change your email", "Enter your new email", editTextEmail, "email");
        });

        editTextDate.setOnClickListener(v -> {
            final Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);

            picker = new DatePickerDialog(UserSettingsActivity.this, R.style.AppTheme_DialogTheme, (vw, year1, month1, dayOfMonth) ->
                    editTextDate.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1), year, month, day);

            picker.show();
        });

        editTextGender.setOnClickListener(v -> {
            final String[] genders = {"Male", "Female"};
            Spinner dropdown = new Spinner(UserSettingsActivity.this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(UserSettingsActivity.this, android.R.layout.simple_spinner_dropdown_item, genders);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dropdown.setAdapter(adapter);
            dropdown.setOnItemSelectedListener(UserSettingsActivity.this);
        });

        saveBtn.setOnClickListener(v -> {

        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        switch (position) {
            case 0:
                editTextGender.setText("Male");
                genderImageView.setImageResource(R.drawable.baseline_male_24);
                break;
            case 1:
                editTextGender.setText("Female");
                genderImageView.setImageResource(R.drawable.baseline_female_24);
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void updateUserData(User user) {
        name = user.getName();
        email = user.getEmail();
        dob = user.getDob();
        username = user.getUsername();
        phone = user.getPhoneNumber();
        gender = user.getGender();

        textViewWelcome.setHint("Welcome " + name);
        editTextName.setHint(username);
        editTextEmail.setHint(email);
        editTextDate.setHint(dob);
        editTextGender.setHint(gender);
        editTextPhone.setHint(phone);

        if (gender.equalsIgnoreCase("male")) {
            genderImageView.setImageResource(R.drawable.baseline_male_24);
        } else {
            genderImageView.setImageResource(R.drawable.baseline_female_24);
        }
    }

    // alertDialogBuilder function
    // The function builds and alertDialog and checks the necessary things to change the text
    public void alertDialogBuilder(String title, String description, EditText editText, String type) {
        // Setup alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingsActivity.this);
        builder.setTitle(title);
        builder.setMessage(description);

        final EditText editTextDialog = new EditText(UserSettingsActivity.this);
        editTextDialog.setTextColor(getResources().getColor(R.color.lavender));
        editTextDialog.setTextSize(18);
        editTextDialog.setHint(description);

        builder.setView(editTextDialog);

        String text = editTextDialog.getText().toString();

        // Open email apps if user clicks continue button.
        builder.setPositiveButton("Confirm", (dialog, which) -> { // Instead of new ... using dialog, which, means onClick because these are the values needed
            if (type == "email") {
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(UserSettingsActivity.this, "The text should not be empty", Toast.LENGTH_SHORT).show();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                    Toast.makeText(UserSettingsActivity.this, "Email is not valid", Toast.LENGTH_SHORT).show();
                } else {
                    editText.setHint(editTextDialog.getText());
                    User user = sqLiteHelper.getCurrentUser();
                    user.setEmail(text);
                    sqLiteHelper.updateUser(user);
                }
            } else {
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(UserSettingsActivity.this, "The text should not be empty", Toast.LENGTH_SHORT).show();
                } else {
                    editText.setHint(editTextDialog.getText());
                }
            }
        });

        // Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        // Show the AlertDialog
        alertDialog.show();
    }
}