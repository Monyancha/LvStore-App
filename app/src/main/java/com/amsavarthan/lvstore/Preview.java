package com.amsavarthan.lvstore;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class Preview extends AppCompatActivity {

    private String url;

    public static void startActivity(Context context, String url){
        context.startActivity(new Intent(context,Preview.class).putExtra("url",url));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Screenshot");

        url=getIntent().getStringExtra("url");
        ImageView photoView=findViewById(R.id.photoView);
        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.placeholder))
                .load(url)
                .into(photoView);

    }
}
