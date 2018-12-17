package de.digisocken.caputhown;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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
import java.util.Locale;

public class ScrollingActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private ArrayList<Bitmap> bmps;
    private FloatingActionButton fab;

    // todo
    private boolean highRes = false;

    String mCurrentPhotoPath;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmps = new ArrayList<>();
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = (TextView) findViewById(R.id.largeText);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePic();
            }
        });
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
            textView.setText("processing...");
            new ToMovieTask().execute();
            return true;
        } else if (id == R.id.action_picture) {
            takePic();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ToMovieTask extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
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

            String path = file.getPath() +"/"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".mp4";
            try {
                if (!file.exists()) file.mkdirs();
                file = new File(path);
                if (!file.exists()) file.createNewFile();
            } catch (Exception e) {
                return false;
            }

            try {
                out = NIOUtils.writableFileChannel(file.getAbsolutePath());
                AndroidSequenceEncoder encoder = new AndroidSequenceEncoder(out, Rational.R(15, 1));
                for (Bitmap bitmap : bmps) {
                    encoder.encodeImage(bitmap);
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
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                textView.setText("Ok");
                bmps.clear();
            } else {
                textView.setText("fail");
            }
            super.onPostExecute(aBoolean);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Bitmap mutableBitmap = Bitmap.createScaledBitmap(
                    photo,
                    2*photo.getWidth(),
                    2*photo.getHeight(),
                    true
            );
            bmps.add(mutableBitmap);

            CharSequence sp = "  " + Integer.toString(bmps.size()) + "\n";
            SpannableStringBuilder ssb = new SpannableStringBuilder(sp);
            ssb.append(textView.getText());
            Drawable dr = new BitmapDrawable(getResources(), mutableBitmap);
            dr.setBounds(
                    0,
                    0,
                    mutableBitmap.getWidth()/2,
                    mutableBitmap.getHeight()/2
            );

            ImageSpan isp = new ImageSpan(dr);
            ssb.setSpan(
                    isp,
                    0,
                    1,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
            );
            textView.setText(ssb);

            Snackbar.make(fab, "picture added", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }
}
