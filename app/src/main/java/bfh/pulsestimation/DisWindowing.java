package bfh.pulsestimation;

import Jama.Matrix;

public class DisWindowing {

	private Matrix X_old;
	private Matrix X_Out;

	private int length_sig;
	private int number_sig;

	private int[] col;



	DisWindowing(int l_sig, int n_sig) {

		length_sig = l_sig;
		number_sig = n_sig;

		col= new int[number_sig];
		for(int i =0; i <number_sig; i++){
			col[i] = i;
		}

		X_old = new Matrix(length_sig,n_sig);

	}

	public Jama.Matrix disolveWindow(Jama.Matrix X){
		/* Calculate output */
		X_Out = X_old.plus(X.getMatrix(0, length_sig-1, col));

		/* Store X old*/
		X_old = X.getMatrix(length_sig, (2*length_sig)-1, col).copy();

		return X_Out;
	}



}