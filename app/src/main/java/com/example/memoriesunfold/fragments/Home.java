package com.example.memoriesunfold.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.memoriesunfold.R;
import com.example.memoriesunfold.adapter.DashboardCardsAdapter;

import java.util.ArrayList;

public class Home extends Fragment {
    ArrayList<SlideModel> imageArraylist = new ArrayList<>();
    ImageSlider imageSlider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        imageSlider = rootView.findViewById(R.id.image_slider);

        imageArraylist.add(new SlideModel(R.drawable.create_memory,"Create your memories", ScaleTypes.FIT));
        imageArraylist.add(new SlideModel(R.drawable.share_loves,"share your love through the memories", ScaleTypes.FIT));
        imageArraylist.add(new SlideModel(R.drawable.capture_their_reaction,"capture the reaction with this surprise", ScaleTypes.FIT));

        imageSlider.setImageList(imageArraylist);

        return rootView;
    }

}