package de.digisocken.stop_o_moto;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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

public class EntryAdapter extends DragItemAdapter<PicEntry, EntryAdapter.ViewHolder> {
    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    private Activity activity;

    EntryAdapter(Activity context, ArrayList<PicEntry> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        setItemList(list);
        activity = context;
    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).index;
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

        final PicEntry entry = mItemList.get(position);

        holder.tt.setText(String.format("%03d", entry.index));
        holder.iv.setImageBitmap(entry.thumbnail);
        holder.iv.setMinimumHeight(entry.thumbnail.getHeight());
        holder.iv.setMinimumWidth(entry.thumbnail.getWidth());

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

        holder.itemView.setTag(entry);
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
}
