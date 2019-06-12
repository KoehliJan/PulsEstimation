package bfh.pulsestimation;

import android.util.Log;

import java.util.ArrayList;

import Jama.Matrix;

public class RRDetecter {

	ArrayList<Peak> Peak_Element;
	
	double RR_Ref;
	double RR_Sum;
	double RR_Sum2;
	double RRi;
	
	int Numb_RR_Interval;
	int Numb_Seg;
	int x_row;
	
	public RRDetecter(int x_row_dimension) {
		
		x_row = x_row_dimension;
		Numb_RR_Interval=4;
		Numb_Seg = 0;
		
		Peak_Element = new ArrayList<>();
		Peak_Element.add(new Peak(0,0,0,0));
	}
	
	public void process(Matrix R_Peaks_New,int Numb_New_R_Peaks) {

		// Berechnen der RR_Intervale und der R_Peaks
		for(int i=0 ; i<= Numb_New_R_Peaks ; i++) {

			double amp =  R_Peaks_New.get(i, 0);
			double rrInterval = ((R_Peaks_New.get(i, 1)+(x_row*Numb_Seg))/256) - Peak_Element.get(Peak_Element.size()-1).getTimeStamp();
			double timestamp = (R_Peaks_New.get(i, 1)+(x_row*Numb_Seg)) / 256;

			int sampleNumber = (int) R_Peaks_New.get(i, 1) + (int) (x_row*Numb_Seg);

			Log.v("RR detector","sampleNumber: "+ sampleNumber +" ");

			Peak_Element.add(new Peak(amp, rrInterval, timestamp, sampleNumber));
		}
		
		// Jeder Peak �berpr�fen
		for( int i=Numb_RR_Interval ; i<= Peak_Element.size()-1 ; i++) {
			RRi = Peak_Element.get(i).getRR_Interval();					// Aktueller Peak
			if(Peak_Element.size()>=5) {
				RR_Ref = (0.2*Peak_Element.get(i-3).getRR_Interval()) + (0.3*Peak_Element.get(i-2).getRR_Interval()) + (0.5*Peak_Element.get(i-1).getRR_Interval());
				RR_Sum = RRi;
				RR_Sum2 = RRi+Peak_Element.get(i-1).getRR_Interval();

				
				if( RRi < (0.7*RR_Ref) || RRi < 0.2) {					// Ist das RR-Intervall zu klein?
					for(int k=i+1 ; k<Peak_Element.size()-2 ; k++) {	
						if( RRi < (0.7*RR_Ref) || RRi < 0.2) {
							RR_Sum = RR_Sum + Peak_Element.get(k).getRR_Interval();		// Aufsummieren der Peaks bis passendes Intervall gefunden wird
						}else {
							if( Math.abs(RR_Sum - RR_Ref) < (0.3*RR_Ref)) {				
								double RR_new;
								if( Math.abs(RR_Sum + Peak_Element.get(k+1).getRR_Interval() - RR_Ref) < Math.abs(RR_Sum - RR_Ref)) {
									RR_new = RR_Sum + Peak_Element.get(k+1).getRR_Interval();
									Peak_Element.remove(k);
								}else {
									RR_new = RR_Sum;
								}

								if(Math.abs(RR_new - RR_Ref) < Math.abs(RR_Sum2 - RR_Ref)) {
									Peak_Element.get(k).setRR_Interval(RR_new);			// speichern des neu berechneten RR-Intervals					
								}else {
									Peak_Element.get(k).setRR_Interval(RR_Sum2);		// speichern des neu berechneten RR-Intervals		
								}
								
								for (int l=i ; l<k ; l++) {								// entfernen der zukleinen peaks
									Peak_Element.remove(l);
								}
								Numb_RR_Interval++;
								break;
							}else {
								if( Math.abs( RR_Sum2 - RR_Ref) < (0.3 * RR_Ref)) {		
									Peak_Element.get(k).setRR_Interval(RR_Sum2);		// speichern des neu berechneten RR-Intervals
									for (int l=i ; l<k ; l++) {
										Peak_Element.remove(l);							// entfernen der zukleinen peaks
									}
									Numb_RR_Interval++;
									break;
								}else {	// kein passendes Interval wurde berechnet
									// Check manually
									System.out.print("Check manually \n");
									
									Peak_Element.remove(i);
									break;
								}
							}
						}
					}
				}
				else if( RRi > (1.8*RR_Ref)) {		// Ist das RR-Intervall zu gross
					Peak_Element.remove(i);			
				}
				else {
					// RR is fine
					Numb_RR_Interval++;
				}
			}else {
				break;
			}
		}
		Numb_Seg++;
	}
	
	public XYSample[] getPeaks(){
		XYSample[] samples = new XYSample[Peak_Element.size()];

		for (int i = 0; i < samples.length; i++){
			samples[i] = new XYSample(Peak_Element.get(i).getSampleNr(), Peak_Element.get(i).getAmplitude());
		}

		return samples;
	}

}
