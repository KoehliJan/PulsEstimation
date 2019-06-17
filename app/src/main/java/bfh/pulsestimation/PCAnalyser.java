package bfh.pulsestimation;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class PCAnalyser{


	private int segmentCounter = 0;

	private Jama.Matrix EigenVec;
	private Jama.Matrix CoeffReduced;
	private Jama.Matrix Score;
	private Jama.Matrix X_Reduced;

	// Eigenvalue decomposition
	private Jama.Matrix Cov;
	private EigenvalueDecomposition Eig;
	private int nbr_pc;

	private double Alpha;
	
	public PCAnalyser(double alpha) {
		Alpha = alpha;
	}

	public Jama.Matrix PCA_Reduction(Jama.Matrix X){

		if (segmentCounter < 4){
			/* Calculate Coeffs */
			calcKovarianceMatrix(X);
			Eig = Cov.eig();
			calc_numberPC();
			dimReduction(nbr_pc);
		}

		/* Reduce the datamatrix  */
		Score=X.times(CoeffReduced);
		X_Reduced=Score.times(CoeffReduced.transpose());

		segmentCounter++;
		return X_Reduced;
	}
	
	private void calcKovarianceMatrix(Matrix X) {						// Berechnen der Kovarianze Matrix
		double column = (X.getColumnDimension()-1);
		Cov = X.transpose();
		Cov = Cov.times(X);
		Cov = Cov.times(1/column);
	}

	private void dimReduction(int nbr_pc) {						// Reduziert die Hauptkomponente auf die Anzahl nbr_pc
		EigenVec=Eig.getV();
		int row=EigenVec.getRowDimension();
		int column=EigenVec.getColumnDimension();
		CoeffReduced=EigenVec.getMatrix(0,(column-1),(row-nbr_pc),(row-1));	
	}


	/*In calc_numberPC wird die minimale Anzahl Komponente berechnet mit welchen eine Gesamtstreunung
	 * von Alpha erreicht wird. Wichtig zu beachten ist, das die Eigenwerden wie folgt in der Matrix
	 * angeordnet sind:
	 * 
	 * 		kleinster Eigenwert		0				0				0
	 * 		0						Eigenwert		0				0
	 * 		0						0				Eigenwert		0
	 *		0						0				0				gr�sster Eigenwert
	 *
	 *Die Berechnung beginnt mit den gr�ssten Eignewerten und arbeitet sich in Richtung kleine vor.
	 * 
	 */

	private void calc_numberPC() {
		int length_Lat = Eig.getD().getRowDimension();
		
		for (int i = length_Lat-1; i >=0; i--) {
			if(Eig.getD().getMatrix(i, length_Lat-1,i,length_Lat-1).trace()/Eig.getD().trace()>=Alpha) {
				nbr_pc = length_Lat-i;
				return;
			}
		}
			
	}

	public void reset(){
		segmentCounter = 0;
	}

}