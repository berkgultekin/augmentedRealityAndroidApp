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
	private AugmentedPOI mPoi2;

	private double mAzimuthReal = 0;
	private double mAzimuthTeoretical = 0;
    private double mAzimuthTeoretical2 = 0;
	private static double AZIMUTH_ACCURACY = 15;
	private double mMyLatitude = 0;
	private double mMyLongitude = 0;
    private double mMyDistance = 0;
    private int myFlag = 0;

	private MyCurrentAzimuth myCurrentAzimuth;
	private MyCurrentLocation myCurrentLocation;

	TextView descriptionTextView;
    TextView teamMemberInfo;
	TextView leftArrowText;
    TextView leftArrowText2;
	TextView rightArrowText;
    TextView rightArrowText2;

	ImageView pointerIcon;
	ImageView leftArrow;
    ImageView leftArrow2;
	ImageView rightArrow;
    ImageView rightArrow2;

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
				"Berk",
				39.797600, 32.810727
		);

		mPoi2 = new AugmentedPOI(
				"Ali",
				39.799435, 32.824124
		);

		/*mPoi3 = new AugmentedPOI(
				"Veli",
				39.797600, 32.810727
		);

		mPoi4 = new AugmentedPOI(
				"Ayse",
				39.797600, 32.810727
		);*/
	}

	public double calculateTeoreticalAzimuth(AugmentedPOI thePoi) {
		double dX = thePoi.getPoiLatitude() - mMyLatitude;
		double dY = thePoi.getPoiLongitude() - mMyLongitude;

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
		mAzimuthTeoretical = calculateTeoreticalAzimuth(mPoi);
        mAzimuthTeoretical2 = calculateTeoreticalAzimuth(mPoi2);
		Toast.makeText(this,"latitude: " + location.getLatitude()
                            +" longitude: " +location.getLongitude(), Toast.LENGTH_SHORT).show();
		updateDescription();
	}

	@Override
	public void onAzimuthChanged(float azimuthChangedFrom, float azimuthChangedTo) {

        //double filteredValue = azimuthChangedFrom + ((azimuthChangedTo - azimuthChangedFrom) / (8 / 1));

		mAzimuthReal = azimuthChangedTo;
		mAzimuthTeoretical = calculateTeoreticalAzimuth(mPoi);
        mAzimuthTeoretical2 = calculateTeoreticalAzimuth(mPoi2);

		pointerIcon = (ImageView) findViewById(R.id.icon);

		leftArrow = (ImageView) findViewById(R.id.left_arrow);
        leftArrow2 = (ImageView) findViewById(R.id.left_arrow2);
		rightArrow = (ImageView) findViewById(R.id.right_arrow);
        rightArrow2 = (ImageView) findViewById(R.id.right_arrow2);

        teamMemberInfo = (TextView) findViewById(R.id.teamMemberInfo);

		leftArrowText = (TextView) findViewById(R.id.left_arrow_text);
        leftArrowText2 = (TextView) findViewById(R.id.left_arrow_text2);
		rightArrowText = (TextView) findViewById(R.id.right_arrow_text);
        rightArrowText2 = (TextView) findViewById(R.id.right_arrow_text2);

		double minAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(0);
		double maxAngle = calculateAzimuthAccuracy(mAzimuthTeoretical).get(1);

        double minAngle2 = calculateAzimuthAccuracy(mAzimuthTeoretical2).get(0);
        double maxAngle2 = calculateAzimuthAccuracy(mAzimuthTeoretical2).get(1);

        findIt(minAngle, maxAngle, mAzimuthReal);
        if(myFlag == 1 || myFlag == 2)
            findIt2(minAngle2, maxAngle2, mAzimuthReal);
        else
            findIt(minAngle2, maxAngle2, mAzimuthReal);

		updateDescription();
	}

	private void findIt(double minAngle, double maxAngle, double mAzimuthReal){
        if (isBetween(minAngle, maxAngle, mAzimuthReal)) {
            visibilitySetter(pointerIcon);
        } else {
            double min = minAngle - 175;
            double max = maxAngle + 175;
            teamMemberInfo.setVisibility(View.INVISIBLE);
            if(min < 0) {
                double min1 = min + 360;

                if ((min1 < mAzimuthReal && 360 > mAzimuthReal) ||
                        (minAngle < mAzimuthReal && 0 < mAzimuthReal)) {
                    visibilitySetter(rightArrow);
                }
                else{
                    visibilitySetter(leftArrow);
                }
            }
            else if (min >= 0){
                if (mAzimuthReal < minAngle && mAzimuthReal > min) {
                    visibilitySetter(rightArrow);
                }
                else{
                    visibilitySetter(leftArrow);
                }
            }

            if(max > 360) {
                double max1 = max - 360;
                if ((max1 > mAzimuthReal && 0 < mAzimuthReal) ||
                        (maxAngle < mAzimuthReal && 360 > mAzimuthReal)) {
                    visibilitySetter(leftArrow);
                }
                else{
                    visibilitySetter(rightArrow);
                }
            }
            else if (max <= 360){
                if ( mAzimuthReal > maxAngle && mAzimuthReal < max) {
                    visibilitySetter(leftArrow);
                }
                else{
                    visibilitySetter(rightArrow);
                }
            }
        }
    }

    private void findIt2(double minAngle, double maxAngle, double mAzimuthReal){
        if (isBetween(minAngle, maxAngle, mAzimuthReal)) {
            visibilitySetter2(pointerIcon);
        } else {
            double min = minAngle - 175;
            double max = maxAngle + 175;
            teamMemberInfo.setVisibility(View.INVISIBLE);
            if(min < 0) {
                double min1 = min + 360;

                if ((min1 < mAzimuthReal && 360 > mAzimuthReal) ||
                        (minAngle < mAzimuthReal && 0 < mAzimuthReal)) {
                    visibilitySetter2(rightArrow);
                }
                else{
                    visibilitySetter2(leftArrow);
                }
            }
            else if (min >= 0){
                if (mAzimuthReal < minAngle && mAzimuthReal > min) {
                    visibilitySetter2(rightArrow);
                }
                else{
                    visibilitySetter2(leftArrow);
                }
            }

            if(max > 360) {
                double max1 = max - 360;
                if ((max1 > mAzimuthReal && 0 < mAzimuthReal) ||
                        (maxAngle < mAzimuthReal && 360 > mAzimuthReal)) {
                    visibilitySetter2(leftArrow);
                }
                else{
                    visibilitySetter2(rightArrow);
                }
            }
            else if (max <= 360){
                if ( mAzimuthReal > maxAngle && mAzimuthReal < max) {
                    visibilitySetter2(leftArrow);
                }
                else{
                    visibilitySetter2(rightArrow);
                }
            }
        }
    }

	private void visibilitySetter(ImageView visibleView){
		if(visibleView == pointerIcon){
			pointerIcon.setVisibility(View.VISIBLE);
			leftArrow.setVisibility(View.INVISIBLE);
			leftArrowText.setVisibility(View.INVISIBLE);
			rightArrow.setVisibility(View.INVISIBLE);
			rightArrowText.setVisibility(View.INVISIBLE);
            myFlag = 1;
		}
		else if(visibleView == leftArrow){
			pointerIcon.setVisibility(View.INVISIBLE);
			leftArrow.setVisibility(View.VISIBLE);
			leftArrowText.setVisibility(View.VISIBLE);
			rightArrow.setVisibility(View.INVISIBLE);
			rightArrowText.setVisibility(View.INVISIBLE);
            myFlag = 2;
		}
		else if(visibleView == rightArrow){
			pointerIcon.setVisibility(View.INVISIBLE);
			leftArrow.setVisibility(View.INVISIBLE);
			leftArrowText.setVisibility(View.INVISIBLE);
			rightArrow.setVisibility(View.VISIBLE);
			rightArrowText.setVisibility(View.VISIBLE);
            myFlag = 3;
		}
	}

    private void visibilitySetter2(ImageView visibleView){
        if(visibleView == pointerIcon){
            pointerIcon.setVisibility(View.VISIBLE);
            leftArrow2.setVisibility(View.INVISIBLE);
            leftArrowText2.setVisibility(View.INVISIBLE);
            rightArrow2.setVisibility(View.INVISIBLE);
            rightArrowText2.setVisibility(View.INVISIBLE);
            myFlag = 1;
        }
        else if(visibleView == leftArrow){
            pointerIcon.setVisibility(View.INVISIBLE);
            leftArrow2.setVisibility(View.VISIBLE);
            leftArrowText2.setVisibility(View.VISIBLE);
            rightArrow2.setVisibility(View.INVISIBLE);
            rightArrowText2.setVisibility(View.INVISIBLE);
            myFlag = 2;
        }
        else if(visibleView == rightArrow){
            pointerIcon.setVisibility(View.INVISIBLE);
            leftArrow2.setVisibility(View.INVISIBLE);
            leftArrowText2.setVisibility(View.INVISIBLE);
            rightArrow2.setVisibility(View.VISIBLE);
            rightArrowText2.setVisibility(View.VISIBLE);
            myFlag = 3;
        }
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
        mMyDistance = distance(mMyLatitude, mMyLongitude, 39.797600, 32.810727);
        teamMemberInfo.setText("NAME: BERK GÜLTEKİN\n" +
                                "CALCULATED DISTANCE: " + mMyDistance);
        teamMemberInfo.setVisibility(View.VISIBLE);
    }
}
