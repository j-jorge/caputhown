package de.digisocken.stop_o_moto;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.jcodec.containers.mp4.boxes.CompositionOffsetsBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class EntryAdapter extends BaseAdapter {
    public String squery;
    public ArrayList<PicEntry> picEntries = new ArrayList<>();
    private Activity activity;

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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        view = activity.getLayoutInflater().inflate(R.layout.entry_line, viewGroup, false);
        TextView  tt = (TextView) view.findViewById(R.id.line_title);
        final ImageView iv = (ImageView) view.findViewById(R.id.line_pic);

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
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popup = new PopupMenu(activity, iv);
                popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem mitem) {
                        int j = mitem.getItemId();
                        int id = picEntries.size() -i-1;
                        Log.d("index", Integer.toString(id));
                        if (j == R.id.action_up) {
                            if (i==0) return true;
                            for (int x=0; x<picEntries.size(); x++) {
                                Log.d("Before", picEntries.get(x).title);
                            }
                            upOrder(id);
                            for (int x=0; x<picEntries.size(); x++) {
                                Log.d("After", picEntries.get(x).title);
                            }
                            asort();
                            for (int x=0; x<picEntries.size(); x++) {
                                Log.d("Aftersort", picEntries.get(x).title);
                            }
                            notifyDataSetChanged();
                            return true;
                        } else if (j == R.id.action_del){
                            picEntries.remove(i);
                            asort();
                            notifyDataSetChanged();
                            return true;
                        } else if (j == R.id.action_down) {
                            if (id==0) return true;
                            for (int x=0; x<picEntries.size(); x++) {
                                Log.d("Before", picEntries.get(x).title);
                            }
                            downOrder(id);
                            for (int x=0; x<picEntries.size(); x++) {
                                Log.d("After", picEntries.get(x).title);
                            }
                            asort();
                            for (int x=0; x<picEntries.size(); x++) {
                                Log.d("Aftersort", picEntries.get(x).title);
                            }
                            notifyDataSetChanged();
                            return true;
                        } else {
                            return onMenuItemClick(mitem);
                        }
                    }
                });

                popup.show();
            }
        });

        return view;
    }

    private void upOrder(int id) {
        Collections.swap(picEntries, id, id+1);
        PicEntry pe = picEntries.get(id);
        pe.order++;
        //pe.title = String.format("%03d", pe.order);

        pe = picEntries.get(id+1);
        pe.order--;
        //pe.title = String.format("%03d", pe.order);
    }

    private void downOrder(int id) {
        Collections.swap(picEntries, id, id-1);
        PicEntry pe = picEntries.get(id);
        pe.order--;
        //pe.title = String.format("%03d", pe.order);

        pe = picEntries.get(id-1);
        pe.order++;
        //pe.title = String.format("%03d", pe.order);
    }



    public void asort() {

        Collections.sort(picEntries, new Comparator<PicEntry>() {
            @Override
            public int compare(PicEntry f1, PicEntry f2) {
                int s1 = f1.order;
                int s2 = f2.order;

                return s2-s1;
            }
        });
    }

    public void sort() {

        Collections.sort(picEntries, new Comparator<PicEntry>() {
            @Override
            public int compare(PicEntry f1, PicEntry f2) {
                int s1 = f1.order;
                int s2 = f2.order;

                return s1-s2;
            }
        });
    }
}