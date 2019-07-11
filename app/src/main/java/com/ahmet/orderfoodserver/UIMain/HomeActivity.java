package com.ahmet.orderfoodserver.UIMain;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.ahmet.orderfoodserver.Common.Common;
import com.ahmet.orderfoodserver.Fragment.CategoriesFragment;
import com.ahmet.orderfoodserver.Fragment.OrderStatusFragment;
import com.ahmet.orderfoodserver.MainActivity;
import com.ahmet.orderfoodserver.R;

import android.view.View;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Server App");
        setSupportActionBar(toolbar);

        Common.addFragment(new CategoriesFragment(), R.id.frame_layout_home, getSupportFragmentManager());

        // init Paper
        Paper.init(this);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headView = navigationView.getHeaderView(0);
        TextView mTxtName = headView.findViewById(R.id.txt_name);
        mTxtName.setText(Common.mCurrentUser.getName());

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {

            Common.addFragment(new CategoriesFragment(), R.id.frame_layout_home, getSupportFragmentManager());
            //fab.setVisibility(View.VISIBLE);

        } else if (id == R.id.nav_cart) {

        } else if (id == R.id.nav_order) {

            Common.addFragment(new OrderStatusFragment(), R.id.frame_layout_home, getSupportFragmentManager());

        } else if (id == R.id.nav_sign_out) {

            // Delete Remember phone and password
            Paper.book().destroy();

            // Log out
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkPermission() {

        if (ActivityCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                        CODE_IMAGE_PERMISSION);
            }

            return;
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                        CODE_IMAGE_PERMISSION);
            }
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        switch (requestCode){
//            case CODE_IMAGE_PERMISSION:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    intent.setType("image/*");
//                    startActivityForResult(intent , CODE_IMAGE_REQUEST);
//                }else {
//                    Toast.makeText(FoodActivity.this, "Sorry, I it's not allowed for me to do access Stoareg", Toast.LENGTH_SHORT).show();
//                }
//        }
    }
}
