package bfh.pulsestimation;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    static int N_CHANNELS_3 = 3;
    static int K_MAX = 8;
    static double ALPHA = 0.85;

    private PowerManager.WakeLock mWakeLock = null;

    EcgPlotter ecgPlotter;

    DataScan dataScan;

    Algorithmus algorithmus;

    PeakDetecter peakDetecter;
    RRDetecter rrDetecter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // keep the system awake while this app is running
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "PulsEstimation:wakelocktag");
        mWakeLock.acquire();



        /* Create Ecg Plotter */
        ecgPlotter = new EcgPlotter(this);

        /* Create Algorithm Object*/
        algorithmus = new Algorithmus(this, (int)(2.5 * 256), 3,8,0.85);

        /* Create Data Scan Object */
        dataScan = new DataScan(this, algorithmus);

        /* Create Peak detection */
        peakDetecter = new PeakDetecter(this,(int)(2.5 * 256),256);

        /* Creat RR processing */
        rrDetecter = new RRDetecter((int)(2.5 * 256));


    }


    public Algorithmus getAlgorithmus() {
        return algorithmus;
    }

    public DataScan getDataScan() {
        return dataScan;
    }

    public EcgPlotter getEcgPlotter() {
        return ecgPlotter;
    }

    public PeakDetecter getPeakDetecter() {
        return peakDetecter;
    }

    public RRDetecter getRrDetecter() {
        return rrDetecter;
    }

    @Override
    protected void onDestroy() {


        dataScan.shutDown();
        ecgPlotter.stop();

        // Wake locks should be released in onPause, however in order to keep the system awake while
        // scan is running and the application is minimized, the release method is called here
        if(mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
        super.onDestroy();
    }
}
