package com.labs.okey.commonride.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.model.RideAnnotated;

/**
 * Created by Oleg on 08-Feb-15.
 */
public class RatingAdapter extends ArrayAdapter<RideAnnotated> {

    Context context;
    int layoutResourceId;
    LayoutInflater m_inflater = null;

    public RatingAdapter(Context context, int layoutResourceId) {

        super(context, layoutResourceId);

        this.context = context;
        this.layoutResourceId = layoutResourceId;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RatingHolder holder = null;

        if( row == null ) {

            row = m_inflater.inflate(layoutResourceId, parent, false);

            holder = new RatingHolder();

            holder.rbView = (RatingBar)row.findViewById(R.id.ratingBar);
        } else {
            holder = (RatingHolder)row.getTag();
        }

        holder.rbView.setRating(4.3f);

        return row;
    }

    static class RatingHolder {
        TextView txtView;
        RatingBar rbView;
    }
}
