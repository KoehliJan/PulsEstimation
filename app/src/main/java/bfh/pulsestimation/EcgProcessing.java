package bfh.pulsestimation;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

public class EcgProcessing {

	private MainActivity activity;

	int nChannels;
	int kMax;
	double alpha;

	
	private JamaFilter HP;
	private JamaFilter LP;
	private ChannelNormalizer channelNormalizer;
	private BartlettWindow bartlettWindow;
	private DelayEmbedder delayEmbedder;
	private PCAnalyser pcAnalyser;
	private SampleCounter sampleCounter;



	/* Thread for the EcgProcessing */
	private HandlerThread algoHandlerThread;
	private Looper algoLooper;
	private Handler algoHandler;


	private Jama.Matrix X_HP_filtered;
	private Jama.Matrix X_LP_filtered;
	private Jama.Matrix X_normalized;
	private Jama.Matrix X_window;
	private Jama.Matrix X_embedded;
	private Jama.Matrix X_PCA_reduced;
	private Jama.Matrix X_Reconstructed;
    private Jama.Matrix X_out;

    private Segment X_processed;

	EcgProcessing(MainActivity a, int length_X, int n_channels, int k_max, double _alpha) {

		/* Main Activity reference */
		activity = a;

		/* Parameters */
		nChannels = n_channels;
		kMax = k_max;
		alpha = _alpha;

		/* Load Filter Coefficients */
		double[][] Num_HP =  loadFilterCoeff(a,R.array.hp_num);
		double[][] Num_LP =  loadFilterCoeff(a,R.array.lp_num);

		/* Create Filters */
		HP = new JamaFilter(Num_HP, length_X, nChannels);
		LP = new JamaFilter(Num_LP, length_X, nChannels);

		/* Create Normalizer */
		channelNormalizer = new ChannelNormalizer(nChannels);

		/* Create BartlettWindow Objects */
		bartlettWindow = new BartlettWindow(length_X, nChannels);

		/* Create delayEmbedder Objects */
		delayEmbedder = new DelayEmbedder(length_X * 2, nChannels, kMax);

		/* Create PCA Object */
		pcAnalyser = new PCAnalyser(alpha);

		/* Create Sample Counter */
		sampleCounter = new SampleCounter();

		/* Create Thread, Looper, and Handler */
		algoHandlerThread = new HandlerThread("HandlerThread_for_algorithm");
		algoHandlerThread.start();
		algoLooper = algoHandlerThread.getLooper();
		algoHandler = new Handler(algoLooper);

	}


	public void processOnAlgoThread(final Jama.Matrix X_in){
		/* Do the calculation on the Thread for the algorithm */
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

		/* Normalize Channel */
		X_normalized = channelNormalizer.normalize(X_LP_filtered);

		/* Hamming Window */
		X_window = bartlettWindow.applyWindow(X_normalized);
		Log.v("Algo","Window");

		/* Delay delayEmbedder */
		X_embedded = delayEmbedder.embed(X_window);
		Log.v("Algo","Embed");

		/* Principal Component reduction */
		X_PCA_reduced =  pcAnalyser.PCA_Reduction(X_embedded);
		Log.v("Algo","PCA");

		/* Frame reconstruction */
		X_Reconstructed = delayEmbedder.reconstruct(X_PCA_reduced);
		Log.v("Algo","FrameReconstruct");

		/* Overlap and add */
		X_out = bartlettWindow.overlapAndAdd(X_Reconstructed);
		Log.v("Algo","DisWindow");

		/* Add a timestamp in form of sample number */
		X_processed = sampleCounter.stampSegment(X_out);

		/* Get stop time and display the calculation time */
		long timeFinish = System.currentTimeMillis();
		Log.v("Algo","Process Done in " + (timeFinish - timeStart) + " ms.");

		/* Estimate the heart rate */
		activity.getPulseEstimationCh1().process(X_processed.getChannel(0));
		activity.getPulseEstimationCh2().process(X_processed.getChannel(1));
		activity.getPulseEstimationCh3().process(X_processed.getChannel(2));

		/* Add Segment to Ecg Plotter */
		activity.getEcgPlotter().addData(X_processed, activity.getPulseEstimationCh1().getPeaks(),activity.getPulseEstimationCh2().getPeaks(),activity.getPulseEstimationCh3().getPeaks());


	}


	private double[][] loadFilterCoeff(Activity a, int id){

        /* Load Filter Coefficient from XML File */
		String[] num_Strings = a.getResources().getStringArray(id);
		int nCoeff = num_Strings.length;
		double[][]  Num = new double[1][nCoeff];
		for(int i =0; i < nCoeff; i++){
			/* Store Coefficients in flipped Order in the double Array */
			Num[0][nCoeff-1 -i] = Double.parseDouble(num_Strings[i]);
		}
		return Num;
	}

	public void reset(){

		/* Reset all  */
		sampleCounter.reset();
		channelNormalizer.reset();
		bartlettWindow.reset();
		pcAnalyser.reset();

	}
}
