package bfh.pulsestimation;


import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.view.View.OnClickListener;
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
import java.util.EnumSet;


/**
 * Created by koehl on 03.06.2019.
 */

public class DataScan {

    private  int segmentSize;
    private  int eventSize = 10;
    private  double sampleRate;
    private  int scanBufferSize;

    private double[][] mScanData;
    private Jama.Matrix mSliceData;
    private int neededSampleCount;

    private static String LOGTAG = "DataScan";


    StatusViewer statusViewer;
    Switch sConnection;


    private MainActivity activity;


    private DaqDeviceManager mDaqDeviceManager;
    private DaqDevice mDaqDevice;
    private AiDevice mAiDevice;

    private ArrayAdapter<DaqDeviceDescriptor> mDaqDevInventoryAdapter;

    Algorithmus algorithmus;


    DataScan(MainActivity a,  Algorithmus algo, int l_segment, double fa){

        /* Main activity reference */
        activity = a;
        algorithmus = algo;

        /* Set parameters */
        segmentSize = l_segment;
        scanBufferSize = 2 * (segmentSize + eventSize);
        sampleRate = fa;

        /* Create Daq Device Manager */
        mDaqDeviceManager = new DaqDeviceManager(activity);

    }

    public void setInteraction(Switch sConnect, ProgressBar pBW){

        this.sConnection = sConnect;
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

        statusViewer = new StatusViewer(pBW);
    }



    private void connect(){

        detectDaqDevices();

    }




    private void detectDaqDevices() {

        Log.v(LOGTAG,"Detect");

        /* Find available DAQ devices */
        final ArrayList<DaqDeviceDescriptor> daqDevInventory = mDaqDeviceManager.getDaqDeviceInventory();

        Log.v(LOGTAG,"Show Devices");
        Log.v(LOGTAG,daqDevInventory.toString());

        if(daqDevInventory.size() > 0){

            Log.v(LOGTAG,"Get Detected Devices");
            /* Get the Names from Detected Devices */
            String DevNames[] = new String[daqDevInventory.size()];
            for (int i = 0; i < daqDevInventory.size(); i++){
                DevNames[i] = daqDevInventory.get(i).devString;
            }


            /* Create Dialog to choose Device */
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

            /* Create and show Alert Dialog */
            builder.create().show();

        }else {
            /* No devices detected */
            statusViewer.message("No Devices detected");
            statusViewer.setStatus(mDaqDevice.isConnected());
        }
    }

    private void connectDaqDevice(){

        Log.v(LOGTAG,"Connecting to " + mDaqDevice.toString());

        /* Get Device Info */
        DaqDeviceInfo devInfo = mDaqDevice.getInfo();

        /* Check if this DAQ Device has an Analog Input subsystem */
        if(devInfo.hasAiDev()) {
            AiInfo aiInfo = mDaqDevice.getAiDev().getInfo();

            /* Check if the analog input subsystem has 3 analog input channels */
            int NumChannels = aiInfo.getTotalNumChans();
            if (NumChannels < 3) {
                statusViewer.message("3 channels recommended. Selected device has only "+NumChannels+" analog input Channels. ");
                statusViewer.setStatus(mDaqDevice.isConnected());
            }else {

                /* Check if this device has connection permission */
                if(mDaqDevice.hasConnectionPermission()) {
                    /* This device already has connection permission. Calling onDaqDevicePermission with true (permission granted) */
                    mDeviceConnectionPermissionListener.onDaqDevicePermission(mDaqDevice.getDescriptor(), true);
                }
                else {
                    /* Request permission for connecting to the selected device. onDaqDevicePermission() will be called if permission granted or not */
                    try {
                        mDaqDevice.requestConnectionPermission(mDeviceConnectionPermissionListener);
                    } catch (ULException e) {
                        Log.e(LOGTAG,e.toString());
                        statusViewer.message(e.toString());
                    }
                }
            }
        }else {
            statusViewer.message("Selected device does not support analog input.");
            statusViewer.setStatus(mDaqDevice.isConnected());
        }
    }


    public DaqDeviceConnectionPermissionListener mDeviceConnectionPermissionListener = new DaqDeviceConnectionPermissionListener() {
        public void onDaqDevicePermission(DaqDeviceDescriptor daqDeviceDescriptor, boolean permissionGranted) {

            if(permissionGranted) {
                /* permission granted to establish connection */
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


                            /* Connection successful, prepare and start scan */
                            prepareScan();
                            startScan();

                            /* Start Updating Ecg Plot */
                            activity.getEcgPlotter().run();
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


    private void prepareScan(){
        /* Create Enum Set of Events which should be bind to the Event Listener*/
        EnumSet<DaqEventType> eventTypes =  EnumSet.of(DaqEventType.ON_DATA_AVAILABLE, DaqEventType.ON_END_OF_INPUT_SCAN, DaqEventType.ON_INPUT_SCAN_ERROR);
        /* Bind Events to Daq Event Listener */
        try {
            mDaqDevice.enableEvent(eventTypes, eventSize, mDaqEventListener);
        } catch (ULException e) {
            e.printStackTrace();
            statusViewer.message("Error: "+ e.toString());
        }

    }


    private void startScan(){

        if (mDaqDevice.isConnected()){

            /* Set Channel */
            AiChanMode mode = AiChanMode.DIFFERENTIAL;
            Range range = Range.BIP5VOLTS;

            /* Set sample rate */
            double rate = sampleRate;

            /* Define scan options. Continuous Scan*/
            EnumSet<AiScanOption> options = EnumSet.of(AiScanOption.DEFAULTIO, AiScanOption.CONTINUOUS);

            /* Define unit volts */
            AiUnit mUnit = AiUnit.VOLTS;

            /* Define number of channels */
            int mChanCount = 3;

            /* Initialize data buffers */
            mScanData = new double[mChanCount][scanBufferSize];
            mSliceData = new Jama.Matrix(segmentSize,mChanCount);

            mAiDevice = mDaqDevice.getAiDev();

            neededSampleCount = segmentSize;

            try {

                /* Start collecting data */
                double actualScanRate = mAiDevice.aInScan(0, 2, mode, range, scanBufferSize, rate, options, mUnit, mScanData);
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
                /* Stop collecting data */
                mAiDevice.stopBackground();
        } catch (ULException e) {
            e.printStackTrace();
        }

    }

    private void disconnectDaqDevice(){
        Log.v(LOGTAG,"Disconnect");

        /* Disconnect from device */
        mDaqDevice.disconnect();
        statusViewer.setStatus(mDaqDevice.isConnected());

        /* Stop updating plot */
        activity.getEcgPlotter().stop();

    }

    public void shutDown(){

        if(mDaqDevice != null){
            mDaqDeviceManager.releaseDaqDevice(mDaqDevice);
        }
        mDaqDevice = null;

    }

    /* DAQ Event Listener. Gets fired on data available, on end of input scan and on input scan error. */
    public DaqEventListener mDaqEventListener = new DaqEventListener(){

        @Override
        public void onDaqEvent(DaqDevice daqDevice, DaqEventType daqEventType, Object eventData, ErrorInfo errorInfo) {

            Integer currentSampleCount = (Integer) eventData;

            switch(daqEventType) {
                case ON_DATA_AVAILABLE:

                    /* If enough samples collected */
                    if (currentSampleCount > neededSampleCount){

                        /* Read from scan buffer and copy into slice buffer */
                        setSliceData();

                        /* Start the signal processing */
                        algorithmus.processOnAlgoThread(mSliceData);
                    }

                    break;

                case ON_END_OF_INPUT_SCAN:
                    Log.v(LOGTAG, daqDevice.toString() +": "+ daqEventType.toString());

                    /* Shouldn't be fired in continuous mode*/

                    /* Stop scan and disconnect device. */
                    stopAInScan();
                    disconnectDaqDevice();


                    break;
                case ON_INPUT_SCAN_ERROR:

                    /* Log the error */
                    Log.e(LOGTAG, daqDevice.toString() +": "+ errorInfo.toString());

                    /* Stop and disconnect device */
                    stopAInScan();
                    disconnectDaqDevice();

                    break;
                default:
                    break;
            }

        }
    };

    private void setSliceData(){

        int iStartOfSlice = neededSampleCount - segmentSize;
        int idx;

        Log.v(LOGTAG,""+iStartOfSlice);

        /* Copy and transpose the Slice out of the ScanData*/
        for(int ch = 0 ; ch < mSliceData.getColumnDimension(); ch++  ){
            for (int i = 0; i < mSliceData.getRowDimension(); i++){

                idx = (iStartOfSlice + i) % scanBufferSize;
                mSliceData.set(i,ch,mScanData[ch][idx]);
            }
        }

        /* Increment needed sample count with segment size. */
        neededSampleCount = neededSampleCount + segmentSize;
    }


    private class StatusViewer{

        private ProgressBar pbWaiting;

        StatusViewer(ProgressBar pbW){
            pbWaiting = pbW;
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
        /**/
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


    public void reset(){

        /* Restart Scan */
        stopAInScan();
        if (mDaqDevice != null){
            startScan();
        }

    }


}
