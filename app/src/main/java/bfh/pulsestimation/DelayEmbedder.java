package bfh.pulsestimation;

import android.util.Log;

import Jama.Matrix;

public class DelayEmbedder {

	private int number_sig;
	private int length_sig;
	private int length_embedded;
	private int width_embedded;


	/* Embedding Buffer */
	private Jama.Matrix X_embedded;

	/* Reconstruction Buffers */
	private Jama.Matrix x_out;
	private Jama.Matrix X_1;
	private Jama.Matrix X_2;
	private Jama.Matrix X_3;
	private double[] sum = {0,0,0};


	private int kmax;


	public DelayEmbedder(int l_sig, int n_sig,int k_max) {

		length_sig = l_sig;
		number_sig = n_sig;
		kmax = k_max;

		X_embedded = new Matrix((length_sig - kmax),((kmax+1)* number_sig));

		length_embedded = length_sig - kmax;
		width_embedded = number_sig * (kmax+1);

		X_1 = new Matrix(length_sig,kmax+1);
		X_2 = new Matrix(length_sig,kmax+1);
		X_3 = new Matrix(length_sig,kmax+1);

		x_out = new Matrix((length_sig+kmax),number_sig);
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

	public Jama.Matrix reconstruct(Jama.Matrix X){

		/* Check Dimensions */
		if ( X.getRowDimension() == length_embedded && X.getColumnDimension() == width_embedded){


			X_1 = X.getMatrix(0, length_embedded-1, (1+kmax)*0, ((1+kmax)*1)-1).copy();
			X_2 = X.getMatrix(0, length_embedded-1, (1+kmax)*1, ((1+kmax)*2)-1).copy();
			X_3 = X.getMatrix(0, length_embedded-1, (1+kmax)*2, ((1+kmax)*3)-1).copy();

			int Row_X1 = X_1.getRowDimension();
			int Col_X1 = X_1.getColumnDimension();

			int k;
			int a;
			int b;
			int m=0;

			for(int i = 0;i <= Row_X1-1; i++) {
				if(i<Col_X1) {
					k = i;
				}else {
					k = Col_X1-1;
				}


				sum[0]=0;
				sum[1]=0;
				sum[2]=0;
				for(int j = 0 ; j<=k ; j++) {
					a = i-j;
					b = j;
					sum[0] = sum[0]+X_1.get(a, b);
					sum[1] = sum[1]+X_2.get(a, b);
					sum[2] = sum[2]+X_3.get(a, b);
				}
				x_out.set(m, 0, sum[0]/(k+1));
				x_out.set(m, 1, sum[1]/(k+1));
				x_out.set(m, 2, sum[2]/(k+1));
				m++;
				if(i==Row_X1-1) {
					for(int p=1 ; p<Col_X1 ; p++) {

						sum[0]=0;
						sum[1]=0;
						sum[2]=0;
						for(int j=p; j<=k ; j++) {
							a = i-j+p;
							b = j;
							sum[0] = sum[0]+X_1.get(a, b);
							sum[1] = sum[1]+X_2.get(a, b);
							sum[2] = sum[2]+X_3.get(a, b);
						}
						x_out.set(m, 0, sum[0]/((k+1)-p));
						x_out.set(m, 1, sum[1]/((k+1)-p));
						x_out.set(m, 2, sum[2]/((k+1)-p));
						m++;
					}
				}

			}

			return x_out;

		}else{
			Log.e("Frame Reconstruct","Frame Reconstruct not possible. Matrix dimensions must agree. Return Zero Matrix.") ;
			return x_out.times(0);
		}




	}
}