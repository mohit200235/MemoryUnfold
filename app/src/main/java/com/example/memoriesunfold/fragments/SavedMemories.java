package com.example.memoriesunfold.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.memoriesunfold.Database.DatabaseHelper;
import com.example.memoriesunfold.MaiUi.AddNewMemory;
import com.example.memoriesunfold.MaiUi.MemoryData;
import com.example.memoriesunfold.R;
import com.example.memoriesunfold.adapter.AddMemoryCardAdapter;
import com.example.memoriesunfold.adapter.ViewMemoryCardAdapter;
import com.example.memoriesunfold.model.DataMemoryModel;
import com.example.memoriesunfold.model.NewMemoryCreateData;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavedMemories extends Fragment implements ViewMemoryCardAdapter.OnItemClickListener, ViewMemoryCardAdapter.OnItemLongClickListener {

    DatabaseHelper databaseHelper;
    RecyclerView recyclerView_saved;

    TextView noRecordFound;
    Dialog loadingDialog;

    String imageUrl;
    List<String> imageListUrl = new ArrayList<>();

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
        loadingDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().getAttributes().windowAnimations = R.style.AlertDialogAnimation;
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
//                        saveImageToStorage(position);
//                        sendDataToServerFinalMethod(position);
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
//        for (DataMemoryModel dataMemoryModel : dataMemoryModelList) {
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
            Log.d("key123", "sendDataToServerFinalMethod: " + key);
            databaseReference.child("Memories").child(key).setValue(memoryDataMap);
            loadingDialog.dismiss();
            Toast.makeText(getActivity(), "Data send successfully", Toast.LENGTH_SHORT).show();
        } else {
            loadingDialog.dismiss();
            Toast.makeText(getActivity(), "check internet connections", Toast.LENGTH_SHORT).show();
        }
    }

//    public void saveImageToStorage(int position) {
//        List<DataMemoryModel> dataMemoryModelList = databaseHelper.getDataByMemoryId(newMemoryCreateDataList.get(position).getId());
//        Map<String, Object> dataMemoryModelsMap = new HashMap<>();
//
//        for (int i = 0; i < dataMemoryModelList.size(); i++) {
//            DataMemoryModel dataMemoryModel = dataMemoryModelList.get(i);
//            Map<String, Object> memoryMap = new HashMap<>();
//            memoryMap.put("id", dataMemoryModel.getId());
//
//            if (dataMemoryModel.getImage() != null) {
//                Uri imageUri = convertByteArrayToUri(getActivity(), dataMemoryModel.getImage(), dataMemoryModel.getId());
//                // Use a unique key for each image, for example, based on the image index or ID
//                memoryMap.put("image_" + i + dataMemoryModel.getId(), imageUri.toString());
//            }
//
//            dataMemoryModelsMap.put("images_" + i + "_" + dataMemoryModel.getId(), memoryMap);
//            loadingDialog.dismiss();
//            Log.d("imagurls", "saveImageToStorage: " + memoryMap);
//        }
//
//        Map<String, Object> memoryMap2 = new HashMap<>();
//        memoryMap2.put("myimages", dataMemoryModelsMap);
//        Log.d("imagurls2", "saveImageToStorage: " + memoryMap2);
//
//        //to get all images with corresponding ids
//        Map<String, Object> myImagesMap = (Map<String, Object>) memoryMap2.get("myimages");
//
//        for (Map.Entry<String, Object> entry : myImagesMap.entrySet()) {
//            String imageKey = entry.getKey();
//            Map<String, Object> imageMap = (Map<String, Object>) entry.getValue();
//
//            String id = imageMap.get("id").toString();
//            if (imageMap.get("image_" + imageKey.substring(7)) != null) {
//                Uri imageUrl5 = Uri.parse(imageMap.get("image_" + imageKey.substring(7)).toString());
//                Log.d("imagurls4", "saveImageToStorage: " + id + "\n" + imageUrl5);
//                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Memory_Data_images")
//                        .child(id);
//
//                storageReference.putFile(imageUrl5).addOnSuccessListener(taskSnapshot -> {
//                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
//                    while (!uriTask.isComplete()) ;
//                    Uri uriImage = uriTask.getResult();
//                    imageUrl = uriImage.toString();
//
//                    imageListUrl.add(imageUrl);
//                    Log.d("imagurl3", "onSuccess: " + imageUrl);
//
//                    // Check if all images have been processed and URLs obtained
//                    if (imageListUrl.size() == myImagesMap.size()) {
//                        sendDataToServerFinalMethod(position);
//                    }
//                }).addOnFailureListener(e -> {
//                    Log.d("fireerror", "onFailure: " + e);
//                    imageListUrl.add("null"); // Add null if there's a failure
//                });
//            } else {
//                imageListUrl.add("null");
//            }
//        }
//        Log.d("imaglist", "saveImageToStorage: " + imageListUrl);
//    }


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
        List<String> downloadUrlArray = new ArrayList<>();
        List<DataMemoryModel> dataMemoryModelList = databaseHelper.getDataByMemoryId(memory_id);
        if (isNetworkAvailable(getActivity())) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("data1").child("Images/").child(String.valueOf(Math.random()));
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("data1").child("images/");
            for (int i = 0; i < dataMemoryModelList.size(); i++) {
                final int index = i;
                if (dataMemoryModelList.get(i).getImage() != null) {
                    Uri imageUri = convertByteArrayToUri(getActivity(), dataMemoryModelList.get(i).getImage());
                    int main_id = dataMemoryModelList.get(i).getId();
                    StorageReference ref = storageReference.child(String.valueOf(main_id));
                    UploadTask uploadTask = ref.putFile(imageUri);
                    uploadTask.addOnSuccessListener((UploadTask.TaskSnapshot taskSnapshot) -> {
//                                ExecutorService service = Executors.newSingleThreadExecutor();
//                                service.execute(new Runnable() {
//                                    @Override
//                                    public void run() {
                                        ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                            Log.d("imageurl", "tryFirebaseSaveImage: "+uri);
                                            if (uri == null) {
                                                downloadUrlArray.add("null1");
                                            }else {

                                                downloadUrlArray.add(uri.toString());
                                            }
                                            Log.d("imageUpload", "onSuccess: " + downloadUrlArray);
                                            if (downloadUrlArray.size() == dataMemoryModelList.size()) {
                                                sendDataToServerFinalMethod(position, downloadUrlArray);
                                                Log.d("firebaseimage2", "tryFirebaseSaveImage: " + downloadUrlArray);
                                            }
                                            if (getActivity() != null) {
                                                Toast.makeText(getActivity(), "success:", Toast.LENGTH_SHORT).show();
                                            }
                                        });
//                                    }
//                                });

                            }
                    );
                    Log.d("firebaseimage", "tryFirebaseSaveImage: " + imageUri);
                } else {
                    downloadUrlArray.add("null");
                    Log.d("imageurl", "tryFirebaseSaveImage: call");
                    if (downloadUrlArray.size() == dataMemoryModelList.size()) {
                        sendDataToServerFinalMethod(position, downloadUrlArray);
                        Log.d("firebaseimage2", "tryFirebaseSaveImage: " + downloadUrlArray);
                    }
//                int main_id=dataMemoryModelList.get(i).getId();
//                StorageReference ref =storageReference
//                        .child(String.valueOf(main_id));
//                UploadTask uploadTask =ref
//                        .putBytes("".getBytes());
////                UploadTask uploadTask = storageReference.child("Images").child(memory_id+"").child(String.valueOf(i)).putBytes("".getBytes());
//                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Toast.makeText(getActivity(), "success:", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                uriArray.add("null");
                }
            }
        } else {
            loadingDialog.dismiss();
            Toast.makeText(getActivity(), "check internet connections", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to check internet connectivity
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    //    public void getDataFromServer() {
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//        databaseReference.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                Object obj = snapshot.getValue();
//                parseMemoryData(obj);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//    public void parseMemoryData(Object memoryData) {
//        try {
//            JSONObject memoryOject = new JSONObject((Map) memoryData);
//            JSONObject getAllData = memoryOject.getJSONObject("Memories");
//            JSONObject filteredByKey = getAllData.getJSONObject("-Nl-2u8xb_Bgy-UXgSfV");
//            JSONObject DataMemoryModels = filteredByKey.getJSONObject("DataMemoryModels");
//            // Getting an iterator for the keys in "DataMemoryModels"
//            Iterator<String> keys = DataMemoryModels.keys();
//
//            // Looping through the keys and accessing each object
//            while (keys.hasNext()) {
//                String key = keys.next();
//                JSONObject innerObject = DataMemoryModels.getJSONObject(key);
//                Log.d("childVe1111", "parseMemoryData: - date :" + innerObject.get("date") + "image: " + innerObject.get("image"));
//            }
//        } catch (JSONException e) {
//            throw new RuntimeException(e);
//        }
//    }
//    public String decodeImageUrl(String img) {
//        try {
//            String decodedURL = URLDecoder.decode(img, "UTF-8");
//            return decodedURL;
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//    public Uri convertByteArrayToUri(Context context, byte[] byteArray, int memoryId) {
//        // Convert byte array to Bitmap
//        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//
//        // Save the Bitmap to a file with a unique name based on memory ID
//        File imageFile = saveBitmapToFile(context, bitmap, "image_" + memoryId + ".jpg");
//
//        // Get the URI from the file
//        return Uri.fromFile(imageFile);
//    }
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