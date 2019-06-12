package bfh.pulsestimation;

import android.util.Log;

import Jama.Matrix;

public class Segment {

	private Matrix X;
	private int[] sampleCount;

	Segment(Matrix X_in, int[] counts) {
		X = X_in.copy();						// Erstellen der Daten Matrix
		sampleCount = counts;
	}

	public int getSampleNumber(int i){
		return sampleCount[i];
	}

	public Jama.Matrix getChannel( int ch){
		//Log.v("Segment: ", "Segment length: " + X.getRowDimension());
		return X.getMatrix(0,X.getRowDimension()-1,ch,ch);
	}

	public XYSample[] getSamples(int channel){

		XYSample[] samples = new XYSample[X.getRowDimension()];

		for (int i = 0; i < X.getRowDimension(); i++){
			samples[i] = new XYSample(sampleCount[i], X.get(i,channel));
		}
		//Log.v("Segment: ", "Segment length: " + samples.length);
		return samples;
	}
	

}