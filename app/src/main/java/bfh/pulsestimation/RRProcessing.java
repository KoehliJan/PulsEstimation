package bfh.pulsestimation;

import java.util.ArrayList;

import Jama.Matrix;

public class RRProcessing {

	ArrayList<Peak> Peaks;
	
	double RR_Ref;
	double RR_Sum;
	double RR_Sum2;
	double RRi;
	
	int Numb_RR_Interval;
	int Numb_valid;
	int Numb_Seg;
	int x_row;


	
	public RRProcessing(int x_row_dimension) {
		
		x_row = x_row_dimension;
		reset();
	}
	
	public void process(Matrix R_Peaks_New,int Numb_New_R_Peaks) {

		// Berechnen der RR_Intervale und der R_Peaks
		for(int i=0 ; i<= Numb_New_R_Peaks ; i++) {

			double amp =  R_Peaks_New.get(i, 0);
			double rrInterval = ( (R_Peaks_New.get(i, 1)+(x_row * Numb_Seg)) /256) - Peaks.get(Peaks.size()-1).getTimeStamp();
			double timestamp = ( R_Peaks_New.get(i, 1)+(x_row * Numb_Seg)) / 256;

			int sampleNumber = (int) R_Peaks_New.get(i, 1) + (int) (x_row*Numb_Seg);

			Peaks.add(new Peak(amp, rrInterval, timestamp, sampleNumber));
		}
		
		// Jeder Peak �berpr�fen
		for(int i = Numb_RR_Interval; i <= Peaks.size()-1 ; i++) {

			RRi = Peaks.get(i).getRR_Interval();					// Aktueller Peak

			if(Peaks.size() >= 5) {

				RR_Ref = (0.2* Peaks.get(i-3).getRR_Interval()) + (0.3* Peaks.get(i-2).getRR_Interval()) + (0.5* Peaks.get(i-1).getRR_Interval());
				RR_Sum = RRi;
				RR_Sum2 = RRi + Peaks.get(i-1).getRR_Interval();

				/* Interval is to small */
				if( RRi < (0.7*RR_Ref) || RRi < 0.2) {

					for(int k = i+1; k< Peaks.size()-2 ; k++) {

						if( RRi < (0.7*RR_Ref) || RRi < 0.2) {

							/* Summation of RR intervals*/
							RR_Sum = RR_Sum + Peaks.get(k).getRR_Interval();

						}else {
							if( Math.abs(RR_Sum - RR_Ref) < (0.3*RR_Ref) ) {
								double RR_new;

								if( Math.abs(RR_Sum + Peaks.get(k+1).getRR_Interval() - RR_Ref) < Math.abs(RR_Sum - RR_Ref)) {
									RR_new = RR_Sum + Peaks.get(k+1).getRR_Interval();
									Peaks.remove(k);
								}else {
									RR_new = RR_Sum;
								}

								if(Math.abs(RR_new - RR_Ref) < Math.abs(RR_Sum2 - RR_Ref)) {
									Peaks.get(k).setRR_Interval(RR_new);			// speichern des neu berechneten RR-Intervals
								}else {
									Peaks.get(k).setRR_Interval(RR_Sum2);		// speichern des neu berechneten RR-Intervals
								}
								
								for (int l=i ; l<k ; l++) {								// entfernen der zukleinen peaks
									Peaks.remove(l);
								}

								Numb_RR_Interval++;
								Numb_valid++;
								break;
							}else {
								if( Math.abs( RR_Sum2 - RR_Ref) < (0.3 * RR_Ref)) {		
									Peaks.get(k).setRR_Interval(RR_Sum2);		// speichern des neu berechneten RR-Intervals
									for (int l=i ; l<k ; l++) {
										Peaks.remove(l);							// entfernen der zukleinen peaks
									}
									Numb_RR_Interval++;
									Numb_valid++;
									break;
								}else {	// kein passendes Interval wurde berechnet
									// Check manually
									System.out.print("Check manually \n");
									
									Peaks.remove(i);
									break;
								}
							}
						}
					}
				}
				else if( RRi > (1.8*RR_Ref)) {		// Ist das RR-Intervall zu gross
					Peaks.remove(i);
				}
				else {
					// RR is fine
					Numb_RR_Interval++;
					Numb_valid++;
				}
			}else {
				break;
			}


		}
		Numb_Seg++;
	}
	
	public XYSample[] getPeaks(){
		XYSample[] samples = new XYSample[Peaks.size()];

		for (int i = 0; i < samples.length; i++){
			samples[i] = new XYSample(Peaks.get(i).getSampleNr(), Peaks.get(i).getAmplitude());
		}

		return samples;
	}

	public XYSample[] getHeartRate(){

		if (Numb_valid > 0){

			XYSample[] samples = new XYSample[Numb_valid];

			for (int i = 0; i < samples.length; i++){
				samples[i] = new XYSample(Peaks.get(i).getSampleNr(), Peaks.get(i).getHeartRate());
			}
			return samples;

		}else{

			return new XYSample[0];
		}





	}

	public void reset(){

		Numb_RR_Interval = 4;
		Numb_valid = -1;
		Numb_Seg = 0;

		Peaks = new ArrayList<>();
		Peaks.add(new Peak(0,0,0,0));

	}

}
