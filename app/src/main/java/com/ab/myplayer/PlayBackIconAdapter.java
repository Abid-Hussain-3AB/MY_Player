package com.ab.myplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlayBackIconAdapter extends RecyclerView.Adapter<PlayBackIconAdapter.ViewHolder> {
    private ArrayList<IconModel> iconModelsList;
    private Context context;
    private onItemClickListener mListener;
    public interface onItemClickListener
    {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(onItemClickListener listener)
    {
        mListener = listener;
    }

    public PlayBackIconAdapter(ArrayList<IconModel> iconModelsList, Context context) {
        this.iconModelsList = iconModelsList;
        this.context = context;
    }

    @NonNull
    @Override
    public PlayBackIconAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.icon_layout,parent,false);
        return new ViewHolder(view,mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayBackIconAdapter.ViewHolder holder, int position) {
        holder.icon.setImageResource(iconModelsList.get(position).getImageView());
        holder.iconName.setText(iconModelsList.get(position).getIconTitle());

    }

    @Override
    public int getItemCount() {
        return iconModelsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView iconName;
        ImageView icon;
        public ViewHolder(@NonNull View itemView, onItemClickListener listener) {
            super(itemView);
            icon = itemView.findViewById(R.id.playback_icon);
            iconName = itemView.findViewById(R.id.icon_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!=null)
                    {
                        int position = getAdapterPosition();
                        if (position!=RecyclerView.NO_POSITION)
                        {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
