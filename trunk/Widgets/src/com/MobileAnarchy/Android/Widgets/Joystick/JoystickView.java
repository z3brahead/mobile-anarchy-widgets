package com.MobileAnarchy.Android.Widgets.Joystick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {

	// =========================================
	// Private Members
	// =========================================

	private final String TAG = "JoystickView";
	private Paint _circlePaint;
	private Paint _handlePaint;
	private double _touchX, _touchY;
	private int _innerPadding;
	private int _handleRadius;
	private int _handleInnerBoundaries;
	private JoystickMovedListener _listener;
	private int _sensitivity;

	// =========================================
	// Constructors
	// =========================================

	public JoystickView(Context context) {
		super(context);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initJoystickView();
	}

	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initJoystickView();
	}

	// =========================================
	// Initialization
	// =========================================

	private void initJoystickView() {
		setFocusable(true);

		_circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		_circlePaint.setColor(Color.GRAY);
		_circlePaint.setStrokeWidth(1);
		_circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		_handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		_handlePaint.setColor(Color.DKGRAY);
		_handlePaint.setStrokeWidth(1);
		_handlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		_innerPadding = 10;
		_sensitivity = 10;
	}

	// =========================================
	// Public Methods 
	// =========================================

	public void setOnJostickMovedListener(JoystickMovedListener listener) {
		this._listener = listener;
	}
	
	// =========================================
	// Drawing Functionality 
	// =========================================

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Here we make sure that we have a perfect circle
		int measuredWidth = measure(widthMeasureSpec);
		int measuredHeight = measure(heightMeasureSpec);
		int d = Math.min(measuredWidth, measuredHeight);

		_handleRadius = (int)(d * 0.25);
		_handleInnerBoundaries = _handleRadius;
		
		setMeasuredDimension(d, d);
	}

	private int measure(int measureSpec) {
		int result = 0;
		// Decode the measurement specifications.
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		if (specMode == MeasureSpec.UNSPECIFIED) {
			// Return a default size of 200 if no bounds are specified.
			result = 200;
		} else {
			// As you want to fill the available space
			// always return the full available bounds.
			result = specSize;
		}
		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int px = getMeasuredWidth() / 2;
		int py = getMeasuredHeight() / 2;
		int radius = Math.min(px, py);

		// Draw the background
		canvas.drawCircle(px, py, radius - _innerPadding, _circlePaint);

		// Draw the handle
		canvas.drawCircle((int) _touchX + px, (int) _touchY + py, _handleRadius,
				_handlePaint);

		canvas.save();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int actionType = event.getAction();
		if (actionType == MotionEvent.ACTION_MOVE) {
			int px = getMeasuredWidth() / 2;
			int py = getMeasuredHeight() / 2;
			int radius = Math.min(px, py) - _handleInnerBoundaries;

			_touchX = (event.getX() - px);
			_touchX = Math.max(Math.min(_touchX, radius), -radius);

			_touchY = (event.getY() - py);
			_touchY = Math.max(Math.min(_touchY, radius), -radius);

			// Coordinates
			Log.d(TAG, "X:" + _touchX + "|Y:" + _touchY);

			// Pressure
			if (_listener != null) {
				_listener.OnMoved((int) (_touchX / radius * _sensitivity), (int) (_touchY  / radius * _sensitivity));
			}

			invalidate();
		} else if (actionType == MotionEvent.ACTION_UP) {
			returnHandleToCenter();
			Log.d(TAG, "X:" + _touchX + "|Y:" + _touchY);
		}
		return true;
	}

	private void returnHandleToCenter() {

		Handler handler = new Handler();
		int numberOfFrames = 5;
		final double intervalsX = (0 - _touchX) / numberOfFrames;
		final double intervalsY = (0 - _touchY) / numberOfFrames;

		for (int i = 0; i < numberOfFrames; i++) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					_touchX += intervalsX;
					_touchY += intervalsY;
					invalidate();
				}
			}, i * 40);
		}

		if (_listener != null) {
			_listener.OnReleased();
		}
	}
}
