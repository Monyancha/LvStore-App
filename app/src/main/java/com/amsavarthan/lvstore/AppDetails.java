package com.amsavarthan.lvstore;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE;
import static android.app.DownloadManager.EXTRA_DOWNLOAD_ID;

public class AppDetails extends AppCompatActivity{

    String name,version,release,long_d,logo,download_link;
    String s1,s2,s3,s4,s5,s6,s7,s8,s9;
    int count=0;
    TextView version_txt,release_txt,long_d_txt,name_text;
    ImageView logo_view;
    RecyclerView recyclerView;
    ScreenshotsAdapter adapter;
    List<Screenshots> screenshots=new ArrayList<>();

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public static void startActivity(Activity activity, Context context,View view, String name, String version, String release, String long_d, String logo,String s1,String s2,String s3,String s4,String s5,String s6,String s7,String s8,String s9,String download_link){
        if(Build.VERSION.SDK_INT>=21){
            context.startActivity(new Intent(context,AppDetails.class)
                            .putExtra("link",download_link)
                            .putExtra("s1",s1)
                            .putExtra("s2",s2)
                            .putExtra("s3",s3)
                            .putExtra("s4",s4)
                            .putExtra("s5",s5)
                            .putExtra("s6",s6)
                            .putExtra("s7",s7)
                            .putExtra("s8",s8)
                            .putExtra("s9",s9)
                            .putExtra("name",name)
                            .putExtra("version",version)
                            .putExtra("release",release)
                            .putExtra("long_d",long_d)
                            .putExtra("logo",logo),
                    ActivityOptions.makeSceneTransitionAnimation(activity, view,"logo").toBundle());
        }else{
            context.startActivity(new Intent(context,AppDetails.class)
                    .putExtra("link",download_link)
                    .putExtra("s1",s1)
                    .putExtra("s2",s2)
                    .putExtra("s3",s3)
                    .putExtra("s4",s4)
                    .putExtra("s5",s5)
                    .putExtra("s6",s6)
                    .putExtra("s7",s7)
                    .putExtra("s8",s8)
                    .putExtra("s9",s9)
                    .putExtra("name",name)
                    .putExtra("version",version)
                    .putExtra("release",release)
                    .putExtra("long_d",long_d)
                    .putExtra("logo",logo));
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);
        name=getIntent().getStringExtra("name");

        setReceiver();

        download_link=getIntent().getStringExtra("link");
        version=getIntent().getStringExtra("version");
        release=getIntent().getStringExtra("release");
        long_d=getIntent().getStringExtra("long_d");
        logo=getIntent().getStringExtra("logo");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("App Detail");

        version_txt=findViewById(R.id.version);
        release_txt=findViewById(R.id.release);
        long_d_txt=findViewById(R.id.long_d);
        logo_view=findViewById(R.id.logo);
        name_text=findViewById(R.id.name);

        name_text.setText(name);
        version_txt.setText(String.format("Version : %s",version));
        release_txt.setText(String.format("Release : %s",release));
        long_d_txt.setText(long_d);

        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.mipmap.placeholder))
                .load(logo)
                .into(logo_view);

        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter=new ScreenshotsAdapter(screenshots,this);
        recyclerView.setAdapter(adapter);

        setScreenshots();

        FloatingActionButton fab=findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(AppDetails.this);
            builder.setTitle("Download "+name);
            builder.setMessage("Are you sure do you want to download "+name+" ?");
            builder.setPositiveButton("Yes", (dialog, which) -> {

                PermissionListener listener= SnackbarOnDeniedPermissionListener.Builder
                        .with(findViewById(R.id.layout),"Storage permission is required for downloading files.")
                        .withOpenSettingsButton("Settings")
                        .build();

                Dexter.withActivity(this)
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new CompositePermissionListener(listener, new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                downloadImage(download_link);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                            }
                        }))
                        .check();

            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog=builder.create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.setCancelable(true);
            dialog.show();
        });

    }

    ArrayList<Long> list=new ArrayList<>();
    private long refid;
    private DownloadManager downloadManager;
    public BroadcastReceiver onComplete=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            long referenceId=intent.getLongExtra(EXTRA_DOWNLOAD_ID,-1);
            list.remove(referenceId);
            if(list.isEmpty()){
                NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    setupChannels(notificationManager);
                }
                NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(AppDetails.this,"admin_id");
                Notification notification;
                notification=mBuilder
                        .setAutoCancel(true)
                        .setContentTitle("Download completed")
                        .setColorized(true)
                        .setSmallIcon(R.drawable.ic_file_download_white_24dp)
                        .setContentText("File has been downloaded successfully")
                        .build();
                notificationManager.notify(0,notification);
                try{
                    Snackbar.make(findViewById(R.id.layout),"Download Success",Snackbar.LENGTH_LONG).show();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(context, "Download success", Toast.LENGTH_SHORT).show();
                }
            }

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager) {

        CharSequence name="Downloads";
        String desc="Used to show progress of Downloads";
        NotificationChannel channel;
        channel=new NotificationChannel("admin_id",name,NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(desc);
        channel.enableVibration(true);
        if(notificationManager!=null){
            notificationManager.createNotificationChannel(channel);
        }

    }

    private void setReceiver() {

        downloadManager=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        registerReceiver(onComplete,new IntentFilter(ACTION_DOWNLOAD_COMPLETE));

    }


    private void downloadImage(String download_link) {

        try {
            DownloadManager.Request request=new DownloadManager.Request(Uri.parse(download_link));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI);
            request.setAllowedOverRoaming(true);
            request.setVisibleInDownloadsUi(true);
            request.setTitle(name);
            request.setDescription("Download in progress...");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"/LvStore/"+name+"_"+version+".apk");

            Snackbar.make(findViewById(R.id.layout),"Downloading...",Snackbar.LENGTH_LONG).show();

            refid=downloadManager.enqueue(request);
            list.add(refid);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setScreenshots() {

        s1=getIntent().getStringExtra("s1");
        s2=getIntent().getStringExtra("s2");
        s3=getIntent().getStringExtra("s3");
        s4=getIntent().getStringExtra("s4");
        s5=getIntent().getStringExtra("s5");
        s6=getIntent().getStringExtra("s6");
        s7=getIntent().getStringExtra("s7");
        s8=getIntent().getStringExtra("s8");
        s9=getIntent().getStringExtra("s9");

        if(!TextUtils.isEmpty(s1)){
            screenshots.add(new Screenshots(s1));
            ++count;
        }
        if(!TextUtils.isEmpty(s2)){
            screenshots.add(new Screenshots(s2));
            ++count;
        }
        if(!TextUtils.isEmpty(s3)){
            screenshots.add(new Screenshots(s3));
            ++count;
        }
        if(!TextUtils.isEmpty(s4)){
            screenshots.add(new Screenshots(s4));
            ++count;
        }
        if(!TextUtils.isEmpty(s5)){
            screenshots.add(new Screenshots(s5));
            ++count;
        }
        if(!TextUtils.isEmpty(s6)){
            screenshots.add(new Screenshots(s6));
            ++count;
        }
        if(!TextUtils.isEmpty(s7)){
            screenshots.add(new Screenshots(s7));
            ++count;
        }
        if(!TextUtils.isEmpty(s8)){
            screenshots.add(new Screenshots(s8));
            ++count;
        }
        if(!TextUtils.isEmpty(s9)){
            screenshots.add(new Screenshots(s9));
            ++count;
        }

        if(count>0){
            findViewById(R.id.screenshots).setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }

    }

}
