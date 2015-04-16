package com.devglyph.reitittaja.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.models.Route;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RoutesAdapter extends ArrayAdapter<Route> {

    private ArrayList<Route> mRoutes;
    private Context mContext;

    /**
     * Save the references to the view
     */
    static class ViewHolder {
        public TextView duration;
        public TableRow iconRow;
        public TextView routeId;
        public TextView startTime;
        public TextView endTime;
        public TextView dummyTextView;
    }

    public RoutesAdapter(Context context, ArrayList<Route> routes) {
        super(context, R.layout.routes_list_item, routes);
        this.mRoutes = routes;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        Route route = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.routes_list_item, parent, false);

            holder = new ViewHolder();
            holder.duration = (TextView) convertView.findViewById(R.id.text_s_duration);
            holder.iconRow = (TableRow) convertView.findViewById(R.id.row_for_icons);
            holder.startTime = (TextView) convertView.findViewById(R.id.text_s_st_time);
            holder.endTime = (TextView) convertView.findViewById(R.id.text_s_end_time);
            holder.dummyTextView = (TextView) convertView.findViewById(R.id.line_number);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.startTime.setText(parseTimeToHHMM(route.getStartLocation().getDepartureTime()));
        holder.endTime.setText(parseTimeToHHMM(route.getEndLocation().getArrivalTime()));

        holder.iconRow.removeAllViews();

        addLegIconsToGroup(holder, route);

        holder.duration.setText( calculateDurationInHHMM(route.getDuration()));
        holder.duration.setTextColor(Color.BLACK);

        return convertView;
    }

    private String parseTimeToHHMM(Date date) {
        SimpleDateFormat simpleDateFormat;

        simpleDateFormat = new SimpleDateFormat("HHmm", Locale.US);
        return simpleDateFormat.format(date);
    }

    /**
     * Format the duration to X h Y min (Z sec) format.
     * @param duration in seconds
     * @return duration in string X h Y min format, if h and min > 0, else return Z sec
     */
    public static String calculateDurationInHHMM(double duration) {
        String hh = "";
        String mm = "";
        String ss = "";

        int h = 0;
        int m = 0;
        int s = 0;

        if (((int) duration / 3600) > 0) {
            h = ((int) duration / 3600);
            hh = h + " h ";
        }

        if (((int) duration % 3600) / 60 > 0) {
            m = (((int) duration % 3600) / 60);
            mm = m + " min ";
        }

        if (h == 0 && m == 0) {
            s = (int) duration; //the duration is already in seconds, so just cast it
            ss = s + " sec";
        }

        return hh + mm + ss;
    }

    /**
     * Add leg icons to the group
     * @param holder
     * @param route
     */
    private void addLegIconsToGroup(ViewHolder holder, Route route) {
        // Add leg icons to group
        for (int i = 0; i < route.getLegs().size(); i++) {
            String icon = "" + route.getLegs().get(i).getType();
            //int iIcon = Integer.parseInt(icon);

            TextView tView = new TextView(mContext);
            //tView.setCompoundDrawablesWithIntrinsicBounds(null,
            //        mContext.getResources().getDrawable(iIcon), null, null);
            tView.setText(route.getLegs().get(i).getLineCode());

            tView.setFocusable(false);
            tView.setTextColor(Color.BLACK);
            android.view.ViewGroup.LayoutParams frame = holder.dummyTextView.getLayoutParams();
            tView.setLayoutParams(frame);

            holder.iconRow.addView(tView);
        }
    }
}
