package bfh.pulsestimation;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import org.achartengine.model.XYSeries;

public class Algorithmus {

	MainActivity activity;

	int nChannels;
	int kMax;
	double alpha;

	
	private Jama_Filter HP;
	private Jama_Filter LP;
	private Windowing Ham;
	private DisWindowing Seg;
	private DelayEmbedder Embedding;
	private PCAnalyser pcAnalyser;
	private FrameReconstructor_v2 Reconstruction;
	private DisWindowing disWin;
	private SampleCounter sampleCounter;


	/* Thread for the Algorithmus */
	private HandlerThread algoHandlerThread;
	private Looper algoLooper;
	private Handler algoHandler;


	private Jama.Matrix X_HP_filtered;
	private Jama.Matrix X_LP_filtered;
	private Jama.Matrix X_hamming;
	private Jama.Matrix X_embedded;
	private Jama.Matrix X_PCA_reduced;
	private Jama.Matrix X_Reconstructed;
    private Jama.Matrix X_out;

    private Segment X_processed;

	Algorithmus(MainActivity a, int length_X, int n_channels, int k_max, double _alpha) {

		activity = a;

		/* Parameters */
		nChannels = n_channels;
		kMax = k_max;
		alpha = _alpha;

		/* Load Filter Coefficients */
		double[][] Num_HP =  loadFilterCoeff(a,R.array.hp_num);
		double[][] Num_LP =  loadFilterCoeff(a,R.array.lp_num);

		/* Create Filters */
		HP = new Jama_Filter(Num_HP, length_X, nChannels);
		LP = new Jama_Filter(Num_LP, length_X, nChannels);

		/* Create Windowing Objects */
		Ham = new Windowing(length_X, nChannels);
		disWin = new DisWindowing(length_X, nChannels);

		/* Create Embedding Objects */
		Embedding = new DelayEmbedder(length_X * 2, nChannels, kMax);
		Reconstruction = new FrameReconstructor_v2(length_X * 2, nChannels,kMax );

		/* Create PCA Object */
		pcAnalyser = new PCAnalyser(alpha);

		/* Create Sample Counter */
		sampleCounter = new SampleCounter();

		/* Create Thread, Looper, and Handler*/
		algoHandlerThread = new HandlerThread("HandlerThread_for_algorithm");
		algoHandlerThread.start();
		algoLooper = algoHandlerThread.getLooper();
		algoHandler = new Handler(algoLooper);


	}

	public void processOnAlgoThread(final Jama.Matrix X_in){
		algoHandler.post(new Runnable() {
			@Override
			public void run() {
				process(X_in);
			}
		});
	}

	private void process(Jama.Matrix X_in) {

		/* Get Start time */
		long timeStart = System.currentTimeMillis();

		Log.v("Algo","Process Started at Time: " + timeStart + " ms");


		/* Highpass filtering */
		X_HP_filtered = HP.filter(X_in);
		Log.v("Algo","HP");

		/* Lowpass filtering */
		X_LP_filtered = LP.filter(X_HP_filtered);
		Log.v("Algo","LP");

		/* Hamming Window */
		X_hamming = Ham.window(X_LP_filtered);
		Log.v("Algo","Window");

		/* Delay Embedding */
		X_embedded = Embedding.embed(X_hamming);
		Log.v("Algo","Embed");

		/* Principal Component reduction */
		X_PCA_reduced = pcAnalyser.PCA_Reduction(X_embedded);
		Log.v("Algo","PCA");

		/* Frame reconstruction */
		X_Reconstructed = Reconstruction.reconstruct(X_PCA_reduced);
		Log.v("Algo","FrameReconstruct");

		/* Overlap and add */
		X_out = disWin.disolveWindow(X_Reconstructed);
		Log.v("Algo","DisWindow");

		/* Add a timestamp in form of sample number */
		X_processed = sampleCounter.stampSegment(X_out,0);

		/* Get stop time and display the calculation time */
		long timeFinish= System.currentTimeMillis();
		Log.v("Algo","Process Done in " + (timeFinish - timeStart) + " ms.");



		activity.getPeakDetecter().process(X_processed);

		activity.getRrDetecter().process(activity.getPeakDetecter().getR_Peaks(),activity.getPeakDetecter().getAnzPeaks());

		/* Plot processed Signal */
		activity.getEcgPlotter().addData(X_processed, activity.getRrDetecter().getPeaks());

	}


	private double[][] loadFilterCoeff(Activity a, int id){
        /* Load Filter Coefficient */
		String[] num_Strings = a.getResources().getStringArray(id);
		int nCoeff = num_Strings.length;
		double[][]  Num = new double[1][nCoeff];
		for(int i =0; i < nCoeff; i++){
			// Store Coefficients in flipped Order in the double Array
			Num[0][nCoeff-1 -i] = Double.parseDouble(num_Strings[i]);
		}
		return Num;
	}
}
