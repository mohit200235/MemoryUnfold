package com.example.memoriesunfold.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.memoriesunfold.model.DataMemoryModel;
import com.example.memoriesunfold.model.DataMemoryModelView;
import com.example.memoriesunfold.model.NewMemoryCreateData;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Memory_Unfold.db";
    public static final int DATABASE_VERSION = 4;
    public static final String CREATE_MEMORY_TABLE = "createMemory";
    public static final String CREATE_MEMORY_DATA_TABLE = "memoryData";
    public static final String CREATE_MEMORY_DATA_URL_TABLE = "memoryDataUrl";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String create_table = "create table " + CREATE_MEMORY_TABLE + "(id Integer PRIMARY KEY AutoIncrement ,name TEXT,number TEXT,isSend number)";

        // Add a foreign key to the memoryData table that references the createMemory table
        String create_table2 = "CREATE TABLE " + CREATE_MEMORY_DATA_TABLE +
                "(id_data INTEGER PRIMARY KEY AUTOINCREMENT," +
                " image BLOB," +
                " date TEXT," +
                " description TEXT," +
                " memory_id INTEGER," +  // Foreign key referencing createMemory table
                " FOREIGN KEY(memory_id) REFERENCES " + CREATE_MEMORY_TABLE + "(id)" +
                ")";

        // Create the new table to store image URLs
        String createImageUrlTable = "CREATE TABLE " + CREATE_MEMORY_DATA_URL_TABLE +
                "(id_data INTEGER PRIMARY KEY AUTOINCREMENT," +
                " image TEXT," +
                " date TEXT," +
                " description TEXT," +
                " memory_id INTEGER," +
                " FOREIGN KEY(memory_id) REFERENCES " + CREATE_MEMORY_TABLE + "(id)" +
                ")";

// Execute this query in your onCreate() method
        sqLiteDatabase.execSQL(createImageUrlTable);
        sqLiteDatabase.execSQL(create_table);
        sqLiteDatabase.execSQL(create_table2);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("Drop Table if exists " + CREATE_MEMORY_TABLE);
        sqLiteDatabase.execSQL("Drop Table if exists " + CREATE_MEMORY_DATA_URL_TABLE);
        sqLiteDatabase.execSQL("Drop Table if exists " + CREATE_MEMORY_DATA_TABLE);
        onCreate(sqLiteDatabase);

    }

    public long CreateNewMemory(String name, int number,int isSend) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number);
        values.put("isSend",isSend);
        long l = sqLiteDatabase.insert(CREATE_MEMORY_TABLE, null, values);

        return l;
    }

    public boolean updateMemory(int memory_id,int isSend){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("isSend",isSend);

            int id = memory_id;

            // Use a parameterized update query with the correct WHERE clause
            int i = sqLiteDatabase.update(
                    CREATE_MEMORY_TABLE,
                    contentValues,
                    "id = ?"  ,
                    new String[]{String.valueOf(id)}
            );

            if (i < 0) {
                return false;
            }
        return true;
    }

    public ArrayList ShowMemory() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + CREATE_MEMORY_TABLE, null);

        ArrayList al = new ArrayList();
        if (cursor.moveToFirst()) {

            for (int i = 0; i < cursor.getCount(); i++) {
                ArrayList<NewMemoryCreateData> arrayList = new ArrayList<>();

                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                String number = cursor.getString(2);
                int isSend =cursor.getInt(3);

                NewMemoryCreateData newMemoryCreateData = new NewMemoryCreateData(id, name, number,isSend);

                arrayList.add(newMemoryCreateData);
                al.add(arrayList);
                cursor.moveToNext();

            }
        }
        return al;
    }

    public boolean deleteData(int memory_id){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        // Delete from CREATE_MEMORY_DATA_TABLE first
//        int j = sqLiteDatabase.delete(
//                CREATE_MEMORY_DATA_TABLE,
//                "memory_id = ?",
//                new String[]{String.valueOf(memory_id)}
//        );

        // Then, delete from CREATE_MEMORY_TABLE
        int i = sqLiteDatabase.delete(
                CREATE_MEMORY_TABLE,
                "id = ?",
                new String[]{String.valueOf(memory_id)}
        );


        // Return true if at least one deletion was successful
        return i > 0;
    }

    public boolean deleteMemoryData(int memory_id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        // Delete from CREATE_MEMORY_DATA_TABLE first
        int j = sqLiteDatabase.delete(
                CREATE_MEMORY_DATA_TABLE,
                "memory_id = ?",
                new String[]{String.valueOf(memory_id)}
        );

        return j>0;
    }

    public boolean AddMemoryData(int memory_id, ArrayList<DataMemoryModel> memoryModels) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        for (DataMemoryModel model : memoryModels) {

            ContentValues contentValues = new ContentValues();
            contentValues.put("image", model.getImage());
            contentValues.put("date", model.getDate());
            contentValues.put("description", model.getDescription());
            contentValues.put("memory_id", memory_id);

            long l = sqLiteDatabase.insert(CREATE_MEMORY_DATA_TABLE, null, contentValues);

            if (l < 0) {
                return false;
            }
        }
        return true;
    }

    public boolean AddMemoryDataWithImageUrl(int memory_id, ArrayList<DataMemoryModelView> memoryModelViews) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        for (DataMemoryModelView model : memoryModelViews) {

            ContentValues contentValues = new ContentValues();
            contentValues.put("image", model.getImage());
            contentValues.put("date", model.getDate());
            contentValues.put("description", model.getDescription());
            contentValues.put("memory_id", memory_id);

            long l = sqLiteDatabase.insert(CREATE_MEMORY_DATA_URL_TABLE, null, contentValues);

            if (l < 0) {
                return false;
            }
        }
        return true;
    }

    public List<DataMemoryModel> showAllDataOfMemory() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from " + CREATE_MEMORY_DATA_TABLE, null);

        List<DataMemoryModel> dataList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int id  = cursor.getInt(0);
                int memory_id = cursor.getInt(4);
                byte[] image = cursor.getBlob(1);
                String date = cursor.getString(2);
                String desc = cursor.getString(3);

                DataMemoryModel dataMemoryModel = new DataMemoryModel(id,memory_id, image, date, desc);
                dataList.add(dataMemoryModel);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return dataList;
    }

    public List<DataMemoryModel> getDataByMemoryId(int memory_id) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        List<DataMemoryModel> dataList = new ArrayList<>();

        // Use a parameterized query to avoid SQL injection
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + CREATE_MEMORY_DATA_TABLE +
                " WHERE memory_id = ?", new String[]{String.valueOf(memory_id)});

        if (cursor.moveToFirst()) {
            do {
                int id= cursor.getInt(0);
                byte[] image = cursor.getBlob(1);
                String date = cursor.getString(2);
                String desc = cursor.getString(3);

                DataMemoryModel dataMemoryModel = new DataMemoryModel(id,memory_id, image, date, desc);
                dataList.add(dataMemoryModel);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return dataList;
    }

    public List<DataMemoryModelView> getDataByMemoryIdWithImageUrl(int memory_id) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        List<DataMemoryModelView> dataList = new ArrayList<>();

        // Use a parameterized query to avoid SQL injection
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + CREATE_MEMORY_DATA_URL_TABLE +
                " WHERE memory_id = ?", new String[]{String.valueOf(memory_id)});

        if (cursor.moveToFirst()) {
            do {
                int id= cursor.getInt(0);
                String image = cursor.getString(1);
                String date = cursor.getString(2);
                String desc = cursor.getString(3);

                DataMemoryModelView dataMemoryModelView = new DataMemoryModelView(id,memory_id, image, date, desc);
                dataList.add(dataMemoryModelView);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return dataList;
    }


    public boolean UpdateMemoryData(int memory_id, ArrayList<DataMemoryModel> memoryModels) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        for (DataMemoryModel model : memoryModels) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("image", model.getImage());
            contentValues.put("date", model.getDate());
            contentValues.put("description", model.getDescription());

            int id = model.getId();

            // Use a parameterized update query with the correct WHERE clause
            int i = sqLiteDatabase.update(
                    CREATE_MEMORY_DATA_TABLE,
                    contentValues,
                    "memory_id = ? AND id_data = ?",  // Assuming id_data is the primary key of the memory data table
                    new String[]{String.valueOf(memory_id), String.valueOf(id)}
            );

            if (i < 0) {
                return false;
            }
        }
        return true;
    }

}
