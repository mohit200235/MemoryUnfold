package com.example.memoriesunfold.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriesunfold.R;

import java.util.ArrayList;

public class DashboardCardsAdapter extends RecyclerView.Adapter<DashboardCardsAdapter.ViewHolder> {

    Context context;
    ArrayList<String> arrayListText;
    ArrayList<Integer> arrayListImages;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public DashboardCardsAdapter(Context context, ArrayList<String> arrayListText, ArrayList<Integer> arrayListImages) {
        this.context = context;
        this.arrayListText = arrayListText;
        this.arrayListImages = arrayListImages;
    }

    @NonNull
    @Override
    public DashboardCardsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.custom_dashboard_cards, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardCardsAdapter.ViewHolder holder, int position) {
        holder.button.setText(arrayListText.get(position));
        Drawable drawable = context.getResources().getDrawable(arrayListImages.get(position), null);
        holder.relativeLayout.setBackground(drawable);
        holder.button.setText(arrayListText.get(position));
        // Set an OnClickListener for the button
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayListText.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout relativeLayout;
        private Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.btn_text);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);
        }

    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}