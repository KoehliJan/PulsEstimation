package bfh.pulsestimation;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class PCAnalyser{


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

		calcKovarianceMatrix(X);									// Kovarianze Matrix berechnen
		Eig = Cov.eig();											// Eigenwerte und Eigenvektoren berechnen
		Numb_PC(Alpha);
		dimReduction(nbr_pc);										// Reduzieren der Hauptkomponente auf nbr_pc
		Score=X.times(CoeffReduced);								// Projezieren der Matrix auf die Hauptkomponente
		X_Reduced=Score.times(CoeffReduced.transpose());			// R�cktransformation auf die Matrix

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
	/*In Numb_PC wird die minimale Anzahl Komponente berechnet mit welchen eine Gesamtstreunung
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

	private void Numb_PC(double Alpha) {
		int length_Lat = Eig.getD().getRowDimension();
		
		for (int i = length_Lat-1; i >=0; i--) {
			if(Eig.getD().getMatrix(i, length_Lat-1,i,length_Lat-1).trace()/Eig.getD().trace()>=Alpha) {
				nbr_pc = length_Lat-i;
				return;
			}
		}
			
	}
	
	public int getNbr_PC() {
		return nbr_pc;
	}


	public double[][] getKovarianceMatrix(){
		return Cov.getArray();
	}
	public double[][] getPrincipalComponent() {
		// TODO Auto-generated method stub
		return Eig.getV().getArray();
	}
	public double[][] getLatent() {
		// TODO Auto-generated method stub
		return Eig.getD().getArray();
	}
	public double[][] getReducedPrincipalComponent() {
		// TODO Auto-generated method stub
		return CoeffReduced.getArray();
	}
	public double[][] getScore() {
		// TODO Auto-generated method stub
		return Score.getArray();
	}
	public double[][] getReducedX_double() {
		// TODO Auto-generated method stub
		return X_Reduced.getArray();
	}
	public Matrix getReducedX() {
		// TODO Auto-generated method stub
		return X_Reduced;
	}
	

	
}