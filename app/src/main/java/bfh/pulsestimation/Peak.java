package bfh.pulsestimation;

public class Peak {

	private double Amplitude;
	private double RR_Interval;

	private double sampleNr;
	private double timeStamp;
	
	public Peak(double amp, double interval, double t, int n) {

		Amplitude = amp;
		RR_Interval = interval;
		timeStamp = t;
		sampleNr = n;

	}


	public double getAmplitude() {
		return Amplitude;
	}

	public void setAmplitude(double amplitude) {
		Amplitude = amplitude;
	}

	public double getSampleNr() {
		return sampleNr;
	}

	public void setSampleNr(double sampleNr) {
		this.sampleNr = sampleNr;
	}

	public double getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(double timeStamp) {
		this.timeStamp = timeStamp;
	}

	public double getRR_Interval() {
		return RR_Interval;
	}

	public void setRR_Interval(double RR_Interval) {
		this.RR_Interval = RR_Interval;
	}
}
