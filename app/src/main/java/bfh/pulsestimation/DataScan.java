package bfh.pulsestimation;


import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.mcc.ul.AiChanMode;
import com.mcc.ul.AiDevice;
import com.mcc.ul.AiInfo;
import com.mcc.ul.AiScanOption;
import com.mcc.ul.AiUnit;
import com.mcc.ul.DaqDevice;
import com.mcc.ul.DaqDeviceConnectionPermissionListener;
import com.mcc.ul.DaqDeviceDescriptor;
import com.mcc.ul.DaqDeviceInfo;
import com.mcc.ul.DaqDeviceManager;
import com.mcc.ul.DaqEventListener;
import com.mcc.ul.DaqEventType;
import com.mcc.ul.ErrorInfo;
import com.mcc.ul.Range;
import com.mcc.ul.ULException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by koehl on 03.06.2019.
 */

public class DataScan {

    private static int BUFFERSIZE = (int) (2.5 * 256);
    private static int EVENTSIZE = 10;
    private static double RATE = 256;
    private final static int SAMPLE_PER_CHAN = 2 * (BUFFERSIZE + EVENTSIZE);

    private double[][] mScanData;
    private Jama.Matrix mSliceData;
    private int neededSampleCount = BUFFERSIZE;

    private static String LOGTAG = "DataScan";


    Switch sConnection;

    StatusViewer statusViewer;

    private MainActivity activity;


    private DaqDeviceManager mDaqDeviceManager;
    private DaqDevice mDaqDevice;
    private AiDevice mAiDevice;

    private ArrayAdapter<DaqDeviceDescriptor> mDaqDevInventoryAdapter;

    Algorithmus algorithmus;


    DataScan(MainActivity a,Algorithmus algo){

        activity = a;

        algorithmus = algo;

        init();


    }



    private void connect(){
        detectDaqDevices();
    }


    private void init(){

        sConnection = (Switch) activity.findViewById(R.id.switchConnection);
        sConnection.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sConnection.isChecked()){
                    connect();
                }else{
                    disconnectDaqDevice();
                }

            }
        });

        statusViewer = new StatusViewer();


        mDaqDeviceManager = new DaqDeviceManager(activity);



    };

    private void detectDaqDevices() {

        Log.v(LOGTAG,"Detect");

        // Find available DAQ devices
        final ArrayList<DaqDeviceDescriptor> daqDevInventory = mDaqDeviceManager.getDaqDeviceInventory();

        Log.v(LOGTAG,"Show Devices");
        Log.v(LOGTAG,daqDevInventory.toString());

        if(daqDevInventory.size() > 0){

            Log.v(LOGTAG,"Get Detected Devices");
            // Get the Names from Detected Devices
            String DevNames[] = new String[daqDevInventory.size()];
            for (int i = 0; i < daqDevInventory.size(); i++){
                DevNames[i] = daqDevInventory.get(i).devString;
            }

            Log.v(LOGTAG,"create Dialog");
            // Create Dialog to choose Device
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.chooseDev)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Log.v(LOGTAG,"Dialog canceled");
                            statusViewer.setStatus(false);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.v(LOGTAG,"Dialog canceled");
                            statusViewer.setStatus(false);
                        }
                    })
                    .setItems(DevNames, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {


                            // Release current Device
                            if(mDaqDevice != null){
                                mDaqDeviceManager.releaseDaqDevice(mDaqDevice);
                            }

                            // Create a DaqDevice object for the selected device
                            mDaqDevice = mDaqDeviceManager.createDaqDevice(daqDevInventory.get(which));

                            // Start connection
                            statusViewer.setStatusWaiting();
                            connectDaqDevice();
                        }
                    });



            builder.create().show();

        }else {
            // No devices detected
            statusViewer.message("No Devices detected");
            statusViewer.setStatus(mDaqDevice.isConnected());
        }

    }

    private void connectDaqDevice(){

        Log.v(LOGTAG,mDaqDevice.toString());

        DaqDeviceInfo devInfo = mDaqDevice.getInfo();

        // Check if this DAQ Device has 3 analog input channels (subsystem)
        if(devInfo.hasAiDev()) {
            AiInfo aiInfo = mDaqDevice.getAiDev().getInfo();

            int NumChannels = aiInfo.getTotalNumChans();
            if (NumChannels < 3) {
                statusViewer.message("Selected device has only "+NumChannels+" analog input Channels");
                statusViewer.setStatus(mDaqDevice.isConnected());
            }else {

                // Check if this device has connection permission
                if(mDaqDevice.hasConnectionPermission()) {
                    // This device already has connection permission. try to connect to it
                    mDeviceConnectionPermissionListener.onDaqDevicePermission(mDaqDevice.getDescriptor(), true);
                }
                else {
                    //Request permission for connecting to the selected device
                    try {
                        mDaqDevice.requestConnectionPermission(mDeviceConnectionPermissionListener);
                    } catch (ULException e) {
                        Log.e(LOGTAG,e.toString());
                        statusViewer.message(e.toString());
                    }
                }
            }
        }else {
            statusViewer.message("Selected device does not support analog input");
            statusViewer.setStatus(mDaqDevice.isConnected());
        }
    }


    public DaqDeviceConnectionPermissionListener mDeviceConnectionPermissionListener = new DaqDeviceConnectionPermissionListener() {
        public void onDaqDevicePermission(DaqDeviceDescriptor daqDeviceDescriptor, boolean permissionGranted) {
            if(permissionGranted) {

                // Try to connect on different Thread
                new Thread(new Runnable() {
                    public void run(){
                        try {
                            // Establish connection to the DAQ device
                            mDaqDevice.connect();

                        } catch (Exception e) {
                            Log.v(LOGTAG,"Unable to connect to " + mDaqDevice + ". " + e.getMessage());
                            statusViewer.message("Unable to connect to " + mDaqDevice + ". " + e.getMessage());
                        }
                        statusViewer.setStatus(mDaqDevice.isConnected());

                        if(mDaqDevice.isConnected()){
                            startScan();
                        }

                    }
                }).start();

            }
            else {
                Log.v(LOGTAG,"Permission denied to connect to " + mDaqDevice);
                statusViewer.message("Permission denied to connect to " + mDaqDevice);
                statusViewer.setStatus(mDaqDevice.isConnected());
            }

        }
    };


    private void startScan(){

        if (mDaqDevice.isConnected()){

            int lowChan = 0;
            int highChan = 2;

            AiChanMode mode = AiChanMode.DIFFERENTIAL;
            Range range = Range.BIP5VOLTS;

            double rate = RATE;

            EnumSet<AiScanOption> options = EnumSet.of(AiScanOption.DEFAULTIO, AiScanOption.CONTINUOUS);

            AiUnit mUnit = AiUnit.VOLTS;

            int mChanCount = highChan >= lowChan ? highChan - lowChan + 1 : 1;
            mScanData = new double[mChanCount][SAMPLE_PER_CHAN];
            mSliceData = new Jama.Matrix(BUFFERSIZE,mChanCount);

            mAiDevice = mDaqDevice.getAiDev();

            // Event types to enable
            EnumSet<DaqEventType> eventTypes =  EnumSet.of(DaqEventType.ON_DATA_AVAILABLE, DaqEventType.ON_END_OF_INPUT_SCAN, DaqEventType.ON_INPUT_SCAN_ERROR);

            neededSampleCount = BUFFERSIZE;

            // Bind Events to Daq Event Listener (this)
            try {
                mDaqDevice.enableEvent(eventTypes, EVENTSIZE, mDaqEventListener);

                /* Reset Ecg Plot */
                activity.getEcgPlotter().reset();


                /* Start Updating Ecg Plot*/
                activity.getEcgPlotter().run();

                @SuppressWarnings("unused")
                //Collect the values by calling the aInScan function
               double actualScanRate = mAiDevice.aInScan(lowChan, highChan, mode, range, SAMPLE_PER_CHAN, rate, options, mUnit, mScanData);
                Log.v(LOGTAG," Start Scan with Scan rate: "+ actualScanRate);

            } catch (final ULException e) {
                statusViewer.message("Could not start Scan");
                Log.e(LOGTAG,"Could not start Scan: " + e.toString());
                mDaqDevice.disconnect();
                statusViewer.setStatus(mDaqDevice.isConnected());
                statusViewer.message("Error: "+ e.toString());
            }


        }
    }

    void stopAInScan() {
        try {
            if(mAiDevice != null)
                mAiDevice.stopBackground();
        } catch (ULException e) {
            e.printStackTrace();
        }

    }

    private void disconnectDaqDevice(){
        Log.v(LOGTAG,"Disconnect");
        mDaqDevice.disconnect();
        statusViewer.setStatus(mDaqDevice.isConnected());
        activity.getEcgPlotter().stop();
    }

    public void shutDown(){

        if(mDaqDevice != null){
            mDaqDeviceManager.releaseDaqDevice(mDaqDevice);
        }
        mDaqDevice = null;

    }


    public DaqEventListener mDaqEventListener = new DaqEventListener(){

        @Override
        public void onDaqEvent(DaqDevice daqDevice, DaqEventType daqEventType, Object eventData, ErrorInfo errorInfo) {

            Integer currentSampleCount = (Integer) eventData;

            switch(daqEventType) {
                case ON_DATA_AVAILABLE:

                    if (currentSampleCount > neededSampleCount){
                        //Log.v(LOGTAG, daqDevice.toString() +": "+ daqEventType.toString() +" Current Sample Count: "+ currentSampleCount + " | " + neededSampleCount);

                        setSliceData();
                        algorithmus.processOnAlgoThread(mSliceData);
                        Log.v(LOGTAG,"DataScan sended Data at Time: " + System.currentTimeMillis() +" ms");
                    }

                    break;
                case ON_END_OF_INPUT_SCAN:
                    Log.v(LOGTAG, daqDevice.toString() +": "+ daqEventType.toString());
                    stopAInScan();
                    disconnectDaqDevice();


                    break;
                case ON_INPUT_SCAN_ERROR:
                    Log.v(LOGTAG, "Connected: " + mDaqDevice.isConnected());
                    Log.e(LOGTAG, daqDevice.toString() +": "+ errorInfo.toString());
                    stopAInScan();
                    disconnectDaqDevice();

                    break;
                default:
                    break;
            }

        }
    };

    private void setSliceData(){
        int iStartOfSlice = neededSampleCount - BUFFERSIZE;
        int idx;

        //Log.v(LOGTAG,"iStartOfSlice: " + iStartOfSlice);

        /* Copy and transpose the Slice out of the ScanData*/
        for(int ch = 0 ; ch < mSliceData.getColumnDimension(); ch++  ){
            for (int i = 0; i < mSliceData.getRowDimension(); i++){

                idx = (iStartOfSlice + i) % SAMPLE_PER_CHAN;
                mSliceData.set(i,ch,mScanData[ch][idx]);
                // Log Channel One
                //if (ch == 0){
                //    Log.v(LOGTAG,"" + idx + " = " + " ( " + iStartOfSlice + " + " + i + " ) " + " % " + SAMPLE_PER_CHAN);
                //}
            }
        }
        // Log Channel One
        //Log.v(LOGTAG,"Scan");
        //Log.v(LOGTAG,Arrays.toString(mScanData[0]));

        //Log.v(LOGTAG,"Slice");
        //Log.v(LOGTAG,Arrays.toString(mSliceData[0]));

        neededSampleCount = neededSampleCount + BUFFERSIZE;
    }


    private class StatusViewer{

        private ProgressBar pbWaiting;
        private LinearLayout llConnection;

        StatusViewer(){
            pbWaiting = (ProgressBar) activity.findViewById(R.id.progressbar_waiting);
            llConnection = (LinearLayout) activity.findViewById(R.id.linearlayout_connection);
        }

        public void setStatus(final boolean connected){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pbWaiting.setVisibility(View.GONE);
                    sConnection.setEnabled(true);
                    sConnection.setChecked(connected);
                }
            });

        }

        public void setStatusWaiting(){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pbWaiting.setVisibility(View.VISIBLE);
                    sConnection.setEnabled(false);
                }
            });
        }

        public void message(final String msg){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity.getBaseContext(), msg,Toast.LENGTH_LONG).show();
                }
            });


        }


    }

    public int getCurrentSampleCount(){
        try {
            if (mAiDevice != null){
                return mAiDevice.getStatus().currentCount;
            }else {
                return -1;
            }

        } catch (ULException e) {
            e.printStackTrace();
            Log.e(LOGTAG,e.toString());
            return -1;
        }
    }


}
