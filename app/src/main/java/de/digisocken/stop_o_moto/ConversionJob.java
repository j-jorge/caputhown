package de.digisocken.stop_o_moto;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

class ConversionJob extends AsyncTask<Void, Void, Void>
{
    private static String TAG = ConversionJob.class.getName();

    public interface Listener
    {
        void succeed(String basename);
        void failed();
    }

    private final AppFiles m_app_files;
    private final ArrayList<PicEntry> m_pictures;
    private final boolean m_rap;
    private final boolean m_slow;
    private final boolean m_build_main_mp4;
    private final boolean m_build_gif;
    private final boolean m_build_whatsapp_mp4;
    private final FFmpeg m_ffmpeg;
    private final Listener m_listener;
    private final AtomicInteger m_pending_tasks_count = new AtomicInteger(0);

    private File m_main_file;
    private String m_basename;

    public ConversionJob
        (AppFiles app_files, ArrayList<PicEntry> pictures, boolean rap,
         boolean slow, boolean main_mp4, boolean gif, boolean whatsapp,
         FFmpeg ffmpeg, Listener listener)
    {
        m_app_files = app_files;
        m_pictures = pictures;
        m_rap = rap;
        m_slow = slow;
        m_build_main_mp4 = main_mp4;
        m_build_gif = gif;
        m_build_whatsapp_mp4 = whatsapp;
        m_ffmpeg = ffmpeg;
        m_listener = listener;
    }

    @Override
    protected Void doInBackground(Void... ignored)
    {
        // Three tasks: main video, gif, whatsapp video.
        m_pending_tasks_count.addAndGet(3);

        FileChannelWrapper out = null;
        m_basename =
            (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());

        m_main_file = m_app_files.create_app_file(m_basename + ".mp4");

        try {
            out = NIOUtils.writableFileChannel(m_main_file.getAbsolutePath());
            AndroidSequenceEncoder encoder;
            if (m_slow) {
                encoder = new AndroidSequenceEncoder(out, Rational.R(8, 1));
            } else {
                encoder = new AndroidSequenceEncoder(out, Rational.R(13, 1));
            }

            for (PicEntry entry : m_pictures)
                encoder.encodeImage(entry.picture);

            if (m_rap && !m_pictures.isEmpty())
                for (int i = m_pictures.size() - 1; i != 0; --i)
                    encoder.encodeImage(m_pictures.get(i).picture);

            encoder.finish();
            oneEncodingCompleted();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            m_listener.failed();
        } catch (IOException e) {
            e.printStackTrace();
            m_listener.failed();
        } finally {
            NIOUtils.closeQuietly(out);

            if (m_ffmpeg == null) {
                oneEncodingCompleted(); // gif
                oneEncodingCompleted(); // WhatsApp
            } else {
                if (m_build_gif) {
                    String[] cmdgif = {"-i", m_main_file.getPath(), m_app_files.app_folder().getPath() + "/" + m_basename + ".gif"};
                    conversion(cmdgif, false);
                } else
                    oneEncodingCompleted();


                if (m_build_whatsapp_mp4) {
                    String[] cmd_wa = {
                        "-i", m_main_file.getPath(),
                        "-c:v", "libx264",
                        "-profile:v", "baseline",
                        "-level", "3.0",
                        "-pix_fmt", "yuv420p",
                        m_app_files.app_folder().getPath() + "/" + m_basename + "whatsapp.mp4"
                    };
                    conversion(cmd_wa, false);
                } else
                    oneEncodingCompleted();
            }
        }

        return null;
    }

    private void conversion(final String[] cmd, final boolean delFinal) {
        try {
            m_ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onFailure(String message) {
                    Log.e(TAG, message);
                    m_listener.failed();
                }

                @Override
                public void onFinish() {
                    oneEncodingCompleted();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
            e.printStackTrace();
        }
    }

    private void oneEncodingCompleted() {
        if (m_pending_tasks_count.decrementAndGet() != 0)
            return;

        if (!m_build_main_mp4)
            m_main_file.delete();

        m_listener.succeed(m_basename);
    }
}
