package com.amsavarthan.lvstore;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static android.app.DownloadManager.ACTION_DOWNLOAD_COMPLETE;
import static android.app.DownloadManager.EXTRA_DOWNLOAD_ID;
import static android.content.Context.DOWNLOAD_SERVICE;

public class AppsFragment extends Fragment {

    View view;
    Context context;
    RecyclerView mRecyclerView;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseFirestore mFirestore;
    Adapter mAdapter;
    List<Apps> appsList=new ArrayList<>();

    public AppsFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.frag_apps,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context=view.getContext();

        mAuth= FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        mFirestore= FirebaseFirestore.getInstance();
        mRecyclerView=view.findViewById(R.id.recyclerView);

        setReceiver();

        LinearLayoutManager layoutManager=new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter=new Adapter(appsList,context,getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mFirestore.collection("Apps")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {

                    if(e!=null){
                        e.printStackTrace();
                        return;
                    }

                    for(DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()){

                        if(doc.getType()== DocumentChange.Type.ADDED){

                            if(doc.getDocument().getString("name").equals("LvStore")){
                                String version=doc.getDocument().getString("version");
                                String link=doc.getDocument().getString("link");

                                //TODO : change 'b' for all upcoming updates
                                if(!TextUtils.equals(version,"1.0")){
                                    Snackbar.make(view.findViewById(R.id.layout),"New Update Available",Snackbar.LENGTH_INDEFINITE)
                                            .setAction("Download", v -> {
                                                if(!TextUtils.isEmpty(link)) {
                                                    downloadUpdate(link, version);
                                                }
                                            })
                                            .show();
                                }

                            }

                        }

                    }

                });

        getApps();

    }

    private void getApps() {

        final ProgressDialog dialog=new ProgressDialog(context);
        dialog.setMessage("Fetching Apps....");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        try {
            mFirestore.collection("Apps")
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {

                        if (e != null) {
                            dialog.dismiss();
                            Toast.makeText(context, "Some error occurred, Please report to the developer.", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                            return;
                        }

                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                            if (documentChange.getType() == DocumentChange.Type.ADDED) {

                                if (!TextUtils.equals(documentChange.getDocument().getString("name"), "LvStore")){
                                    Apps app = documentChange.getDocument().toObject(Apps.class).withId(documentChange.getDocument().getId());
                                    appsList.add(app);
                                    mAdapter.notifyDataSetChanged();
                                    dialog.dismiss();

                                }

                            }

                        }

                    });
        }catch (Exception e){
            e.printStackTrace();
            dialog.dismiss();
        }

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
                NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                    setupChannels(notificationManager);
                }
                NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(context,"admin_id");
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
                    Snackbar.make(view.findViewById(R.id.layout),"Download Success",Snackbar.LENGTH_LONG).show();
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

        downloadManager=(DownloadManager)context.getSystemService(DOWNLOAD_SERVICE);
        context.registerReceiver(onComplete,new IntentFilter(ACTION_DOWNLOAD_COMPLETE));

    }


    private void downloadUpdate(String download_link,String version) {

        try {
            DownloadManager.Request request=new DownloadManager.Request(Uri.parse(download_link));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI);
            request.setAllowedOverRoaming(true);
            request.setVisibleInDownloadsUi(true);
            request.setTitle("LvStore");
            request.setDescription("Download in progress...");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"/LvStore/Lvstore_"+version+".apk");

            Snackbar.make(view.findViewById(R.id.layout),"Downloading...",Snackbar.LENGTH_LONG).show();

            refid=downloadManager.enqueue(request);
            list.add(refid);

        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
