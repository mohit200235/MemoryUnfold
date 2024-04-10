package com.example.memoriesunfold.MaiUi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.memoriesunfold.Database.DatabaseHelper;
import com.example.memoriesunfold.R;
import com.example.memoriesunfold.adapter.AddMemoryCardAdapter;
import com.example.memoriesunfold.model.NewMemoryCreateData;

import java.util.ArrayList;
import java.util.List;

public class AddNewMemory extends AppCompatActivity implements AddMemoryCardAdapter.OnItemClickListener,AddMemoryCardAdapter.OnItemLongClickListener{
    Dialog loadingDialog;
    RecyclerView recyclerViewAddMemory;
    Dialog Name_of_frame;
    List<Integer> idlist= new ArrayList<>();
    ArrayList<NewMemoryCreateData> newMemoryCreateDataList = new ArrayList<>();
    AddMemoryCardAdapter addMemoryCardAdapter;
    ArrayList<Integer> CardsNumbers = new ArrayList<>();

    ImageView createNewMemoryIcon;
    EditText getNameOFMemoryEditText;
    Button CreateNewMemoryButton;
    EditText NumberOfCardsEditText;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_memory);

        recyclerViewAddMemory = findViewById(R.id.recyclerAdd);
        createNewMemoryIcon = findViewById(R.id.CreateNew);
        recyclerViewAddMemory.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //check how data shows us from database
        if (!databaseHelper.ShowMemory().isEmpty()) {
            fetchMemoryDataFromDatabase();
        }


        //loading Dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.custom_loadig_dialog);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_alert_dialog_background));
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        loadingDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().getAttributes().windowAnimations = R.style.AlertDialogAnimation;


        //create dialog for asking name of the frame
        Name_of_frame = new Dialog(this);
        Name_of_frame.setContentView(R.layout.custom_asking_name_of_memory);
        Name_of_frame.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_alert_dialog_background));
        int width2 = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        Name_of_frame.getWindow().setLayout(width2, ViewGroup.LayoutParams.WRAP_CONTENT);
        Name_of_frame.setCancelable(true);
        Name_of_frame.getWindow().getAttributes().windowAnimations = R.style.AlertDialogAnimation;

        getNameOFMemoryEditText = Name_of_frame.findViewById(R.id.Name1OfMemoryEditText);
        NumberOfCardsEditText = Name_of_frame.findViewById(R.id.Name2OfMemoryEditText);
        CreateNewMemoryButton = Name_of_frame.findViewById(R.id.CreateMemory);


        CreateNewMemoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = getNameOFMemoryEditText.getText().toString();
                String number = NumberOfCardsEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(number)) {

//                    String stringWithLineBreaks = name.replace(" ", "\n");

                    if (number.matches("^[0-9]+$")) {
                        int num = Integer.parseInt(number);
                        if (num > 5 || num < 1) {
                            NumberOfCardsEditText.setError("Number must be between 1 and 5");
                            return;
                        }
                    }else {
                        NumberOfCardsEditText.setError("Enter a valid number");
                        return;
                    }

                    //save data using model class and poass to adapter
                    newMemoryCreateDataList.add(new NewMemoryCreateData(name, number));
                    CardsNumbers.add(Integer.valueOf(number));

                    //insert in database here
                    InsertNewMemory(name, Integer.parseInt(number));

                    //set adapter
                    addMemoryCardAdapter = new AddMemoryCardAdapter(AddNewMemory.this, newMemoryCreateDataList);
                    recyclerViewAddMemory.setAdapter(addMemoryCardAdapter);
                    addMemoryCardAdapter.setOnItemClickListener(AddNewMemory.this);
                    Name_of_frame.dismiss();
                } else {
                    loadingDialog.dismiss();
                    Toast.makeText(AddNewMemory.this, "Enter details first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        createNewMemoryIcon.setOnClickListener(view -> {
            loadingDialog.show();
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    loadingDialog.dismiss();
                    Name_of_frame.show();
                }
            };
            handler.postDelayed(runnable, 2000);
        });

    }

    private void fetchMemoryDataFromDatabase() {

            ArrayList<ArrayList<NewMemoryCreateData>> memoryArrayList = databaseHelper.ShowMemory();
            ArrayList<NewMemoryCreateData> updatedList = new ArrayList<>(); // Temporary list for updated data

            for (ArrayList<NewMemoryCreateData> memoryList : memoryArrayList) {
                for (NewMemoryCreateData newMemoryCreateData : memoryList) {

                    int id = newMemoryCreateData.getId();

                    if (databaseHelper.getDataByMemoryId(id).isEmpty() && databaseHelper.getDataByMemoryIdWithImageUrl(id).isEmpty()){
                        updatedList.add(newMemoryCreateData);
                    }
                }
            }

            // Update the adapter only if new data is available
            if (!updatedList.isEmpty()) {
                newMemoryCreateDataList.clear(); // Clear the old list
                newMemoryCreateDataList.addAll(updatedList); // Update with new data

                if (addMemoryCardAdapter == null) {
                    // Create a new adapter if it doesn't exist
                    addMemoryCardAdapter = new AddMemoryCardAdapter(AddNewMemory.this, newMemoryCreateDataList);
                    recyclerViewAddMemory.setAdapter(addMemoryCardAdapter);
                    // Set the click listener for the adapter
                    addMemoryCardAdapter.setOnItemClickListener(this);
                    addMemoryCardAdapter.setOnItemLongClickListener(AddNewMemory.this);
                } else {
                    // Notify the adapter that the data set has changed
                    addMemoryCardAdapter.notifyDataSetChanged();
                }
            }
        }

    private void InsertNewMemory(String name1, int number1) {

        long l  = databaseHelper.CreateNewMemory(name1, number1);
        // Check if the ID is not already in the list before adding
        if (!idlist.contains((int) l)) {
            idlist.add((int) l);
            saveIdListToSharedPreferences();
        }
        if (l>0){
            fetchMemoryDataFromDatabase();
            Toast.makeText(this, "memory created successfully", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveIdListToSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("idList", TextUtils.join(",", idlist));
        editor.apply();
    }

    @Override
    public void onItemClick(int position) {

        loadingDialog.show();

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                loadingDialog.dismiss();
                Intent intent = new Intent(AddNewMemory.this, MemoryData.class);
                intent.putExtra("card", newMemoryCreateDataList.get(position).getNumber());
                intent.putExtra("id",newMemoryCreateDataList.get(position).getId());
                startActivity(intent);

            }
        };

        handler.postDelayed(runnable, 2000);

    }

    @Override
    public void onItemLongClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewMemory.this);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure you wants to delete this memory");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                loadingDialog.show();

                int new_id_from_data=newMemoryCreateDataList.get(position).getId();
                boolean  b= databaseHelper.deleteMemoryData(new_id_from_data);
                boolean  b1= databaseHelper.deleteData(new_id_from_data);
                Handler handler = new Handler();

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (b || b1){
                            loadingDialog.dismiss();
                            // Remove the item from your list
                            newMemoryCreateDataList.remove(position);

                            addMemoryCardAdapter.notifyItemRemoved(position);
                            addMemoryCardAdapter.notifyDataSetChanged();
                            addMemoryCardAdapter.deleteItem(position);
                            Toast.makeText(AddNewMemory.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        }else{
                            loadingDialog.dismiss();
                            Toast.makeText(AddNewMemory.this, "some error occurred!", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                handler.postDelayed(runnable,1500);

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}