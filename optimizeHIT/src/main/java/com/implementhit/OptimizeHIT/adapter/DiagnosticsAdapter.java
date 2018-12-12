package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.util.FontsHelper;

/**
 * Created by anhaytananun on 19.01.16.
 */
public class DiagnosticsAdapter extends ArrayAdapter<Boolean> {
    private Boolean[] diagnosticsData;
    private String[] diagnosticsLabels;
    private Context context;

    public DiagnosticsAdapter(Context context, Boolean[] diagnosticsData) {
        super(context, R.layout.item_diagnostics);

        this.context = context;
        this.diagnosticsData = diagnosticsData;
        this.diagnosticsLabels = context.getResources().getStringArray(R.array.diagnostics_items);
    }

    @Override
    public int getCount() {
        return diagnosticsData.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView diagnosticsIcon = null;
        TextView diagnosticsLabel = null;

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_diagnostics, parent, false);

            diagnosticsIcon = (TextView) convertView.findViewById(R.id.permission_icon);
            diagnosticsIcon.setTypeface(FontsHelper.sharedHelper(context).fontello());
        }

        if (diagnosticsIcon == null) {
            diagnosticsIcon = (TextView) convertView.findViewById(R.id.permission_icon);
        }

        diagnosticsLabel = (TextView) convertView.findViewById(R.id.label);

        diagnosticsLabel.setText(diagnosticsLabels[position]);

        int icon = diagnosticsData[position] ? R.string.icon_ok_bubble : R.string.icon_cancel_circled_dark;
        int color = diagnosticsData[position] ? R.color.green : R.color.red;

        diagnosticsIcon.setText(icon);
        diagnosticsIcon.setTextColor(context.getResources().getColor(color));

        return convertView;
    }

    public void refreshDiagnostics(Boolean[] diagnosticsData) {
        this.diagnosticsData = diagnosticsData;
        notifyDataSetChanged();
    }
}
