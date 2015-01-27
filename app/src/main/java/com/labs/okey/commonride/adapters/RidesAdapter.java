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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleg on 21-Jan-15.
 */
public class RidesAdapter extends ArrayAdapter<RideAnnotated> {

    Context context;
    int layoutResourceId;
    List<RideAnnotated> rides = new ArrayList<RideAnnotated>();

    LayoutInflater m_inflater = null;

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

            row.setTag(holder);
        }
        else {
            holder = (RidesHolder)row.getTag();
        }

        holder.txtView.setText(ride.getDriver() + " offers a ride from");
        String desc =  String.format("%s to %s", ride.from, ride.to);
        holder.txtDescription.setText(desc);
        holder.txtFreePlaces.setText(Integer.toString(ride.freePlaces));
        Drawable d = LoadImageFromWebOperations(ride.picture_url);
        holder.imageView.setImageDrawable(d);

        return row;
    }

    private Drawable LoadImageFromWebOperations(String url) {
        try
        {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        }catch (Exception e) {
            System.out.println("Exc="+e);
            return null;
        }
    }

    static class RidesHolder {
        TextView txtView;
        TextView txtDescription;
        TextView txtFreePlaces;
        ImageView imageView;
    }
}
