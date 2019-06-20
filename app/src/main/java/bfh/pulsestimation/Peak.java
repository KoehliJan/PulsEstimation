package bfh.pulsestimation;

public class Peak {

	private double Amplitude;
	private double RR_Interval;

	private double sampleNr;
	private double timeStamp;

	private double heartRate;
	
	public Peak(double amp, double interval, double t, int n) {

		Amplitude = amp;
		RR_Interval = interval;
		timeStamp = t;
		sampleNr = n;

		heartRate = rr_to_heartrate(interval);

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
		this.heartRate = rr_to_heartrate(RR_Interval);
	}

	public double getHeartRate(){
		return heartRate;
	}


	private static double rr_to_heartrate(double rrInterval){

		/* Returns the heartrate in bpm*/
		if (rrInterval != 0){
			return 60 / rrInterval;
		}else{
			return 0;
		}

	}


}
