package com.amsavarthan.lvstore;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

/**
 * Created by amsavarthan on 9/2/19.
 */

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private List<Apps> appsList;
    private Context context;
    private Activity activity;

    public Adapter(List<Apps> appsList, Context context, Activity activity) {
        this.appsList = appsList;
        this.context = context;
        this.activity=activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app,parent,false);

        return new ViewHolder(view);
    }

	@Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final Apps app=appsList.get(position);

        holder.name.setText(app.getName());
        holder.version.setText(String.format(" %s ", app.getVersion()));
        holder.release.setText(String.format(" %s ", app.getRelease()));
        holder.short_d.setText(app.getShort_d());

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.mipmap.placeholder))
                .load(app.getLogo())
                .into(holder.logo);

        holder.item.setOnClickListener(v -> AppDetails.startActivity(activity,context,holder.logo,app.getName(),app.getVersion(),app.getRelease(),app.getLong_d(),app.getLogo()
        ,app.getS1(),app.getS2(),app.getS3(),app.getS4(),app.getS5(),app.getS6(),app.getS7(),app.getS8(),app.getS9(),app.getDownload_link()));

    }

    @Override
    public int getItemCount() {
        return appsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,version,release,short_d;
        ImageView logo;
        CardView item;

        public ViewHolder(View itemView) {
            super(itemView);

            item=itemView.findViewById(R.id.item);
            name=itemView.findViewById(R.id.name);
            version=itemView.findViewById(R.id.version);
            release=itemView.findViewById(R.id.release);
            logo=itemView.findViewById(R.id.logo);
            short_d=itemView.findViewById(R.id.short_d);


        }
    }
}
