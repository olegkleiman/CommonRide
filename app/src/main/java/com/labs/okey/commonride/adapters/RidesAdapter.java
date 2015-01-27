package com.labs.okey.commonride.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.model.Ride;
import com.labs.okey.commonride.model.RideAnnotated;
import com.labs.okey.commonride.utils.DrawableManager;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Oleg on 21-Jan-15.
 */
public class RidesAdapter extends ArrayAdapter<RideAnnotated> {

    Context context;
    int layoutResourceId;
    List<RideAnnotated> rides = new ArrayList<RideAnnotated>();

    LayoutInflater m_inflater = null;

    DrawableManager mDrawableManager;

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

        mDrawableManager = new DrawableManager();
    }

    @Override
    public void add(RideAnnotated ride){
        super.add(ride);
    }

    public int getCount() {
        return this.rides.size();
    }

    public RideAnnotated getItem(int index){
        return this.rides.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RidesHolder holder = null;

        RideAnnotated ride = this.getItem(position);

        if( row == null ) {

            row = m_inflater.inflate(layoutResourceId, parent, false);

            holder = new RidesHolder();

            holder.txtView = (TextView)row.findViewById(R.id.txtTitle);
            holder.txtDescription = (TextView)row.findViewById(R.id.txtRideDescription);
            holder.txtFreePlaces = (TextView)row.findViewById(R.id.txtFreePlaces);
            holder.imageView = (ImageView)row.findViewById(R.id.imgUserPic);
            holder.txtRideTime = (TextView)row.findViewById(R.id.txtRideTime);

            row.setTag(holder);
        }
        else {
            holder = (RidesHolder)row.getTag();
        }

        holder.txtView.setText(ride.first_name + " " + ride.last_name + " offers a ride from");
        String desc = String.format("%s to %s", ride.from, ride.to);
        holder.txtDescription.setText(desc);
        holder.txtFreePlaces.setText(Integer.toString(ride.freePlaces));
        SimpleDateFormat df = new SimpleDateFormat("EEEE MMM dd, yyyy");
        String whenStarts = df.format(ride.whenStarts);
        holder.txtRideTime.setText("at " + whenStarts);

        mDrawableManager.fetchDrawableOnThread(ride.picture_url,
                                               holder.imageView);

        return row;
    }

    static class RidesHolder {
        TextView txtView;
        TextView txtDescription;
        TextView txtFreePlaces;
        ImageView imageView;
        TextView txtRideTime;
    }
}
