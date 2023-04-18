package com.stav.completenotes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stav.completenotes.utils.Item;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static User currentUser;
    public SQLiteHelper(@Nullable Context context) {
        super(context, "CompleteNotes.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDB) {
        // Creating user table with email, username and password columns
        MyDB.execSQL("CREATE TABLE users(email TEXT primary key, username TEXT, password TEXT, dob TEXT, name TEXT, phoneNumber TEXT, gender TEXT, userId LONGTEXT)");

        // Creating boards table with user, board name and its items.
        MyDB.execSQL("CREATE TABLE boards(username TEXT primary key, items LONGTEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase MyDB, int i, int i1) {
        // Delete Boards.
        MyDB.execSQL("DROP TABLE users");
        MyDB.execSQL("DROP TABLE boards");

        // Creating new Data Base
        onCreate(MyDB);
    }

    // Inserts user values, email, password, and username
    public Boolean insertUser(String email, String username, String password, String gender, String phoneNumber, String dob, String name) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues contentValuesUser = new ContentValues();
        contentValuesUser.put("email", email);
        contentValuesUser.put("username", username);
        contentValuesUser.put("password", password);
        contentValuesUser.put("gender", gender);
        contentValuesUser.put("phoneNumber", phoneNumber);
        contentValuesUser.put("dob", dob);
        contentValuesUser.put("name", name);

        ContentValues contentValuesBoard = new ContentValues();
        contentValuesBoard.put("username", username);
        contentValuesBoard.put("items", "{}");

        long result = MyDB.insert("users", null, contentValuesUser);
        MyDB.insert("boards", null, contentValuesBoard);

        if (result == -1) return false;
        return true;
    }

    public Boolean updateBoard(String username, String items) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        // Setting values in ContentValues
        contentValues.put("username", username);
        contentValues.put("items", items);

        // Updating board
        long result = MyDB.update("boards", contentValues, "username = ?", new String[] {username});

        if (result == -1) return false;
        return true;
    }

    // Checking if email exists, gets String email, returns boolean
    public Boolean emailRegistered(String email) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("SELECT * FROM users WHERE email = ?", new String[] {email});

        if (cursor.getCount() > 0)
            return true;

        return false;
    }

    // Checking if username is exists, gets String username, returns boolean
    public Boolean usernameRegistered(String username) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        Cursor cursor = MyDB.rawQuery("SELECT * FROM users WHERE username = ?", new String[] {username});

        if (cursor.getCount() > 0)
            return true;

        return false;
    }

    // Checking if the username/email and password are matching and correct, returns boolean
    public Boolean checkUserPassword(String username, String password) {
        SQLiteDatabase MyDB = this.getWritableDatabase();

        // Checking username and password
        Cursor cursorUsername = MyDB.rawQuery("SELECT * FROM users WHERE username = ? AND password = ?", new String[] {username, password});
        if (cursorUsername.getCount() > 0) {
            return true;
        }

        // Checking email and password
        Cursor cursorEmail = MyDB.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", new String[]{username, password});
        if (cursorEmail.getCount() > 0) return true;

        return false;
    }
    // Gets user items from database, returns string json of items
    public String getItems(String username) {
        String json = "[]";
        SQLiteDatabase MyDB = this.getWritableDatabase();

        Cursor result = MyDB.rawQuery("SELECT items FROM boards WHERE username = ?", new String[] {username});
        if( result != null && result.moveToFirst() ){
            json = result.getString(0);
            result.close();
        }
        return json;
    }
    // Gets the last item id to be able to create new item with unique id
    public int getLastId(String username) {
        int id = -1;
        String json = "[]";
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ArrayList<Item> items;

        // Gets the item list in json
        Cursor result = MyDB.rawQuery("SELECT items FROM boards WHERE username = ?", new String[] {username});
        if( result != null && result.moveToFirst() ){
            json = result.getString(0);
            Type listType = new TypeToken<ArrayList<Item>>(){}.getType();
            // Converting json to arraylist using Gson extension
            items = new Gson().fromJson(json, listType);
            // Checks the biggest id and returns it + 1
            if (items.size() > 0)
                id = items.size() + 1;
            result.close();
        }

        return id;
    }

    // Function gets String email and return a User with the full details
    public User getUserByEmail(String email) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        String username, dob, name, phoneNumber, gender, userId;
        Cursor result = MyDB.rawQuery("SELECT * FROM users WHERE email = ?", new String[] {email});
        if( result != null && result.moveToFirst() ){
            username = result.getString(1);
            dob = result.getString(3);
            name = result.getString(4);
            phoneNumber = result.getString(5);
            gender = result.getString(6);
            userId = result.getString(7);
            result.close();

            return new User(username, email, dob, name, phoneNumber, gender, userId);
        }

        return null;
    }
    // Function gets String username and return a User with the full details
    public User getUserByUsername(String username) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        String email, dob, name, phoneNumber, gender, userId;
        Cursor result = MyDB.rawQuery("SELECT * FROM users WHERE username = ?", new String[] {username});
        if( result != null && result.moveToFirst() ){
            email = result.getString(0);
            dob = result.getString(3);
            name = result.getString(4);
            phoneNumber = result.getString(5);
            gender = result.getString(6);
            userId = result.getString(7);
            result.close();

            return new User(username, email, dob, name, phoneNumber, gender, userId);
        }

        return null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
