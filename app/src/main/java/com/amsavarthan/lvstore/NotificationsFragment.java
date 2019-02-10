package com.amsavarthan.lvstore;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    View view;
    Context context;
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    FirebaseFirestore mFirestore;
    NotificationsAdapter adapter;
    List<Notification> notifications=new ArrayList<>();

    public NotificationsFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.frag_notifications,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context=view.getContext();
        linearLayout=view.findViewById(R.id.null_id);
        recyclerView=view.findViewById(R.id.recyclerView);
        mFirestore=FirebaseFirestore.getInstance();

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager=new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter=new NotificationsAdapter(notifications,context);
        recyclerView.setAdapter(adapter);

        getNotifications();

    }

    private void getNotifications() {

        final ProgressDialog dialog=new ProgressDialog(context);
        dialog.setMessage("Just a sec....");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        mFirestore.collection("Notifications")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {

                    if(e!=null){
                        Toast.makeText(context, "Some error fetching notifications, please report to developer", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return;
                    }

                    if(!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                Notification notification = doc.getDocument().toObject(Notification.class);
                                notifications.add(notification);
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();

                            }

                        }
                    }else{
                        linearLayout.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }

                });


    }


}
