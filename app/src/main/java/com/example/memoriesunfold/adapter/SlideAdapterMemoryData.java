package com.example.memoriesunfold.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.memoriesunfold.Database.DatabaseHelper;
import com.example.memoriesunfold.MainActivity;
import com.example.memoriesunfold.R;
import com.example.memoriesunfold.model.DataMemoryModel;
import com.example.memoriesunfold.model.DataMemoryModelView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class SlideAdapterMemoryData extends PagerAdapter {

    Context context;
    byte[] imageArray;
    DatabaseHelper databaseHelper;
    ArrayList<DataMemoryModel> arrayList;
    ArrayList<DataMemoryModel> updatedArrayList = new ArrayList<>();
    Uri selectedImageUri;
    int Cards;
    ArrayList<DataMemoryModel> memoryArrayList = new ArrayList<>();
    ArrayList<DataMemoryModelView> memoryViewArrayList = new ArrayList<>();
    private ViewPager viewPager;
    int id;
    int id_main;
    String passId = null;
    Dialog loadingDialog;
    List<DataMemoryModel> dataMemoryModelList;
    List<DataMemoryModelView> dataMemoryModelViewList;

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    public SlideAdapterMemoryData(Context context, int cards, int id, List<DataMemoryModel> dataMemoryModelList) {
        this.context = context;
        this.Cards = cards;
        this.id = id;
        this.dataMemoryModelList = dataMemoryModelList;

        // Initialize memoryArrayList with the provided dataMemoryModelList
        this.memoryArrayList = new ArrayList<>(dataMemoryModelList);
    }

    public SlideAdapterMemoryData(Context context, int cards, int id, String passId, List<DataMemoryModelView> dataMemoryModelViewList) {
        this.context = context;
        this.Cards = cards;
        this.id = id;
        this.dataMemoryModelViewList = dataMemoryModelViewList;
        this.passId = passId;

        // Initialize memoryArrayList sswith the provided dataMemoryModelList
        this.memoryViewArrayList = new ArrayList<>(dataMemoryModelViewList);
    }


    @Override
    public int getCount() {
        return Cards;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.custom_slide_memory_data, container, false);
        TextView totalNumber = v.findViewById(R.id.totalNumber);
        ImageView imageView = v.findViewById(R.id.ImageAddNew);
        Button Next = v.findViewById(R.id.next);
        Button edit = v.findViewById(R.id.edit);
        EditText Date = v.findViewById(R.id.Date);
        EditText description = v.findViewById(R.id.Description);
        ProgressBar progressBar = v.findViewById(R.id.progressBar);
        LinearLayout layout = v.findViewById(R.id.linearLayout);
        LinearLayout submitDataToServerLinearLayout = v.findViewById(R.id.SubmitDataToServerLinearLayout);
        TextView submitDataToServer = v.findViewById(R.id.SubmitDataToServer);

        databaseHelper = new DatabaseHelper(context);

        //loading Dialog
        loadingDialog = new Dialog(context);
        loadingDialog.setContentView(R.layout.custom_loadig_dialog);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().getAttributes().windowAnimations = R.style.AlertDialogAnimation;

        //upload image and hide the layout and show the imageView
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageChosser();
            }
        });

        int pos = position + 1;
        String s = pos + "/" + Cards;
        totalNumber.setText(s);

        int cards = (pos * 100) / Cards;

        progressBar.setProgress(cards);
        int progress = progressBar.getProgress();


        //show this whe there exits a saved data inside
        if (dataMemoryModelList != null && !dataMemoryModelList.isEmpty()) {
            byte[] image = dataMemoryModelList.get(position).getImage();
            imageView.setVisibility(View.VISIBLE);
            layout.setVisibility(View.INVISIBLE);
            edit.setVisibility(View.VISIBLE);
            edit.setOnClickListener(view -> {
                imageView.setOnClickListener(view1 -> ImageChosser());
                Date.setClickable(true);
                description.setClickable(true);
                Next.setVisibility(View.VISIBLE);
            });
            if (image != null) {
                Bitmap bitmap = convertByteArrayToBitmap(image);
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.img);
            }
            Date.setText(dataMemoryModelList.get(position).getDate());
            description.setText(dataMemoryModelList.get(position).getDescription());
            Date.setClickable(false);
            description.setClickable(false);
            Next.setVisibility(View.GONE);
        } else if (dataMemoryModelViewList != null && !dataMemoryModelViewList.isEmpty()) {
            String image = dataMemoryModelViewList.get(position).getImage();
            imageView.setVisibility(View.VISIBLE);
            layout.setVisibility(View.INVISIBLE);
            if (image != null) {
                Picasso.get().load(image).into(imageView);
            } else {
                imageView.setImageResource(R.drawable.img);
            }
            Date.setText(dataMemoryModelViewList.get(position).getDate());
            description.setText(dataMemoryModelViewList.get(position).getDescription());
            Date.setClickable(false);
            description.setClickable(false);
            Next.setVisibility(View.GONE);
        }


        submitDataToServer.setOnClickListener(view -> {
            sendDataToServer();
        });


        //this help to change the next and save based on progress
        if (progress == 100) {
            Next.setText("SAVE");
        } else {
            Next.setText("NEXT");
        }

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String date = Date.getText().toString();
                String desc = description.getText().toString();


                //check for edit button click and next cliking functionallity changes
                if (!dataMemoryModelList.isEmpty()) {
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);

                    imageArray = byteArrayOutputStream.toByteArray();


                    id_main = dataMemoryModelList.get(position).getId();
                    DataMemoryModel updateDataModel = new DataMemoryModel(id_main, id, imageArray, date, desc);

                    int pos = viewPager.getCurrentItem();
                    memoryArrayList.set(pos, updateDataModel);

                    boolean updateSuccess = databaseHelper.UpdateMemoryData(id, memoryArrayList);
                    loadingDialog.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismiss();
                            if (updateSuccess) {
                                Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Update error", Toast.LENGTH_SHORT).show();
                            }
                            if (pos < Cards - 1) {
                                viewPager.setCurrentItem(pos + 1, true);
                            } else {
                                // Send data for future reference
                                Intent i = new Intent(context, MainActivity.class);
                                context.startActivity(i);
                            }
                        }
                    }, 2000);


                } else {

                    //for change the image while adding
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ImageChosser();
                        }
                    });

                    byte[] imageArray1 = null;

                    if (imageView.getDrawable() != null) {

                        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);

                        imageArray1 = byteArrayOutputStream.toByteArray();
                    }


                    DataMemoryModel updateDataModel = new DataMemoryModel(id, imageArray1, date, desc);
                    memoryArrayList.add(updateDataModel);
                    int pos = viewPager.getCurrentItem();

                    if (pos < Cards - 1) {
                        viewPager.setCurrentItem(pos + 1, true);
                    } else {
                        arrayList = getMemoryArrayList();
                        boolean addSuccess = databaseHelper.AddMemoryData(id, arrayList);
                        loadingDialog.show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                if (addSuccess) {
                                    Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Add error", Toast.LENGTH_SHORT).show();
                                }

                                Intent i = new Intent(context, MainActivity.class);
                                context.startActivity(i);
                            }
                        }, 2000);
                    }
                }
            }
        });

        v.setTag(position);
        container.addView(v);
        return v;

    }

    private void sendDataToServer() {

        loadingDialog.show();

        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Are you sure");
                builder.setMessage("Please make sure it can't be edit after send !");
                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // here set method for send the data to server and delete this memory from database by delete query set
                        //delete this data here

                        loadingDialog.show();
                        sendDataToServerFinalMethod();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        };

        handler.postDelayed(runnable, 1800);

    }

    private void sendDataToServerFinalMethod() {
        //data send mehtod here
        loadingDialog.dismiss();
        Toast.makeText(context, "Data send successfully", Toast.LENGTH_SHORT).show();
    }

    //save the image in byte array format in database
    private byte[] convertImageUriToByteArray(Context context, Uri imageUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

        if (inputStream != null) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }

        return null;
    }

    //change byte array to back to image
    public Bitmap convertByteArrayToBitmap(byte[] byteArray) {
        if (byteArray != null) {
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } else {
            return null;
        }
    }

    private void ImageChosser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        ((Activity) context).startActivityForResult(Intent.createChooser(i, "Select Picture"), 100);

    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            // Retrieve the selected image URI and handle it as needed
            selectedImageUri = data.getData();
            try {
                imageArray = convertImageUriToByteArray(context, selectedImageUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            updateCurrentItemWithImage(selectedImageUri);
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateCurrentItemWithImage(Uri selectedImageUri) {
        // Get the currently displayed view in the ViewPager
        View currentItemView = (View) viewPager.findViewWithTag(viewPager.getCurrentItem());

        if (currentItemView != null) {
            // Find the ImageView in the current view
            ImageView imageView = currentItemView.findViewById(R.id.ImageAddNew);
            LinearLayout layout = currentItemView.findViewById(R.id.linearLayout);

            // Update the ImageView with the selected image
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageURI(selectedImageUri);
            layout.setVisibility(View.GONE);
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public ArrayList<DataMemoryModel> getMemoryArrayList() {
        return memoryArrayList;
    }
}
