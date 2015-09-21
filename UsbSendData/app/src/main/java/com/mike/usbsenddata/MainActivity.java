package com.mike.usbsenddata;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends Activity
{
    // static --> implies this value is the same across all class instances
    // final  --> implies variable can only be initialized once
    private static final int targetVendorID  = 1027;
    private static final int targetProductID = 24577;

    private static final String ACTION_USB_PERMISSION = " com.mike.usbsenddata.USB_PERMISSION";

    PendingIntent mPermissionIntent;

    UsbDevice           myDevice         = null;
    UsbInterface        usbInterface     = null;
    UsbEndpoint         endpointIn       = null;
    UsbEndpoint         endpointOut      = null;
    UsbDeviceConnection usbDevConnection = null;

    TextView displayProgress;
    TextView deviceInfo;
    TextView textRx;
    EditText textEdit;

    Button btnCheckDevice;
    Button btnSendData;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCheckDevice  = (Button)findViewById(R.id.buttonCheck);
        btnSendData     = (Button)findViewById(R.id.buttonSend);
        deviceInfo      = (TextView)findViewById(R.id.textData);
        displayProgress = (TextView)findViewById(R.id.headline);
        textEdit        = (EditText)findViewById(R.id.textOut);
        textRx          = (TextView)findViewById(R.id.textRx);

        displayProgress.setText("Starting...");

        setUpIntentFilters();
        connectUsb();

        btnCheckDevice.setOnClickListener(buttonCheckOnClickListener);

        btnSendData.setOnClickListener(buttonSendOnClickListener);

    }

    ///////////////////////////////////////////////////////////////////////////
    //---------------------------- setUpIntentFilter ------------------------//
    // registers the broadcast receiver
    private void setUpIntentFilters()
    {
        String msg = "regPermReceiver()";

        displayProgress.setText(msg);

        int requestCode = 0;

        // this is used for requesting permission
        mPermissionIntent = PendingIntent.getBroadcast(this, requestCode, new Intent(ACTION_USB_PERMISSION), 0);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);

        // This registration handles the permission broadcast receiver
        registerReceiver( mPermissionBcastRx, filter );

        // register the device_attached and device_detached broadcast receivers
        registerReceiver( mAttachDetachBCastRx, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED) );
        registerReceiver( mAttachDetachBCastRx, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED) );

    }

    ///////////////////////////////////////////////////////////////////////////
    //--------------------- buttonCheckOnClickListener -----------------------//
    View.OnClickListener buttonCheckOnClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String curText = "";

            displayProgress.setText("checking with button");

            if (myDevice != null)
            {
                curText += "btnCheck - VID: " + myDevice.getVendorId() + " - " +
                           "PID: " + myDevice.getProductId();
            }
            else
            {
                curText = "device not found with button";
            }

            deviceInfo.setText(curText);
        }
    };

    ///////////////////////////////////////////////////////////////////////////
    //--------------------- buttonSendOnClickListener -----------------------//
    View.OnClickListener buttonSendOnClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            String curText = "";

            displayProgress.setText("checking with button");

            if (myDevice != null)
            {
                // get the text from the textOut EditText widgent
                String dataOut = textEdit.getText().toString();

                // convert the string to byte array for data transfer
                byte[] bytesOut = dataOut.getBytes();

                // set the timeout to zero
                int timeout = 2000;

                // do a bulk transfer to the USB slave
                int usbResult = usbDevConnection.bulkTransfer( endpointOut, bytesOut, bytesOut.length, timeout);

                curText = "Bulk Transfer: " + usbResult;

            }
            else
            {
                curText = "device is unavailable";
            }

            displayProgress.setText(curText);
        }
    };

    ///////////////////////////////////////////////////////////////////////////
    //----------------------------- connectUsb ------------------------------//
    private void connectUsb()
    {
        String msg = "connectUsb()";

        displayProgress.setText(msg);

        /*----- couldn't get this to work
        // USB connection details
        Intent intent = new Intent(ACTION_USB_PERMISSION);
        // getting a device from the attached device described in the intent filter
        myDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        */

        searchEndPoint();

        if ( usbInterface != null )
        {
            msg = "device found";
            displayProgress.setText(msg);
            setupUsbComm();
        }
        else
        {
            msg = "device not found";
            displayProgress.setText(msg);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //--------------------------- searchEndPoint ----------------------------//
    // searches through the USB deviceList to find the one with the target Vendor and Product ID
    private void searchEndPoint()
    {
        displayProgress.setText("searchEndPoint()");

        usbInterface = null;
        endpointOut  = null;
        endpointIn   = null;

        // Search device list for targetVendorID and targetProductID
        if (myDevice == null)
        {
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

            while (deviceIterator.hasNext())
            {
                UsbDevice device = deviceIterator.next();

                if ((device.getVendorId() == targetVendorID)
                        && (device.getProductId() == targetProductID))
                {
                    myDevice = device;
                }
            }
        }

        // check if deviceFound still equals null
        if (myDevice == null)
        {
            String msg = "error: device not found";
            displayProgress.setText(msg);
        }
        else
        {
            // display all of the USB devices info
            String s = "VID: " + myDevice.getVendorId() + " - " +
                       "PID: " + myDevice.getProductId();
            deviceInfo.setText(s);

            // Search for UsbInterface with a USB_ENDPOINT_XFER_BULK
            // and direction of USB_DIR_OUT and USB_DIR_IN
            for (int i = 0; i < myDevice.getInterfaceCount(); ++i)
            {
                UsbInterface usbIf = myDevice.getInterface(i);

                UsbEndpoint tOut = null;
                UsbEndpoint tIn  = null;

                int tEndpointCnt = usbIf.getEndpointCount();

                if (tEndpointCnt >= 2)
                {
                    for (int j = 0; j < tEndpointCnt; ++j)
                    {
                        if ( usbIf.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK )
                        {
                            if ( usbIf.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT )
                            {
                                tOut = usbIf.getEndpoint(j);
                            }
                            else if ( usbIf.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN )
                            {
                                tIn = usbIf.getEndpoint(j);
                            }
                        }
                    }
                    if ( (tOut != null) && (tIn != null) )
                    {
                        // This interface has both USB_DIR_OUT
                        // and USB_DIR_IN of USB_ENDPOINT_XFER_BULK
                        usbInterface = usbIf;
                        endpointOut  = tOut;
                        endpointIn   = tIn;
                    }
                }
            }   // END FOR LOOP

            if (usbInterface == null)
            {
                displayProgress.setText("Not suitable USB_IF found!");
            }
            else
            {
                displayProgress.setText("USB_IF found!");
            }
        }   // END ELSE
    }

    ///////////////////////////////////////////////////////////////////////////
    //---------------------------- setupUsbComm -----------------------------//
    private boolean setupUsbComm()
    {
        final int requestType = 0x40;
        final int BAUD_RATE   = 9600;

        int hexBaud = getHexBaudRate(BAUD_RATE );

        boolean wasSuccess = false;

        UsbManager manager  = (UsbManager)getSystemService(Context.USB_SERVICE);

        Boolean permitToRead = manager.hasPermission(myDevice);

        if (permitToRead)
        {
            usbDevConnection = manager.openDevice(myDevice);

            if ( usbDevConnection != null )
            {
                boolean forceClaim = true;

                usbDevConnection.claimInterface(usbInterface, forceClaim );


                //controlTransfer inputs(requestType,requestID,value,index,buffer,length,timeout)
                // Purpose: reset,
                usbDevConnection.controlTransfer( requestType, 0, 0, 0, null, 0, 0 );
                // Purpose: clear Rx
                usbDevConnection.controlTransfer( requestType, 0, 1, 0, null, 0, 0 );
                // Purpose: clear Tx
                usbDevConnection.controlTransfer( requestType, 0, 2, 0, null, 0, 0 );
                // Purpose: Flow Control = None
                usbDevConnection.controlTransfer( requestType, 0x02, 2, 0, null, 0, 0 );
                // Purpose: Set BaudRate
                usbDevConnection.controlTransfer( requestType, 0x03, hexBaud, 0, null, 0, 0 );
                // Purpose: Set data-bits, parity, stop-bit
                // 0x0008 = 8 data-bits, no parity, 1 stop-bit
                usbDevConnection.controlTransfer( requestType, 0x04, 0x0008,  0, null, 0, 0 );

                wasSuccess = true;
            }
        }
        else
        {
            manager.requestPermission(myDevice, mPermissionIntent);

            displayProgress.setText("Permission: " + permitToRead);
        }

        return wasSuccess;
    }

    ///////////////////////////////////////////////////////////////////////////
    //--------------------------- getLineEncoding -----------------------------//
    private int getHexBaudRate(int baudRate)
    {
        // default to 9600
        int hexBaudRate = 0x4138;

        switch (baudRate)
        {
            case 19200:
                hexBaudRate = 0x809C;
                break;
            case 38400:
                hexBaudRate = 0xC04E;
                break;
            case 57600:
                hexBaudRate = 0x0034;
                break;
            case 115200:
                hexBaudRate = 0x001A;
                break;
            case 230400:
                hexBaudRate = 0x000D;
                break;
            case 460800:
                hexBaudRate = 0x4006;
                break;
            case 921600:
                hexBaudRate = 0x8003;
                break;
            default:
                hexBaudRate = 0x4138;
                break;
        }


        return hexBaudRate;
    }


    ///////////////////////////////////////////////////////////////////////////
    //--------------------------- mPermissionBcastRx ------------------------------//
    private final BroadcastReceiver mPermissionBcastRx = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if ( ACTION_USB_PERMISSION.equals(action) )
            {
                String msg = "ACTION_USB_PERMISSION";
                displayProgress.setText( msg );

                synchronized (this)
                {
                    UsbDevice device = (UsbDevice)
                            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    boolean isPermGranted
                            = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);

                    if ( isPermGranted )
                    {
                        if (device != null)
                        {
                            // call method to set up device communication
                            connectUsb();
                        }
                    }
                    else
                    {
                        msg = "permission denied for device " + device;
                        Log.d("ERROR: USB", msg);
                        displayProgress.setText( msg );
                    }
                }
            }
        }
    };


    private final BroadcastReceiver mAttachDetachBCastRx =  new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if ( UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action) )
            {

                myDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                displayProgress.setText("ACTION_USB_DEVICE_ATTACHED: \n" + myDevice.toString());

                connectUsb();

            }
            else if ( UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action) )
            {

                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                displayProgress.setText("ACTION_USB_DEVICE_DETACHED: \n" + device.toString());

                // make sure the device is myDevice, otherwise we don't care if it is detached
                if( device!=null )
                {
                    if( device == myDevice )
                    {
                        releaseUsb();
                    }
                }

                displayProgress.setText("");
            }
        }

    };


    ///////////////////////////////////////////////////////////////////////////
    //----------------------------- releaseUsb ------------------------------//
    private void releaseUsb()
    {
        String msg = "releaseUsb()";

        displayProgress.setText(msg);

        // only release if the device connection and interface exist.
        if ( usbDevConnection != null )
        {
            if ( usbInterface != null )
            {
                usbDevConnection.releaseInterface(usbInterface);
                usbInterface = null;
            }
            usbDevConnection.close();
            usbDevConnection = null;
        }

        myDevice          = null;
        usbInterface      = null;
        endpointIn        = null;
        endpointOut       = null;
    }

    @Override
    protected void onDestroy()
    {
        releaseUsb();
        unregisterReceiver(mPermissionBcastRx);
        unregisterReceiver(mAttachDetachBCastRx);
        super.onDestroy();
    }

}
