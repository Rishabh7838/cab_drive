package com.hello.one.cabdrive.historyRecyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.hello.one.cabdrive.HistorySingleActivity;
import com.hello.one.cabdrive.R;

/**
 * Created by one on 10/21/2017.
 */

public class historyViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener{

public TextView rideId;
public TextView time;
public historyViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideId = (TextView) itemView.findViewById(R.id.rideid);
        time = (TextView) itemView.findViewById(R.id.time);
        }


@Override
public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), HistorySingleActivity.class);
        Bundle b = new Bundle();
        b.putString("rideId", rideId.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);
        }
        }
