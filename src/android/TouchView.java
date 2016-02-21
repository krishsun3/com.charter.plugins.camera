package com.performanceactive.plugins.camera;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class TouchView extends View {

	//private final String TAG = "TESTTESTTESTESTTESTESTEST";

	private boolean mLeftTopBool = false;
	private boolean mRightTopBool = false;
	private boolean mLeftBottomBool = false;
	private boolean mRightBottomBool = false;
	
	private Drawable mLeftTopIcon;
	private Drawable mRightTopIcon;
	private Drawable mLeftBottomIcon;
	private Drawable mRightBottomIcon;
	
	// Starting positions of the bounding box

	private float mLeftTopPosX = 320;
	private float mLeftTopPosY = 280;

	private float mRightTopPosX = 860;
	private float mRightTopPosY = 280;

	private float mLeftBottomPosX = 320;
	private float mLeftBottomPosY = 410;

	private float mRightBottomPosX = 860;
	private float mRightBottomPosY = 410;
	private float mPosX;
	private float mPosY;

	private float mLastTouchX;
	private float mLastTouchY;

	private Paint topLine;
	private Paint bottomLine;
	private Paint leftLine;
	private Paint rightLine;

	private Rect buttonRec;
	
	private int mCenter;

	private static final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;

	// you can ignore this 
	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;
	

	public TouchView(Context context){
		super(context);
		init(context);
		Log.i("TouchView", "Constructor");
	}

	public TouchView(Context context, AttributeSet attrs){
		super (context,attrs);
		init(context);
		Log.i("TouchView", "Constructor1");
	}

	public TouchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		Log.i("TouchView", "Constructor2");
	}

	private void init(Context context) {

		// I need to create lines for the bouding box to connect
		Log.i("TouchView", "init");
		topLine = new Paint();
		bottomLine = new Paint();
		leftLine = new Paint();
		rightLine = new Paint();

		setLineParameters(Color.WHITE,2);
		
		// Here I grab the image that will work as the corners of the bounding
		// box and set their positions.
				
		mLeftTopIcon = getDrawable(context, "corners.png");
		
		mCenter = mLeftTopIcon.getMinimumHeight()/2;
		mLeftTopIcon.setBounds((int)mLeftTopPosX, (int)mLeftTopPosY,
				mLeftTopIcon.getIntrinsicWidth()+(int)mLeftTopPosX,
				mLeftTopIcon.getIntrinsicHeight()+(int)mLeftTopPosY);

		mRightTopIcon = getDrawable(context, "corners.png");
		mRightTopIcon.setBounds((int)mRightTopPosX, (int)mRightTopPosY,
				mRightTopIcon.getIntrinsicWidth()+(int)mRightTopPosX,
				mRightTopIcon.getIntrinsicHeight()+(int)mRightTopPosY);

		mLeftBottomIcon = getDrawable(context, "corners.png");
		mLeftBottomIcon.setBounds((int)mLeftBottomPosX, (int)mLeftBottomPosY,
				mLeftBottomIcon.getIntrinsicWidth()+(int)mLeftBottomPosX,
				mLeftBottomIcon.getIntrinsicHeight()+(int)mLeftBottomPosY);

		mRightBottomIcon = getDrawable(context, "corners.png");
		mRightBottomIcon.setBounds((int)mRightBottomPosX, (int)mRightBottomPosY,
				mRightBottomIcon.getIntrinsicWidth()+(int)mRightBottomPosX,
				mRightBottomIcon.getIntrinsicHeight()+(int)mRightBottomPosY);
		// Create our ScaleGestureDetector
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

	}

	private void setLineParameters(int color, float width){
		Log.i("TouchView", "setLineParameters");
		topLine.setColor(color);
		topLine.setStrokeWidth(width);

		bottomLine.setColor(color);
		bottomLine.setStrokeWidth(width);

		leftLine.setColor(color);
		leftLine.setStrokeWidth(width);

		rightLine.setColor(color);
		rightLine.setStrokeWidth(width);
	
	}
	
	public void setRectangleParams(float width, float height){
		Log.i("TouchView", "setRectangleParams");
		float avgWidth=width/2;
		float avgHeight=height/2;
		float avgLineWidth=width/6;
		float avgLineHeight=height/11;
		
		mLeftTopPosX = avgWidth-avgLineWidth;
		mLeftTopPosY = avgHeight-avgLineHeight;

		mRightTopPosX = avgWidth+avgLineWidth;;
		mRightTopPosY = avgHeight-avgLineHeight;;

		mLeftBottomPosX = avgWidth-avgLineWidth;;
		mLeftBottomPosY = avgHeight+avgLineHeight;

		mRightBottomPosX = avgWidth+avgLineWidth;
		mRightBottomPosY = avgHeight+avgLineHeight;
	
	}

	// Draws the bounding box on the canvas. Every time invalidate() is called
	// this onDraw method is called.
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save();
		Log.i("TouchView", "onDraw");
		canvas.drawLine(mLeftTopPosX+mCenter, mLeftTopPosY+mCenter,
				mRightTopPosX+mCenter, mRightTopPosY+mCenter, topLine);
		canvas.drawLine(mLeftBottomPosX+mCenter, mLeftBottomPosY+mCenter,
				mRightBottomPosX+mCenter, mRightBottomPosY+mCenter, bottomLine);
		canvas.drawLine(mLeftTopPosX+mCenter,mLeftTopPosY+mCenter,
				mLeftBottomPosX+mCenter,mLeftBottomPosY+mCenter,leftLine);
		canvas.drawLine(mRightTopPosX+mCenter,mRightTopPosY+mCenter,
				mRightBottomPosX+mCenter,mRightBottomPosY+mCenter,rightLine);


		canvas.restore();
	}


	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		boolean intercept = true;
		Log.i("TouchView", "onTouchEvent");
		Log.i("TouchView","-before change::");
		switch (action) {

		case MotionEvent.ACTION_DOWN: {

			final float x = ev.getX();
			final float y = ev.getY();

			// in CameraPreview we have Rect rec. This is passed here to return
			// a false when the camera button is pressed so that this view ignores
			// the touch event.
			if ((x >= buttonRec.left) && (x <=buttonRec.right) && (y>=buttonRec.top) && (y<=buttonRec.bottom)){
				intercept = false;
				break;
			}

			// is explained below, when we get to this method.
			manhattanDistance(x,y);

			// Remember where we started
			mLastTouchX = x;
			mLastTouchY = y;
			mActivePointerId = ev.getPointerId(0);
			break;
		}

		case MotionEvent.ACTION_MOVE: {

			final int pointerIndex = ev.findPointerIndex(mActivePointerId);
			final float x = ev.getX();
			final float y = ev.getY();
			//Log.i(TAG,"x: "+x);
			//Log.i(TAG,"y: "+y);

			// Only move if the ScaleGestureDetector isn't processing a gesture.
			// but we ignore here because we are not using ScaleGestureDetector.
			if (!mScaleDetector.isInProgress()) {
				final float dx = x - mLastTouchX;
				final float dy = y - mLastTouchY;

				mPosX += dx;
				mPosY += dy;

				invalidate();
			}

			// Calculate the distance moved
			final float dx = x - mLastTouchX;
			final float dy = y - mLastTouchY;


			// Move the object
			if (mPosX >= 0 && mPosX <=800){
				mPosX += dx;
			}
			if (mPosY >=0 && mPosY <= 480){
				mPosY += dy;
			}

			// while its being pressed n it does not overlap the bottom line or right line
			if (mLeftTopBool && ((y+mCenter*2) < mLeftBottomPosY) && ((x+mCenter*2) < mRightTopPosX)){
				if (dy != 0){
					mRightTopPosY = y;
				}
				if (dx != 0){
					mLeftBottomPosX = x;
				}
				mLeftTopPosX = x;//mPosX;
				mLeftTopPosY = y;//mPosY;
			}
			if (mRightTopBool && ((y+mCenter*2) < mRightBottomPosY) && (x > (mLeftTopPosX+mCenter*2))){
				if (dy != 0){
					mLeftTopPosY = y;
				}
				if (dx != 0){
					mRightBottomPosX = x;
				}
				mRightTopPosX = x;//mPosX;
				mRightTopPosY = y;//mPosY;
			}
			if (mLeftBottomBool && (y > (mLeftTopPosY+mCenter*2)) && ((x +mCenter*2) < mRightBottomPosX)){
				if (dx != 0){
					mLeftTopPosX = x;
				}
				if (dy != 0){
					mRightBottomPosY = y;
				}
				mLeftBottomPosX = x;
				mLeftBottomPosY = y;
			}
			if (mRightBottomBool && (y > (mLeftTopPosY+mCenter*2)) && (x > (mLeftBottomPosX+mCenter*2) )){
				if (dx != 0){
					mRightTopPosX = x;
				}
				if (dy != 0){
					mLeftBottomPosY = y;
				}
				mRightBottomPosX = x;
				mRightBottomPosY = y;
			}

			// Remember this touch position for the next move event
			mLastTouchX = x;
			mLastTouchY = y;

			// Invalidate to request a redraw
			
			invalidate();
			break;
		}
		case MotionEvent.ACTION_UP: {
			// when one of these is true, that means it can move when onDraw is called
			mLeftTopBool = false;
			mRightTopBool = false;
			mLeftBottomBool = false;
			mRightBottomBool = false;
			//mActivePointerId = INVALID_POINTER_ID;
			break;
		}

		case MotionEvent.ACTION_CANCEL: {
			mActivePointerId = INVALID_POINTER_ID;
			break;
		}

		case MotionEvent.ACTION_POINTER_UP: {
			// Extract the index of the pointer that left the touch sensor
			final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) 
			>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = ev.getPointerId(pointerIndex);
			if (pointerId == mActivePointerId) {
				// This was our active pointer going up. Choose a new
				// active pointer and adjust accordingly.
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				mLastTouchX = ev.getX(newPointerIndex);
				mLastTouchY = ev.getY(newPointerIndex);
				mActivePointerId = ev.getPointerId(newPointerIndex);
			}
			break;
		}
		}
		System.out.println("TouchView-after change::"+ "mLeftTopPosX:"+mLeftTopPosX+"->mLeftTopPosY:"+mLeftTopPosY +"->mRightTopPosX :"+mRightTopPosX +"->mRightTopPosY :"+mRightTopPosY +"->mLeftBottomPosX:"+mLeftBottomPosX +"->mLeftBottomPosY:"+mLeftBottomPosY+"->mRightBottomPosX:"+mRightBottomPosX+"->mRightBottomPosY :"+mRightBottomPosY );
		return intercept;
	}

	// Where the screen is pressed, calculate the distance closest to one of the 4 corners
	// so that it can get the pressed and moved. Only 1 at a time can be moved.
	private void manhattanDistance(float x, float y) {
		Log.i("TouchView", "manhattanDistance");
		double leftTopMan = Math.sqrt(Math.pow((Math.abs((double)x-(double)mLeftTopPosX)),2)
				+ Math.pow((Math.abs((double)y-(double)mLeftTopPosY)),2));

		double rightTopMan = Math.sqrt(Math.pow((Math.abs((double)x-(double)mRightTopPosX)),2)
				+ Math.pow((Math.abs((double)y-(double)mRightTopPosY)),2));

		double leftBottomMan = Math.sqrt(Math.pow((Math.abs((double)x-(double)mLeftBottomPosX)),2)
				+ Math.pow((Math.abs((double)y-(double)mLeftBottomPosY)),2));

		double rightBottomMan = Math.sqrt(Math.pow((Math.abs((double)x-(double)mRightBottomPosX)),2)
				+ Math.pow((Math.abs((double)y-(double)mRightBottomPosY)),2));

		//Log.i(TAG,"leftTopMan: "+leftTopMan);
		//Log.i(TAG,"RightTopMan: "+rightTopMan);

		if (leftTopMan < 50){
			mLeftTopBool = true;
			mRightTopBool = false;
			mLeftBottomBool = false;
			mRightBottomBool = false;
		}
		else if (rightTopMan < 50){
			mLeftTopBool = false;
			mRightTopBool = true;
			mLeftBottomBool = false;
			mRightBottomBool = false;
		}
		else if (leftBottomMan < 50){
			mLeftTopBool = false;
			mRightTopBool = false;
			mLeftBottomBool = true;
			mRightBottomBool = false;
		}
		else if (rightBottomMan < 50){
			mLeftTopBool = false;
			mRightTopBool = false;
			mLeftBottomBool = false;
			mRightBottomBool = true;
		}

	}
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			Log.i("TouchView", "onScale");
			mScaleFactor *= detector.getScaleFactor();

			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

			invalidate();
			return true;
		}
	}

	public float getmLeftTopPosX(){
		return mLeftTopPosX;
	}
	public float getmLeftTopPosY(){
		return mLeftTopPosY;
	}
	public float getmRightTopPosX(){
		return mRightTopPosX;
	}
	public float getmRightTopPosY(){
		return mRightTopPosY;
	}
	public float getmLeftBottomPosX() {
		return mLeftBottomPosX;
	}
	public float getmLeftBottomPosY() {
		return mLeftBottomPosY;
	}
	public float getmRightBottomPosY() {
		return mRightBottomPosY;
	}
	public float getmRightBottomPosX() {
		return mRightBottomPosX;
	}
	public void setRec(Rect rec) {
		this.buttonRec = rec;
	}

	// calls the onDraw method, I used it in my app Translanguage OCR
	// because I have a thread that needs to invalidate, or redraw
	// you cannot call onDraw from a thread not the UI thread.
	public void setInvalidate() {
		invalidate();
		
	}
	
	private Drawable getDrawable(Context context, String imageName) {
		// Read a Bitmap from Assets
		InputStream inputStream = null;
		Drawable drawable = null;
		try {
			inputStream = context.getAssets().open("res/drawable-hdpi/" + imageName);
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			drawable = new BitmapDrawable(getResources(), bitmap);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return drawable;
	}
}