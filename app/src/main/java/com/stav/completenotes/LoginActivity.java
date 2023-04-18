package com.stav.completenotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.stav.completenotes.db.SQLiteHelper;
import com.stav.completenotes.db.User;

import java.util.Random;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private SQLiteHelper sqLiteHelper;
    private EditText loginEmail, loginPassword;
    // Delay to check if user was logged in before
    private static int SPLASH_TIME_OUT = 3000;
    // SharedPreferences where user logged in and email are saved in order to check if he was logged in before
    public static String PREFS_NAME = "MyPrefsFile";
    // Fingerprint
    private Executor executor;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initializing sql class
        sqLiteHelper = new SQLiteHelper(getApplicationContext());

        // Initializing Design Values
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);

        // Show/Hide password
        ImageView showHidePwd = findViewById(R.id.imageview_show_hide_pwd);
        showHidePwd.setImageResource(R.drawable.ic_show_pwd);
        showHidePwd.setOnClickListener(v -> {   // Instead of using new onclicklistener, which gets view on the onclick, using v -> as a listener
            if (loginPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                // If password is visible then hide
                loginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                // Change Icon
                showHidePwd.setImageResource(R.drawable.ic_show_pwd);

            } else {
                loginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                // Change Icon
                showHidePwd.setImageResource(R.drawable.ic_hide_pwd);
            }
        });
    }

    private BiometricPrompt getBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                AfterFingerPrint();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        });

        // returns Prompt
       return biometricPrompt;
    }

    // registerOnClick function, set onClick on activity_login.xml in button code.
    // The function login with the entered details and if they are correct opening new intent
    public void loginOnClick(View view) {
        String email = loginEmail.getText().toString();
        String password = loginPassword.getText().toString();
        String type = "";

        if (Patterns.EMAIL_ADDRESS.matcher(email).matches())
            type = "email";
        else
            type = "username";

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter an email/username", Toast.LENGTH_SHORT).show();
            loginEmail.setError("Email is required");
            loginEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            loginEmail.setError("Password is required");
            loginEmail.requestFocus();
        } else if(!sqLiteHelper.emailRegistered(email) && !sqLiteHelper.usernameRegistered(email)) {
            Toast.makeText(this, "Username Mail not fount", Toast.LENGTH_SHORT).show();
        } else if (sqLiteHelper.checkUserPassword(email, password)) {
            User user;
            if (type == "email") {
                user = sqLiteHelper.getUserByEmail(email);
            } else {
                user = sqLiteHelper.getUserByUsername(email);
            }
            sqLiteHelper.setCurrentUser(user);


            // Saving the user who logged in at the device so we can make login with fingerprint
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Saving if was logged in
            editor.putBoolean("hasLoggedIn", true);
            // Saving email
            editor.putString("emailLoggedIn", user.getEmail());
            editor.commit();

            updateUI();
        }
    }

    // forgotPasswordOnClick function, set onClick on activity_login.xml in forgot password text code.
    // The function login with the entered details and if they are correct opening new intent
    public void forgotPasswordOnClick(View view) {
        // Setup alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Forgot your password?");
        builder.setMessage("Enter your email address");

        final EditText editTextDialogEmail = new EditText(LoginActivity.this);
        editTextDialogEmail.setTextColor(getResources().getColor(R.color.lavender));
        editTextDialogEmail.setTextSize(18);
        editTextDialogEmail.setHint("Enter your email address");

        builder.setView(editTextDialogEmail);

        // Sending messages
        builder.setPositiveButton("Continue", (dialog, which) -> { // Instead of new ... using dialog, which, means onClick because these are the values needed
            String email = editTextDialogEmail.getText().toString();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(LoginActivity.this, "Please enter an email", Toast.LENGTH_SHORT).show();
                editTextDialogEmail.setError("Email is required");
                editTextDialogEmail.setFocusable(true);
                forgotPasswordOnClick(null);
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(LoginActivity.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                editTextDialogEmail.setError("Email is not valid");
                editTextDialogEmail.setFocusable(true);
                forgotPasswordOnClick(null);
            } else {
                // Getting random code with 4 numbers to send to the user
                String code = getVerificationCode();
                // Getting user details
                User user = sqLiteHelper.getUserByEmail(email);
                String phone = user.getPhoneNumber();

                // new PendingIntent
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                // Get the SmsManager instance and sending sms
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phone, null, "Your verification code to Todo Application is: " + code, pendingIntent, null);

                // Calling useCode function with the code
                useCode(code);
            }
        });

        // Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        // Show the AlertDialog
        alertDialog.show();
    }

    public void useCode(String code) {
        // Setup alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Got your code?");
        builder.setMessage("Enter the code you received");

        final EditText editTextDialogCode = new EditText(LoginActivity.this);
        editTextDialogCode.setTextColor(getResources().getColor(R.color.lavender));
        editTextDialogCode.setTextSize(18);
        editTextDialogCode.setHint("Enter the code");

        builder.setView(editTextDialogCode);

        // Sending messages
        builder.setPositiveButton("Continue", (dialog, which) -> { // Instead of new ... using dialog, which, means onClick because these are the values needed
            String enteredCode = editTextDialogCode.getText().toString();
            if (TextUtils.isEmpty(enteredCode)) {
                Toast.makeText(LoginActivity.this, "Please enter an email", Toast.LENGTH_SHORT).show();
                editTextDialogCode.setError("Email is required");
                editTextDialogCode.setFocusable(true);
                forgotPasswordOnClick(null);
            } else {
                if (enteredCode == code) {
                    codeConfirm();
                } else {
                    codeWrong();
                }
            }
        });
    }

    public void codeConfirm() {

    }

    public void codeWrong() {

    }

    // getVerificationCode, function that returns a random code with 4 numbers to verify the users and allow to change the password.
    public String getVerificationCode() {
        String num = "";
        for (int i = 0; i > 4; i++) {
            Random random = new Random();
            num += String.valueOf(random.nextInt(10));
        }

        return num;
    }

    // The function transfer to the next activity after logged in
    private void updateUI() {
        Intent homeActivity = new Intent(LoginActivity.this, MainActivity.class);
        homeActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeActivity);
        finish();
    }

    // redirectOnClick function, set onClick on activity_login.xml in button code.
    // If user does not have an account redirect to register activity
    public void redirectRegisterOnClick(View view) {
        Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerActivity);
        finish();
    }

    public void fLoginClick(View view) {
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        BiometricPrompt biometricPrompt1 = getBiometricPrompt();
        biometricPrompt1.authenticate(promptInfo);
    }

    private void AfterFingerPrint() {
        // loading last user on the phone and logging in.
        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
            boolean hasLoggedIn = sharedPreferences.getBoolean("hasLoggedIn", false);
            String userEmail = sharedPreferences.getString("emailLoggedIn", "a@gmail.com");
            User user = sqLiteHelper.getUserByEmail(userEmail);

            if (hasLoggedIn) {
                sqLiteHelper.setCurrentUser(user);
                updateUI();
            } else {
                Toast.makeText(this, "You need to login first!", Toast.LENGTH_SHORT).show();
            }
        }, SPLASH_TIME_OUT);
    }

    // Checking if user is already logged in. In such case, loading the home page.
    @Override
    protected void onStart() {
        super.onStart();
    }
}