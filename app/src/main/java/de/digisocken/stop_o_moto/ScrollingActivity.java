package de.digisocken.stop_o_moto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.woxthebox.draglistview.DragItem;
import com.woxthebox.draglistview.DragListView;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Date;

public class ScrollingActivity extends AppCompatActivity {
    private static final String PROJECT_LINK = "https://gitlab.com/deadlockz/caputhown/blob/master/Readme.md";
    private static final int CAMERA_REQUEST = 4711;
    public static String PACKAGE_NAME;
    private ActionBar ab = null;
    private FFmpeg ffmpeg = null;

    private ArrayList<PicEntry> picEntries;
    private EntryAdapter entryAdapter;
    private TextView emptyView;

    private String shareFile = "";
    private AppFiles m_app_files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        PACKAGE_NAME = getApplicationContext().getPackageName();

        m_app_files = new AppFiles(getApplicationContext().getPackageName());

        try {
            ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayShowHomeEnabled(true);
                ab.setHomeButtonEnabled(true);
                ab.setDisplayUseLogoEnabled(true);
                ab.setLogo(R.mipmap.ic_launcher);
                ab.setTitle("  " + getString(R.string.app_name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        DragListView entryList = (DragListView) findViewById(R.id.picList);
        emptyView = (TextView) findViewById(android.R.id.empty);

        picEntries = new ArrayList<>();
        entryList.setLayoutManager(new LinearLayoutManager(this));
        entryAdapter = new EntryAdapter(this, picEntries, R.layout.entry_line, R.id.line_title, false);
        entryList.setAdapter(entryAdapter, true);
        entryList.setCanDragHorizontally(false);
        entryList.getRecyclerView().setVerticalScrollBarEnabled(true);
        entryList.setCustomDragItem(new MyDragItem(this, R.layout.entry_line));

        ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler());
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
            emptyView.setText(R.string.no_support);
            ffmpeg = null;
        }
    }

    private void takePic() {
        File image = m_app_files.create_app_file("_temp.jpg");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Uri photoURI = FileProvider.getUriForFile
                (getApplicationContext(),
                 "de.digisocken.stop_o_moto.fileprovider",
                 image);
            // This extra is required to have a high resolution image.
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_movie) {

            if (ab != null)
                ab.setTitle(R.string.processing);
            shareFile = "";

            SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);

            new ConversionJob
                (m_app_files, picEntries,
                 preferences.getBoolean("timeline_loop_back", false),
                 preferences.getBoolean("build_main_mp4", false),
                 preferences.getBoolean("build_gif", false),
                 preferences.getBoolean("build_whatsapp_mp4", false),
                 preferences.getInt("framerate", 15),
                 ffmpeg,
                 new ConversionJob.Listener() {
                     @Override
                     public void succeed(final String basename) {
                         runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     onPostExecute(true, basename);
                                 }
                             });
                     }

                     @Override
                     public void failed() {
                         runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     onPostExecute(false, null);
                                 }
                             });
                     }
                 })
                .execute();

            return true;

        } else if (id == R.id.action_share) {
            if (shareFile.length() > 0) {
                final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.created_by));
                Uri furi = FileProvider.getUriForFile(
                        getApplicationContext(),
                        "de.digisocken.stop_o_moto.fileprovider",
                        m_app_files.create_app_file(shareFile)
                );
                sharingIntent.setDataAndType(furi, getContentResolver().getType(furi));

                sharingIntent.putExtra(Intent.EXTRA_STREAM, furi);
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.action_share)));
            }
        } else if (id == R.id.action_info) {
            Intent intentProj = new Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_LINK));
            startActivity(intentProj);
            return true;
        } else if (id == R.id.action_picture) {
            takePic();
            return true;
        } else if (id == R.id.action_preferences) {
            openPreferences();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openPreferences() {
        startActivity
            (new Intent(ScrollingActivity.this, PreferenceActivity.class));
    }

    private void onPostExecute(Boolean aBoolean, String basename) {
        if (ab != null) {
            ab.setTitle("  " + getString(R.string.app_name));
        }
        if (aBoolean) {
            emptyView.setText(R.string.ok);
            shareFile = basename + ".gif";
        } else {
            emptyView.setText(R.string.fail);
            shareFile = "";
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            PicEntry pe = new PicEntry();
            pe.index = picEntries.size();

            final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
            final int shortDimension =
                Integer.valueOf(preferences.getString("resolution", "0"));

            Bitmap photo = BitmapFactory.decodeFile
                (m_app_files.app_folder().getPath() + "/_temp.jpg");

            if (shortDimension != 0) {
                int width = photo.getWidth();
                int height = photo.getHeight();

                if (width > height) {
                    width = shortDimension * width / height;
                    height = shortDimension;
                } else {
                    height = shortDimension * height / width;
                    width = shortDimension;
                }

                photo = Bitmap.createScaledBitmap(photo, width, height, true);
            }

            pe.picture = photo;

            try {
                m_app_files.create_app_file("_temp.jpg")
                    .getCanonicalFile()
                    .delete();
            } catch (IOException e) {
                e.printStackTrace();
            }

            pe.thumbnail = Bitmap.createScaledBitmap
                (pe.picture,
                 100,
                 100 * pe.picture.getHeight() / pe.picture.getWidth(),
                 false);

            picEntries.add(pe);
            emptyView.setText("");
            entryAdapter.notifyDataSetChanged();

            takePic();
        }
    }

    private static class MyDragItem extends DragItem {

        MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence text = ((TextView) clickedView.findViewById(R.id.line_title)).getText();
            Drawable dr = ((ImageView) clickedView.findViewById(R.id.line_pic)).getDrawable();
            ((TextView) dragView.findViewById(R.id.line_title)).setText(text);
            ((ImageView) dragView.findViewById(R.id.line_pic)).setImageDrawable(dr);
        }
    }
}
