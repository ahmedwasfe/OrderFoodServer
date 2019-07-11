package com.ahmet.orderfoodserver.Fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import com.ahmet.orderfoodserver.UIMain.FoodActivity;
import com.ahmet.orderfoodserver.Holder.CategoriesHolder;
import com.ahmet.orderfoodserver.Interface.ItmeRecyclerClickListener;
import com.ahmet.orderfoodserver.Model.Categories;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import static android.app.Activity.RESULT_OK;

public class CategoriesFragment extends Fragment {

    // Views
    private RecyclerView mRecyclerCategores;
    private ProgressBar mProgressBar, mProgressUploadImage;
    private FloatingActionButton mFabAddCatogry;
    private Button mBtnSelectImage;
    private ImageView mImgCategory;
    private ImageButton dissmisDialog;

    // FIrebses
    private DatabaseReference mReferenceCategories;
    private StorageReference mStorageRefCategories;
    private FirebaseRecyclerOptions<Categories> mCategoresOptions;
    private FirebaseRecyclerAdapter<Categories, CategoriesHolder> mCategoresAdapter;

    // Model
    private Categories mNewCategories;

    private double mProgress;

    // image URI
    Uri imageUri;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // init Firebase
        mReferenceCategories = FirebaseDatabase.getInstance().getReference().child("Categories");
        mStorageRefCategories = FirebaseStorage.getInstance().getReference();

        // Query categoryQuery = mReferenceCategories.orderByChild("name");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_categories, container, false);

        mRecyclerCategores = layoutView.findViewById(R.id.recycler_categores);
        mRecyclerCategores.setHasFixedSize(true);
        mRecyclerCategores.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayout.VERTICAL));

        // init Progress
        mProgressBar = layoutView.findViewById(R.id.spin_kit__home);
        Sprite wave = new Wave();
        mProgressBar.setIndeterminateDrawable(wave);

        mFabAddCatogry = (FloatingActionButton) layoutView.findViewById(R.id.fab_add_category);

        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerCategores.setVisibility(View.VISIBLE);

        return layoutView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadCategores();

        mFabAddCatogry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            addNewCategory();

            }
        });
    }

    private void addNewCategory() {


        final BottomSheetDialog mSheetDialog = new BottomSheetDialog(getActivity());
        mSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mSheetDialog.setCancelable(false);
        mSheetDialog.setCanceledOnTouchOutside(false);

        View sheetDialogView = getLayoutInflater().inflate(R.layout.sheet_dialog_catogry, null);

        mImgCategory = sheetDialogView.findViewById(R.id.img_add_catogry);
        final TextInputEditText mInputCaegoryName = sheetDialogView.findViewById(R.id.input_add_category_name);
        mBtnSelectImage = sheetDialogView.findViewById(R.id.btn_select_image_category);
        Button mBtnUploadImage = sheetDialogView.findViewById(R.id.btn_upload_image_category);
        dissmisDialog = sheetDialogView.findViewById(R.id.img_dissmis_dialog_category);
        TextView txtNewCategory = sheetDialogView.findViewById(R.id.txt_new_category);

        txtNewCategory.setText("Add New Category");
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

                String categoryName = mInputCaegoryName.getText().toString();

                uploadImage(categoryName);
            }
        });

        dissmisDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dissmisDialog.isEnabled()) {
                    mSheetDialog.dismiss();
                }else {
                    Toast.makeText(getActivity(), "Please wait while upload data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSheetDialog.setContentView(sheetDialogView);
        mSheetDialog.show();
    }

    private void uploadImage(final String categoryName) {

        if (imageUri != null){

            mProgressUploadImage.setVisibility(View.VISIBLE);

            final ProgressDialog dialog = new ProgressDialog(getActivity());

            String imageName = UUID.randomUUID().toString();

            final StorageReference mStorageReference = mStorageRefCategories.child("ImageCategories/" + imageName);

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
                        Toast.makeText(getActivity(), "Uploaded Success", Toast.LENGTH_SHORT).show();
                        dissmisDialog.setEnabled(true);

                        String downUri = task.getResult().toString();
                        Log.d("IMAGE_URI", downUri);


                        mNewCategories = new Categories(categoryName, downUri, System.currentTimeMillis());

                        if (mNewCategories != null) {
                            mReferenceCategories.push().setValue(mNewCategories)
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
                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void checkPermission() {

        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Common.CODE_IMAGE_PERMISSION);
            return;
        }else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Common.CODE_IMAGE_PERMISSION);
            return;
        }
    }

    private void loadCategores() {

                                                                //timestamp
        Query categoriesQuery = mReferenceCategories.orderByChild("name");
        mCategoresOptions = new FirebaseRecyclerOptions.Builder<Categories>()
                .setQuery(categoriesQuery, Categories.class)
                .build();


        mCategoresAdapter = new FirebaseRecyclerAdapter<Categories, CategoriesHolder>(mCategoresOptions) {
            @Override
            protected void onBindViewHolder(@NonNull CategoriesHolder categoriesHolder, final int position, @NonNull final Categories categories) {

                mProgressBar.setVisibility(View.GONE);
                mRecyclerCategores.setVisibility(View.VISIBLE);

                categoriesHolder.mTxtCategoryName.setText(categories.getName());


                Picasso.get()
                        .load(categories.getImage())
                        .placeholder(R.drawable.background_main)
                        .into(categoriesHolder.mImgCategory);

                categoriesHolder.setItemRecyclerClickListener(new ItmeRecyclerClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position, boolean isLongClick) {

                        // Get categoryId and send to food Activity
                        Intent foodIntent = new Intent(getActivity(), FoodActivity.class);
                        // Becouse categoryId is key so we just get key of this item
                        foodIntent.putExtra("categoryId", mCategoresAdapter.getRef(position).getKey());
                        startActivity(foodIntent);

                    }
                });
            }

            @NonNull
            @Override
            public CategoriesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View layoutView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.raw_category, parent, false);

                return new CategoriesHolder(layoutView);
            }
        };

        mCategoresAdapter.notifyDataSetChanged();
        mRecyclerCategores.setAdapter(mCategoresAdapter);
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
                    Toast.makeText(getActivity(), "Sorry, I it's not allowed for me to do access Stoareg", Toast.LENGTH_SHORT).show();
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
                                .into(mImgCategory);
                        mBtnSelectImage.setText("Image Selected !");
                    }
                }

        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mCategoresAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        mCategoresAdapter.stopListening();
    }


    // Update / Delete
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)){

            updateCategoryDialog(mCategoresAdapter.getRef(item.getOrder()).getKey(), mCategoresAdapter.getItem(item.getOrder()));

        } else  if (item.getTitle().equals(Common.DELETE)){

            deleteCategory(mCategoresAdapter.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key) {

        mReferenceCategories.child(key).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Delete category successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCategoryDialog(final String key, final Categories category) {

        final BottomSheetDialog mSheetDialog = new BottomSheetDialog(getActivity());
        mSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mSheetDialog.setCancelable(false);
        mSheetDialog.setCanceledOnTouchOutside(false);

        View sheetDialogView = getLayoutInflater().inflate(R.layout.sheet_dialog_catogry, null);

        mImgCategory = sheetDialogView.findViewById(R.id.img_add_catogry);
        final TextInputEditText mInputCaegoryName = sheetDialogView.findViewById(R.id.input_add_category_name);
        mBtnSelectImage = sheetDialogView.findViewById(R.id.btn_select_image_category);
        Button mBtnUploadImage = sheetDialogView.findViewById(R.id.btn_upload_image_category);
        dissmisDialog = sheetDialogView.findViewById(R.id.img_dissmis_dialog_category);
        TextView txtNewCategory = sheetDialogView.findViewById(R.id.txt_new_category);

        txtNewCategory.setText("Update Category");
        Picasso.get()
                .load(category.getImage())
                .placeholder(R.drawable.background_main)
                .into(mImgCategory);
        dissmisDialog.setEnabled(true);

        mInputCaegoryName.setText(category.getName());

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

                String categoryName = mInputCaegoryName.getText().toString();

                updateImage(key, category, categoryName);
            }
        });

        dissmisDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dissmisDialog.isEnabled()) {
                    mSheetDialog.dismiss();
                }else {
                    Toast.makeText(getActivity(), "Please wait while upload data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSheetDialog.setContentView(sheetDialogView);
        mSheetDialog.show();


    }

    private void updateImage(final String key, final Categories category, final String categoryName) {

        if (imageUri != null){

            mProgressUploadImage.setVisibility(View.VISIBLE);

            final ProgressDialog dialog = new ProgressDialog(getActivity());

            String imageName = UUID.randomUUID().toString();

            final StorageReference mStorageReference = mStorageRefCategories.child("ImageCategories/" + imageName);

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
                        Toast.makeText(getActivity(), "Uploaded Success", Toast.LENGTH_SHORT).show();
                        dissmisDialog.setEnabled(true);

                        String downUri = task.getResult().toString();
                       // Log.d("IMAGE_URI", downUri);

                       category.setImage(downUri);
                       category.setName(categoryName);
                       category.setTimestamp(System.currentTimeMillis());

                        mReferenceCategories.child(key).setValue(category)
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
                    Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }


    }
}

