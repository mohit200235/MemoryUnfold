package com.example.memoriesunfold.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriesunfold.R;
import com.example.memoriesunfold.model.NewMemoryCreateData;

import java.util.ArrayList;

public class ViewMemoryCardAdapter extends RecyclerView.Adapter<ViewMemoryCardAdapter.ViewHolder> {

    Context context;
    ArrayList<NewMemoryCreateData> newMemoryCreateDataArrayList;

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    private OnItemLongClickListener longClickListener;

    private OnItemClickListener onItemClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


    public ViewMemoryCardAdapter(Context context , ArrayList<NewMemoryCreateData> newMemoryCreateDataArrayList){
        this.context =context;
        this.newMemoryCreateDataArrayList = newMemoryCreateDataArrayList;
    }

    @NonNull
    @Override
    public ViewMemoryCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.view_memory_custom_adapter_file,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewMemoryCardAdapter.ViewHolder holder, int position) {

        Animation animationZoomIn = AnimationUtils.loadAnimation(context, R.anim.zoom_out);
        holder.text_memory.setAnimation(animationZoomIn);

        String stringWithLineBreaks = newMemoryCreateDataArrayList.get(position).getName().replace(" ", "\n");
        holder.text_memory.setText(stringWithLineBreaks);

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

        LinearLayout relativeLayout;
        TextView text_memory;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            relativeLayout = itemView.findViewById(R.id.view_relative_layout);
            text_memory =itemView.findViewById(R.id.name_of_memory);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }
}
