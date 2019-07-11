package com.ahmet.orderfoodserver.Holder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahmet.orderfoodserver.Common.Common;
import com.ahmet.orderfoodserver.Interface.ItmeRecyclerClickListener;
import com.ahmet.orderfoodserver.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CategoriesHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener,
        View.OnCreateContextMenuListener {

    public ImageView mImgCategory;
    public TextView mTxtCategoryName;

    private ItmeRecyclerClickListener itmeRecyclerClickListener;

    public CategoriesHolder(@NonNull View itemView) {
        super(itemView);

        mImgCategory = itemView.findViewById(R.id.image_category);
        mTxtCategoryName = itemView.findViewById(R.id.textcategory_name);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    public void setItemRecyclerClickListener(ItmeRecyclerClickListener itmeRecyclerClickListener) {
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

        menu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        menu.add(0, 1, getAdapterPosition(), Common.DELETE);

    }
}
