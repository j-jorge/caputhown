package de.digisocken.stop_o_moto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;

public class ScrollingActivity extends AppCompatActivity {
    private static final String PROJECT_LINK = "https://gitlab.com/deadlockz/caputhown/blob/master/Readme.md";
    private static final int CAMERA_REQUEST = 4711;
    public static String PACKAGE_NAME;
    private ActionBar ab = null;
    private FFmpeg ffmpeg = null;

    private boolean optHigh = false;
    private boolean optSlow = false;

    private boolean optAmovie = false;
    private boolean optRap = false;

    private ArrayList<Pair<Long, PicEntry>> picEntries;
    private EntryAdapter entryAdapter;
    private TextView emptyView;

    private String shareFile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        PACKAGE_NAME = getApplicationContext().getPackageName();

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
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onStart() {}
                @Override
                public void onFailure() {}
                @Override
                public void onSuccess() {}
                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
            emptyView.setText(R.string.no_support);
            ffmpeg = null;
        }
    }

    private void takePic() {
        if (optHigh) {
            File image = createAppFile("_temp.jpg");

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                        "de.digisocken.stop_o_moto.fileprovider",
                        image
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
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

            if (ab != null) ab.setTitle(R.string.processing);
            if (optAmovie) {
                entryAdapter.asort();
            } else {
                entryAdapter.sort();
            }
            shareFile = "";
            new ToMovieTask().execute(optRap, optSlow);
            return true;

        } else if (id == R.id.action_share) {
            if (shareFile.length() > 0) {
                final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.created_by));
                Uri furi = FileProvider.getUriForFile(
                        getApplicationContext(),
                        "de.digisocken.stop_o_moto.fileprovider",
                        createAppFile(shareFile)
                );
                sharingIntent.setDataAndType(furi, getContentResolver().getType(furi));

                sharingIntent.putExtra(Intent.EXTRA_STREAM, furi);
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.action_share)));
            }
        } else if (id == R.id.action_amovie) {
            if (item.isChecked()) {
                item.setChecked(false);
                optAmovie = false;
            } else {
                item.setChecked(true);
                optAmovie = true;
            }
            return true;
        } else if (id == R.id.action_slow) {
            if (item.isChecked()) {
                item.setChecked(false);
                optSlow = false;
            } else {
                item.setChecked(true);
                optSlow = true;
            }
            return true;
        } else if (id == R.id.action_rapmovie) {
            if (item.isChecked()) {
                item.setChecked(false);
                optRap = false;
            } else {
                item.setChecked(true);
                optRap = true;
            }
            return true;
        } else if (id == R.id.action_high) {
            if (item.isChecked()) {
                item.setChecked(false);
                optHigh = false;
            } else {
                item.setChecked(true);
                optHigh = true;
            }
            return true;
        } else if (id == R.id.action_info) {
            Intent intentProj = new Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_LINK));
            startActivity(intentProj);
            return true;
        } else if (id == R.id.action_picture) {
            takePic();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ToMovieTask extends AsyncTask<Boolean, Void, Boolean>{
        private String nameBasic;
        private File file;

        @Override
        protected Boolean doInBackground(Boolean... bools) {
            FileChannelWrapper out = null;
            nameBasic = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
            file = createAppFile(nameBasic + ".mp4");

            try {
                out = NIOUtils.writableFileChannel(file.getAbsolutePath());
                AndroidSequenceEncoder encoder;
                if (bools[1]) {
                    encoder = new AndroidSequenceEncoder(out, Rational.R(8, 1));
                } else {
                    encoder = new AndroidSequenceEncoder(out, Rational.R(13, 1));
                }
                if (bools[0]) entryAdapter.sort();

                for (Pair<Long, PicEntry> pe : picEntries) {
                    encoder.encodeImage(pe.second.pic);
                }

                if (bools[0]) {
                    entryAdapter.asort();
                    for (Pair<Long, PicEntry> pe : picEntries) {
                        encoder.encodeImage(pe.second.pic);
                    }
                }

                encoder.finish();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                NIOUtils.closeQuietly(out);

                if (ffmpeg != null) {
                    // ------------------------------------------------- makes a gif file
                    String[] cmdgif = {"-i", file.getPath(), getAppFolder().getPath() + "/" + nameBasic + ".gif"};
                    conversion(cmdgif, false);

                    // --------------------------------------------- makes whatsapp compatible file
                    String[] cmd_wa = {
                            "-i", file.getPath(),
                            "-c:v", "libx264",
                            "-profile:v", "baseline",
                            "-level", "3.0",
                            "-pix_fmt", "yuv420p",
                            getAppFolder().getPath() + "/" + nameBasic + "whatsapp.mp4"
                    };
                    conversion(cmd_wa, true);
                }

            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (ab != null) {
                ab.setTitle("  " + getString(R.string.app_name));
            }
            if (aBoolean) {
                emptyView.setText(R.string.ok);
                shareFile = nameBasic + ".gif";
                picEntries.clear();
                entryAdapter.notifyDataSetChanged();
            } else {
                emptyView.setText(R.string.fail);
                shareFile = "";
                picEntries.clear();
                entryAdapter.notifyDataSetChanged();
            }
            super.onPostExecute(aBoolean);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            PicEntry pe = new PicEntry();
            long order = picEntries.size();
            pe.title = String.format("%03d", order);

            if (optHigh) {
                Bitmap photo = BitmapFactory.decodeFile(
                        getAppFolder().getPath() + "/_temp.jpg"
                );
                pe.pic = Bitmap.createScaledBitmap(
                        photo,
                        2*(photo.getWidth()/10),
                        2*(photo.getHeight()/10),
                        true
                );
                File f = createAppFile("_temp.jpg");
                try {
                    f.getCanonicalFile().delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                pe.pic = Bitmap.createScaledBitmap(
                        photo,
                        (photo.getWidth()/2)*2,
                        (photo.getHeight()/2)*2,
                        true
                );
            }

            picEntries.add(new Pair<Long, PicEntry>(order, pe));
            emptyView.setText("");
            entryAdapter.asort();
            entryAdapter.notifyDataSetChanged();

            Toast.makeText(this, R.string.picadd, Toast.LENGTH_SHORT).show();
        }
    }

    private void conversion(final String[] cmd, final boolean delFinal) {

        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() { }

                @Override
                public void onProgress(String message) { }

                @Override
                public void onFailure(String message) {
                    emptyView.setText(R.string.fail);
                    Log.e(PACKAGE_NAME, message);
                    picEntries.clear();
                    entryAdapter.notifyDataSetChanged();
                }

                @Override
                public void onSuccess(String message) {
                    if (delFinal) {
                        File f = createAppFile(cmd[1]);
                        try {
                            if (f==null) return;
                            f.getCanonicalFile().delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFinish() { }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
            e.printStackTrace();
        }
    }

    private File getAppFolder() {
        File file = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            file = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    getPackageName()
            );
        } else {
            file = new File(Environment.getExternalStorageDirectory() + "/Documents/"+getPackageName());
        }

        try {
            if (!file.exists()) file.mkdirs();
        } catch (Exception e) {
            return null;
        }
        return file;
    }

    private File createAppFile(String name) {
        File file = getAppFolder();

        String path = file.getPath() + "/" + name;
        try {
            file = new File(path);
            if (!file.exists()) file.createNewFile();
        } catch (Exception e) {
            return null;
        }
        return file;
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
