package com.ahmet.orderfoodserver.Common;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ahmet.orderfoodserver.Model.Request;
import com.ahmet.orderfoodserver.Model.User;
import com.ahmet.orderfoodserver.Remote.IGeoCoordinates;
import com.ahmet.orderfoodserver.Remote.RetrofitClient;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class Common {

    // Current User
    public static User mCurrentUser;

    // Current Request
    public static Request mCurrentRequest;

    // update and Delete
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final String Select_Action = "Select the action";

    // Remember User
    public static final String USER_KEY = "Phone";
    public static final String PASSWORD_KEY = "Password";

    public static final String baseUrl = "https://maps.googleapis.com";

    // Request Code
    public static final int CODE_IMAGE_REQUEST = 80;
    public static final int CODE_IMAGE_PERMISSION = 81;
    public static final int CODE_PLAY_SERVICES_RESOLUTION_REQUEST = 82;
    public static final int CODE_LOCATION_PERMISSION_REQUEST = 83;


    // Check intenet connection
    public static boolean isConnectInternet(Context context){

        ConnectivityManager Connectmanager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        if (Connectmanager != null){

            NetworkInfo[] info = Connectmanager.getAllNetworkInfo();

            for (int i = 0; i < info.length; i++){

                if (info[i].getState() == NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        }
        return false;
    }


    // Add new Fragment
    public static void addFragment(Fragment fragment, int id, FragmentManager fragmentManager){
        fragmentManager.beginTransaction()
                .replace(id, fragment)
                .commit();
    }

    // convert status order
    public static String convertStatus(String status) {

        if (status.equals("0")){
            return "Placed";
        }else if (status.equals("1")){
            return "On my way";
        }

        return "Shipped";
    }

    public static IGeoCoordinates getIGeoCodeService(){

        return RetrofitClient.getClient(baseUrl).create(IGeoCoordinates.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight){

        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float)bitmap.getWidth();
        float scaleY = newHeight / (float)bitmap.getHeight();

        float pivotX = 0, pivotY = 0;

        Matrix scaleMatrix = new Matrix();

        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(scaledBitmap, 0, 0, new Paint(Paint.FAKE_BOLD_TEXT_FLAG));

        return scaledBitmap;
    }
}
