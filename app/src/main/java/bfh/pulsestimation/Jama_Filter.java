package bfh.pulsestimation;

import android.util.Log;

import Jama.Matrix;

public class Jama_Filter {

	private Jama.Matrix Num_Mat;

	private Jama.Matrix z;

    private int	length_num = 0;
	private int number_sig = 0;
	private int length_sig = 0;

	private Jama.Matrix ZX;
    private Jama.Matrix X_filterd;

    private static String LOGTAG = "Jama_Filter";

	
	//Konstruktor
	//Matrix X_filterd;
	Jama_Filter(double[][] Num, int l_sig, int n_sig) {

		Num_Mat = new Jama.Matrix(Num);
		length_num = Num_Mat.getColumnDimension();

        length_sig = l_sig;
        number_sig = n_sig;

        X_filterd = new Jama.Matrix(l_sig,n_sig);
        ZX = new Jama.Matrix((l_sig+length_num),n_sig);
        z = new Jama.Matrix(length_num,number_sig);
    }


    public Jama.Matrix filter(Jama.Matrix X){

	    /* Check Dimensions */
        if ( X.getRowDimension() == length_sig && X.getColumnDimension() == number_sig){


            //Arbeitsmatrix erstellen mit Initialisierungswerten und neuen Wertenr
            ZX.setMatrix(0, (length_num-1), 0, number_sig-1, z);
            ZX.setMatrix(length_num, (length_num+length_sig-1), 0, number_sig-1, X);

            /* Filter Signal*/
            for (int k = 0; k <= (length_sig-1); k++) {
                X_filterd.setMatrix(k,k, 0, number_sig-1,(Num_Mat.times(ZX.getMatrix(1+k, length_num+k, 0, number_sig-1))));
            }

            // Uebergabeparameter abspeichgern
            z.setMatrix(0, length_num-1, 0, number_sig-1, X.getMatrix(length_sig-1-(length_num-1), length_sig-1, 0, number_sig-1));

            /* Return Result */
            return X_filterd;

        }else{
            Log.e(LOGTAG,"Dimensions must agree. Filtering not possible. Unfiltered Signal returned");
            return X;
        }

    }


	public Jama.Matrix getNum(){
		return Num_Mat;
	}
	public Jama.Matrix getz(){
		return z;
	}
}
