package com.labs.okey.commonride.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.widget.FacebookDialog;
import com.labs.okey.commonride.R;
import com.labs.okey.commonride.SingleRideActivity;
import com.labs.okey.commonride.model.Join;
import com.labs.okey.commonride.model.JoinAnnotated;
import com.labs.okey.commonride.model.RideAnnotated;
import com.labs.okey.commonride.utils.Globals;
import com.labs.okey.commonride.utils.RoundedDrawable;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Oleg Kleiman on 28-Jan-15.
 */
public class PassengersAdapter extends ArrayAdapter<JoinAnnotated>{

    private static final String LOG_TAG = "Annex.PassengersAdapter";

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
            holder.btnPassengerLines = (Button)row.findViewById(R.id.passenger_lines);
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

        try{
            Drawable drawable = (Globals.drawMan.userDrawable(context,
                                                            join.passengerId,
                                                            join.picture_url))
                                .get();
            if( drawable != null ) {
                drawable = RoundedDrawable.fromDrawable(drawable);
                ((RoundedDrawable) drawable)
                        .setCornerRadius(Globals.PICTURE_CORNER_RADIUS)
                        .setBorderColor(Color.LTGRAY)
                        .setBorderWidth(Globals.PICTURE_BORDER_WIDTH)
                        .setOval(true);
                holder.imageView.setImageDrawable(drawable);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getCause().toString());
        }

        if( !join.passengerId.equals(mMyUserID)) {
            holder.imgDeleteJoin.setVisibility(View.GONE);

            setupRadialMenu((Activity)context, holder.btnPassengerLines);

            //holder.imgDeleteJoin.setImageDrawable(context.getResources().getDrawable(R.drawable.launcher_32));
        } else {
            holder.btnPassengerLines.setVisibility(View.GONE);
        }

        holder.imgDeleteJoin.setTag(join.Id);
        holder.btnAccept.setTag(join.Id);
        holder.btnDecline.setTag(join.Id);
        holder.btnPassengerLines.setTag(join.phone);

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

    private void setupRadialMenu(final Activity activity, Button actionButton){

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(activity);

        ImageView itemIconFacebook = new ImageView(activity);
        itemIconFacebook.setImageDrawable(activity.getResources().getDrawable(R.drawable.facebook32));
        SubActionButton button1 = itemBuilder.setContentView(itemIconFacebook).build();
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
//                        FacebookDialog dialog = builder.build();
//                        dialog.present();
                } catch(Exception ex) {
                    Log.e(LOG_TAG, ex.getCause().toString());
                }
            }
        });

        ImageView itemIconWhatsup = new ImageView(activity);
        itemIconWhatsup.setImageDrawable(activity.getResources().getDrawable(R.drawable.whatsapp32));
        SubActionButton button2 = itemBuilder.setContentView(itemIconWhatsup).build();

        ImageView itemIconEmail = new ImageView(activity);
        itemIconEmail.setImageDrawable(activity.getResources().getDrawable(R.drawable.email32));
        SubActionButton button3 = itemBuilder.setContentView(itemIconEmail).build();

        ImageView itemIconShare = new ImageView(activity);
        itemIconShare.setImageDrawable(activity.getResources().getDrawable(R.drawable.phone32));
        SubActionButton button4 = itemBuilder.setContentView(itemIconShare).build();
        button4.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String phoneNumber = (String)view.getTag();
                                            SingleRideActivity srActivity =
                                                    (SingleRideActivity)activity;
                                            srActivity.callPhoneNumber(phoneNumber);
                                        }
                                   });

        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(activity)
                .setStartAngle(90)
                .setEndAngle(180)
                .setRadius(activity.getResources().getDimensionPixelSize(R.dimen.radius_medium))
                .addSubActionView(button1)
                .addSubActionView(button2)
                .addSubActionView(button3)
                .addSubActionView(button4)
                .attachTo(actionButton)
                .build();

    }

    static class JoinsHolder {
        TextView txtPassengerName;
        TextView txtJoined;
        ImageView imageView;
        ImageView imgDeleteJoin;
        Button btnPassengerLines;
        ImageView imgAccepted;
        Button btnAccept;
        Button btnDecline;
    }
}
