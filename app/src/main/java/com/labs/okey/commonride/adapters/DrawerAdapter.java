package com.labs.okey.commonride.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.labs.okey.commonride.R;
import com.labs.okey.commonride.SingleRideActivity;

import java.util.List;

/**
 * Created by Oleg Kleiman on 15-Feb-15.
 */
public class DrawerAdapter extends ArrayAdapter<String> {

    Context context;
    List<String> drawerItemList;
    int layoutResID;

    public DrawerAdapter(Context context, int layoutResourceID,
                         List<String> listItems){
        super(context, layoutResourceID, listItems);

        this.context = context;
        this.drawerItemList = listItems;
        this.layoutResID = layoutResourceID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DrawerItemHolder drawerHolder;
        View view = convertView;

        if( view == null ) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            drawerHolder = new DrawerItemHolder();

            view = inflater.inflate(layoutResID, parent, false);
            drawerHolder.Caption = (TextView)view.findViewById(R.id.txtViewDrawerCaption);
            drawerHolder.Image = (ImageView)view.findViewById(R.id.imageViewDrawer);

            view.setTag(drawerHolder);

        } else {
            drawerHolder = (DrawerItemHolder) view.getTag();
        }

        if( position == 0 ) {
            drawerHolder.Image.setVisibility(View.VISIBLE);
        }
        String dItem = (String)this.drawerItemList.get(position);
        drawerHolder.Caption.setText(dItem);

        return view;
    }

    private static class DrawerItemHolder {
        TextView Caption;
        ImageView Image;
    }

}
