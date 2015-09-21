package com.mikehughes.testfeatures;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.w3c.dom.Text;


public class DisplayMessageActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // enable the app icon as the Up button by calling
        // it actually does this anyway, without this line of code
        // at least for API >= 16
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // receive the Intent
        Intent intent = getIntent();
        // get the message from the Main UI Activity
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // dynamically create a new TextView for "this" activity
        TextView textView = new TextView(this);

        textView.setTextSize(40);
        textView.setText("Received \"" + message + "\" from Main UI Activity.");

        // add the textView as the root view of this activities layout
        setContentView(textView);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
