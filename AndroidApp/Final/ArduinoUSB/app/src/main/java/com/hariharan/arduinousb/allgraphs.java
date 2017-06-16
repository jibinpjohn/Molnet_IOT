package com.hariharan.arduinousb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.hariharan.arduinousb.notimportant.DemoBase;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class allgraphs extends DemoBase implements
        OnChartValueSelectedListener {

    private LineChart mChart, mChart2;

    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
    private StringBuilder sb = new StringBuilder();

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            byte[] buffer = new byte[256];
            try {
                data = new String(arg0, "UTF-8");
                buffer=(byte[]) arg0;
                String strIncom = new String(buffer, 0);
                sb.append(strIncom);
                int endOfLineIndex = sb.indexOf("/CS");
                int startCS = sb.indexOf("CS");


                //String packets[] =data.split("ENDPACKET");

                // tvAppend(textView, "Length:" + packets.length+"\n");

                if (endOfLineIndex > 0) {


                    String sbprint = sb.substring((startCS + 2), endOfLineIndex);
                    Date date=new Date();
                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    DateFormat format2 = new SimpleDateFormat("HH:mm:ss");
                    // String formatted = format.format(date);

                    String formatted2 = format2.format(date);
                    sample.setTime_stamp(formatted2);
                    String segments[]=sbprint.split("/");
                    int decimal=Integer.parseInt(segments[2]);
                    sample.setTemperature((float)decimal);
                    decimal=Integer.parseInt(segments[1]);
                    sample.setDielectric((float)decimal);
                    addEntry();
                    addEntry2();

//                    for (int i=0;i<segments.length;i++)
//                    {
//                        tvAppend(textView, "Segment"+i+":" + segments[i]+"\n");
//                    }

                    sb.delete(0, sb.length());




                }

                //  data.concat("\n");

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

                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);

                            serialPort.read(mCallback);


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
                Attach();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                Dettach();

            }
        }

        ;
    };

    public void  Attach()
    {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID != 0x2349)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }
    }

    public void  Dettach()
    {
        serialPort.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.allgraphs);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);
        mChart2 = (LineChart) findViewById(R.id.chart2);
        mChart2.setOnChartValueSelectedListener(this);
        // enable description text
        mChart.getDescription().setText("Time");
        //mChart.setDescription(null);

        mChart2.getDescription().setText("Time");

        // enable touch gestures
        mChart.setTouchEnabled(true);
        mChart2.setTouchEnabled(true);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        mChart2.setDragEnabled(true);
        mChart2.setScaleEnabled(true);
        mChart2.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        mChart2.setPinchZoom(true);
        // set an alternative background color
        mChart.setBackgroundColor(Color.CYAN);
        mChart2.setBackgroundColor(Color.YELLOW);
        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        LineData data2 = new LineData();
        data2.setValueTextColor(Color.BLUE);

        // add empty data
        mChart.setData(data);
        mChart2.setData(data2);
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        Legend l2 = mChart2.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(Typeface.DEFAULT);
        l.setTextColor(Color.BLUE);

        l2.setForm(Legend.LegendForm.DEFAULT);
        l2.setTypeface(Typeface.DEFAULT);
        l2.setTextColor(Color.BLUE);

        XAxis xl = mChart.getXAxis();
        xl.setTypeface(Typeface.DEFAULT);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(900f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        XAxis x2 = mChart2.getXAxis();
        x2.setTypeface(Typeface.DEFAULT);
        x2.setTextColor(Color.BLACK);
        x2.setDrawGridLines(false);
        x2.setAvoidFirstLastClipping(true);
        x2.setEnabled(true);

        YAxis leftAxis2 = mChart2.getAxisLeft();
        leftAxis2.setTypeface(Typeface.DEFAULT);
        leftAxis2.setTextColor(Color.BLACK);
        leftAxis2.setAxisMaximum(300f);
        leftAxis2.setAxisMinimum(0f);
        leftAxis2.setDrawGridLines(true);




        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


        YAxis rightAxis2 = mChart2.getAxisRight();
        rightAxis2.setEnabled(false);




        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
    }

    //  @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.realtime, menu);
//        return true;
//    }

    @Override
    protected void onResume() {
        super.onResume();
        // IntentFilter filter1 = new IntentFilter();
        new Thread(new Runnable() {
            @Override
            public void run() {
//               while(true) {
                runOnUiThread(new Runnable() {
                    IntentFilter filter1 = new IntentFilter();
                    @Override
                    public void run() {


                        IntentFilter filter1 = new IntentFilter();
                        Toast.makeText(getApplicationContext(), "ON RUN ", Toast.LENGTH_SHORT).show();
                        filter1.addAction(ACTION_USB_PERMISSION);
                        filter1.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
                        filter1.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                        registerReceiver(broadcastReceiver, filter1);
                        Attach();

                    }
                });

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //}
        }).start();
//       usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
//       IntentFilter filter1 = new IntentFilter();
//       Toast.makeText(getApplicationContext(), "ON RUN ", Toast.LENGTH_SHORT).show();
//       filter1.addAction(ACTION_USB_PERMISSION);
//       filter1.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//       filter1.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//       registerReceiver(broadcastReceiver, filter1);
//       Attach();

    }


    public  void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            //Calendar cal = Calendar.getInstance();

            //int hour = cal.get(Calendar.SECOND);
            data.addEntry(new Entry(set.getEntryCount(),sample.getTemperature()), 0);
            //data.addEntry(new Entry(set.getEntryCount(),  sample.getTemperature()), 0);
            //data.addEntry(new Entry(hour, (float) (sample.getTemperature()) + 30f), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }
    public  void addEntry2() {

        LineData data2 = mChart2.getData();

        if (data2 != null) {

            ILineDataSet set2 = data2.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set2 == null) {
                set2 = createSet2();
                data2.addDataSet(set2);
            }

            //Calendar cal = Calendar.getInstance();

            //int hour = cal.get(Calendar.SECOND);
            data2.addEntry(new Entry(set2.getEntryCount(),sample.getDielectric()), 0);
            //data.addEntry(new Entry(set.getEntryCount(),  sample.getTemperature()), 0);
            //data.addEntry(new Entry(hour, (float) (sample.getTemperature()) + 30f), 0);
            data2.notifyDataChanged();

            // let the chart know it's data has changed
            mChart2.notifyDataSetChanged();

            // limit the number of visible entries
            mChart2.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart2.moveViewToX(data2.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }







    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Temperature");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(18f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSet2() {

        LineDataSet set = new LineDataSet(null, "Dielectric");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(18f);
        set.setDrawValues(false);
        return set;
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }





}
