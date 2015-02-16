package com.labs.okey.commonride.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.SingleRideActivity;
import com.labs.okey.commonride.model.Join;
import com.labs.okey.commonride.model.JoinAnnotated;
import com.labs.okey.commonride.model.RideAnnotated;
import com.labs.okey.commonride.utils.DrawableManager;
import com.labs.okey.commonride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleg Kleiman on 28-Jan-15.
 */
public class PassengersAdapter extends ArrayAdapter<JoinAnnotated>{

    private static final String LOG_TAG = "CommonRide.PassengersAdapter";

    Context context;
    int layoutResourceId;

    private List<JoinAnnotated> joins = new ArrayList<JoinAnnotated>();
    public List<JoinAnnotated> getJoins() {
        return joins;
    }

    private MobileServiceClient wamsClient;
    private MobileServiceTable<Join> mJoinsTable;

    LayoutInflater m_inflater = null;
    SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM dd, yyyy");

    DrawableManager mDrawableManager;
    String mMyUserID;
    Boolean mShowAcceptDecline;

    private static final String USERIDPREF = "userid";

    public PassengersAdapter(Context context,
                        int layoutResourceId,
                        List<JoinAnnotated> data,
                        Boolean showAcceptDecline) {
        super(context, layoutResourceId, data);

        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.joins = data;
        this.mShowAcceptDecline = showAcceptDecline;

        m_inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mDrawableManager = new DrawableManager();
        mDrawableManager.setRounded()
                .setCornerRadius(20)
                .setBorderColor(Color.GRAY)
                .setBorderWidth(2);

        try {
            wamsClient = new MobileServiceClient(
                    Globals.WAMS_URL,
                    Globals.WAMS_API_KEY,
                    context);
            mJoinsTable = wamsClient.getTable("joins", Join.class);
        } catch(Exception ex) {
            Log.e(LOG_TAG, ex.getMessage());
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mMyUserID = sharedPrefs.getString(USERIDPREF, "");
    }

    public int getCount() {
        return this.joins.size();
    }

    public JoinAnnotated getItem(int index){
        return this.joins.get(index);
    }

    private void updateJoin(JoinAnnotated join, String status) {

        SingleRideActivity parentActivity = (SingleRideActivity)context;
        parentActivity.updateJoin(join,status);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        JoinsHolder holder = null;

        final JoinAnnotated join = this.getItem(position);

        if( row == null ) {
            row = m_inflater.inflate(layoutResourceId, parent, false);

            holder = new JoinsHolder();

            holder.txtPassengerName = (TextView)row.findViewById(R.id.txtPassengerName);
            holder.txtJoined = (TextView)row.findViewById(R.id.txtPassengerWhenJoined);
            holder.imageView = (ImageView)row.findViewById(R.id.imgPassengerPic);
            holder.imgDeleteJoin = (ImageView)row.findViewById(R.id.passenger_delete);
            holder.imgAccepted = (ImageView) row.findViewById(R.id.imgStatus);
            holder.btnAccept = (Button)row.findViewById(R.id.btnPassengerAccept);
            holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateJoin( join, Globals.JOIN_STATUS_ACCEPTED );
                }
            });
            holder.btnDecline = (Button)row.findViewById(R.id.btnPassengerDecline);
            holder.btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateJoin( join, Globals.JOIN_STATUS_DECLINED );
                }
            });

            row.setTag(holder);
        }
        else {
            holder = (JoinsHolder)row.getTag();
        }

        holder.txtPassengerName.setText(join.first_name + " " + join.last_name);

        holder.txtJoined.setText("Joined at " + mDateFormat.format(join.whenJoined));

        mDrawableManager.fetchDrawableOnThread(join.picture_url,
                                                holder.imageView);

        if( !join.passengerId.equals(mMyUserID)) {
              holder.imgDeleteJoin.setVisibility(View.INVISIBLE);
        }

        holder.imgDeleteJoin.setTag(join.Id);
        holder.btnAccept.setTag(join.Id);
        holder.btnDecline.setTag(join.Id);

        if( join.status.equals(Globals.JOIN_STATUS_INIT) ){
            if( mShowAcceptDecline ) {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnDecline.setVisibility(View.VISIBLE);
                holder.imgAccepted.setVisibility(View.INVISIBLE);
            } else {
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnDecline.setVisibility(View.GONE);

                holder.imgAccepted.setImageResource(R.drawable.question2_16);
            }
        }
        else if( join.status.equals(Globals.JOIN_STATUS_ACCEPTED) ) {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);

            holder.imgAccepted.setImageResource(R.drawable.accept2_16);
        } else if( join.status.equals(Globals.JOIN_STATUS_DECLINED) ) {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnDecline.setVisibility(View.GONE);

            holder.imgAccepted.setImageResource(R.drawable.stop_16);
        }


        return row;
    }

    static class JoinsHolder {
        TextView txtPassengerName;
        TextView txtJoined;
        ImageView imageView;
        ImageView imgDeleteJoin;

        ImageView imgAccepted;
        Button btnAccept;
        Button btnDecline;
    }
}
