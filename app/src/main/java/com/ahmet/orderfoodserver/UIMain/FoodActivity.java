package com.ahmet.orderfoodserver.UIMain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmet.orderfoodserver.Common.Common;
import com.ahmet.orderfoodserver.Holder.FoodHolder;
import com.ahmet.orderfoodserver.Interface.ItmeRecyclerClickListener;
import com.ahmet.orderfoodserver.Model.Food;
import com.ahmet.orderfoodserver.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;


public class FoodActivity extends AppCompatActivity {

    private RecyclerView mRecyclerFood;
    private ProgressBar mProgressBar, mProgressUploadImage;
   // private MaterialSearchBar mSearchBar;
    private FloatingActionButton mFabAddFood;
    private Button mBtnSelectImage;
    private ImageView mImgFood;
    private ImageButton dissmisDialog;

    private DatabaseReference mReferenceFood;
    private StorageReference mStorageRefFoods;
    private FirebaseRecyclerAdapter<Food, FoodHolder> mFoodAdapter;
    private FirebaseRecyclerOptions<Food> mFoodOptions;

//    private FirebaseRecyclerAdapter<Food, FoodHolder> mSearchFoodAdapter;
//    private FirebaseRecyclerOptions<Food> mSearchFoodOptions;
//    private List<String> mSearchList = new ArrayList<>();

    // Model
    private Food mNewFood;

    private double mProgress;

    // image URI
    Uri imageUri;

    private String categoryId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);


        // init recycler view
        mRecyclerFood = findViewById(R.id.recycler_foods);
        mRecyclerFood.setHasFixedSize(true);
        mRecyclerFood.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayout.VERTICAL));

        // init progress
        mProgressBar = findViewById(R.id.spin_kit__food);
        Sprite wave = new Wave();
        mProgressBar.setIndeterminateDrawable(wave);
        mProgressBar.setVisibility(View.VISIBLE);

        mFabAddFood = findViewById(R.id.fab_add_food);
        mFabAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNEwFoodDialog();
            }
        });

        mReferenceFood = FirebaseDatabase.getInstance().getReference().child("Foods");
        mStorageRefFoods = FirebaseStorage.getInstance().getReference();

        if (getIntent() != null){
            categoryId = getIntent().getStringExtra("categoryId");
        }

        if (!categoryId.isEmpty() && categoryId != null){
            loadFoods(categoryId);
        }
    }

    private void addNEwFoodDialog() {

        final BottomSheetDialog mSheetDialog = new BottomSheetDialog(this);
        mSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mSheetDialog.setCancelable(false);
        mSheetDialog.setCanceledOnTouchOutside(false);

        View sheetDialogView = getLayoutInflater().inflate(R.layout.sheet_dialog_food, null);

        mImgFood = sheetDialogView.findViewById(R.id.img_add_food);
        final TextInputEditText mInputFoodName = sheetDialogView.findViewById(R.id.input_add_food_name);
        final TextInputEditText mInputFoodPrice = sheetDialogView.findViewById(R.id.input_add_food_price);
        final TextInputEditText mInputFoodDiscount = sheetDialogView.findViewById(R.id.input_add_food_discount);
        final TextInputEditText mInputFoodDescription = sheetDialogView.findViewById(R.id.input_add_food_description);
        mBtnSelectImage = sheetDialogView.findViewById(R.id.btn_select_image_food);
        Button mBtnUploadImage = sheetDialogView.findViewById(R.id.btn_upload_image_food);
        dissmisDialog = sheetDialogView.findViewById(R.id.img_dissmis_dialog_food);
        TextView txtNewFood = sheetDialogView.findViewById(R.id.txt_new_food);

        txtNewFood.setText("Add New Food");
        dissmisDialog.setEnabled(true);

        mProgressUploadImage = sheetDialogView.findViewById(R.id.spin_kit__upload_image);
        Sprite threeBounce = new ThreeBounce();
        mProgressUploadImage.setIndeterminateDrawable(threeBounce);
        mProgressUploadImage.setVisibility(View.GONE);

        mBtnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkPermission();

            }
        });

        mBtnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String foodName = mInputFoodName.getText().toString();
                String foodPrice = mInputFoodPrice.getText().toString();
                String foodDiscount = mInputFoodDiscount.getText().toString();
                String foodDescription = mInputFoodDescription.getText().toString();

                uploadImage(foodName, foodPrice, foodDiscount, foodDescription);

                if (dissmisDialog.isEnabled()){
                    dissmisDialog.setEnabled(false);
                }
            }
        });

        dissmisDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (dissmisDialog.isEnabled()) {
                    mSheetDialog.dismiss();
                }else {
                    Toast.makeText(FoodActivity.this, "Please wait while upload data", Toast.LENGTH_SHORT).show();
                }
            }
        });


        mSheetDialog.setContentView(sheetDialogView);
        mSheetDialog.show();

    }

    private void uploadImage(final String name, final String price, final String discount, final String description) {

        if (imageUri != null){

            mProgressUploadImage.setVisibility(View.VISIBLE);

            final ProgressDialog dialog = new ProgressDialog(FoodActivity.this);

            String imageName = UUID.randomUUID().toString();

            final StorageReference mStorageReference = mStorageRefFoods.child("ImageFoods/" + imageName);

            mStorageReference.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    mProgress = (100.0 * task.getResult().getBytesTransferred() / task.getResult().getTotalByteCount());

                    if (!task.isSuccessful()){

                        dialog.setMessage("Uoloaded " + mProgress + "%");
                        dialog.show();

                        throw task.getException();

                    }else {

                        dialog.setMessage("Uoloaded " + mProgress + "%");
                        dialog.show();
                    }

                    return mStorageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()){
                        mProgressUploadImage.setVisibility(View.GONE);
                        dialog.dismiss();
                        Toast.makeText(FoodActivity.this, "Uploaded Success", Toast.LENGTH_SHORT).show();
                        dissmisDialog.setEnabled(true);

                        String downUri = task.getResult().toString();
                        Log.d("IMAGE_URI", downUri);


                        mNewFood = new Food(name, price, discount, description, downUri, categoryId);

                        if (mNewFood != null) {

                            mReferenceFood.push().setValue(mNewFood)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mProgressUploadImage.setVisibility(View.GONE);
                                            dialog.dismiss();
                                            dissmisDialog.setEnabled(true);
                                        }
                                    });
                        }
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgressUploadImage.setVisibility(View.GONE);
                    Toast.makeText(FoodActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    private void loadFoods(String categoryId) {

        Query foodQuery = mReferenceFood.orderByChild("categoryId").equalTo(categoryId);
       // Query query = foodQuery.orderByChild("name");
        mFoodOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(foodQuery, Food.class)
                .build();

        mFoodAdapter = new FirebaseRecyclerAdapter<Food, FoodHolder>(mFoodOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodHolder foodHolder, int i, @NonNull final Food food) {

                mProgressBar.setVisibility(View.GONE);

                foodHolder.mTxtFoodName.setText(food.getName());

                Picasso.get()
                        .load(food.getImage())
                        .placeholder(R.drawable.background_main)
                        .into(foodHolder.mImgFood);

                foodHolder.setItmeRecyclerClickListener(new ItmeRecyclerClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position, boolean isLongClick) {

//                        Intent detailsIntent = new Intent(FoodActivity.this, FoodDetailsActivity.class);
//                        detailsIntent.putExtra("foodId", mFoodAdapter.getRef(posiotn).getKey());
//                        startActivity(detailsIntent);
                        Toast.makeText(FoodActivity.this, food.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @NonNull
            @Override
            public FoodHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View layoutView = LayoutInflater.from(FoodActivity.this)
                        .inflate(R.layout.raw_food, parent, false);
                return new FoodHolder(layoutView);
            }

        };

        mFoodAdapter.notifyDataSetChanged();
        mRecyclerFood.setAdapter(mFoodAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFoodAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        mFoodAdapter.stopListening();
    }

    private void checkPermission() {

        if (ActivityCompat.checkSelfPermission(FoodActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Common.CODE_IMAGE_PERMISSION);
            }

            return;
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Common.CODE_IMAGE_PERMISSION);
            }
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case Common.CODE_IMAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent , Common.CODE_IMAGE_REQUEST);
                }else {
                    Toast.makeText(FoodActivity.this, "Sorry, I it's not allowed for me to do access Stoareg", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == Common.CODE_IMAGE_REQUEST){
            if (resultCode == RESULT_OK){
                if (data != null && data.getData() != null){
                    imageUri = data.getData();
                    Picasso.get()
                            .load(data.getData())
                            .placeholder(R.drawable.background_main)
                            .into(mImgFood);
                    mBtnSelectImage.setText("Image Selected !");
                }
            }

        }

    }

    // Update / Delete
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)){

            updateFoodDialog(mFoodAdapter.getRef(item.getOrder()).getKey(), mFoodAdapter.getItem(item.getOrder()));

        } else if (item.getTitle().equals(Common.DELETE)){

            deleteFood(mFoodAdapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }


    private void deleteFood(String key) {

        mReferenceFood.child(key).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(FoodActivity.this, "Delete food successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFoodDialog(final String key, final Food food) {

        final BottomSheetDialog mSheetDialog = new BottomSheetDialog(FoodActivity.this);
        mSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mSheetDialog.setCancelable(false);
        mSheetDialog.setCanceledOnTouchOutside(false);

        View sheetDialogView = getLayoutInflater().inflate(R.layout.sheet_dialog_food, null);

        mImgFood = sheetDialogView.findViewById(R.id.img_add_food);
        final TextInputEditText mInputFoodName = sheetDialogView.findViewById(R.id.input_add_food_name);
        final TextInputEditText mInputFoodPrice = sheetDialogView.findViewById(R.id.input_add_food_price);
        final TextInputEditText mInputFoodDiscount = sheetDialogView.findViewById(R.id.input_add_food_discount);
        final TextInputEditText mInputFoodDescription = sheetDialogView.findViewById(R.id.input_add_food_description);
        mBtnSelectImage = sheetDialogView.findViewById(R.id.btn_select_image_food);
        Button mBtnUploadImage = sheetDialogView.findViewById(R.id.btn_upload_image_food);
        dissmisDialog = sheetDialogView.findViewById(R.id.img_dissmis_dialog_food);
        TextView txtNewFood = sheetDialogView.findViewById(R.id.txt_new_food);

        txtNewFood.setText("Update Food");
        dissmisDialog.setEnabled(true);

        mInputFoodName.setText(food.getName());
        mInputFoodPrice.setText(food.getPrice());
        mInputFoodDiscount.setText(food.getDiscount());
        mInputFoodDescription.setText(food.getDescription());
        Picasso.get()
                .load(food.getImage())
                .placeholder(R.drawable.background_main)
                .into(mImgFood);

        mProgressUploadImage = sheetDialogView.findViewById(R.id.spin_kit__upload_image);
        Sprite threeBounce = new ThreeBounce();
        mProgressUploadImage.setIndeterminateDrawable(threeBounce);
        mProgressUploadImage.setVisibility(View.GONE);

        mBtnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkPermission();

            }
        });

        mBtnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String foodName = mInputFoodName.getText().toString();
                String foodPrice = mInputFoodPrice.getText().toString();
                String foodDiscount = mInputFoodDiscount.getText().toString();
                String foodDescription = mInputFoodDescription.getText().toString();

                updateImage(key, food, foodName, foodPrice, foodDiscount, foodDescription);

                if (dissmisDialog.isEnabled()){
                    dissmisDialog.setEnabled(false);
                }
            }
        });

        dissmisDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dissmisDialog.isEnabled()) {
                    mSheetDialog.dismiss();
                }else {
                    Toast.makeText(FoodActivity.this, "Please wait while upload data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSheetDialog.setContentView(sheetDialogView);
        mSheetDialog.show();


    }

    private void updateImage(final String key, final Food food, final String name, final String price, final String discount, final String description) {

        if (imageUri != null){

            mProgressUploadImage.setVisibility(View.VISIBLE);

            final ProgressDialog dialog = new ProgressDialog(FoodActivity.this);

            String imageName = UUID.randomUUID().toString();

            final StorageReference mStorageReference = mStorageRefFoods.child("ImageFoods/" + imageName);

            mStorageReference.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    mProgress = (100.0 * task.getResult().getBytesTransferred() / task.getResult().getTotalByteCount());

                    if (!task.isSuccessful()){

                        dialog.setMessage("Uoloaded " + mProgress + "%");
                        dialog.show();

                        throw task.getException();

                    }else {

                        dialog.setMessage("Uoloaded " + mProgress + "%");
                        dialog.show();
                    }

                    return mStorageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()){
                        mProgressUploadImage.setVisibility(View.GONE);
                        dialog.dismiss();
                        Toast.makeText(FoodActivity.this, "Uploaded Success", Toast.LENGTH_SHORT).show();
                        dissmisDialog.setEnabled(true);

                        String downUri = task.getResult().toString();
                        // Log.d("IMAGE_URI", downUri);

                        food.setImage(downUri);
                        food.setName(name);
                        food.setPrice(price);
                        food.setDiscount(discount);
                        food.setDescription(description);
                        food.setTimestamp(System.currentTimeMillis());

                        mReferenceFood.child(key).setValue(food)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mProgressUploadImage.setVisibility(View.GONE);
                                        //dialog.dismiss();
                                        dissmisDialog.setEnabled(true);
                                    }
                                });


                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mProgressUploadImage.setVisibility(View.GONE);
                    Toast.makeText(FoodActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }


    }
}
