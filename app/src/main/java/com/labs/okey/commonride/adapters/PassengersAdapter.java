package com.labs.okey.commonride.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.model.JoinAnnotated;
import com.labs.okey.commonride.model.RideAnnotated;
import com.labs.okey.commonride.utils.DrawableManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleg on 28-Jan-15.
 */
public class PassengersAdapter extends ArrayAdapter<JoinAnnotated>{

    Context context;
    int layoutResourceId;

    List<JoinAnnotated> joins = new ArrayList<JoinAnnotated>();

    LayoutInflater m_inflater = null;

    DrawableManager mDrawableManager;

    public PassengersAdapter(Context context,
                        int layoutResourceId,
                        List<JoinAnnotated> data) {
        super(context, layoutResourceId, data);

        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.joins = data;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mDrawableManager = new DrawableManager();

    }

    public int getCount() {
        return this.joins.size();
    }

    public JoinAnnotated getItem(int index){
        return this.joins.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        JoinsHolder holder = null;

        JoinAnnotated join = this.getItem(position);

        if( row == null ) {
            row = m_inflater.inflate(layoutResourceId, parent, false);

            holder = new JoinsHolder();

            holder.txtPassengerName = (TextView)row.findViewById(R.id.txtPassengerName);
            holder.txtJoined = (TextView)row.findViewById(R.id.txtPassengerWhenJoined);
            holder.imageView = (ImageView)row.findViewById(R.id.imgPassengerPic);

            row.setTag(holder);
        }
        else {
            holder = (JoinsHolder)row.getTag();
        }

        holder.txtPassengerName.setText(join.first_name + " " + join.last_name);

        SimpleDateFormat df = new SimpleDateFormat("EEEE MMM dd, yyyy");
        holder.txtJoined.setText(df.format(join.whenJoined));

        mDrawableManager.fetchDrawableOnThread(join.picture_url,
                                                holder.imageView);


        return row;
    }

    static class JoinsHolder {
        TextView txtPassengerName;
        TextView txtJoined;
        ImageView imageView;
    }
}
