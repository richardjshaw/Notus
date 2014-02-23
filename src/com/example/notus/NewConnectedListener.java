package com.example.notus;
import android.app.Activity;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import zephyr.android.HxMBT.*;

public class NewConnectedListener extends ConnectListenerImpl
{
    private Handler _OldHandler;
    private Handler _aNewHandler; 
    private int GP_MSG_ID = 0x20;
    private int GP_HANDLER_ID = 0x20;
    private int HR_SPD_DIST_PACKET =0x26;
	
    private final int HEART_RATE = 0x100;
    private final int INSTANT_SPEED = 0x101;
    private final int BATTERY_CHARGE = 0x102;
    private final int HEART_TIME1 = 0x0103;
    private final int HEART_TIME2 = 0x0104;
    private final int HEART_TIME3 = 0x0105;
    private HRSpeedDistPacketInfo HRSpeedDistPacket
	= new HRSpeedDistPacketInfo();

    public NewConnectedListener(Handler handler, Handler _NewHandler) {
	super(handler, null);
	_OldHandler= handler;
	_aNewHandler = _NewHandler;

	// TODO Auto-generated constructor stub
    }

    public void Connected(ConnectedEvent<BTClient> eventArgs) {
	System.out.println(String.format("Connected to BioHarness %s.",
					 eventArgs.getSource().getDevice().getName()));

	//Creates a new ZephyrProtocol object and passes it the BTComms object
	ZephyrProtocol _protocol
	    = new ZephyrProtocol(eventArgs.getSource().getComms());

	_protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
		public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
		    ZephyrPacketArgs msg = eventArgs.getPacket();
		    byte CRCFailStatus;
		    byte RcvdBytes;
				
		    CRCFailStatus = msg.getCRCStatus();
		    RcvdBytes = msg.getNumRvcdBytes() ;

		    if (HR_SPD_DIST_PACKET == msg.getMsgID()) {
			byte [] DataArray = msg.getBytes();

			int HRate =  HRSpeedDistPacket.GetHeartRate(DataArray);
			Message text1 = _aNewHandler.obtainMessage(HEART_RATE);
			Bundle b1 = new Bundle();
			b1.putString("HeartRate", String.valueOf(HRate));
			text1.setData(b1);
			_aNewHandler.sendMessage(text1);
			System.out.println("Heart Rate is "+ HRate);

			double InstantSpeed
			    = HRSpeedDistPacket.GetInstantSpeed(DataArray);
			text1 = _aNewHandler.obtainMessage(INSTANT_SPEED);
			b1.putString("InstantSpeed",
				     String.valueOf(InstantSpeed));
			text1.setData(b1);
			_aNewHandler.sendMessage(text1);
			System.out.println("Instant Speed is "+ InstantSpeed);

			int BatteryCharge
			    = HRSpeedDistPacket.GetBatteryChargeInd(DataArray);
			text1 = _aNewHandler.obtainMessage(BATTERY_CHARGE);
			b1.putString("BatteryCharge",
				     String.valueOf(BatteryCharge));
			text1.setData(b1);
			_aNewHandler.sendMessage(text1);

			// FIXME : duplication

			int[] HeartTimeArr
			    = HRSpeedDistPacket.GetHeartBeatTS(DataArray);

			int HeartTime1 = HeartTimeArr[0];
			text1 = _aNewHandler.obtainMessage(HEART_TIME1);
			b1.putString("HeartTime1",
				     String.valueOf(HeartTime1));
			text1.setData(b1);
			_aNewHandler.sendMessage(text1);

			int HeartTime2 = HeartTimeArr[1];
			text1 = _aNewHandler.obtainMessage(HEART_TIME2);
			b1.putString("HeartTime2",
				     String.valueOf(HeartTime2));
			text1.setData(b1);
			_aNewHandler.sendMessage(text1);

			int HeartTime3 = HeartTimeArr[2];
			text1 = _aNewHandler.obtainMessage(HEART_TIME3);
			b1.putString("HeartTime3",
				     String.valueOf(HeartTime3));
			text1.setData(b1);
			_aNewHandler.sendMessage(text1);
		    }
		}
	    });
    }
}

/*************************************************************************/
