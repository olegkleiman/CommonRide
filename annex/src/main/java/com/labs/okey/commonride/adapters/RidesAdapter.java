package com.labs.okey.commonride.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.model.Ride;
import com.labs.okey.commonride.model.RideAnnotated;
import com.labs.okey.commonride.utils.Globals;
import com.labs.okey.commonride.utils.RoundedDrawable;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Oleg Kleiman on 21-Jan-15.
 */
public class RidesAdapter extends ArrayAdapter<RideAnnotated> {

    private static final String LOG_TAG = "Annex.RidesAdapter";

    Context context;
    int layoutResourceId;
    List<RideAnnotated> rides = new ArrayList<RideAnnotated>();

    LayoutInflater m_inflater = null;
    SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    String mDesc;
    //DrawableManager mDrawableManager;

    public RidesAdapter(Context context,
                        int layoutResourceId) {
        super(context, layoutResourceId);

        this.context = context;
        this.layoutResourceId = layoutResourceId;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mDesc = context.getResources().getString(R.string.ride_desc);

//        mDrawableManager = new DrawableManager();
//        mDrawableManager.setRounded()
//                .setCornerRadius(20)
//                .setBorderColor(Color.GRAY)
//                .setBorderWidth(4);

    }

    // This c'tor is used only in online scenario
    // where the list of rides is ready to binding.
    // For offline scenario it is never used.
    public RidesAdapter(Context context,
                        int layoutResourceId,
                        List<RideAnnotated> data,
                        String userID) {
        super(context, layoutResourceId, data);

        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.rides = data;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDesc = context.getResources().getString(R.string.ride_desc);

        //mDrawableManager = new DrawableManager();
    }

//    @Override
//    public void add(RideAnnotated ride){
//        super.add(ride);
//    }
//
//    public int getCount() {
//        return this.rides.size();
//    }
//
//    public RideAnnotated getItem(int index){
//        return this.rides.get(index);
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RidesHolder holder = null;

        RideAnnotated ride = this.getItem(position);

        if( row == null ) {

            row = m_inflater.inflate(layoutResourceId, parent, false);

            holder = new RidesHolder();

            holder.txtView = (TextView)row.findViewById(R.id.txtTitle);
            //holder.txtDescription = (TextView)row.findViewById(R.id.txtRideDescription);
            holder.txtFreePlaces = (TextView)row.findViewById(R.id.txtFreePlaces);
            holder.imageView = (ImageView)row.findViewById(R.id.imgUserPic);
            holder.txtRideTime = (TextView)row.findViewById(R.id.txtRideTime);

            holder.txtRideFrom = (TextView)row.findViewById(R.id.txtRideFrom);
            holder.txtRideTo = (TextView)row.findViewById(R.id.txtRideTo);

            row.setTag(holder);
        }
        else {
            holder = (RidesHolder)row.getTag();
        }

        String desc = String.format("%s to %s %s",
                        ride.first_name, ride.last_name, mDesc);
        holder.txtView.setText(desc);
        holder.txtRideFrom.setText(ride.ride_from);
        holder.txtRideTo.setText(ride.ride_to);
        holder.txtFreePlaces.setText(Integer.toString(ride.freePlaces));

        String whenStarts = mDateFormat.format(ride.whenStarts);
        holder.txtRideTime.setText("at " + whenStarts);

        try{
            Drawable drawable = (Globals.drawMan.userDrawable(context,
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

//        mDrawableManager.fetchDrawableOnThread(ride.picture_url,
//                                               holder.imageView);

        return row;
    }

    static class RidesHolder {
        TextView txtView;
        TextView txtFreePlaces;
        ImageView imageView;
        TextView txtRideTime;
        TextView txtRideFrom;
        TextView txtRideTo;
    }
}
