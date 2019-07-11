package com.ahmet.orderfoodserver.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.orderfoodserver.Common.Common;
import com.ahmet.orderfoodserver.UIMain.HomeActivity;
import com.ahmet.orderfoodserver.Model.User;
import com.ahmet.orderfoodserver.R;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.FadingCircle;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rey.material.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import io.paperdb.Paper;

public class SignInFragment extends Fragment {

    private TextView mTxtGoToSignUp;
    private TextInputEditText mInputPhone, mInputPassword;
    private Button mBtnSignIn;
    private ProgressBar mProgressBar;
    private ConstraintLayout mConstraintMain;
    private CheckBox mCheckRememberUser;

    private DatabaseReference mReferenceSignIn;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Init Fireabse
        mReferenceSignIn = FirebaseDatabase.getInstance().getReference().child("User");

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_sign_in, container, false);

        mTxtGoToSignUp = layoutView.findViewById(R.id.txt_go_to_sign_up);
        mInputPhone = layoutView.findViewById(R.id.input_phone_signin);
        mInputPassword = layoutView.findViewById(R.id.input_password_signin);
        mBtnSignIn= layoutView.findViewById(R.id.btn_sign_in);
        mProgressBar = layoutView.findViewById(R.id.spin_kit__sign_in);
        mConstraintMain = layoutView.findViewById(R.id.constrain_main_sign_in);
        mCheckRememberUser = layoutView.findViewById(R.id.check_remember_user);

        //Sprite doubleBounce = new DoubleBounce();
        Sprite fadingCircle = new FadingCircle();
        mProgressBar.setIndeterminateDrawable(fadingCircle);


        mProgressBar.setVisibility(View.GONE);
        mConstraintMain.setVisibility(View.VISIBLE);

        // init Paper
        Paper.init(getActivity());

        return layoutView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (getArguments() != null){
            User user = (User) getArguments().getSerializable("");
            Toast.makeText(getActivity(), user.getName() + "/n" + user.getPassword(), Toast.LENGTH_SHORT).show();
        }

        mTxtGoToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Common.addFragment(new SignUpFragment(), R.id.frame_layout_login,
//                        getActivity().getSupportFragmentManager());
            }
        });

        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mConstraintMain.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);

                // mBtnSignIn.setBackgroundResource(R.color.black_overlay);

                String phone = mInputPhone.getText().toString();
                String password = mInputPassword.getText().toString();


                if (Common.isConnectInternet(getActivity())) {

                    if (mCheckRememberUser.isChecked()){
                        mCheckRememberUser.setChecked(true);
                        Paper.book().write(Common.USER_KEY, phone);
                        Paper.book().write(Common.PASSWORD_KEY, password);
                    }

                    mProgressBar.setVisibility(View.VISIBLE);
                    mConstraintMain.setVisibility(View.GONE);
                    signIn(phone, password);
                }else {
                    Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.GONE);
                    mConstraintMain.setVisibility(View.VISIBLE);
                    return;
                }


            }
        });
    }

    private void signIn(final String phone, final String password){

        mReferenceSignIn.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Toast.makeText(getActivity(), dataSnapshot.child("name").getValue().toString(), Toast.LENGTH_SHORT).show();


                if (TextUtils.isEmpty(phone)) {

                    mInputPhone.setError("Please enter phone number");
                    mProgressBar.setVisibility(View.GONE);
                    mConstraintMain.setVisibility(View.VISIBLE);

                }else if(TextUtils.isEmpty(password)){
                    mInputPassword.setError("Please enter password");
                    mProgressBar.setVisibility(View.GONE);
                    mConstraintMain.setVisibility(View.VISIBLE);
                } else {

                    if (Common.isConnectInternet(getActivity())) {

                        // Check if user not exist in database
                        if (dataSnapshot.child(phone).exists()) {

                            // Get User information
                            User user = dataSnapshot.child(phone).getValue(User.class);
                            user.setPhone(phone);

                            if (Boolean.parseBoolean(user.getIsStaff())) {

                                if (user.getPassword().equals(password)) {
//                                     Toast.makeText(getActivity(), "Sign In Successfully", Toast.LENGTH_SHORT).show();
                                    mProgressBar.setVisibility(View.GONE);
                                    // mConstraintMain.setVisibility(View.VISIBLE);

                                    Intent intent = new Intent(getActivity(), HomeActivity.class);
                                    Common.mCurrentUser = user;
                                    startActivity(intent);
//                                    getActivity().finish();

                                } else {
                                    Toast.makeText(getActivity(), "Sign In Failed please check password", Toast.LENGTH_SHORT).show();
                                    mProgressBar.setVisibility(View.GONE);
                                    mConstraintMain.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Toast.makeText(getActivity(), "Please login with staff account", Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.GONE);
                                mConstraintMain.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getActivity(), "User Not Exists", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                            mConstraintMain.setVisibility(View.VISIBLE);
                        }

                    }else {
                        Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                        mConstraintMain.setVisibility(View.VISIBLE);
                        return;
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
