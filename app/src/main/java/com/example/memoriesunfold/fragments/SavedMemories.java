package com.example.memoriesunfold.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.memoriesunfold.Database.DatabaseHelper;
import com.example.memoriesunfold.MaiUi.MemoryData;
import com.example.memoriesunfold.MainActivity;
import com.example.memoriesunfold.R;
import com.example.memoriesunfold.adapter.ViewMemoryCardAdapter;
import com.example.memoriesunfold.model.DataMemoryModel;
import com.example.memoriesunfold.model.NewMemoryCreateData;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedMemories extends Fragment implements ViewMemoryCardAdapter.OnItemClickListener, ViewMemoryCardAdapter.OnItemLongClickListener {

    DatabaseHelper databaseHelper;
    RecyclerView recyclerView_saved;
    TextView noRecordFound;
    Dialog loadingDialog, showKeyDialog;
    TextView textView_with_key;
    Button back_to_home;
    ViewMemoryCardAdapter addMemoryCardAdapter;
    ArrayList<NewMemoryCreateData> newMemoryCreateDataList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_saved_memories, container, false);
        recyclerView_saved = v.findViewById(R.id.recyclerView_saved);
        noRecordFound = v.findViewById(R.id.no_Record);
        recyclerView_saved.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        // Call a method to populate data after ensuring recyclerView_saved is not null
        populateData();

        //loading Dialog
        loadingDialog = new Dialog(getActivity());

        loadingDialog.setContentView(R.layout.custom_loadig_dialog);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.getWindow().getAttributes().windowAnimations = R.style.AlertDialogAnimation;

        //showKeyDialog setup
        showKeyDialog = new Dialog(getActivity());
        showKeyDialog.setContentView(R.layout.show_key_dialog);
        textView_with_key = showKeyDialog.findViewById(R.id.text_with_key);
        back_to_home = showKeyDialog.findViewById(R.id.back_to_home);
        showKeyDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        showKeyDialog.setCancelable(false);
        showKeyDialog.getWindow().getAttributes().windowAnimations = R.style.AlertDialogAnimation;
        return v;
    }

    private void populateData() {
        if (recyclerView_saved != null) {

            ArrayList<ArrayList<NewMemoryCreateData>> memoryArrayList = databaseHelper.ShowMemory();
            ArrayList<NewMemoryCreateData> updatedList = new ArrayList<>(); // Temporary list for updated data
            for (ArrayList<NewMemoryCreateData> memoryList : memoryArrayList) {
                for (NewMemoryCreateData newMemoryCreateData : memoryList) {
                    int id = newMemoryCreateData.getId();
                    if (!databaseHelper.getDataByMemoryId(id).isEmpty()) {
                        updatedList.add(newMemoryCreateData);
                    }
                    if (!databaseHelper.getDataByMemoryIdWithImageUrl(id).isEmpty()) {
                        updatedList.add(newMemoryCreateData);
                    }
                }
            }

            // Update the adapter only if new data is available
            if (!updatedList.isEmpty()) {
                noRecordFound.setVisibility(View.GONE);
                newMemoryCreateDataList.clear(); // Clear the old list
                newMemoryCreateDataList.addAll(updatedList); // Update with new data

                if (addMemoryCardAdapter == null) {
                    // Create a new adapter if it doesn't exist
                    addMemoryCardAdapter = new ViewMemoryCardAdapter(getActivity(), newMemoryCreateDataList);
                    recyclerView_saved.setAdapter(addMemoryCardAdapter);
                    // Set the click listener for the adapter
                    addMemoryCardAdapter.setOnItemClickListener(this);
                    addMemoryCardAdapter.setOnItemLongClickListener(this);
                } else {
                    // Notify the adapter that the data set has changed
                    addMemoryCardAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        //check for already send the data to server or not ...
        if (newMemoryCreateDataList.get(position).isSend() == 0) {
            //means not send the data to sever

            loadingDialog.show();

            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    loadingDialog.dismiss();

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Check!");
                    builder.setMessage("This Memory is not send still, Do you want to send this?");
                    builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // here set method for send the data to server and delete this memory from database by delete query set
                            //delete this data here
                            loadingDialog.show();
                            tryFirebaseSaveImage(position);
                        }
                    });
                    builder.setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Intent intent = new Intent(getActivity(), MemoryData.class);
                            intent.putExtra("card", newMemoryCreateDataList.get(position).getNumber());
                            intent.putExtra("id", newMemoryCreateDataList.get(position).getId());
                            startActivity(intent);

                        }
                    });
                    builder.show();

                }
            };

            handler.postDelayed(runnable, 2000);
        } else if (newMemoryCreateDataList.get(position).isSend() == 1) {
            loadingDialog.show();
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    loadingDialog.dismiss();
                    Intent intent = new Intent(getActivity(), MemoryData.class);
                    intent.putExtra("card", newMemoryCreateDataList.get(position).getNumber());
                    intent.putExtra("id", newMemoryCreateDataList.get(position).getId());
                    startActivity(intent);
                }
            };
            handler.postDelayed(runnable, 1200);
        }
    }

    private void sendDataToServerFinalMethod(int position, List<String> downloadUrls) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("data1");
        List<DataMemoryModel> dataMemoryModelList = databaseHelper.getDataByMemoryId(newMemoryCreateDataList.get(position).getId());
        // Create a map to hold NewMemoryCreateData and its associated DataMemoryModels
        Map<String, Object> memoryDataMap = new HashMap<>();

        // Store NewMemoryCreateData information
        Map<String, Object> newMemoryData = new HashMap<>();
        newMemoryData.put("id", newMemoryCreateDataList.get(position).getId());
        newMemoryData.put("name", newMemoryCreateDataList.get(position).getName());
        newMemoryData.put("number", newMemoryCreateDataList.get(position).getNumber());

        memoryDataMap.put("NewMemoryCreateData", newMemoryData);

        // Store DataMemoryModels information
        Map<String, Object> dataMemoryModelsMap = new HashMap<>();
        for (int i = 0; i < dataMemoryModelList.size(); i++) {
            DataMemoryModel dataMemoryModel = dataMemoryModelList.get(i);
            Map<String, Object> memoryMap = new HashMap<>();
            memoryMap.put("id", dataMemoryModel.getId());
            memoryMap.put("image", downloadUrls.get(i));
            memoryMap.put("memory_id", dataMemoryModel.getMemory_id());
            memoryMap.put("date", dataMemoryModel.getDate());
            memoryMap.put("description", dataMemoryModel.getDescription());

            dataMemoryModelsMap.put(String.valueOf(dataMemoryModel.getId()), memoryMap);
        }

        memoryDataMap.put("DataMemoryModels", dataMemoryModelsMap);

        // Push the entire map containing NewMemoryCreateData and DataMemoryModels under the same key to Firebase
        String key = databaseReference.child("Memories").push().getKey();
        if (isNetworkAvailable(getActivity())) {
            databaseReference.child("Memories").child(key).setValue(memoryDataMap);
            loadingDialog.dismiss();
            //set the isSend data to 1
            boolean is = databaseHelper.updateMemory(newMemoryCreateDataList.get(position).getId(), 1);
            if (is) {
                Toast.makeText(getActivity(), "Data send successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Data send successfully :0", Toast.LENGTH_SHORT).show();
            }
            textView_with_key.setText(key);
            showKeyDialog.show();
            Log.d("key", "sendDataToServerFinalMethod: " + key);

            back_to_home.setOnClickListener(view -> {
                Intent i = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(i);
            });
        } else {
            loadingDialog.dismiss();
            Toast.makeText(getActivity(), "check internet connections", Toast.LENGTH_SHORT).show();
        }
    }

    public Uri convertByteArrayToUri(Context context, byte[] byteArray) {
        // Convert byte array to Bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        // Save the Bitmap to a file with a unique name based on memory ID
        File imageFile = saveBitmapToFile(context, bitmap, "image_" + Math.random() + ".jpg");

        // Get the URI from the file
        return Uri.fromFile(imageFile);
    }

    public void tryFirebaseSaveImage(int position) {
        int memory_id = newMemoryCreateDataList.get(position).getId();
        Map<Integer, String> downloadUrlMap = new HashMap<>(); // Use a map instead of a list
        List<DataMemoryModel> dataMemoryModelList = databaseHelper.getDataByMemoryId(memory_id);
        if (isNetworkAvailable(getActivity())) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("data1").child("Images/");

            for (int i = 0; i < dataMemoryModelList.size(); i++) {
                if (dataMemoryModelList.get(i).getImage() != null) {
                    Uri imageUri = convertByteArrayToUri(getActivity(), dataMemoryModelList.get(i).getImage());
                    int main_id = dataMemoryModelList.get(i).getId();
                    StorageReference ref = storageReference.child(String.valueOf(main_id));
                    UploadTask uploadTask = ref.putFile(imageUri);
                    final int currentIndex = i; // Keep track of current index
                    uploadTask.addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> {
                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            if (uri == null) {
                                downloadUrlMap.put(currentIndex, "null1"); // Associate index with URL
                            } else {
                                downloadUrlMap.put(currentIndex, uri.toString()); // Associate index with URL
                            }
                            if (downloadUrlMap.size() == dataMemoryModelList.size()) {
                                List<String> downloadUrlList = new ArrayList<>();
                                for (int j = 0; j < dataMemoryModelList.size(); j++) {
                                    downloadUrlList.add(downloadUrlMap.get(j)); // Create a list in correct order
                                }
                                sendDataToServerFinalMethod(position, downloadUrlList);
                            }
                            if (getActivity() != null) {
                                Toast.makeText(getActivity(), "success:", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } else {
                    downloadUrlMap.put(i, "null");
                    if (downloadUrlMap.size() == dataMemoryModelList.size()) {
                        List<String> downloadUrlList = new ArrayList<>();
                        for (int j = 0; j < dataMemoryModelList.size(); j++) {
                            downloadUrlList.add(downloadUrlMap.get(j)); // Create a list in correct order
                        }
                        sendDataToServerFinalMethod(position, downloadUrlList);
                    }
                }
            }
        } else {
            loadingDialog.dismiss();
            Toast.makeText(getActivity(), "check internet connections", Toast.LENGTH_SHORT).show();
        }
    }


//public void tryFirebaseSaveImage(int position) {
//    int memory_id = newMemoryCreateDataList.get(position).getId();
//    List<String> downloadUrlArray = new ArrayList<>();
//    List<DataMemoryModel> dataMemoryModelList = databaseHelper.getDataByMemoryId(memory_id);
//    if (isNetworkAvailable(getActivity())) {
//        StorageReference storageReference = FirebaseStorage.getInstance().getReference("data1").child("Images/");
//
//        for (int i = 0; i < dataMemoryModelList.size(); i++) {
//            final int currentIndex = i;
//            if (dataMemoryModelList.get(i).getImage() != null) {
//                Uri imageUri = convertByteArrayToUri(getActivity(), dataMemoryModelList.get(i).getImage());
//                int main_id = dataMemoryModelList.get(i).getId();
//                StorageReference ref = storageReference.child(String.valueOf(main_id));
//                UploadTask uploadTask = ref.putFile(imageUri);
//                int finalI = i;
//                uploadTask.addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> {
//                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
//                        if (uri == null) {
//                            downloadUrlArray.add(currentIndex, "null1");
//                        } else {
//                            Log.d("TAGkey1", "tryFirebaseSaveImage: "+currentIndex +"i-?>?"+ finalI);
//                            downloadUrlArray.add(currentIndex, uri.toString());
//                        }
//                        if (downloadUrlArray.size() == dataMemoryModelList.size()) {
//                            sendDataToServerFinalMethod(position, downloadUrlArray);
//                        }
//                        if (getActivity() != null) {
//                            Toast.makeText(getActivity(), "success:", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                });
//            } else {
//                downloadUrlArray.add(i, "null");
//                if (downloadUrlArray.size() == dataMemoryModelList.size()) {
//                    sendDataToServerFinalMethod(position, downloadUrlArray);
//                }
//            }
//        }
//    } else {
//        loadingDialog.dismiss();
//        Toast.makeText(getActivity(), "check internet connections", Toast.LENGTH_SHORT).show();
//    }
//}

    // Method to check internet connectivity
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private File saveBitmapToFile(Context context, Bitmap bitmap, String fileName) {
        File filesDir = context.getFilesDir();
        File imageFile = new File(filesDir, fileName); // Use the provided file name

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageFile;
    }

    @Override
    public void onItemLongClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete this memory");
        builder.setMessage("Are you sure you wants to delete this memory");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                loadingDialog.show();

                int id_for_delete = newMemoryCreateDataList.get(position).getId();
                boolean b = databaseHelper.deleteMemoryData(id_for_delete);
                boolean b1 = databaseHelper.deleteData(id_for_delete);

                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (b || b1) {
                            loadingDialog.dismiss();
                            newMemoryCreateDataList.remove(position);
                            addMemoryCardAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(getActivity(), "some error occurred!", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                handler.postDelayed(runnable, 1500);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}