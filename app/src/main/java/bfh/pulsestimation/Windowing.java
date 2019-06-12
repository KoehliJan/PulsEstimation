package bfh.pulsestimation;

import android.util.Log;

import Jama.Matrix;

public class Windowing {

	private Jama.Matrix HammCoef;
	private Jama.Matrix X_old;
	private Jama.Matrix X_Window;

	private int length_sig;
	private int number_sig;

	private int[] col;

	Windowing(int l_sig, int n_sig) {

		length_sig = l_sig;
		number_sig = n_sig;

		X_old = new Jama.Matrix(length_sig,number_sig);
		X_Window = new Jama.Matrix(2*length_sig,number_sig);

		col = new int[number_sig];
		for(int i =0; i <number_sig; i++){
			col[i] = i;
		}

		/* Set Hamming Coeff */
		HammCoef = new Jama.Matrix(2*length_sig,number_sig);
		for (int j = 0; j < number_sig; j++){
			for ( int i = 0; i <= (length_sig*2)-1; i ++ ) {
				HammCoef.set(i, j, (0.54-(0.46*Math.cos((2*(Math.PI)*i/(length_sig*2))))));
			}
		}
	}

	public Jama.Matrix window(Jama.Matrix X){


		/* Check Dimensions */
		if ( X.getRowDimension() == length_sig && X.getColumnDimension() == number_sig){

			X_Window.setMatrix(0, X.getRowDimension()-1, col, X_old);
			X_Window.setMatrix(X.getRowDimension(),2*X.getRowDimension()-1, col, X);
			X_Window = X_Window.arrayTimes(HammCoef);
			X_old = X.copy();

			return X_Window;
		}else{
			Log.e("Windowing","Windowing not Possible. Matrix dimensions must agree. Return Zero Matrix.") ;
			return X_Window.times(0);
		}

	}

}