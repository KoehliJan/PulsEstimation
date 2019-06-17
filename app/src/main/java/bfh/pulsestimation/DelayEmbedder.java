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

		/* Check Dimensions */
		if ( x.getRowDimension() == length_sig && x.getColumnDimension() == number_sig){

			/* Do embedding */
			for (int k = 0; k <= kmax; k++) {								// Embedding count
				for (int i = 0; i <= (number_sig -1); i++) {				// For each channel
					int offset = i*(kmax+1);								// Offset to first signal

					/* Fill up embedded matrix*/
					X_embedded.setMatrix(0, (length_sig-1)-kmax, k+offset, k+offset, x.getMatrix(k, (length_sig-1)-(kmax-k), i, i ));
				}
			}
			return X_embedded;
		}else{
			Log.e("Delay Embedding","Embedding not possible. Matrix dimensions must agree. Return Zero Matrix.") ;
			return X_embedded.times(0);
		}

	}
}