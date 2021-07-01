package com.example.sharemycar;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.MenuItem;

import com.example.sharemycar.historyRecyclerView.HistoryAdapter;
import com.example.sharemycar.historyRecyclerView.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.sharemycar.Constants.ACCOUNTS;
import static com.example.sharemycar.Constants.CUSTOMER_OR_DRIVER;

public class HistoryActivity extends AppCompatActivity {
    private String customerOrDriver,userId;
    private RecyclerView  mHistoryRecyclerView;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left);
        getSupportActionBar().setElevation(5);

        mHistoryRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(),HistoryActivity.this);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        customerOrDriver = getIntent().getExtras().getString(CUSTOMER_OR_DRIVER);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child(ACCOUNTS).child(customerOrDriver).child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot history : snapshot.getChildren()){
                        FetchRideInformation(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void FetchRideInformation(String rideKey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String rideId = snapshot.getKey();
                    Long timestamp = 0L;
                    for(DataSnapshot child : snapshot.getChildren()){
                        if (child.getKey().equals("timestamp")){
                            timestamp = Long.valueOf(child.getValue().toString());
                        }
                    }
                    HistoryObject obj = new HistoryObject(rideId,getData(timestamp));
                    resultsHistory.add(obj);

                    mHistoryAdapter.notifyDataSetChanged();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private String getData(Long timestamp) {

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm",cal).toString();

        return date;
    }

    private ArrayList resultsHistory = new ArrayList<HistoryObject>();
    private ArrayList<HistoryObject> getDataSetHistory() {
        return resultsHistory;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}