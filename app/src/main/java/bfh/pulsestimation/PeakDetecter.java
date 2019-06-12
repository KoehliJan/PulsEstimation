package bfh.pulsestimation;

import android.app.Activity;

import Jama.Matrix;


public class PeakDetecter {

	MainActivity activity;
	Jama_Filter DD;
	double init;
	int AnzPeaks;
	int fa;
	double qrs_range_time;
	int qrs_range_length;
	int grs_window_length;

	Matrix Seg_DD;

	Matrix OVERLAP_DD_SQ;
	Matrix OVERLAP_EKG;
	Matrix O_Matrix;										// 0 Matrix zum l�schen der Fenster
	
	double[] QRS_Peaks_Tresh = {0,0,0,0,0};

	Matrix QRS_Peaks_Alt;
	Matrix R_Peaks_Sort;			
	
	PeakDetecter(MainActivity a, int l_sig, int samp_f) {

		fa = samp_f;										// Samplingfrequenz zum berechnen des Zeitstempels
		AnzPeaks = -1;
		init = 7;
		
		qrs_range_time=0.09;
		qrs_range_length = (int) (qrs_range_time*fa);			
		grs_window_length = 2*qrs_range_length+1;			// QRS Fenstergr�sse	
		
		DD = new Jama_Filter(loadFilterCoeff(a,R.array.dd_num),l_sig,1);
		
		O_Matrix = new Matrix(grs_window_length,1);			
		OVERLAP_EKG = new Matrix(grs_window_length,1);
		OVERLAP_DD_SQ = new Matrix(grs_window_length,1);
		QRS_Peaks_Alt = new Matrix(10,2);
		R_Peaks_Sort = new Matrix(10,2);
		
		
		
	}

	public void process(Segment segment) {

		boolean search = true;

		Matrix x = segment.getChannel(0);

		// Double derivation
		Seg_DD = DD.filter(x);

		// Quadration
		Matrix Seg_DD_SQ = Seg_DD.arrayTimes(Seg_DD);

		/* Setup Workermatrix for double derivated and squared Signal */
		Matrix Seg_DD_SQ_Proc = new Matrix((x.getRowDimension()+grs_window_length),1);
		Seg_DD_SQ_Proc.setMatrix(0, OVERLAP_DD_SQ.getRowDimension()-1, 0, OVERLAP_DD_SQ.getColumnDimension()-1, OVERLAP_DD_SQ);
		Seg_DD_SQ_Proc.setMatrix(OVERLAP_DD_SQ.getRowDimension(), OVERLAP_DD_SQ.getRowDimension()+Seg_DD_SQ.getRowDimension()-1, 0, Seg_DD_SQ.getColumnDimension()-1, Seg_DD_SQ);

		/* Set Overlap */
		OVERLAP_DD_SQ.setMatrix(0, grs_window_length-2, 0, 0, Seg_DD_SQ.getMatrix(Seg_DD_SQ.getRowDimension()-grs_window_length+1, Seg_DD_SQ.getRowDimension()-1, 0, 0));		//-grs_window_length-1
		
		
		/* Setup Ekg Signal */
		Matrix Seg_EKG_Proc = new Matrix((x.getRowDimension()+grs_window_length),1);
		Seg_EKG_Proc.setMatrix(0, OVERLAP_EKG.getRowDimension()-1, 0, OVERLAP_EKG.getColumnDimension()-1, OVERLAP_EKG);
		Seg_EKG_Proc.setMatrix(OVERLAP_EKG.getRowDimension(), OVERLAP_EKG.getRowDimension()+x.getRowDimension()-1, 0, x.getColumnDimension()-1, x);

		/* Set Overlap */
		OVERLAP_EKG.setMatrix(0, grs_window_length-2, 0, 0, Seg_EKG_Proc.getMatrix(Seg_EKG_Proc.getRowDimension()-grs_window_length+1, Seg_EKG_Proc.getRowDimension()-1, 0, 0));

		Matrix QRS_Peaks_New = new Matrix(10,2);
		Matrix R_Peaks_New = new Matrix(10,2);
		
		int i = -1;
		
		if(init>0) {
			// Initialisierung des Treshold. Es werden nur die Werte ab t=0.1 f�r die Berechunung betrachtet
			// Wir suchen den gr�ssten Wert f�r die erste Berechnung des Treshold
			if(init==7) {
				double[][] Max=max(Seg_DD_SQ.getMatrix((int) (0.1*1/fa), Seg_DD_SQ.getRowDimension()-1, 0, 0),0);
				for(int k=0 ; k<5 ; k++) {
					QRS_Peaks_Tresh[k]=Max[0][0];
				}
			}else {
				double[][] Max=max(Seg_DD_SQ,0);
				if(Max[0][0] > QRS_Peaks_Tresh[0]) {
					for(int k=0 ; k<5 ; k++) {
						QRS_Peaks_Tresh[k]=Max[0][0];
						QRS_Peaks_Alt.set(k, 0, Max[0][0]);
						QRS_Peaks_Alt.set(k, 1, 1);
					}
				}
			}
			init=init-(x.getRowDimension()/fa);								// Init-Timer aktualisieren
		}else {
			init=-1;
			double treshold=0.3* mean(QRS_Peaks_Tresh);						// Treshold berechnen
			while(search) {
				double[][] Max=max(Seg_DD_SQ_Proc,0);
				if((Max[0][0]<treshold)||(i==9)) {							
					search=false;	
					
				}else {
					
					
					if(Max[0][1]<qrs_range_length) {						// Wurde der Peak im letzten Segment erkannt?
						// Set magnitudes of this QRS Region to 0
						Seg_DD_SQ_Proc.setMatrix(0, (int) (Max[0][1])+qrs_range_length, 0, Seg_DD_SQ_Proc.getColumnDimension()-1, new Matrix((int) (Max[0][1])+qrs_range_length+1,1) );

					}else if(Max[0][1]>Seg_DD_SQ_Proc.getRowDimension()-qrs_range_length-1){		// Wird der Peak im n�chsten Segment erkennt?
						// Set magnitudes of this QRS Region to 0
						Seg_DD_SQ_Proc.setMatrix((int) (Max[0][1])-qrs_range_length, Seg_DD_SQ_Proc.getRowDimension()-1, 0, Seg_DD_SQ_Proc.getColumnDimension()-1,new Matrix(Seg_DD_SQ_Proc.getRowDimension()-(int) (Max[0][1])+qrs_range_length,1) );

					}else {	// Befindet sich der Peak im aktuallen Segment
						i++;
						
						// Store detected qrs peak in grs_peaks
						QRS_Peaks_New.setMatrix(i, i, 0, 1, new Matrix(Max));
						
						// Set magnitudes of this QRS Region to 0
						Seg_DD_SQ_Proc.setMatrix((int) (Max[0][1])-qrs_range_length, (int) (Max[0][1])+qrs_range_length, 0, Seg_DD_SQ_Proc.getColumnDimension()-1,O_Matrix );

						// Detect the R peak in the original Signal in this Interval

						/* Get the Ecg in this Peak region */
						Jama.Matrix ecg_peak_region = Seg_EKG_Proc.getMatrix((int) (Max[0][1])-qrs_range_length, (int) (Max[0][1])+qrs_range_length, 0, Seg_EKG_Proc.getColumnDimension()-1);

						/* Get the maximum of this region */
						Matrix R_Peak = new Matrix ( max(ecg_peak_region,0) );

						//R_Peak.set(0, 1, R_Peak.get(0, 1)+(int) (Max[0][1])-grs_window_length-qrs_range_length+1);	//qrs_range_length);
						R_Peak.set(0, 1, R_Peak.get(0, 1)+(int) (Max[0][1])-grs_window_length-qrs_range_length+1);	//qrs_range_length);

						
						// Store Peak in R_Peak Array
						R_Peaks_New.setMatrix(i, i, 0, 1, R_Peak);

					}
					
					
				}			
			}
			
		
		// Speichern der QRS_Pekas f�r die Berechnung des neuen  Treshhold.
		// 1. Speichern der neuen Peaks
		int j;
		Matrix QRS_Peaks_Calc=QRS_Peaks_New.copy();
		for(j=0 ;(j<=i)&&(j<5);j++) {
			double[][] Max=max(QRS_Peaks_Calc,1);
			QRS_Peaks_Calc.set((int)Max[0][1], 1, 0);
			QRS_Peaks_Tresh[4-j]=QRS_Peaks_Calc.get((int)Max[0][1], 0);
			
		}
		// 2. Auff�llen mit den Peaks aus dem letzten Segment
		for(int k=j ;k<=4;k++) {
			
			double[][] Max=max(QRS_Peaks_Alt,1);
			QRS_Peaks_Alt.set((int)Max[0][1], 1, 0);
			QRS_Peaks_Tresh[4-k]=QRS_Peaks_Alt.get((int)Max[0][1], 0);
			
		}
		
		R_Peaks_Sort= sort(R_Peaks_New,1,i);			// Sortieren nach der Zeitlichen Reienfolge
		AnzPeaks=i;										// Anzahl neu detectierter Peaks abspeichern
		QRS_Peaks_Alt=QRS_Peaks_New.copy();				// Peaks ans n�chste Segment weitergeben
		
		
		}
		
	}
	// Funktion zum berechnen des Mittelwertes
	public double mean(double[] x) {
		double mean=0;
		for(int i=0 ; i<x.length ; i++) {
			mean = mean + x[i];
		}
		return mean/x.length;
	}
	// Funktion zum berechnen des absoluten Maximums (Spallte -> Absolute Arrayposition 0-...)
	// max gibt die Maximale Amplitude Max(0,0) und die Position Max(0,1) zur�ck.
	public double[][] max (Matrix x,int Spalte){
		double[][] Max= {{0,0}};

		for (int i=0 ; i<x.getRowDimension() ; i++) {
			if((Math.abs(Max[0][0]))<(Math.abs(x.get(i, Spalte)))) {
				Max[0][0]=x.get(i, Spalte);
				Max[0][1]=i;
			}
		}
		return Max;
	}
	// Funktion zum Sortieren der Peaks
	public Matrix sort(Matrix x,int Spalte,int Numb) {
		// Num = Anzahl Elemente - 1 (Arraypositionen)
		Matrix x_out=x.copy();
		for(int i=Numb ; i>=0 ; i--) {
			double[][] Max=max(x,Spalte);
			
			x_out.set(i, 0, x.get((int)Max[0][1], 0));
			x_out.set(i, 1, x.get((int)Max[0][1], 1));
			
			x.set((int)Max[0][1], 1, 0);
		}
		return x_out;
	}

	private double[][] loadFilterCoeff(Activity a, int id){
		/* Load Filter Coefficient */
		String[] num_Strings = a.getResources().getStringArray(id);
		int nCoeff = num_Strings.length;
		double[][]  Num = new double[1][nCoeff];
		for(int i =0; i < nCoeff; i++){
			// Store Coefficients in flipped Order in the double Array
			Num[0][nCoeff-1 -i] = Double.parseDouble(num_Strings[i]);
		}
		return Num;
	}
	
	public Matrix getR_Peaks(){
		return R_Peaks_Sort;
	}
	public double[][] getR_Peaks_double(){
		return R_Peaks_Sort.getArray();
	}
	public double[] getTreshold() {
		return QRS_Peaks_Tresh;
	}
	public double[][] getQRS_Alt_double() {
		return QRS_Peaks_Alt.getArray();
	}
	public int getAnzPeaks(){
		return AnzPeaks;
	}


}
