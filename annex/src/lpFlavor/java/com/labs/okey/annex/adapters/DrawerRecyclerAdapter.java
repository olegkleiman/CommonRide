package com.labs.okey.annex.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.utils.Globals;
import com.labs.okey.commonride.utils.RoundedDrawable;

import java.util.concurrent.ExecutionException;

/**
 * Created by Oleg Kleiman on 18-Apr-15.
 */
public class DrawerRecyclerAdapter extends RecyclerView.Adapter<DrawerRecyclerAdapter.ViewHolder>{

    private static final String LOG_TAG = "Annex.DrawerAdapter";

    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
    // IF the view under inflation and population is header or Item
    private static final int TYPE_ITEM = 1;

    private Context mContext;
    private String mNavTitles[];
    private int mIcons[];
    private String mName;
    private String mEmail;
    private String mPictureURL;

    public DrawerRecyclerAdapter(Context context,
                                 String[] titles,
                                 int[] icons,
                                 String name,
                                 String email,
                                 String pictureURL){

        mContext = context;

        mNavTitles = titles;
        mIcons = icons;
        mName = name;
        mEmail = email;
        mPictureURL = pictureURL;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            // Inflate row layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_item_raw,parent,false);
            ViewHolder vhItem = new ViewHolder(v,viewType);
            return vhItem;

        } else if (viewType == TYPE_HEADER) {
            // Inflate header layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header,parent,false);
            ViewHolder vhHeader = new ViewHolder(v,viewType);
            return vhHeader;
        }

        return null;
    }

    // Next we override a method which is called when the item in a row is needed to be displayed, here the int position
    // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
    // which view type is being created 1 for item row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.holderId == 1) {
            holder.rowTextView.setText(mNavTitles[position - 1]);
            holder.rowImageView.setImageResource(mIcons[position -1]);//
        } else{

            Drawable drawable = null;
            try {
                drawable = (Globals.drawMan.userDrawable(mContext,
                        "1",
                        mPictureURL)).get();
                drawable = RoundedDrawable.fromDrawable(drawable);
                ((RoundedDrawable) drawable)
                        .setCornerRadius(Globals.PICTURE_CORNER_RADIUS)
                        .setBorderColor(Color.LTGRAY)
                        .setBorderWidth(Globals.PICTURE_BORDER_WIDTH)
                        .setOval(true);

                holder.imageProfile.setImageDrawable(drawable);
            } catch (InterruptedException | ExecutionException e) {
                Log.e(LOG_TAG, e.getMessage());
            }

            holder.txtName.setText(mName);
            holder.txtEmail.setText(mEmail);

        }
    }

    @Override
    public int getItemCount() {
        return mNavTitles.length+1;
    }

    // With the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        int holderId;

        // Header views
        TextView txtName;
        TextView txtEmail;
        ImageView imageProfile;

        // Row views
        ImageView rowImageView;
        TextView rowTextView;

        public ViewHolder(View itemView, int ViewType) {
            super(itemView);

            if(ViewType == TYPE_ITEM) {

                rowTextView = (TextView) itemView.findViewById(R.id.rowText);
                rowImageView = (ImageView) itemView.findViewById(R.id.rowIcon);

                holderId = 1;
            } else {

                txtName = (TextView) itemView.findViewById(R.id.name);
                txtEmail = (TextView) itemView.findViewById(R.id.email);
                imageProfile = (ImageView) itemView.findViewById(R.id.circleView);

                holderId = 0;
            }
        }
    }
}
