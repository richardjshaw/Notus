package com.example.notus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Set;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Environment;

import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import com.example.notus.R;

import zephyr.android.HxMBT.*;

/*************************************************************************/

public class Notus extends Activity {
    BluetoothAdapter adapter = null;
    BTClient _bt;
    ZephyrProtocol _protocol;
    NewConnectedListener _NConnListener;
    private final int HEART_RATE = 0x100;
    private final int INSTANT_SPEED = 0x101;
    private final int BATTERY_CHARGE = 0x102;
    private final int HEART_TIMES = 0x0103;

    Boolean amLogging = false;
    FileOutputStream myOutputStream;
    int myLogLineInd = 0;

    

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

	/* Sending a message to android that we are going to initiate 
	   a pairing request*/
	IntentFilter filter
	    = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
	/*Registering a new BTBroadcast receiver from the Main Activity 
	  context with pairing request event*/
	this.getApplicationContext().
	    registerReceiver(new BTBroadcastReceiver(), filter);

	/* Registering the BTBondReceiver in the application that the status
	   of the receiver has changed to Paired */
        IntentFilter filter2
	    = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
	this.getApplicationContext().registerReceiver(new BTBondReceiver(),
						      filter2);
        
	//Obtaining the handle to act on the CONNECT button
        final TextView statusTv = (TextView) findViewById(R.id.labelStatusMsg);
	String statusText  = "Not Connected to HxM ! !";
	statusTv.setText(statusText);

	Button btnConnect = (Button) findViewById(R.id.ButtonConnect);

        if (btnConnect != null) {
	    btnConnect.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
			String BhMacID = "00:07:80:9D:8A:E8";
			//String BhMacID = "00:07:80:88:F6:BF";
			adapter = BluetoothAdapter.getDefaultAdapter();
        			
			Set<BluetoothDevice> pairedDevices
			    = adapter.getBondedDevices();
        			
			if (pairedDevices.size() > 0) {
			    for (BluetoothDevice device : pairedDevices) {
                        	if (device.getName().startsWith("HXM")) {
				    BluetoothDevice btDevice = device;
				    BhMacID = btDevice.getAddress();
				    break;
                        	}
			    }
			}
        			
			//BhMacID = btDevice.getAddress();
			BluetoothDevice Device
			    = adapter.getRemoteDevice(BhMacID);
			String DeviceName = Device.getName();
			_bt = new BTClient(adapter, BhMacID);
			_NConnListener
			    = new NewConnectedListener(Newhandler,Newhandler);
			_bt.addConnectedEventListener(_NConnListener);
        			
			TextView tv1 = (EditText)findViewById(R.id.labelHeartRate);
			tv1.setText("000");
        			
			tv1 = (EditText)findViewById(R.id.labelInstantSpeed);
			tv1.setText("0.0");
        			 
			//tv1 = (EditText)findViewById(R.id.labelSkinTemp);
			//tv1.setText("0.0");
        			 
			//tv1 = (EditText)findViewById(R.id.labelPosture);
			//tv1.setText("000");
        			 
			//tv1 = (EditText)findViewById(R.id.labelPeakAcc);
			//tv1.setText("0.0");

			if(_bt.IsConnected()) {
			    _bt.start();
			    String statusText 
				= "Connected to HxM " + DeviceName;
			    statusTv.setText(statusText);
						 
			    //Reset all the values to 0s
			} else {
			    String statusText = "Unable to Connect !";
			    statusTv.setText(statusText);
			}
		    }
        	});
        }

        /*Obtaining the handle to act on the DISCONNECT button*/
        Button btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);

        if (btnDisconnect != null) {
	    btnDisconnect.setOnClickListener(new OnClickListener() {
		    @Override
		    /*Functionality to do if the button DISCONNECT is touched*/
			public void onClick(View v) {
			// TODO Auto-generated method stub
			/*Reset the global variables*/
			String statusText  = "Disconnected from HxM!";
			statusTv.setText(statusText);

			/*This disconnects listener from acting on received 
			  messages*/	
			_bt.removeConnectedEventListener(_NConnListener);
			/*Close the communication with the device 
			  & throw an exception if failure*/
			_bt.Close();	
		    }
        	});
        }

	/*Obtaining the handle to act on the LOG button*/
	final Button btnLog = (Button) findViewById(R.id.ButtonLog);

	if (btnLog != null) {
	    btnLog.setOnClickListener(new OnClickListener() {
		    @Override
		    /*Functionality to do if the button LOG is touched*/
			public void onClick(View v) {
			if (amLogging) {
			    amLogging = false;

			    try {
				myOutputStream.flush();
				myOutputStream.close();
			    } catch (Exception e) {
				e.printStackTrace();
			    }

			    String statusText  = "Stopped logging";
			    statusTv.setText(statusText);

			    btnLog.setText("Start Log");
			} else {
			    SimpleDateFormat s 
				= new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
			    String timeStamp = s.format(new Date());

			    String logFilename
				= "notus_" + timeStamp + ".log";
			    String newline = "\n";

			    try {
				File sdCard
				   = Environment.getExternalStorageDirectory();
				File dir = new File(sdCard.getAbsolutePath()
						    + "/notus");
				dir.mkdirs();
				File file = new File(dir, logFilename);
				myOutputStream = new FileOutputStream(file);

				String logHdr = "Notus Log";
				myOutputStream.write(logHdr.getBytes());
				myOutputStream.write(newline.getBytes());

				amLogging = true;
				String statusText  = "Started logging";
				statusTv.setText(statusText);

				btnLog.setText("Stop Log");
			    } catch (Exception e) {
				String statusText  = "Failed to open log!";
				statusTv.setText(statusText);
			    }
			}
		    }
		});
	}
    }
    
    private class BTBondReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
	    Bundle b = intent.getExtras();
	    BluetoothDevice device 
		= adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
	    Log.d("Bond state", "BOND_STATED = " + device.getBondState());
	}
    }

    private class BTBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
	    Log.d("BTIntent", intent.getAction());
	    Bundle b = intent.getExtras();
	    Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
	    Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());

	    try {
		BluetoothDevice device
		    = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
		Method m = BluetoothDevice.class.getMethod("convertPinToBytes",
							   new Class[] {
							       String.class} );
		byte[] pin = (byte[])m.invoke(device, "1234");
		m = device.getClass().getMethod("setPin",
						new Class [] {pin.getClass()});
		Object result = m.invoke(device, pin);
		Log.d("BTTest", result.toString());
	    } catch (SecurityException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    } catch (NoSuchMethodException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    } catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (InvocationTargetException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    final  Handler Newhandler = new Handler() {
	    public void handleMessage(Message msg) {
		TextView tv = null;

		switch (msg.what) {
		case HEART_RATE:
		    String HeartRateStr
			= msg.getData().getString("HeartRate");
		    int HeartRate = Integer.parseInt(HeartRateStr);

		    if (HeartRate < 0) {
			HeartRate += 128;
		    }

		    HeartRateStr = Integer.toString(HeartRate);

		    tv = (EditText)findViewById(R.id.labelHeartRate);
		    //System.out.println("Heart Rate Info is "+ HeartRateStr);

		    if (tv != null) {
			tv.setText(HeartRateStr);
		    }

		    if (amLogging) {
			String newline = "\n";

			if (myLogLineInd == 0) {
			    SimpleDateFormat s 
				= new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss");
			    String timeStamp = s.format(new Date());

			    try {
				myOutputStream.write(timeStamp.getBytes());
				myOutputStream.write(newline.getBytes());
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			}

			if (++myLogLineInd == 60) {
			    myLogLineInd = 0;
			}

			try {
			    myOutputStream.write(HeartRateStr.getBytes());
			    myOutputStream.write(newline.getBytes());
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }

		    break;
    		
		case INSTANT_SPEED:
		    String InstantSpeedtext
			= msg.getData().getString("InstantSpeed");
		    tv = (EditText)findViewById(R.id.labelInstantSpeed);

		    if (tv != null) {
			tv.setText(InstantSpeedtext);
		    }

		    break;

		case BATTERY_CHARGE:
		    String BatteryChargeText
			= msg.getData().getString("BatteryCharge");
		    tv = (EditText)findViewById(R.id.labelBatteryCharge);
		    
		    if (tv != null) {
			tv.setText(BatteryChargeText);
		    }

		    break;

		case HEART_TIMES:
		    String HeartTimesText
			= msg.getData().getString("HeartTimes");
		    String[] timeStrs = HeartTimesText.split(",");

		    for (int beatInd=0; beatInd <3; ++beatInd) {
			switch (beatInd) {
			case 0:
			    tv = (EditText)findViewById(R.id.HeartTime1);
			    break;
			case 1:
			    tv = (EditText)findViewById(R.id.HeartTime2);
			    break;
			case 2:
			    tv = (EditText)findViewById(R.id.HeartTime3);
			    break;
			default:
			    break;
			}

			if (tv != null) {
			    tv.setText(timeStrs[beatInd]);
			}
		    }

		    break;
		}
	    }
	};
	
}

/*************************************************************************/
