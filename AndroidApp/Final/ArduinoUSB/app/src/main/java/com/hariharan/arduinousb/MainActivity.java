package com.hariharan.arduinousb;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
    Button startButton, sendButton, clearButton, stopButton;
    TextView textView;
    EditText editText;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    private StringBuilder sb = new StringBuilder();

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            //byte[] buffer = new byte[256];
            try {
                data = new String(arg0, "UTF-8");
              //buffer=(byte[]) arg0;
              //String strIncom = new String(buffer, 0);
                sb.append(data);
               int endOfLineIndex = sb.indexOf("/CS");
                int startCS = sb.indexOf("CS");


                //String packets[] =data.split("ENDPACKET");

               // tvAppend(textView, "Length:" + packets.length+"\n");

                if (endOfLineIndex > 0) {


                    String sbprint = sb.substring((startCS + 2), endOfLineIndex);
//                    String sbprint = sb.substring((startCS + 2), endOfLineIndex);
                    Date date=new Date();
                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    DateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                    String formatted = format.format(date);
                    tvAppend(textView,"Time:" + formatted+"\n");
  //                  tvAppend(textView,"String:" + sbprint+"\n");
//                    String formatted2 = format2.format(date);
//                    sample.setTime_stamp(formatted2);
                    String segments[] =sbprint.split("/");
                    //tvAppend(textView, "PAKet:" +sbprint+"\n");
                    int decimal=Integer.parseInt(segments[2]);
                    sample.setTemperature((float)decimal);
                    tvAppend(textView, "Temperature:" + sample.getTemperature()+"\n");
                    decimal=Integer.parseInt(segments[1]);
                    sample.setDielectric((float)decimal);
                    tvAppend(textView, "Dielectric:" + sample.getDielectric()+"\n");

                    tvAppend(textView, "SINK_ID:" + Integer.parseInt(segments[3])+"\n");


                    tvAppend(textView, "PACKET_TYPE:" + Integer.parseInt(segments[4])+"\n");

                    tvAppend(textView, "PACKET_LENGTH:" + Integer.parseInt(segments[5])+"\n");
                    tvAppend(textView, "SOURCE_ID:" + Integer.parseInt(segments[6])+"\n");
                    tvAppend(textView, "PACKET_SEND:" + Integer.parseInt(segments[7])+"\n");
                    tvAppend(textView, "SENDING_RETRIES:" + Integer.parseInt(segments[8])+"\n");
                    tvAppend(textView, "LOST_PACKET:" + Integer.parseInt(segments[9])+"\n");
                    tvAppend(textView, "RSS:" + Integer.parseInt(segments[10])+"\n");
                    tvAppend(textView, "RTT:" + Integer.parseInt(segments[11])+"\n");
                    tvAppend(textView, "eeprom:" + Integer.parseInt(segments[12])+"\n");

//                    for (int i=0;i<segments.length;i++)
//                    {
//                        tvAppend(textView, "Segment"+i+":" + segments[i]+"\n");
//                    }


                    sb.delete(0, sb.length());



                }

               //data.concat("\n");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            setUiEnabled(true);
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            tvAppend(textView,"Serial Connection Opened!\n");

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart(startButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop(stopButton);

            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        startButton = (Button) findViewById(R.id.buttonStart);
        sendButton = (Button) findViewById(R.id.buttonSend);
        clearButton = (Button) findViewById(R.id.buttonClear);
        stopButton = (Button) findViewById(R.id.buttonStop);
        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);


    }

    public void setUiEnabled(boolean bool) {
        startButton.setEnabled(!bool);
        sendButton.setEnabled(bool);
        stopButton.setEnabled(bool);
        textView.setEnabled(bool);

    }

    public void onClickStart(View view) {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
//                if (deviceVID != 0x2341)//Arduino Vendor ID
//                {
                PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                usbManager.requestPermission(device, pi);
                keep = false;
//                } else {
//                    connection = null;
//                    device = null;
//                }

                if (!keep)
                    break;
            }
        }


    }

    public void onClickSend(View view) {
        String string = editText.getText().toString();
        serialPort.write(string.getBytes());
        tvAppend(textView, "\nData Sent : " + string + "\n");

    }

    public void onClickStop(View view) {
        setUiEnabled(false);
        serialPort.close();
        tvAppend(textView,"\nSerial Connection Closed! \n");

    }

    public void onClickClear(View view) {
        textView.setText(" ");
    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }

    public void onClickPlot(View view) {

//        Button androidMPrealtimeButton = (Button) findViewById(R.id.android_MP_Realtime);
//        androidMPrealtimeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,allgraphs.class));


//            }
//        });
    }


}
