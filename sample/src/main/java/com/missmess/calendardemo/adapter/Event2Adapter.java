package com.missmess.calendardemo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author wl
 * @since 2018/01/11 18:15
 */
public class Event2Adapter extends RecyclerView.Adapter<Event2Adapter.ViewHolder> {
    private boolean avail = true;

    public void setAvail(boolean avail) {
        if (this.avail == avail)
            return;
        this.avail = avail;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView textView = new TextView(parent.getContext());
        textView.setPadding(40, 40, 40, 40);
        return new ViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tv.setText("测试数据" + position);
    }

    @Override
    public int getItemCount() {
        if (avail)
        return 13;
        else return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView;
        }
    }
}
