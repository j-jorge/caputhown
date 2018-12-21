package de.digisocken.caputhown;

import android.app.Activity;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;

public class EntryAdapter extends BaseAdapter {
    public String squery;
    public ArrayList<PicEntry> picEntries = new ArrayList<>();
    Activity activity;

    EntryAdapter(Activity context) {
        super();
        squery = null;
        activity = context;
    }

    public void addItem(PicEntry item) {
        picEntries.add(item);
    }

    public void clear() {
        picEntries.clear();
    }

    public void filter(String query, EntryAdapter entryAdapter) {
        clear();
        squery = query;
        query = query.toLowerCase();
        ScrollingActivity.data_total = entryAdapter.getCount();
        ScrollingActivity.data_line = 0;

        for (int i = 0; i < ScrollingActivity.data_total; i++) {
            PicEntry picEntry = (PicEntry) entryAdapter.getItem(i);
            if (picEntry.title.toLowerCase().contains(query)) {
                addItem(picEntry);
                ScrollingActivity.data_line++;
            }
        }
        sort();
        ScrollingActivity.data_line = 0;
    }

    @Override
    public int getCount() {
        return picEntries.size();
    }

    @Override
    public Object getItem(int i) {
        return picEntries.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = activity.getLayoutInflater().inflate(R.layout.entry_line, viewGroup, false);
        TextView  tt = (TextView) view.findViewById(R.id.line_title);
        ImageView iv = (ImageView) view.findViewById(R.id.line_pic);

        tt.setText(picEntries.get(i).title);
        iv.setImageBitmap(picEntries.get(i).pic);
        iv.setMinimumHeight(picEntries.get(i).pic.getHeight());
        iv.setMinimumWidth(picEntries.get(i).pic.getWidth());

        if (i%2==0) {
            view.setBackgroundColor(ContextCompat.getColor(
                    activity.getApplicationContext(),
                    R.color.evenCol
            ));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(
                    activity.getApplicationContext(),
                    R.color.oddCol
            ));
        }
        return view;
    }


    public void asort() {

        Collections.sort(picEntries, new Comparator<PicEntry>() {
            @Override
            public int compare(PicEntry f1, PicEntry f2) {
                String s1 = f1.title;
                String s2 = f2.title;

                return s2.compareTo(s1);
            }
        });
    }

    public void sort() {

        Collections.sort(picEntries, new Comparator<PicEntry>() {
            @Override
            public int compare(PicEntry f1, PicEntry f2) {
                String s1 = f1.title;
                String s2 = f2.title;

                return s1.compareTo(s2);
            }
        });
    }
}