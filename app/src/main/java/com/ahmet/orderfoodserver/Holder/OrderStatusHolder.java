package com.ahmet.orderfoodserver.Holder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.ahmet.orderfoodserver.Common.Common;
import com.ahmet.orderfoodserver.Interface.ItmeRecyclerClickListener;
import com.ahmet.orderfoodserver.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OrderStatusHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,
                        View.OnCreateContextMenuListener {

    public TextView mTxtOrderId, mTxtOrderStatus, mTxtOrderPhone, mTxtOrderAddress;

    private ItmeRecyclerClickListener itmeRecyclerClickListener;

    public OrderStatusHolder(@NonNull View itemView) {
        super(itemView);

        mTxtOrderId = itemView.findViewById(R.id.txt_order_status_Id);
        mTxtOrderStatus = itemView.findViewById(R.id.txt_order_status);
        mTxtOrderPhone = itemView.findViewById(R.id.txt_order_status_phone);
        mTxtOrderAddress = itemView.findViewById(R.id.txt_order_status_address);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    public void setItmeRecyclerClickListener(ItmeRecyclerClickListener itmeRecyclerClickListener) {
        this.itmeRecyclerClickListener = itmeRecyclerClickListener;
    }

    @Override
    public void onClick(View view) {
        itmeRecyclerClickListener.onItemClickListener(view, getAdapterPosition(), false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle(Common.Select_Action);
        menu.setHeaderIcon(R.drawable.ic_restaurant_menu);

        menu.add(0,0, getAdapterPosition(), Common.UPDATE);
        menu.add(0,1, getAdapterPosition(), Common.DELETE);

    }
}
