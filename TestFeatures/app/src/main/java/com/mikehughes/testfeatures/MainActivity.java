package com.mikehughes.testfeatures;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity

{
    public final static String EXTRA_MESSAGE   = "com.mikehughes.testfeatures.message";
    public final static String SHARED_PREF_KEY = "com.mikehughes.testfeatures.PREF_FILE_KEY";


    private String mMessage;

    double mLatitude;
    double mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            ActionBar actionBar = getActionBar();
            actionBar.setHomeButtonEnabled(false);
        }

        //gpsStuff();
    }

    public void sharedPrefRead(View view)
    {
        SharedPreferences sharedPref =
                this.getPreferences(Context.MODE_PRIVATE);

        String strDefault = "-default-";

        String readMessage = sharedPref.getString(SHARED_PREF_KEY, strDefault);

        Toast.makeText(this, "Read \"" + readMessage + "\" from shared pref.", Toast.LENGTH_SHORT).show();

    }

    public void sharedPrefWrite()
    {
        SharedPreferences sharedPref =
                this.getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        Toast.makeText(this, "sharedPrefWrite()", Toast.LENGTH_SHORT).show();


        editor.remove(SHARED_PREF_KEY);
        editor.commit();

        editor.putString(SHARED_PREF_KEY, mMessage);
        editor.commit();
    }

    /*
    public void gpsStuff()
    {
        LocationManager locationManager =
                (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled)
        {
            mToast.makeText(this, "GPS disabled...", Toast.LENGTH_SHORT);
            mToast.show();
        }
        else
        {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null)
            {
                mLatitude  = location.getLatitude();
                mLongitude = location.getLongitude();

                mToast.makeText(this, "Longitude = " + mLongitude, Toast.LENGTH_SHORT);
                mToast.show();
                mToast.makeText(this, "Latitude = " + mLatitude, Toast.LENGTH_SHORT);
                mToast.show();
            }
        }

    }
    */

    // Called when UI button is click with the android:onClick
    public void sendMessage(View view)
    {
        // create an intent to start a new activity
        // activity = DisplayMessageActivity
        // this is the Context
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        // get the text from the EditText widget
        EditText editText = (EditText)findViewById(R.id.edit_message);
        // place the text into a string
        mMessage = editText.getText().toString();
        // write this to the shared preferences
        sharedPrefWrite();
        // add the message to the intent to provide to the new activity
        // the putExtra provides a key/value pair to the intent
        intent.putExtra(EXTRA_MESSAGE, mMessage);
        // finally, start the activity specified by the intent
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        boolean returnVal = false;

        switch(id)
        {
            case R.id.action_search:
               // openSearch();
                returnVal = true;
                break;
            case R.id.action_settings:
               // openSettings();
                returnVal = true;
                break;
            default:
                returnVal = super.onOptionsItemSelected(item);
                break;
        }

        return returnVal;
    }

    public void onPause()
    {
        super.onPause();
    }

    public void onStop()
    {
        super.onStop();


    }

    public void onRestart()
    {
        super.onRestart();
    }


    public void onStart()
    {
        super.onStart();
    }

    public void onResume()
    {
        super.onResume();
    }

    public void onDestroy()
    {
        super.onDestroy();  // Always call the superclass

        // Stop method tracing that the activity started during onCreate()
        //android.os.Debug.stopMethodTracing();
    }
}
