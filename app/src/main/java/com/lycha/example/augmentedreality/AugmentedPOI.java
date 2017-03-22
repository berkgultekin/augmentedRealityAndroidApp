package com.lycha.example.augmentedreality;

public class AugmentedPOI {
	private int mId;
	private String mName;
	private double mLatitude;
	private double mLongitude;
	
	public AugmentedPOI(String newName,
						double newLatitude, double newLongitude) {
		this.mName = newName;
        this.mLatitude = newLatitude;
        this.mLongitude = newLongitude;
	}
	
	public int getPoiId() {
		return mId;
	}
	public void setPoiId(int poiId) {
		this.mId = poiId;
	}
	public String getPoiName() {
		return mName;
	}
	public void setPoiName(String poiName) {
		this.mName = poiName;
	}
	public double getPoiLatitude() {
		return mLatitude;
	}
	public void setPoiLatitude(double poiLatitude) {
		this.mLatitude = poiLatitude;
	}
	public double getPoiLongitude() {
		return mLongitude;
	}
	public void setPoiLongitude(double poiLongitude) {
		this.mLongitude = poiLongitude;
	}
}
