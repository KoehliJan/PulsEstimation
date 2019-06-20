package bfh.pulsestimation;

import android.util.Log;

public class BartlettWindow {

	private Jama.Matrix BartCoef;

	/* Apply Window Buffers */
	private Jama.Matrix X_Segment_old;
	private Jama.Matrix X_Window;

	/* Overlap and Add Buffers*/
	private Jama.Matrix X_Window_old;
	private Jama.Matrix X_Segment;

	private int length_sig;
	private int number_sig;

	private int[] col;

	BartlettWindow(int l_sig, int n_sig) {

		length_sig = l_sig;
		number_sig = n_sig;

		X_Segment_old = new Jama.Matrix(length_sig,number_sig);
		X_Window = new Jama.Matrix(2*length_sig,number_sig);

		X_Window_old = new Jama.Matrix(length_sig,number_sig);
		X_Segment = new Jama.Matrix(length_sig, number_sig);



		col = new int[number_sig];
		for(int i =0; i <number_sig; i++){
			col[i] = i;
		}

		/* Set Bartlett Coeff */
		BartCoef = new Jama.Matrix(2*length_sig,number_sig);
		for (int j = 0; j < number_sig; j++){
			for ( int i = 0; i <= (length_sig*2)-1; i ++ ) {

				BartCoef.set(i, j, (2/((double) length_sig*2-1)*(((double) length_sig*2-1)/2-Math.abs(i-((double) length_sig*2-1)/2))));
				//Log.v("Window","Result: "+ (  (2/((double) length_sig*2-1)*(((double) length_sig*2-1)/2-Math.abs(i-((double) length_sig*2-1)/2)))    ));
				//BartCoef.set(i, j, (0.54-(0.46*Math.cos((2*(Math.PI)*i/(length_sig*2))))));

			}
		}
	}

	public Jama.Matrix applyWindow(Jama.Matrix X){

		/* Check Dimensions */
		if ( X.getRowDimension() == length_sig && X.getColumnDimension() == number_sig){

			X_Window.setMatrix(0, X.getRowDimension()-1, col, X_Segment_old);
			X_Window.setMatrix(X.getRowDimension(),2*X.getRowDimension()-1, col, X);
			X_Window = X_Window.arrayTimes(BartCoef);
			X_Segment_old = X.copy();

			return X_Window;
		}else{
			Log.e("BartlettWindow","BartlettWindow not Possible. Matrix dimensions must agree. Return Zero Matrix.") ;
			return X_Window.times(0);
		}

	}

	public Jama.Matrix overlapAndAdd(Jama.Matrix X){

		/* Calculate output. (Overlap and Add) */
		X_Segment = X_Window_old.plus(X.getMatrix(0, length_sig-1, col));

		/* Store X old*/
		X_Window_old = X.getMatrix(length_sig, (2*length_sig)-1, col).copy();

		return X_Segment;
	}

	public void reset(){
		X_Segment_old = X_Segment_old.times(0);
		X_Window_old = X_Window_old.times(0);
	}

}