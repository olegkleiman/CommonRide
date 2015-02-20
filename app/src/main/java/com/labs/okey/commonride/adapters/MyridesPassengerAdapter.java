package com.labs.okey.commonride.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.model.Join;
import com.labs.okey.commonride.model.Ride;

/**
 * Created by Oleg Kleiman on 19-Feb-15.
 */
public class MyridesPassengerAdapter extends ArrayAdapter<Join> {

    Context context;
    int layoutResourceId;

    LayoutInflater m_inflater = null;

    public MyridesPassengerAdapter(Context context,
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
        Holder holder = null;

        Join join = this.getItem(position);

        if( row == null ) {
            row = m_inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.txtTitle1 = (TextView)row.findViewById(R.id.txtMyRidesPassengerTitle1);
            holder.txtTitle2 = (TextView)row.findViewById(R.id.txtMyRidesPassengerTitle2);
            holder.txtFrom = (TextView)row.findViewById(R.id.txtMyRidesPassengerFrom);
            holder.txtTo = (TextView)row.findViewById(R.id.txtMyRidesPassengerTo);

            row.setTag(holder);
        }else {
            holder = (Holder)row.getTag();
        }

            return row;
    }

    static class Holder {
        TextView txtTitle1;
        TextView txtTitle2;

        TextView txtFrom;
        TextView txtTo;
    }
}
