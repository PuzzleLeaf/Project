package com.tistory.puzzleleaf.rankofalcohol.rank;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tistory.puzzleleaf.rankofalcohol.R;
import com.tistory.puzzleleaf.rankofalcohol.model.AlcoholObject;
import com.tistory.puzzleleaf.rankofalcohol.rank.adapter.RankRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class RankDetailActivity extends AppCompatActivity {

    //recycler
    private LinearLayoutManager linearLayoutManager;
    private RankRecyclerAdapter rankRecyclerAdapter;
    @BindView(R.id.rank_detail_recycler_view) RecyclerView rankDetailRecyclerView;
    List<AlcoholObject> alcoholObjectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        alcoholObjectList = intent.getParcelableArrayListExtra("data");
        //Recycler
        rankRecyclerAdapter = new RankRecyclerAdapter(this, alcoholObjectList);
        linearLayoutManager = new LinearLayoutManager(this);
        rankDetailRecyclerView.setLayoutManager(linearLayoutManager);
        rankDetailRecyclerView.setAdapter(rankRecyclerAdapter);

    }
}
