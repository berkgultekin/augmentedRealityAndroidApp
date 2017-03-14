package com.lycha.example.augmentedreality;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Location;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraViewActivity extends Activity implements
		SurfaceHolder.Callback, OnLocationChangedListener, OnAzimuthChangedListener{

	private Camera mCamera;
	private SurfaceHolder mSurfaceHolder;
	private boolean isCameraviewOn = false;
	private AugmentedPOI mPoi;

	private double mAzimuthReal = 0;
	private double mAzimuthTeoretical = 0;
	private static double AZIMUTH_ACCURACY = 15;
	private double mMyLatitude = 0;
	private double mMyLongitude = 0;
    private double mMyDistance = 0;

	private MyCurrentAzimuth myCurrentAzimuth;
	private MyCurrentLocation myCurrentLocation;

	TextView descriptionTextView;
    TextView teamMemberInfo;
	ImageView pointerIcon;
	ImageView leftArrow;
	ImageView rightArrow;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera_view);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		setupListeners();
		setupLayout();
		setAugmentedRealityPoint();
	}

	private void setAugmentedRealityPoint() {
		mPoi = new AugmentedPOI(
				"Ankara",
				"Ankara Gölbaşı",
				39.893953, 32.785954
		);
	}

	public double calculateTeoreticalAzimuth() {
		double dX = mPoi.getPoiLatitude() - mMyLatitude;
		double dY = mPoi.getPoiLongitude() - mMyLongitude;

		double phiAngle;
		double tanPhi;
		double azimuth = 0;

		tanPhi = Math.abs(dY / dX);
		phiAngle = Math.atan(tanPhi);
		phiAngle = Math.toDegrees(phiAngle);

		if (dX > 0 && dY > 0) { // I quater
			return azimuth = phiAngle;
		} else if (dX < 0 && dY > 0) { // II
			return azimuth = 180 - phiAngle;
		} else if (dX < 0 && dY < 0) { // III
			return azimuth = 180 + phiAngle;
		} else if (dX > 0 && dY < 0) { // IV
			return azimuth = 360 - phiAngle;
		}

		return phiAngle;
	}
	
	private List<Double> calculateAzimuthAccuracy(double azimuth) {
		double minAngle = azimuth - AZIMUTH_ACCURACY;
		double maxAngle = azimuth + AZIMUTH_ACCURACY;
		List<Double> minMax = new ArrayList<Double>();

		if (minAngle < 0)
			minAngle += 360;

		if (maxAngle >= 360)
			maxAngle -= 360;

		minMax.clear();
		minMax.add(minAngle);
		minMax.add(maxAngle);

		return minMax;
	}

	private boolean isBetween(double minAngle, double maxAngle, double azimuth) {
		if (minAngle > maxAngle) {
			if (isBetween(0, maxAngle, azimuth) && isBetween(minAngle, 360, azimuth))
				return true;
		} else {
			if (azimuth > minAngle && azimuth < maxAngle)
				return true;
		}
		return false;
	}

	private void updateDescription() {
		descriptionTextView.setText(" AzimuthTeoretical: " + mAzimuthTeoretical +
                "\n AzimuthReal: " + mAzimuthReal +
                "\n Latitude: " + mMyLatitude + "  Longitude: " + mMyLongitude );
	}

	@Override
	public void onLocationChanged(Location location) {
		mMyLatitude = location.getLatitude();
		mMyLongitude = location.getLongitude();
		mAzimuthTeoretical = calculateTeoreticalAzimuth();
		Toast.makeText(this,"latitude: " + location.getLatitude()
                            +" longitude: " +location.getLongitude(), Toast.LENGTH_SHORT).show();
		updateDescription();
	}

	@Override
	public void onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo) {

        double filteredValue = azimuthChangedFrom + ((azimuthChangedTo - azimuthChangedFrom) / (8 / 1));
        //filteredValue = oldValue + (newValue - oldValue) / (smoothing / timeSinceLastUpdate)

		mAzimuthReal = filteredValue;
		mAzimuthTeoretical = calculateTeoreticalAzimuth();

		pointerIcon = (ImageView) findViewById(R.id.icon);
		leftArrow = (ImageView) findViewById(R.id.left_arrow);
		rightArrow = (ImageView) findViewById(R.id.right_arrow);
        teamMemberInfo = (TextView) findViewById(R.id.teamMemberInfo);

		double minAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(0);
		double maxAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(1);

		if (isBetween(minAngle, maxAngle, mAzimuthReal)) {
			pointerIcon.setVisibility(View.VISIBLE);
			leftArrow.setVisibility(View.INVISIBLE);
			rightArrow.setVisibility(View.INVISIBLE);
		} else {
			double min = minAngle - 175;
			double max = maxAngle + 175;
            teamMemberInfo.setVisibility(View.INVISIBLE);
			if(min < 0) {
				double min1 = min + 360;

                if ((min1 < mAzimuthReal && 360 > mAzimuthReal) ||
                    (minAngle < mAzimuthReal && 0 < mAzimuthReal)) {
					pointerIcon.setVisibility(View.INVISIBLE);
					leftArrow.setVisibility(View.INVISIBLE);
					rightArrow.setVisibility(View.VISIBLE);
				}
                else{
                    pointerIcon.setVisibility(View.INVISIBLE);
                    leftArrow.setVisibility(View.VISIBLE);
                    rightArrow.setVisibility(View.INVISIBLE);
                }
			}
            else if (min >= 0){
                if (mAzimuthReal < minAngle && mAzimuthReal > min) {
                    pointerIcon.setVisibility(View.INVISIBLE);
                    leftArrow.setVisibility(View.INVISIBLE);
                    rightArrow.setVisibility(View.VISIBLE);
                }
                else{
                    pointerIcon.setVisibility(View.INVISIBLE);
                    leftArrow.setVisibility(View.VISIBLE);
                    rightArrow.setVisibility(View.INVISIBLE);
                }
            }

			if(max > 360) {
                double max1 = max - 360;
				if ((max1 > mAzimuthReal && 0 < mAzimuthReal) ||
                    (maxAngle < mAzimuthReal && 360 > mAzimuthReal)) {
					pointerIcon.setVisibility(View.INVISIBLE);
					leftArrow.setVisibility(View.VISIBLE);
					rightArrow.setVisibility(View.INVISIBLE);
				}
                else{
                    pointerIcon.setVisibility(View.INVISIBLE);
                    leftArrow.setVisibility(View.INVISIBLE);
                    rightArrow.setVisibility(View.VISIBLE);
                }
			}
            else if (max <= 360){
                if ( mAzimuthReal > maxAngle && mAzimuthReal < max) {
                    pointerIcon.setVisibility(View.INVISIBLE);
                    leftArrow.setVisibility(View.VISIBLE);
                    rightArrow.setVisibility(View.INVISIBLE);
                }
                else{
                    pointerIcon.setVisibility(View.INVISIBLE);
                    leftArrow.setVisibility(View.INVISIBLE);
                    rightArrow.setVisibility(View.VISIBLE);
                }
            }

		}

		updateDescription();
	}

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

	@Override
	protected void onStop() {
		myCurrentAzimuth.stop();
		myCurrentLocation.stop();
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		myCurrentAzimuth.start();
		myCurrentLocation.start();
	}

	private void setupListeners() {
		myCurrentLocation = new MyCurrentLocation(this);
		myCurrentLocation.buildGoogleApiClient(this);
		myCurrentLocation.start();

		myCurrentAzimuth = new MyCurrentAzimuth(this, this);
		myCurrentAzimuth.start();
	}

	private void setupLayout() {
		descriptionTextView = (TextView) findViewById(R.id.cameraTextView);

		getWindow().setFormat(PixelFormat.UNKNOWN);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraview);
		mSurfaceHolder = surfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		if (isCameraviewOn) {
			mCamera.stopPreview();
			isCameraviewOn = false;
		}

		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(mSurfaceHolder);
				mCamera.startPreview();
				isCameraviewOn = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		mCamera.setDisplayOrientation(90);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
		isCameraviewOn = false;
	}

    public void onClick(View v) {
        mMyDistance = distance(mMyLatitude, mMyLongitude, 39.893953, 32.785954);
        teamMemberInfo.setText("NAME: BERK GÜLTEKİN\n" +
                                "CALCULATED DISTANCE: " + mMyDistance);
        teamMemberInfo.setVisibility(View.VISIBLE);
    }
}
