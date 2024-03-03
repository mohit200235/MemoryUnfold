package com.example.memoriesunfold;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.memoriesunfold.Database.DatabaseHelper;
import com.example.memoriesunfold.MaiUi.AddNewMemory;
import com.example.memoriesunfold.MaiUi.MemoryData;
import com.example.memoriesunfold.adapter.SlideAdapterMemoryData;
import com.example.memoriesunfold.databinding.ActivityMainBinding;
import com.example.memoriesunfold.fragments.Home;
import com.example.memoriesunfold.fragments.SavedMemories;
import com.example.memoriesunfold.model.DataMemoryModel;
import com.example.memoriesunfold.model.DataMemoryModelView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Dialog loadingDialog;
    Dialog SearchDailog;

    DatabaseHelper databaseHelper;
    String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().hide();

//        search = binding.searchIcon;
        databaseHelper = new DatabaseHelper(this);

        //loading Dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.custom_loadig_dialog);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_alert_dialog_background));
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        loadingDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().getAttributes().windowAnimations = R.style.AlertDialogAnimation;


        //loading Dialog
        SearchDailog = new Dialog(this);
        SearchDailog.setContentView(R.layout.custom_finding_memory_by_key);
        SearchDailog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.custom_alert_dialog_background));
        int width2 = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        SearchDailog.getWindow().setLayout(width2, ViewGroup.LayoutParams.WRAP_CONTENT);
        SearchDailog.setCancelable(true);
        SearchDailog.getWindow().getAttributes().windowAnimations = R.style.AlertDialogAnimation;

        EditText EnterKey = SearchDailog.findViewById(R.id.EnterKey);
        Button searchButton = SearchDailog.findViewById(R.id.SearchButton);

        searchButton.setOnClickListener(view -> {
            if (EnterKey.getText().toString().trim() != null && !EnterKey.getText().toString().trim().isEmpty()) {
                key = EnterKey.getText().toString().trim();
                //Make changes here now
                loadingDialog.show();
                getDataFromServer(key);

            } else {
                EnterKey.setError("Enter the key first");
            }
        });


        //starting state
        replaceFragment(new Home());
        binding.bottomNavigationView.setBackground(null);
        binding.floatingButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddNewMemory.class);
            startActivity(intent);
            overridePendingTransition(R.anim.bottom_to_top, R.anim.top_to_bottom_anim);
        });

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new Home());
                binding.searchIcon.setVisibility(View.VISIBLE);
            } else if (item.getItemId() == R.id.Me) {
                replaceFragment(new SavedMemories());
                binding.searchIcon.setVisibility(View.GONE);
            }
            return true;
        });


        binding.searchIcon.setOnClickListener(view -> {
            SearchDailog.show();
        });


        if (getIntent().getParcelableArrayListExtra("array") != null && !getIntent().getParcelableArrayListExtra("array").isEmpty()) {

            ArrayList<DataMemoryModel> modelArrayList = getIntent().getParcelableArrayListExtra("array");
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("finalGetArrayList", modelArrayList);
            Fragment fragment = new SavedMemories();
            fragment.setArguments(bundle);
            replaceFragment(fragment);
            binding.bottomNavigationView.getMenu().findItem(R.id.Me).setChecked(true);
            Log.d("TAG", "onCreate: " + modelArrayList);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.three_dots_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.AppInfo) {
            return true;
        } else {
            return true;
        }

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }


    public void getDataFromServer(String key) {
        if (isNetworkAvailable(MainActivity.this)) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    Object obj = snapshot.getValue();
                    parseMemoryData(obj, key);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            loadingDialog.dismiss();
            Toast.makeText(this, "check internet conections", Toast.LENGTH_SHORT).show();
        }
    }

        public void parseMemoryData(Object memoryData, String MyKey) {
//        Log.d("key12345", "parseMemoryData: "+memoryData);
        if(isNetworkAvailable(MainActivity.this)) {
            try {
                JSONObject memoryOject = new JSONObject((Map) memoryData);
                JSONObject getAllData = memoryOject.getJSONObject("Memories");
//                Log.d("key1234", "parseMemoryData: "+getAllData);
                if (getAllData.has(MyKey)) {
                    JSONObject filteredByKey = getAllData.getJSONObject(MyKey);
                    SearchDailog.dismiss();
                    Log.d("key1234561", "parseMemoryData: "+filteredByKey);

                    List<JSONObject> dataMemoryModelList = new ArrayList<>();

                    JSONArray DataMemoryModels = filteredByKey.getJSONArray("DataMemoryModels");
                    Log.d("key123456", "parseMemoryData: "+DataMemoryModels);
                    for (int i  = 1 ; i  < DataMemoryModels.length() ; i++){
//                        dataMemoryModelList.add((JSONObject) DataMemoryModels.get(i));
                        Log.d("key1234567", "parseMemoryData: "+DataMemoryModels.get(i));
                        dataMemoryModelList.add((JSONObject) DataMemoryModels.get(i));
                    }
//                    Log.d("key123456", "parseMemoryData: "+DataMemoryModels);


                    //get details from this ad call the adapter ad everythig ...
                    JSONObject NewMemoryCreateData = filteredByKey.getJSONObject("NewMemoryCreateData");


                    covertJsonIntoDataMemory(dataMemoryModelList, NewMemoryCreateData);
                } else {
                    SearchDailog.dismiss();
                    loadingDialog.dismiss();
                    Toast.makeText(this, "no memory exits , please check your key again", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                loadingDialog.dismiss();
                Log.d("key1234", "parseMemoryData: "+e);
                Toast.makeText(this, "error occurred"+e, Toast.LENGTH_SHORT).show();
            }
        }else{
            loadingDialog.dismiss();
            Toast.makeText(this, "check internet connections", Toast.LENGTH_SHORT).show();
        }
    }
//    public void parseMemoryData(Object memoryData, String MyKey) {
//        try {
//            JSONObject memoryObject = new JSONObject((Map) memoryData);
//            JSONObject allMemories = memoryObject.getJSONObject("Memories");
//            Log.d("key1234", "parseMemoryData: "+allMemories);
//
//            if (allMemories.has(MyKey)) {
//                JSONObject memoryItem = allMemories.getJSONObject(MyKey);
//                JSONArray dataMemoryModelsArray = memoryItem.getJSONArray("DataMemoryModels");
//                JSONObject newMemoryCreateData = memoryItem.getJSONObject("NewMemoryCreateData");
//
//                List<JSONObject> dataMemoryModelList = new ArrayList<>();
//
//                for (int i = 0; i < dataMemoryModelsArray.length(); i++) {
//                    JSONObject innerJsonObject = ((JSONArray) dataMemoryModelsArray).optJSONObject(i);
//                    if (innerJsonObject != null) {
//                        dataMemoryModelList.add(innerJsonObject);
//                    }
//                }
//
//                SearchDailog.dismiss();
//                covertJsonIntoDataMemory(dataMemoryModelList, newMemoryCreateData);
//            } else {
//                SearchDailog.dismiss();
//                loadingDialog.dismiss();
//                Toast.makeText(this, "No memory exists, please check your key again", Toast.LENGTH_SHORT).show();
//            }
//        } catch (JSONException e) {
//            loadingDialog.dismiss();
//            Log.d("key1234", "parseMemoryData: " + e);
//            Toast.makeText(this, "Error occurred" + e, Toast.LENGTH_SHORT).show();
//        }
//    }

    public void covertJsonIntoDataMemory(List<JSONObject> jsonList, JSONObject NewMemoryCreateData) throws JSONException {
        ArrayList<DataMemoryModelView> dataMemoryModelList = new ArrayList<>();
        String cards = (String) NewMemoryCreateData.get("number");
        AtomicInteger imageFetchCounter = new AtomicInteger(jsonList.size());

        String memoryname = (String) NewMemoryCreateData.get("name");
        int memory_id_ = NewMemoryCreateData.getInt("id");
        for (JSONObject jsonObject : jsonList) {
            try {
//                loadingDialog.dismiss();
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONObject innerJsonObject = jsonObject.getJSONObject(key);

                    // Extract values from the inner JSON object
                    int id = innerJsonObject.getInt("id");
                    int memoryId = innerJsonObject.getInt("memory_id");
                    String imageString = innerJsonObject.getString("image");
                    Log.d("imageStri", "covertJsonIntoDataMemory: " + imageString);
                    String date = innerJsonObject.getString("date");
                    String description = innerJsonObject.getString("description");

                    DataMemoryModelView dataMemoryModelView = new DataMemoryModelView(id, memoryId, imageString, date, description);

                    dataMemoryModelList.add(dataMemoryModelView);
//                    final byte[][] imageByteArray = {null};

//                    imageFetchCounter.incrementAndGet();
//                    fetchImageFromFirebaseStorage(imageString, new ImageDataCallback() {
//                        @Override
//                        public void onImageDataReceived(byte[] imageData) {
//                            byte[] imageByteArray = imageData;
//                            Log.d("byte", "onImageDataReceived: "+imageByteArray);
//                            // Create a DataMemoryModel object
//                            DataMemoryModel dataMemoryModel = new DataMemoryModel(id, memoryId, imageByteArray, date, description);
//                            // Add the DataMemoryModel to the list
//                            dataMemoryModelList.add(dataMemoryModel);
////                            saveByteArray(imageData);
//                            // Check if all image fetch tasks are completed
//                            if (imageFetchCounter.get() == jsonList.size()) {
//                                // All images fetched, proceed with further processing
//                                handleDataMemoryList(dataMemoryModelList, cards, memory_id_,memoryname);
//                            }
//                        }
//                    });

//                    DataMemoryModel dataMemoryModel = new DataMemoryModel(id, memoryId, imageByteArray, date, description);
//                            // Add the DataMemoryModel to the list
//                            dataMemoryModelList.add(dataMemoryModel);

//                    byte[] imageByteArray = convertImageUriToByteArray1(this, Uri.parse(imageString));
                }
                handleDataMemoryList(dataMemoryModelList, cards, memory_id_, memoryname);
            } catch (JSONException e) {
                loadingDialog.dismiss();
                e.printStackTrace();
            }
        }
    }

    private void handleDataMemoryList(ArrayList<DataMemoryModelView> dataMemoryModelViewList, String cards, int memory_id_, String memoryname) {
        if (cards.equals("0")) {
            loadingDialog.dismiss();
            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dataMemoryModelViewList.isEmpty()) {
            loadingDialog.dismiss();
            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (memory_id_ == 0) {
            loadingDialog.dismiss();
            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }


        long l = databaseHelper.CreateNewMemory(memoryname, Integer.parseInt(cards));
//
        boolean addSuccess = databaseHelper.AddMemoryDataWithImageUrl((int) l, dataMemoryModelViewList);
        if (addSuccess) {
            Toast.makeText(this, "Succesfully Added ", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "error occrred", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(this, MemoryData.class);
        intent.putExtra("card", cards);
        intent.putExtra("id", (int) l);
        intent.putExtra("passId", "104");

        //we ca't sed this datathrough itet , ow do use ew model class ad directly upload image in imageView
//        intent.putExtra("datamemoryModelViewList", dataMemoryModelViewList);
        loadingDialog.dismiss();
        startActivity(intent);

//        SlideAdapterMemoryData slideAdapterMemoryData = new SlideAdapterMemoryData(this,Integer.parseInt(cards),memory_id_,dataMemoryModelList);
        Log.d("code1", "covertJsonIntoDataMemory: " + l + dataMemoryModelViewList);

    }

//    private void  saveByteArray(byte[] imageData) {
//        Log.d("byte", "saveByteArray: "+ Arrays.toString(imageData));
//    }


//    private byte[] convertImageUriToByteArray1(Context context, Uri imageUri) throws IOException {
//        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
//
//        if (inputStream != null) {
//            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
//            return byteArrayOutputStream.toByteArray();
//        }
//
//        return null;
//    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

//    public void fetchImageFromFirebaseStorage(String imageUrl, ImageDataCallback callback) {
//        new AsyncTask<String, Void, byte[]>() {
//            @Override
//            protected byte[] doInBackground(String... params) {
//                String downloadUrl = params[0];
//                try {
//                    URL url = new URL(downloadUrl);
//                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                    connection.connect();
//
//                    InputStream inputStream = connection.getInputStream();
//
//                    // Read the input stream
//                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                    byte[] buffer = new byte[1024];
//                    int bytesRead;
//
//                    while ((bytesRead = inputStream.read(buffer)) != -1) {
//                        byteArrayOutputStream.write(buffer, 0, bytesRead);
//                    }
//
//                    // Close streams and connection
//                    inputStream.close();
//                    byteArrayOutputStream.close();
//                    connection.disconnect();
//
//                    return byteArrayOutputStream.toByteArray();
//                } catch (IOException e) {
//                    Log.e("FirebaseStorage", "Error fetching image: " + e.getMessage());
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(byte[] result) {
//                // Pass the fetched byte array through the callback
//                if (result != null) {
//                    callback.onImageDataReceived(result);
//                } else {
//                    // Handle the case where fetching the image failed
//                }
//            }
//        }.execute(imageUrl);
//    }


//    private Bitmap convertImageByteArrayToBitmap(byte[] imageBytes) {
//        // Convert byte array to Bitmap if needed
//        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//    }


    public interface ImageDataCallback {
        void onImageDataReceived(byte[] imageData);
    }

}

