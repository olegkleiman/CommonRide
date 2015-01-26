package com.labs.okey.commonride.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.model.Ride;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleg on 21-Jan-15.
 */
public class RidesAdapter extends ArrayAdapter<Ride> {

    Context context;
    int layoutResourceId;
    List<Ride> rides = new ArrayList<Ride>();

    LayoutInflater m_inflater = null;

    public RidesAdapter(Context context,
                        int layoutResourceId,
                        List<Ride> data,
                        String userID) {
        super(context, layoutResourceId, data);

        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.rides = data;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void add(Ride ride){
        super.add(ride);
    }

    public int getCount() {
        return this.rides.size();
    }

    public Ride getItem(int index){
        return this.rides.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RidesHolder holder = null;

        Ride ride = this.getItem(position);

        if( row == null ) {

            row = m_inflater.inflate(layoutResourceId, parent, false);

            holder = new RidesHolder();

            holder.txtView = (TextView)row.findViewById(R.id.txtTitle);
            holder.txtFreePlaces = (TextView)row.findViewById(R.id.txtFreePlaces);

            row.setTag(holder);
        }
        else {
            holder = (RidesHolder)row.getTag();
        }

        holder.txtView.setText(ride.getDriver());
        holder.txtFreePlaces.setText(Integer.toString(ride.freePlaces));

        return row;
    }

    static class RidesHolder {
        TextView txtView;
        TextView txtFreePlaces;
    }
}
