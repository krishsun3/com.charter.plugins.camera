package com.performanceactive.plugins.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;


public class CameraPreview extends Activity implements SensorEventListener {
	
	


	private Preview mPreview; 
	private ImageView mTakePicture;
	private TouchView mView;

	private boolean mAutoFocus = true;

	private boolean mFlashBoolean = false;

	private SensorManager mSensorManager;
	private Sensor mAccel;
	private boolean mInitialized = false;
	private float mLastX = 0;
	private float mLastY = 0;
	private float mLastZ = 0;
	private Rect rec = new Rect();

	private int mScreenHeight;
	private int mScreenWidth;

	private boolean mInvalidate = false;

	private File mLocation = new File(Environment.
			getExternalStorageDirectory(),"test.jpg");
	private Camera camera;
    private RelativeLayout layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("CameraPreview", "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	
		layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        layout.setLayoutParams(layoutParams);
       
		// the accelerometer is used for autofocus
		mSensorManager = (SensorManager) getSystemService(Context.
				SENSOR_SERVICE);
		mAccel = mSensorManager.getDefaultSensor(Sensor.
				TYPE_ACCELEROMETER);
				
		/*int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

		String toastMsg;
		switch(screenSize) {
			case Configuration.SCREENLAYOUT_SIZE_LARGE:
				toastMsg = "Large screen";
				break;
			case Configuration.SCREENLAYOUT_SIZE_NORMAL:
				toastMsg = "Normal screen";
				break;
			case Configuration.SCREENLAYOUT_SIZE_SMALL:
				toastMsg = "Small screen";
				break;
			default:
				toastMsg = "Screen size is neither large, normal or small";
		}
		Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
		
		
		int density= getResources().getDisplayMetrics().densityDpi;
		Log.i("CameraPreview", "density:"+density);
		switch(density)
		{
		case DisplayMetrics.DENSITY_LOW:
		   Toast.makeText(this, "LDPI:"+density, Toast.LENGTH_SHORT).show();
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			 Toast.makeText(this, "MDPI:"+density, Toast.LENGTH_SHORT).show();
			break;
		case DisplayMetrics.DENSITY_HIGH:
			Toast.makeText(this, "HDPI:"+density, Toast.LENGTH_SHORT).show();
			break;
		case DisplayMetrics.DENSITY_XHIGH:
			 Toast.makeText(this, "XHDPI:"+density, Toast.LENGTH_SHORT).show();
			break;
			
		default:
			Toast.makeText(this, density, Toast.LENGTH_SHORT).show();
		}*/

		// get the window width and height to display buttons
		// according to device screen size
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		mScreenHeight = displaymetrics.heightPixels;
		mScreenWidth = displaymetrics.widthPixels;
	    
		Log.i("CameraPreview", "Screen size:" + mScreenHeight + "," + mScreenWidth);
		// Get the dimensions of this drawable to set margins
		// for the ImageView that is used to take picturestonDrawable = this.getResources().getDrawable(R.drawable.camera);

		mTakePicture = new ImageView(this);
		mTakePicture.setId(12343);
		mTakePicture.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		
		setBitmap(mTakePicture, "camera.png");
		
		LayoutParams lp = new LayoutParams(mTakePicture.getLayoutParams());
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        
		lp.setMargins((int)((double)mScreenWidth*.85),
				(int)((double)mScreenHeight*.70) ,
				(int)((double)mScreenWidth*.85) + mTakePicture.getDrawable().getMinimumWidth(),
				(int)((double)mScreenHeight*.70) + mTakePicture.getDrawable().getMinimumHeight());
		mTakePicture.setLayoutParams(lp);
		Log.i("CameraPreview", "mTakePicture.getDrawable():" + mTakePicture.getDrawable().getMinimumWidth() + "," +  mTakePicture.getDrawable().getMinimumHeight());
		rec.set((int)((double)mScreenWidth*.85),
				(int)((double)mScreenHeight*.10) ,
				(int)((double)mScreenWidth*.85) + mTakePicture.getDrawable().getMinimumWidth(), 
				(int)((double)mScreenHeight*.70) + mTakePicture.getDrawable().getMinimumHeight());
		mTakePicture.setOnClickListener(previewListener);
		
		 	mPreview = new Preview(this);
			mPreview.setId(12345);
			mPreview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
			mView = new TouchView(this);
			mView.setId(12346);
			mView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
			mView.setRec(rec);
			mView.setRectangleParams(mScreenWidth,mScreenHeight);
			
			layout.addView(mPreview);
			layout.addView(mView);
			layout.addView(mTakePicture);
			setContentView(layout);
        

	}

	// this is the autofocus call back
	private AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){
		
		public void onAutoFocus(boolean autoFocusSuccess, Camera arg1) {
			//Wait.oneSec();
			mAutoFocus = true;
			Log.i("CameraPreview", "onAutoFocus");
		}};
		
		// with this, gett the ratio between screen size and pixels
		// of the image so we capture only the rectangular area of the
		// image and save it.
		public Double[] getRatio(){
			Log.i("CameraPreview", "getRatio");
			Size s = mPreview.getCameraParameters().getPreviewSize();
			double heightRatio = (double)s.height/(double)mScreenHeight;
			double widthRatio = (double)s.width/(double)mScreenWidth;
			Double[] ratio = {heightRatio,widthRatio};
			return ratio;
		}

		private OnClickListener flashListener = new OnClickListener(){

			@Override
			public void onClick(View v) {
				Log.i("CameraPreview", "onClick");
				if (mFlashBoolean){
					mPreview.setFlash(false);
				}
				else{
					mPreview.setFlash(true);
				}
				mFlashBoolean = !mFlashBoolean;
			}

		};

		// This method takes the preview image, grabs the rectangular
		// part of the image selected by the bounding box and saves it.
		// A thread is needed to save the picture so not to hold the UI thread.
		private OnClickListener previewListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("CameraPreview", "onClick1");
				if (mAutoFocus){
					mAutoFocus = false;
					//mPreview.setCameraFocus(myAutoFocusCallback);
					//Wait.oneSec();
					takePicture();
				}
				boolean pressed = false;
				if (!mTakePicture.isPressed()){
					pressed = true;
				}
			}	   
		};
		
		 private void takePicture() {
			 Wait.oneSec();
				Thread tGetPic = new Thread( new Runnable() {
					public void run() {
						Double[] ratio = getRatio();
						int left = (int) (ratio[1]*(double)mView.getmLeftTopPosX());
						// 0 is height
						int top = (int) (ratio[0]*(double)mView.getmLeftTopPosY());

						int right = (int)(ratio[1]*(double)mView.getmRightBottomPosX());

						int bottom = (int)(ratio[0]*(double)mView.getmRightBottomPosY());

						//savePhoto(mPreview.getPic(left,top,right,bottom));
						//byte[] cameraData= convertBitmap(mPreview.getPic(left,top,right,bottom));
						String imgString = convertBitmapToString(mPreview.getPic(left,top,right,bottom));
						if (imgString != null) {
					        Intent intent = new Intent();
					        intent.putExtra("ImageData", imgString);
					        setResult(RESULT_OK, intent);
					    } else {
					        setResult(RESULT_CANCELED);
					    }
						finish();
					} 
				});
				tGetPic.start();
		    }


		// just to close the app and release resources.
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			Log.i("CameraPreview", "onKeyDown");
			if (keyCode == KeyEvent.KEYCODE_BACK){
				finish();
			}
			return super.onKeyDown(keyCode, event); 
		}
		
		private boolean savePhoto(Bitmap bm) {
			Log.i("CameraPreview", "savePhoto");
			FileOutputStream image = null;
			try {
				image = new FileOutputStream(mLocation);
				System.out.println(mLocation.getAbsolutePath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			bm.compress(CompressFormat.JPEG, 100, image);
			if (bm != null) {
				int h = bm.getHeight();
				int w = bm.getWidth();
				Log.i("savePhoto", "savePhoto(): Bitmap WxH is " + w + "x" + h);
			} else {
				//Log.i(TAG, "savePhoto(): Bitmap is null..");
				return false;
			}
			return true;
		}

		public boolean onInterceptTouchEvent(MotionEvent ev) {
			Log.i("CameraPreview", "onInterceptTouchEvent");
			final int action = ev.getAction();
			boolean intercept = false;
			switch (action) {
			case MotionEvent.ACTION_UP:
				break;
			case MotionEvent.ACTION_DOWN:
				float x = ev.getX();
				float y = ev.getY();
				// here we intercept the button press and give it to this 
				// activity so the button press can happen and we can take 
				// a picture.
				if ((x >= rec.left) && (x <= rec.right) && (y>=rec.top) && (y<=rec.bottom)){
					intercept = true;
				}
				break;
			}
			return intercept;
		}

		// mainly used for autofocus to happen when the user takes a picture
		// I also use it to redraw the canvas using the invalidate() method
		// when I need to redraw things.
		public void onSensorChanged(SensorEvent event) {
			//Log.i("CameraPreview", "onSensorChanged");
			if (mInvalidate == true){
				mView.invalidate();
				mInvalidate = false;
			}
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			if (!mInitialized){
				mLastX = x;
				mLastY = y;
				mLastZ = z;
				mInitialized = true;
			}
			float deltaX  = Math.abs(mLastX - x);
			float deltaY = Math.abs(mLastY - y);
			float deltaZ = Math.abs(mLastZ - z);

			if (deltaX > .5 && mAutoFocus){ //AUTOFOCUS (while it is not autofocusing)
				mAutoFocus = false;
				mPreview.setCameraFocus(myAutoFocusCallback);
			}
			if (deltaY > .5 && mAutoFocus){ //AUTOFOCUS (while it is not autofocusing)
				mAutoFocus = false;
				mPreview.setCameraFocus(myAutoFocusCallback);
			}
			if (deltaZ > .5 && mAutoFocus){ //AUTOFOCUS (while it is not autofocusing) */
				mAutoFocus = false;
				mPreview.setCameraFocus(myAutoFocusCallback);
			}

			mLastX = x;
			mLastY = y;
			mLastZ = z;

		}

		// extra overrides to better understand app lifecycle and assist debugging
		@Override
		protected void onDestroy() {
			super.onDestroy();
			Log.i("CameraPreview", "onDestroy");
		}

		@Override
		protected void onPause() {
			super.onPause();
			Log.i("CameraPreview", "onPause");
			mSensorManager.unregisterListener(this);
		}

		@Override
		protected void onResume() {
			super.onResume();
			mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
			Log.i("CameraPreview", "onResume");
		}

		@Override
		protected void onRestart() {
			super.onRestart();
			Log.i("CameraPreview", "onRestart");
		}

		@Override
		protected void onStop() {
			super.onStop();
			Log.i("CameraPreview", "onStop");
		}

		@Override
		protected void onStart() {
			super.onStart();
			Log.i("CameraPreview", "onStart()");
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
		
		private void setBitmap(ImageView imageView, String imageName) {
	        try {
	            InputStream imageStream = getAssets().open("res/drawable-hdpi/" + imageName);
	            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
	            imageView.setImageBitmap(bitmap);
	            imageStream.close();
	        } catch (Exception e) {
	            Log.e("CameraPreview", "Could load image", e);
	        }
	    }
		
		private String convertBitmapToString(Bitmap bitmap){
			Log.i("CameraPreview", "convertBitmapToString");
			 ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
			 String jsOut= null;
        try {
            if (bitmap.compress(CompressFormat.JPEG, 50, jpeg_data)) {
                byte[] code = jpeg_data.toByteArray();
                byte[] output = Base64.encode(code, Base64.NO_WRAP);
                jsOut = new String(output);
                output = null;
                code = null;
            }else{
				Log.i("CameraPreview", "Error on compress");
			}
        } catch (Exception e) {
            Log.e("CameraPreview","Error compressing image.", e);
        }
        jpeg_data = null;
		return jsOut;
		}

}