package de.digisocken.stop_o_moto;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class EntryAdapter extends DragItemAdapter<Pair<Long, PicEntry>, EntryAdapter.ViewHolder> {
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    private Activity activity;

    EntryAdapter(Activity context, ArrayList<Pair<Long, PicEntry>> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setItemList(list);
        activity = context;
    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).first;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);

        holder.tt.setText(mItemList.get(position).second.title);
        holder.iv.setImageBitmap(mItemList.get(position).second.pic);
        holder.iv.setMinimumHeight(mItemList.get(position).second.pic.getHeight());
        holder.iv.setMinimumWidth(mItemList.get(position).second.pic.getWidth());

        if (position%2==0) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(
                    activity.getApplicationContext(),
                    R.color.evenCol
            ));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(
                    activity.getApplicationContext(),
                    R.color.oddCol
            ));
        }
        holder.ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(position);
                notifyDataSetChanged();
            }
        });

        holder.itemView.setTag(mItemList.get(position));
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView tt;
        ImageView iv;
        ImageButton ib;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            tt = itemView.findViewById(R.id.line_title);
            iv = itemView.findViewById(R.id.line_pic);
            ib = itemView.findViewById(R.id.line_trash);
        }
    }

    public void asort() {

        Collections.sort(mItemList, new Comparator<Pair<Long,PicEntry>>() {
            @Override
            public int compare(Pair<Long,PicEntry> f1, Pair<Long,PicEntry> f2) {
                long s1 = f1.first;
                long s2 = f2.first;

                return (int) (s2-s1);
            }
        });
    }

    public void sort() {

        Collections.sort(mItemList, new Comparator<Pair<Long,PicEntry>>() {
            @Override
            public int compare(Pair<Long,PicEntry> f1, Pair<Long,PicEntry> f2) {
                long s1 = f1.first;
                long s2 = f2.first;

                return (int) (s1-s2);
            }
        });
    }
}