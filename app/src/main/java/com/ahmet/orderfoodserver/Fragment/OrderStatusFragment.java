package com.ahmet.orderfoodserver.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ahmet.orderfoodserver.Common.Common;
import com.ahmet.orderfoodserver.Holder.OrderStatusHolder;
import com.ahmet.orderfoodserver.Interface.ItmeRecyclerClickListener;
import com.ahmet.orderfoodserver.Model.Request;
import com.ahmet.orderfoodserver.R;
import com.ahmet.orderfoodserver.UIMain.TrakingOrderActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class OrderStatusFragment extends Fragment {

    private RecyclerView mRecyclerOrderStatus;
    private ProgressBar mProgressBar;

    private DatabaseReference mReferenceOrderStatus;
    private FirebaseRecyclerOptions<Request> mRequestOption;
    private FirebaseRecyclerAdapter<Request, OrderStatusHolder> mRequestAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReferenceOrderStatus = FirebaseDatabase.getInstance().getReference().child("Request");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layoutView = inflater.inflate(R.layout.fragment_order_status, container, false);

        mRecyclerOrderStatus = layoutView.findViewById(R.id.recycler_order_status);
        mRecyclerOrderStatus.setHasFixedSize(true);
        mRecyclerOrderStatus.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayout.VERTICAL));

        mProgressBar = layoutView.findViewById(R.id.spin_kit__order_status);
        Sprite wave = new Wave();
        mProgressBar.setIndeterminateDrawable(wave);
        mProgressBar.setVisibility(View.VISIBLE);

        return layoutView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadAllOrders();
    }

    private void loadAllOrders() {

        mRequestOption = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(mReferenceOrderStatus, Request.class)
                .build();

        mRequestAdapter = new FirebaseRecyclerAdapter<Request, OrderStatusHolder>(mRequestOption) {
            @Override
            protected void onBindViewHolder(@NonNull OrderStatusHolder orderStatusHolder, int position, @NonNull final Request request) {

                mProgressBar.setVisibility(View.GONE);

                orderStatusHolder.mTxtOrderId.setText(mRequestAdapter.getRef(position).getKey());
                orderStatusHolder.mTxtOrderPhone.setText(request.getPhone());
                orderStatusHolder.mTxtOrderAddress.setText(request.getAddress());
                orderStatusHolder.mTxtOrderStatus.setText(Common.convertStatus(request.getStatus()));

                orderStatusHolder.setItmeRecyclerClickListener(new ItmeRecyclerClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position, boolean isLongClick) {

                        Intent intent = new Intent(getActivity(), TrakingOrderActivity.class);
                        Common.mCurrentRequest = request;
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public OrderStatusHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View layoutView = LayoutInflater.from(getActivity())
                                    .inflate(R.layout.raw_order_status, parent, false);
                return new OrderStatusHolder(layoutView);
            }
        };

        mRequestAdapter.notifyDataSetChanged();
        mRecyclerOrderStatus.setAdapter(mRequestAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        mRequestAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mRequestAdapter.stopListening();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)){

            updateOrderDialog(mRequestAdapter.getRef(item.getOrder()).getKey(), mRequestAdapter.getItem(item.getOrder()));

        }else if (item.getTitle().equals(Common.DELETE)){

            deleteOrderDialog(mRequestAdapter.getRef(item.getOrder()).getKey());

        }

        return super.onContextItemSelected(item);
    }

    private void deleteOrderDialog(String key) {

        mReferenceOrderStatus.child(key).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Deleted Successflly", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateOrderDialog(final String key, final Request request) {

        final BottomSheetDialog mSheetDialog = new BottomSheetDialog(getActivity());
        mSheetDialog.setCanceledOnTouchOutside(false);
        mSheetDialog.setCancelable(false);

        View layoutView = getLayoutInflater().inflate(R.layout.sheet_dialog_update_order, null);

        final AppCompatSpinner mSpinner = layoutView.findViewById(R.id.spinner_update_order);
        Button mBtnUpdateOrder = layoutView.findViewById(R.id.btn_update_order);
        final ImageButton dissmisDialog = layoutView.findViewById(R.id.img_dissmis_dialog_order);

        List<String> mListOrder = new ArrayList<String>();
        mListOrder.add("Placed");
        mListOrder.add("On my way");
        mListOrder.add("Shipped");

        mSpinner.setAdapter(new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item,mListOrder));

        mBtnUpdateOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                request.setStatus(String.valueOf(mSpinner.getSelectedItemPosition()));

                mReferenceOrderStatus.child(key).setValue(request)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(), "Order Updated", Toast.LENGTH_SHORT).show();
                                mSheetDialog.dismiss();
                            }
                        });
            }
        });

        dissmisDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSheetDialog.dismiss();
            }
        });

        mSheetDialog.setContentView(layoutView);
        mSheetDialog.show();
    }
}
