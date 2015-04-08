package com.labs.okey.commonride.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.model.Ride;

import java.text.SimpleDateFormat;


/**
 * Created by Oleg Kleiman on 18-Feb-15.
 */
public class MyRidesDriverAdapter extends ArrayAdapter<Ride> {

    Context context;
    int layoutResourceId;

    LayoutInflater m_inflater = null;

    public MyRidesDriverAdapter(Context context,
                        int layoutResourceId) {
        super(context, layoutResourceId);

        this.context = context;
        this.layoutResourceId = layoutResourceId;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder;

        Ride ride = this.getItem(position);

        if( row == null ) {
            row = m_inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.txtTitle1 = (TextView)row.findViewById(R.id.txtMyRidesDriverTitle1);
            holder.txtTitle2 = (TextView)row.findViewById(R.id.txtMyRidesDriverTitle2);
            holder.txtFrom = (TextView)row.findViewById(R.id.txtMyRidesDriverFrom);
            holder.txtTo = (TextView)row.findViewById(R.id.txtMyRidesDriverTo);

            row.setTag(holder);

        } else {
            holder = (Holder)row.getTag();
        }

        SimpleDateFormat df = new SimpleDateFormat("dd");
        holder.txtTitle1.setText( df.format(ride.whenStarts) );

        df = new SimpleDateFormat("MMM");
        holder.txtTitle2.setText( df.format(ride.whenStarts) );

        holder.txtFrom.setText( ride.ride_from );
        holder.txtTo.setText( ride.ride_to );

        return row;
    }


    static class Holder {
        TextView txtTitle1;
        TextView txtTitle2;

        TextView txtFrom;
        TextView txtTo;
    }

}
