package com.devglyph.reitittaja.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import com.devglyph.reitittaja.R;

import java.util.ArrayList;
import java.util.HashMap;

public class RoutesAdapter extends BaseExpandableListAdapter {

    private ArrayList<ArrayList<HashMap<String, String>>> mChildData;
    private ArrayList<HashMap<String, String>> mGroupData;
    private Context mContext;
    private final LayoutInflater inf;

    /**
     * Save the references to the view
     */
    static class ViewHolder {
        public TextView duration;
        public TextView firstRowBorder;
        public TableRow iconRow;
        public TextView routeId;
        public TextView startTime;
        public TextView endTime;
        public TextView dummyTextView;
    }

    public RoutesAdapter(ArrayList<ArrayList<HashMap<String, String>>> childData, ArrayList<HashMap<String, String>> groupData, Context context) {
        this.mChildData = childData;
        this.mGroupData = groupData;
        this.mContext = context;
        inf = LayoutInflater.from(context);
    }

    @Override
    public int getGroupCount() {
        return mGroupData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildData.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inf.inflate(R.layout.expandable_list_group, null);

            holder = new ViewHolder();
            holder.duration = (TextView) convertView.findViewById(R.id.text_s_duration);
            holder.firstRowBorder = (TextView) convertView.findViewById(R.id.first_row_top_border);
            holder.iconRow = (TableRow) convertView.findViewById(R.id.row_for_icons);
            holder.startTime = (TextView) convertView.findViewById(R.id.text_s_st_time);
            holder.endTime = (TextView) convertView.findViewById(R.id.text_s_end_time);
            holder.dummyTextView = (TextView) convertView.findViewById(R.id.line_number);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.startTime.setText(mGroupData.get(groupPosition).get("st_time"));
        holder.endTime.setText(mGroupData.get(groupPosition).get("end_time"));

        holder.firstRowBorder.setText("Routes");
        holder.firstRowBorder.setVisibility(View.VISIBLE);

        holder.iconRow.removeAllViews();

        addLegIconsToGroup(holder, groupPosition);

        holder.duration.setText(mGroupData.get(groupPosition).get("duration"));
        holder.duration.setTextColor(Color.BLACK);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TextView textView = new TextView(mContext);
        textView.setText(getChild(groupPosition, childPosition).toString());
        return textView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * Add leg icons to the group
     * @param holder
     * @param groupPosition
     */
    private void addLegIconsToGroup(ViewHolder holder, int groupPosition) {
        // Add leg icons to group
        for (int i = 0; i < mChildData.get(groupPosition).size(); i++) {
            String icon = mChildData.get(groupPosition).get(i).get("icon");
            //int iIcon = Integer.parseInt(icon);

            TextView tView = new TextView(mContext);
            //tView.setCompoundDrawablesWithIntrinsicBounds(null,
            //        mContext.getResources().getDrawable(iIcon), null, null);
            tView.setText(mChildData.get(groupPosition).get(i).get("routeId"));

            tView.setFocusable(false);
            tView.setTextColor(Color.BLACK);
            android.view.ViewGroup.LayoutParams frame = holder.dummyTextView.getLayoutParams();
            tView.setLayoutParams(frame);

            holder.iconRow.addView(tView);
        }
    }
}
