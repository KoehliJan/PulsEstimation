package bfh.pulsestimation;

import android.content.Context;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;


public class MainActivity extends AppCompatActivity {

    /* Define constants */
    static int N_CHANNELS_3 = 3;
    static int K_MAX = 8;
    static double ALPHA = 0.85;
    static int LENGTH_SEGMENT = (int)(2.5 * 256);


    /* Power Manager for WakeLock */
    private PowerManager.WakeLock mWakeLock = null;

    /* Worker Objects */
    private EcgPlotter ecgPlotter;
    private HeartRatePlotter heartRatePlotter;
    private DataScan dataScan;
    private Algorithmus algorithmus;
    private PulseEstimationClass pulsEstimationCh1;
    private PulseEstimationClass pulsEstimationCh2;
    private PulseEstimationClass pulsEstimationCh3;


    /* UI Elements */
    private Switch sConnection;
    private ProgressBar pbConnecting;
    private ImageButton bReset;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Turn on Wake Lock -> keeps the system awake */
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "PulseEstimationClass:wakelocktag");
        mWakeLock.acquire();


        /* Create Heart Rate Plotter */
        heartRatePlotter = new HeartRatePlotter(this);
        /* Create Ecg Plotter */
        ecgPlotter = new EcgPlotter(this);
        /* Create Algorithm Object*/
        algorithmus = new Algorithmus(this, LENGTH_SEGMENT, N_CHANNELS_3,K_MAX,ALPHA);
        /* Create Data Scan Object */
        dataScan = new DataScan(this, algorithmus, LENGTH_SEGMENT,256);
        /* Create pulsestimation for each channel */
        pulsEstimationCh1 = new PulseEstimationClass(this,LENGTH_SEGMENT,256);
        pulsEstimationCh2 = new PulseEstimationClass(this,LENGTH_SEGMENT,256);
        pulsEstimationCh3 = new PulseEstimationClass(this,LENGTH_SEGMENT,256);

    }

    public void reset(){

        /* Reset all */
        algorithmus.reset();
        pulsEstimationCh1.reset();
        pulsEstimationCh2.reset();
        pulsEstimationCh3.reset();
        ecgPlotter.reset();
        heartRatePlotter.reset();
        dataScan.reset();
        Log.v("Main Activity", "Reset");

    }

    /* Getter and Setter for Worker Objects */
    public Algorithmus getAlgorithmus() {
        return algorithmus;
    }

    public DataScan getDataScan() {
        return dataScan;
    }

    public EcgPlotter getEcgPlotter() {
        return ecgPlotter;
    }

    public PulseEstimationClass getPulseEstimationCh1() {
        return pulsEstimationCh1;
    }

    public PulseEstimationClass getPulseEstimationCh2() {
        return pulsEstimationCh2;
    }

    public PulseEstimationClass getPulseEstimationCh3() {
        return pulsEstimationCh3;
    }

    public HeartRatePlotter getHeartRatePlotter() {
        return heartRatePlotter;
    }

    @Override
    protected void onDestroy() {

        /* Shut down Datascan */
        dataScan.shutDown();

        /* Stop updating realtime plots */
        ecgPlotter.stop();

        /* Release Wake Lock */
        if(mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        /* Get UI Elements for interaction with DataScan */
        sConnection = (Switch) menu.findItem(R.id.MenuEntryConnect).getActionView().findViewById(R.id.switchConnection);
        pbConnecting =(ProgressBar) menu.findItem(R.id.MenuEntryConnect).getActionView().findViewById(R.id.progressbar_waiting);
        dataScan.setInteraction(sConnection,pbConnecting);

        /* Reset Button*/
        bReset = (ImageButton) menu.findItem(R.id.MenuEntryReset).getActionView().findViewById(R.id.resetButton);
        bReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        return true;
    }

}
