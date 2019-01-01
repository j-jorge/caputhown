package de.digisocken.caputhown;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScrollingActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private ActionBar ab = null;
    private FFmpeg ffmpeg = null;

    // todo
    private boolean highRes = false;

    String mCurrentPhotoPath;

    private EntryAdapter entryAdapter;
    private ListView entryList;
    public static int data_total = 1;
    public static int data_line = 0;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

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

        entryList = (ListView) findViewById(R.id.picList);
        entryAdapter = new EntryAdapter(this);
        emptyView = (TextView) findViewById(android.R.id.empty);
        entryList.setEmptyView(emptyView);
        entryList.setAdapter(entryAdapter);

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
        try {
            if (highRes) {
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH).format(new Date());
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                File image = File.createTempFile(
                        timeStamp,
                        ".jpg",
                        storageDir
                );
                mCurrentPhotoPath = image.getAbsolutePath();

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    Uri photoURI = FileProvider.getUriForFile(getApplicationContext(),
                            "de.digisocken.caputhown.fileprovider",
                            image
                    );
                    // todo: use this high res picture
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                }
            } else {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
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
            entryAdapter.sort();
            if (ab != null) ab.setTitle(R.string.processing);
            new ToMovieTask().execute(false, false);
            return true;
        } else if (id == R.id.action_amovie) {
            entryAdapter.asort();
            if (ab != null) ab.setTitle(R.string.processing);
            new ToMovieTask().execute(false, false);
            return true;
        } else if (id == R.id.action_slow) {
            entryAdapter.sort();
            if (ab != null) ab.setTitle(R.string.processing);
            new ToMovieTask().execute(false, true);
            return true;
        } else if (id == R.id.action_rapmovie) {
            entryAdapter.sort();
            if (ab != null) ab.setTitle(R.string.processing);
            new ToMovieTask().execute(true, false);
            return true;
        } else if (id == R.id.action_picture) {
            takePic();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ToMovieTask extends AsyncTask<Boolean, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Boolean... bools) {
            FileChannelWrapper out = null;
            File file = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                file = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                        getPackageName()
                );
            } else {
                file = new File(Environment.getExternalStorageDirectory() + "/Documents/"+getPackageName());
            }

            String nameBasic = file.getPath() + "/" + (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
            String path = nameBasic + ".mp4";
            try {
                if (!file.exists()) file.mkdirs();
                file = new File(path);
                if (!file.exists()) file.createNewFile();
            } catch (Exception e) {
                return false;
            }

            try {
                out = NIOUtils.writableFileChannel(file.getAbsolutePath());
                AndroidSequenceEncoder encoder = null;
                if (bools[1]) {
                    encoder = new AndroidSequenceEncoder(out, Rational.R(8, 1));
                } else {
                    encoder = new AndroidSequenceEncoder(out, Rational.R(13, 1));
                }
                if (bools[0]) entryAdapter.sort();

                for (PicEntry pe : entryAdapter.picEntries) {
                    encoder.encodeImage(pe.pic);
                }

                if (bools[0]) {
                    entryAdapter.asort();
                    for (PicEntry pe : entryAdapter.picEntries) {
                        encoder.encodeImage(pe.pic);
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
                    String[] cmdgif = {"-i", path, nameBasic + ".gif"};
                    conversion(cmdgif, false);
                    // ------------------------------------------------- makes whatsapp compatible file
                    String[] cmd_wa = {
                            "-i", path,
                            "-c:v", "libx264",
                            "-profile:v", "baseline",
                            "-level", "3.0",
                            "-pix_fmt", "yuv420p",
                            nameBasic + "whatsapp.mp4"
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
                entryAdapter.clear();
                data_line = 0;
                entryAdapter.notifyDataSetChanged();
            } else {
                emptyView.setText(R.string.fail);
                entryAdapter.clear();
                data_line = 0;
                entryAdapter.notifyDataSetChanged();
            }
            super.onPostExecute(aBoolean);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            PicEntry pe = new PicEntry();
            pe.title = String.format("%03d", data_line+1);

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            pe.pic = Bitmap.createScaledBitmap(
                    photo,
                    2*photo.getWidth(),
                    2*photo.getHeight(),
                    true
            );
            entryAdapter.addItem(pe);
            data_line++;
            entryAdapter.asort();
            entryAdapter.notifyDataSetChanged();

            Toast.makeText(this, R.string.picadd, Toast.LENGTH_SHORT).show();
        }
    }

    private void conversion(final String[] cmd, final boolean delFinal) {
        ;

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
                    entryAdapter.clear();
                    data_line = 0;
                    entryAdapter.notifyDataSetChanged();
                }

                @Override
                public void onSuccess(String message) {
                    if (delFinal) {
                        String path = cmd[1];
                        File f = new File(path);
                        try {
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
}
