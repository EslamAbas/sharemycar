package com.example.sharemycar.historyRecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.sharemycar.R;

import androidx.recyclerview.widget.RecyclerView;

public class HistoryViewHolders  extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView rideId;
    public TextView time;

    public  HistoryViewHolders(View itemView){
        super (itemView);
        itemView.setOnClickListener(this);

        rideId = (TextView) itemView.findViewById(R.id.rideId);

        time = (TextView) itemView.findViewById(R.id.time );
    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent(view.getContext(),HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId",rideId.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);

    }
}
