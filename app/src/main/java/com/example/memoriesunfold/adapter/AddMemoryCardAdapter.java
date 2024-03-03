package com.example.memoriesunfold.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriesunfold.Database.DatabaseHelper;
import com.example.memoriesunfold.R;
import com.example.memoriesunfold.model.NewMemoryCreateData;

import java.util.ArrayList;

public class AddMemoryCardAdapter extends RecyclerView.Adapter<AddMemoryCardAdapter.ViewHolder> {

    Context context;
    ArrayList<NewMemoryCreateData> newMemoryCreateDataArrayList;

    private OnItemClickListener onItemClickListener;
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public AddMemoryCardAdapter(Context context , ArrayList<NewMemoryCreateData> newMemoryCreateDataArrayList){
        this.context =context;
        this.newMemoryCreateDataArrayList = newMemoryCreateDataArrayList;
    }



    @NonNull
    @Override
    public AddMemoryCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.custom_add_new_memory_card_layout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AddMemoryCardAdapter.ViewHolder holder, int position) {

        holder.memoryText.setText(newMemoryCreateDataArrayList.get(position).getName());

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener!=null){
                    onItemClickListener.onItemClick(position);
                }
            }
        });

        holder.relativeLayout.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
            }
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return newMemoryCreateDataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        Button memoryText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            relativeLayout = itemView.findViewById(R.id.relativeLayoutAddMemory);
            memoryText  =itemView.findViewById(R.id.memory_text);
        }
    }

    public void deleteItem(int position){
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        if (!databaseHelper.getDataByMemoryId(1).isEmpty()){

        }

    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
}
