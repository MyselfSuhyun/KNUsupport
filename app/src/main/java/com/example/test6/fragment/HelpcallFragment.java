package com.example.test6.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.test6.R;
import com.example.test6.helpchat.CallActivity;
import com.example.test6.model.HelpModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HelpcallFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_helpcall, container, false);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.fragment_helpcall);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new HelpcallFragment.HelpcallFragmentRecyclerViewAdapter());


        return view;
    }

    class HelpcallFragmentRecyclerViewAdapter extends RecyclerView.Adapter {

        List<HelpModel> helpModels;

        public HelpcallFragmentRecyclerViewAdapter() {
            helpModels = new ArrayList<>();
            final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("helpcall").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    helpModels.clear();

                    for(DataSnapshot snapshot :dataSnapshot.getChildren()){


                        HelpModel helpModel = snapshot.getValue(HelpModel.class);

                        if(helpModel.matching.equals("매칭대기")!=true){
                            continue;
                        }


                        helpModels.add(helpModel);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_help,parent,false);


            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            ((CustomViewHolder)holder).helptitle.setText(helpModels.get(position).helptitle);
            ((CustomViewHolder)holder).helpcontent.setText(helpModels.get(position).helpcontent);
            ((CustomViewHolder)holder).helpname.setText(helpModels.get(position).helpanswer);
            ((CustomViewHolder)holder).helptime.setText(helpModels.get(position).helptime);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), CallActivity.class);
                    intent.putExtra("destinationUid",helpModels.get(position).myuid);
                    ActivityOptions activityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright,R.anim.toleft);
                        startActivity(intent,activityOptions.toBundle());
                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            return helpModels.size();
        }
    }
    private class CustomViewHolder extends RecyclerView.ViewHolder {
        public TextView helptitle,helpcontent,helpname,helptime;

        public CustomViewHolder(View view) {
            super(view);
            helptitle = (TextView) view.findViewById(R.id.helpItem_title);
            helpcontent = (TextView) view.findViewById(R.id.helpItem_content);
            helpname = (TextView) view.findViewById(R.id.helpItem_name);
            helptime = (TextView) view.findViewById(R.id.helpItem_timestamp);
        }
    }

}
