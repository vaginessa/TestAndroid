package mikehughes.recordaccelerometer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class MainActivity extends Activity implements SensorEventListener,
        View.OnClickListener
{
    private ToggleButton  toggleOnOff;
    private Button        buttonDisplay;
    private TextView      statusView;
    private TextView      messageView;

    private SensorManager                sensorManager;
    private boolean                      startedYet = false;
    private ArrayList<AccelerometerData> sensorData;

    String fileName = "accel";

    File             curFile;
    BufferedWriter   writer;

    private LinearLayout layout;
    private View         chart;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // the sensor components
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorData    = new ArrayList();

        // connect the UI components
        buttonDisplay = (Button)findViewById(R.id.buttonDisplay);
        toggleOnOff   = (ToggleButton)findViewById(R.id.toggleButton);
        statusView    = (TextView)findViewById(R.id.textView);
        messageView   = (TextView)findViewById(R.id.textMessage);

        layout = (LinearLayout) findViewById(R.id.chartLayout);

        statusView.setText("Starting...");

        if (sensorData == null || sensorData.size() == 0)
        {
            buttonDisplay.setEnabled(false);
        }

        // set the onClickListeners
        buttonDisplay.setOnClickListener(this);

        toggleOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isCheckedOn)
            {
                if (isCheckedOn)
                {
                    long timeStamp     = System.currentTimeMillis();
                    String curFileName = fileName + "_" + timeStamp + ".txt";
                    curFile = new File(getExternalFilesDir(null), curFileName);

                    buttonDisplay.setEnabled(true);

                    try
                    {
                        if ( !curFile.exists() )
                        {
                            curFile.createNewFile();
                        }

                        writer = new BufferedWriter(new FileWriter(curFile,true));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        statusView.setText("Failed to open a fileStream...");
                    }

                    messageView.setText("Is that all you got?");

                    // The toggle is enabled
                    startedYet = true;
                    sensorData = new ArrayList();
                    Sensor accelMeter = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    sensorManager.registerListener(MainActivity.this, accelMeter, SensorManager.SENSOR_DELAY_GAME);

                    statusView.setText("Collecting Data...");
                }
                else
                {
                    closeFile();
                    statusView.setText("Stopped collecting Data - Closed File.");
                }
            }
        });

    }

    private void closeFile()
    {
        try
        {
            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            statusView.setText("Failed to close a fileStream...");
        }

        MediaScannerConnection.scanFile(MainActivity.this,
                new String[]{curFile.toString()},
                null,
                null);

        sensorManager.unregisterListener(MainActivity.this);

        buttonDisplay.setEnabled(false);
        toggleOnOff.setChecked(false);

        messageView.setText("Work harder next time!");
        // The toggle is disabled
        startedYet = false;
        sensorData.clear();// clear out the data for next time
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (startedYet)
        {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (startedYet)
        {
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];

            long timeStamp         = System.currentTimeMillis();
            AccelerometerData data = new AccelerometerData(timeStamp, x, y, z);

            sensorData.add(data);

            String curDataWrite = data.toString();
            try
            {
                writer.write( curDataWrite );
            }
            catch (Exception e)
            {
                e.printStackTrace();
                statusView.setText("Failed to write to fileStream...");
            }

            DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00000" );
            double x2dec = new Double(df2.format(x)).doubleValue();
            double y2dec = new Double(df2.format(y)).doubleValue();
            double z2dec = new Double(df2.format(z)).doubleValue();

            statusView.setText("x=" + x2dec + "     y=" + y2dec + "     z=" + z2dec);
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.buttonDisplay)
        {
            startedYet = false;
            layout.removeAllViews();

            openChart();
            closeFile();
            statusView.setText("Stopped collecting Data and Displaying - Closed File.");
        }
    }

    private void openChart()
    {
        if (sensorData != null || sensorData.size() > 0)
        {
            long t = sensorData.get(0).getTimestamp();
            XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();

            XYSeries xSeries = new XYSeries("X");
            XYSeries ySeries = new XYSeries("Y");
            XYSeries zSeries = new XYSeries("Z");

            for (int i=0; i<sensorData.size(); ++i)
            {
                AccelerometerData data = sensorData.get(i);

                xSeries.add(data.getTimestamp() - t, data.getX());
                ySeries.add(data.getTimestamp() - t, data.getY());
                zSeries.add(data.getTimestamp() - t, data.getZ());
            }

            dataSet.addSeries(xSeries);
            dataSet.addSeries(ySeries);
            dataSet.addSeries(zSeries);

            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);

            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            yRenderer.setLineWidth(1);
            yRenderer.setDisplayChartValues(false);

            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.BLUE);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setDisplayChartValues(false);

            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)");
            multiRenderer.setXTitle("Sensor Data");
            multiRenderer.setYTitle("Values of Acceleration");
            multiRenderer.setZoomButtonsVisible(true);
            for (int i = 0; i < sensorData.size(); ++i)
            {
                multiRenderer.addXTextLabel(i + 1, ""
                        + (sensorData.get(i).getTimestamp() - t));
            }
            for (int i = 0; i < 12; ++i)
            {
                multiRenderer.addYTextLabel(i + 1, ""+i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);


            // Creating a Line Chart
            chart = ChartFactory.getLineChartView(getBaseContext(), dataSet,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            layout.addView(chart);

        }
        else
        {
            statusView.setText("Error: Trying to display before data exists.");
        }

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
