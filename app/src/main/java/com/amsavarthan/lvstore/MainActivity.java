package com.amsavarthan.lvstore;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.NoCopySpan;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseFirestore mFirestore;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setDrawer(toolbar);

        mUser=FirebaseAuth.getInstance().getCurrentUser();
        mFirestore=FirebaseFirestore.getInstance();

        if(mUser==null){
            AlertDialog.Builder inputAlert=new AlertDialog.Builder(this);
            inputAlert.setTitle("Hey there, User!");
            inputAlert.setMessage("Let me know that you are using this app, Just enter your name below to get started");

            LayoutInflater inflater=this.getLayoutInflater();
            View dialogView=inflater.inflate(R.layout.dialog_view,null);
            inputAlert.setView(dialogView);

            final EditText userInput=dialogView.findViewById(R.id.input);

            inputAlert.setPositiveButton("Yeah that's my name", null);
            AlertDialog alertDialog=inputAlert.create();

            alertDialog.setOnShowListener(dialog -> {
                Button button=((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(v -> {

                    String userInputValue=userInput.getText().toString();

                    if(TextUtils.isEmpty(userInputValue)){
                        Toast.makeText(MainActivity.this, "Please enter your name to continue", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final ProgressDialog dialogg=new ProgressDialog(MainActivity.this);
                    dialogg.setMessage("Just a sec....");
                    dialogg.setIndeterminate(true);
                    dialogg.setCanceledOnTouchOutside(false);
                    dialogg.show();

                    HashMap<String,Object> map=new HashMap<>();
                    map.put("name",userInputValue);
                    map.put("creation_time",String.valueOf(System.currentTimeMillis()));
                    mFirestore.collection("Users")
                            .add(map)
                            .addOnSuccessListener(documentReference -> FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(authResult -> {

                                showFragent(new AppsFragment());
                                dialogg.dismiss();
                                dialog.dismiss();

                            }).addOnFailureListener(e -> {
                                e.printStackTrace();
                                dialog.dismiss();
                                dialogg.dismiss();
                                Toast.makeText(MainActivity.this, "Sorry there was a technical error, But you may proceed", Toast.LENGTH_SHORT).show();
                                showFragent(new AppsFragment());
                            }))
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                dialog.dismiss();
                                dialogg.dismiss();
                                Toast.makeText(MainActivity.this, "Sorry there was a technical error, But you may proceed", Toast.LENGTH_SHORT).show();
                                showFragent(new AppsFragment());
                            });

                });
            });

            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }else {
            showFragent(new AppsFragment());
        }

    }

    private void setDrawer(Toolbar toolbar) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_apps) {
            showFragent(new AppsFragment());
        } else if (id == R.id.nav_notification) {
            showFragent(new NotificationsFragment());
        }else if (id == R.id.nav_git) {
            startWeb("https://github.com/lvamsavarthan");
        }else if (id == R.id.nav_gplus) {
            startWeb("https://plus.google.com/118398512849520996107");
        } else if (id == R.id.nav_fb) {
            startWeb("https://www.facebook.com/lvamsavarthan");
        } else if (id == R.id.nav_insta) {
            startWeb("https://www.instagram.com/lvamsavarthan");
        } else if (id == R.id.nav_twitter) {
            startWeb("https://www.twitter.com/amsavarthanlv");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showFragent(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container,fragment)
                .commit();
    }

    private void startWeb(String url){
        Intent i=new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}
