package com.labs.okey.annex.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.model.RideAnnotated;
import com.labs.okey.commonride.utils.Globals;
import com.labs.okey.commonride.utils.RoundedDrawable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Oleg Kleiman on 09-Apr-15.
 */
public class RecyclerRidesAdapter extends RecyclerView.Adapter<RecyclerRidesAdapter.ViewHolder> {

    private static final String LOG_TAG = "Annex.RecyclerAdapter";

    private List<RideAnnotated> rides = new ArrayList<RideAnnotated>();
    private Context mContext;

    String mDesc;
    SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    public RecyclerRidesAdapter(Context context) {
        mContext = context;

        mDesc = context.getResources().getString(R.string.ride_desc);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ride_item_row, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        RideAnnotated ride = rides.get(position);
        holder.setItem(ride);

        holder.txtRideFrom.setText(ride.ride_from);
        holder.txtRideTo.setText(ride.ride_to);
        holder.txtFreePlaces.setText(Integer.toString(ride.freePlaces));

        String desc = String.format("%s %s %s",
                ride.first_name, ride.last_name, mDesc);

        holder.txtView.setText(desc);
        String whenStarts = mDateFormat.format(ride.whenStarts);
        holder.txtRideTime.setText("at " + whenStarts);

        try{
            Drawable drawable = (Globals.drawMan.userDrawable(mContext,
                    ride.driverId,
                    ride.picture_url)).get();
            drawable = RoundedDrawable.fromDrawable(drawable);
            ((RoundedDrawable) drawable)
                    .setCornerRadius(Globals.PICTURE_CORNER_RADIUS)
                    .setBorderColor(Color.LTGRAY)
                    .setBorderWidth(Globals.PICTURE_BORDER_WIDTH)
                    .setOval(true);

            holder.imageView.setImageDrawable(drawable);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getCause().toString());
        }
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    public RideAnnotated get(int position){
        return rides.get(position);
    }

    public void add(RideAnnotated item){
        rides.add(item);
        notifyItemInserted(getItemCount() - 1);
    }

    public void clear(){
        final int size = getItemCount();
        rides.clear();
        notifyItemRangeRemoved(0, size);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
            //implements View.OnClickListener, View.OnLongClickListener{

        public TextView txtView;
        public TextView txtRideTime;
        TextView txtRideFrom;
        TextView txtRideTo;
        TextView txtFreePlaces;
        ImageView imageView;

        RideAnnotated mRide;

        public ViewHolder(View row) {
            super(row);

            //row.setOnClickListener(this);

            txtView = (TextView)row.findViewById(R.id.txtTitle);
            txtRideTime = (TextView)row.findViewById(R.id.txtRideTime);
            txtRideFrom = (TextView)row.findViewById(R.id.txtRideFrom);
            txtRideTo = (TextView)row.findViewById(R.id.txtRideTo);
            txtFreePlaces = (TextView)row.findViewById(R.id.txtFreePlaces);
            imageView = (ImageView)row.findViewById(R.id.imgUserPic);
        }

        public void setItem(RideAnnotated item) {
            mRide = item;
        }

//        @Override
//        public void onClick(View view) {
//
//            Log.d(LOG_TAG, "onClick " + getPosition() + "" + mRide.ride_from);
//
////            Intent intent = new Intent(MainActivity.this,
////                    SingleRideActivity.class);
////            Bundle b = new Bundle();
////            b.putString("rideId", mRide.Id);
////            intent.putExtras(b);
////            startActivity(intent);
//        }
//
//        @Override
//        public boolean onLongClick(View view) {
//            return false;
//        }
    }
}
