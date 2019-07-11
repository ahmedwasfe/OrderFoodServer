package com.ahmet.orderfoodserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ahmet.orderfoodserver.Common.Common;
import com.ahmet.orderfoodserver.Model.User;
import com.ahmet.orderfoodserver.UIMain.HomeActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private Button btn_go_to_login, btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_go_to_login = findViewById(R.id.btn_go_to_login);

        Paper.init(this);

        btn_go_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        String phone = Paper.book().read(Common.USER_KEY);
        String password = Paper.book().read(Common.PASSWORD_KEY);

        if (phone != null && password != null){
            if (!phone.isEmpty() && !password.isEmpty()){
                signIn(phone, password);
                btn_go_to_login.setEnabled(false);
                Toast.makeText(this, "Logining Please wait", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void signIn(final String phone, final String password){

        // Init Fireabse
        DatabaseReference mReferenceSignIn = FirebaseDatabase.getInstance().getReference().child("User");


        mReferenceSignIn.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Toast.makeText(getActivity(), dataSnapshot.child("name").getValue().toString(), Toast.LENGTH_SHORT).show();

                if (Common.isConnectInternet(MainActivity.this)) {
                    // Check if user not exist in database
                    if (dataSnapshot.child(phone).exists()) {
                        // Get User information
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone);
                        if (user.getPassword().equals(password)) {
                            // Toast.makeText(getActivity(), "Sign In Successfully", Toast.LENGTH_SHORT).show();

//                            mProgressBar.setVisibility(View.GONE);
                            //mConstraintMain.setVisibility(View.VISIBLE);

                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            Common.mCurrentUser = user;
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(MainActivity.this, "Sign In Failed please check password", Toast.LENGTH_SHORT).show();
//                            mProgressBar.setVisibility(View.GONE);
//                            mConstraintMain.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "User Not Exists", Toast.LENGTH_SHORT).show();
//                        mProgressBar.setVisibility(View.GONE);
//                        mConstraintMain.setVisibility(View.VISIBLE);
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
