package com.mikehughes.testfeatures;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mike on 7/21/2015.
 */
public class FileManagement
{
    FileOutputStream mFileOutStream = null;

    File mFileOut = null;

    public void createFile(Context context, String fileName)
    {
        try
        {
            // create the file
            mFileOut = new File(context.getFilesDir(), fileName);
            if ( !mFileOut.exists() )
            {
                mFileOut.createNewFile();
            }

            mFileOutStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
            Toast.makeText(context, "Error opening file stream", Toast.LENGTH_SHORT).show();
        }

    }


}
