package com.hello.one.cabdrive.historyRecyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hello.one.cabdrive.R;

import java.util.List;

/**
 * Created by one on 10/21/2017.
 */

public class HistoryAdapters extends RecyclerView.Adapter<historyViewHolders> {

    private List<historyObjects> itemList;
    private Context context;

    public HistoryAdapters(List<historyObjects> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public historyViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        historyViewHolders rcv = new historyViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(historyViewHolders holder, final int position) {
        holder.rideId.setText(itemList.get(position).getRideId());
        if(itemList.get(position).getTime()!=null){
            holder.time.setText(itemList.get(position).getTime());
        }
    }
    @Override
    public int getItemCount() {
        return this.itemList.size();
    }

}
