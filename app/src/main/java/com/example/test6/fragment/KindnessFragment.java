package com.example.test6.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test6.GreatActivity;
import com.example.test6.R;
import com.example.test6.helpchat.CallActivity;
import com.example.test6.model.GreatModel;
import com.example.test6.model.HelpModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class KindnessFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kindnesslist, container, false);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.fragment_kindnesslist);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new KindnessFragment.KindnessFragmentRecyclerViewAdapter());


        return view;
    }

    class KindnessFragmentRecyclerViewAdapter extends RecyclerView.Adapter {

        List<GreatModel> greatModels;

        public KindnessFragmentRecyclerViewAdapter() {
            greatModels = new ArrayList<>();
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("Scores").child(myUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    greatModels.clear();

                    for(DataSnapshot snapshot :dataSnapshot.getChildren()){


                        GreatModel greatModel = snapshot.getValue(GreatModel.class);

                        if(greatModel.MeetVisible.equals("Unvisible")==true){
                            continue;
                        }




                        greatModels.add(greatModel);
                    }
                    notifyDataSetChanged();

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kindnes,parent,false);


            return new KindnessFragment.CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            ((KindnessFragment.CustomViewHolder)holder).greattitle.setText(greatModels.get(position).MeetContext);
            ((KindnessFragment.CustomViewHolder)holder).greattime.setText(greatModels.get(position).Meettime);
            ((KindnessFragment.CustomViewHolder)holder).meetrating.setRating(greatModels.get(position).MeetScore);



        }

        @Override
        public int getItemCount() {
            return greatModels.size();
        }
    }
    private class CustomViewHolder extends RecyclerView.ViewHolder {
        public TextView greattitle,greattime;
        public RatingBar meetrating;

        public CustomViewHolder(View view) {
            super(view);
            meetrating = (RatingBar) view.findViewById(R.id.greatItem_rating);
            greattitle = (TextView) view.findViewById(R.id.greatItem_title);
            greattime = (TextView) view.findViewById(R.id.greatItem_timestamp);
        }
    }

}
