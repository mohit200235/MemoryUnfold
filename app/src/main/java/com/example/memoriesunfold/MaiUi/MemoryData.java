package com.example.memoriesunfold.MaiUi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.memoriesunfold.Database.DatabaseHelper;
import com.example.memoriesunfold.R;
import com.example.memoriesunfold.adapter.SlideAdapterMemoryData;
import com.example.memoriesunfold.model.DataMemoryModel;
import com.example.memoriesunfold.model.DataMemoryModelView;

import java.util.ArrayList;
import java.util.List;

public class MemoryData extends AppCompatActivity {

    ViewPager viewPager;
    SlideAdapterMemoryData memoryData;
    DatabaseHelper databaseHelper;

    List<DataMemoryModel> dataMemoryModelArrayList = new ArrayList<>();
    List<DataMemoryModelView> dataMemoryModelViewArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_data);
        viewPager = findViewById(R.id.viewPager);
        databaseHelper = new DatabaseHelper(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        String passId = null;

        String cards = getIntent().getStringExtra("card");
        int id = getIntent().getIntExtra("id", -1);

        if (id != -1) {
            checkDtaUSingId(id);
        }

        if (!dataMemoryModelViewArrayList.isEmpty()) {
            memoryData = new SlideAdapterMemoryData(this, Integer.parseInt(cards), id, passId, dataMemoryModelViewArrayList);
        } else {
            memoryData = new SlideAdapterMemoryData(this, Integer.parseInt(cards), id, dataMemoryModelArrayList);
        }
        memoryData.setViewPager(viewPager);
        viewPager.setAdapter(memoryData);
    }

    private void checkDtaUSingId(int id) {

        if (!databaseHelper.getDataByMemoryId(id).isEmpty()) {
            dataMemoryModelArrayList = databaseHelper.getDataByMemoryId(id);
            return;
        }
        if (!databaseHelper.getDataByMemoryIdWithImageUrl(id).isEmpty()) {
            dataMemoryModelViewArrayList = databaseHelper.getDataByMemoryIdWithImageUrl(id);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Call the adapter's method to handle the result
        if (memoryData != null) {
            memoryData.handleActivityResult(requestCode, resultCode, data);
        }
    }

}