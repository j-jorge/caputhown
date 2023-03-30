package de.digisocken.stop_o_moto;

import android.os.Environment;

import java.io.File;

class AppFiles
{
    private final String m_package_name;

    public AppFiles(String package_name)
    {
        m_package_name = package_name;
    }

    public File app_folder()
    {
        File file = null;
        if (android.os.Build.VERSION.SDK_INT
            >= android.os.Build.VERSION_CODES.KITKAT)
            file = new File
                (Environment.getExternalStoragePublicDirectory
                 (Environment.DIRECTORY_DOCUMENTS),
                 m_package_name);
        else
            file = new File
                (Environment.getExternalStorageDirectory()
                 + "/Documents/" + m_package_name);

        try {
            if (!file.exists())
                file.mkdirs();
        } catch (Exception e) {
            return null;
        }

        return file;
    }

    public File create_app_file(String name)
    {
        try
        {
            String path = app_folder().getPath() + "/" + name;
            File file = new File(path);

            if (!file.exists())
                file.createNewFile();

            return file;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
