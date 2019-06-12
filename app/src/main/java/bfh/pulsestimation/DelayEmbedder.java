package bfh.pulsestimation;

import android.util.Log;

import Jama.Matrix;

public class DelayEmbedder {

	private Jama.Matrix X_embedded;

	private int number_sig;
	private int length_sig;

	private int kmax;


	public DelayEmbedder(int l_sig, int n_sig,int k_max) {

		length_sig = l_sig;
		number_sig = n_sig;

		kmax = k_max;

		X_embedded = new Matrix((length_sig - kmax),((kmax+1)* number_sig));
	}

	public Jama.Matrix embed(Jama.Matrix x){

		//Log.v("Embedding", "Input: Signal length: "+ x.getRowDimension() + " | Signal Channels: " + x.getColumnDimension());
		//Log.v("Embedding", "Needed: Signal length: "+ length_sig + " | Signal Channels: " +number_sig);

		/* Check Dimensions */
		if ( x.getRowDimension() == length_sig && x.getColumnDimension() == number_sig){

			/* Do embedding */
			for (int k = 0; k <= kmax; k++) {																		// Embedding
				for (int i = 0; i <= (number_sig -1); i++) {																// F�r jedes Signal
					int offset = i*(kmax+1);																		// Abstand zwischen den Signalen
					X_embedded.setMatrix(0, (length_sig-1)-kmax, k+offset, k+offset, x.getMatrix(k, (length_sig-1)-(kmax-k), i, i ));		// start row, end row, start column, end column
				}
			}
			return X_embedded;
		}else{
			Log.e("Delay Embedding","Embedding not possible. Matrix dimensions must agree. Return Zero Matrix.") ;
			return X_embedded.times(0);
		}

	}
}