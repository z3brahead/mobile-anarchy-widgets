package com.MobileAnarchy.Android.Widgets.DockPanel;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class DockPanel extends LinearLayout {

	// =========================================
	// Private members
	// =========================================

	private static final String TAG = "DockPanel";
	private DockPosition _position;
	private int _contentLayoutId;
	private int _handleButtonDrawableId;
	private Boolean _isOpen;
	private Boolean _animationRunning;
	private FrameLayout _contentPlaceHolder;
	private ImageButton _toggleButton;
	private int _animationDuration;

	// =========================================
	// Constructors
	// =========================================

	public DockPanel(Context context, int contentLayoutId,
			int handleButtonDrawableId, Boolean isOpen) {
		super(context);

		_contentLayoutId = contentLayoutId;
		_handleButtonDrawableId = handleButtonDrawableId;
		_isOpen = isOpen;

		Init(null);
	}

	public DockPanel(Context context, AttributeSet attrs) {
		super(context, attrs);

		// to prevent from crashing the designer
		try {
			Init(attrs);
		} catch (Exception ex) {
		}
	}

	// =========================================
	// Initialization
	// =========================================

	private void Init(AttributeSet attrs) {
		setDefaultValues(attrs);

		createHandleToggleButton();

		// create the handle container
		FrameLayout handleContainer = new FrameLayout(getContext());
		handleContainer.addView(_toggleButton);

		// create and populate the panel's container, and inflate it
		_contentPlaceHolder = new FrameLayout(getContext());
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li = (LayoutInflater) getContext().getSystemService(
				infService);
		li.inflate(_contentLayoutId, _contentPlaceHolder, true);

		// setting the layout of the panel parameters according to the docking
		// position
		if (_position == DockPosition.LEFT || _position == DockPosition.RIGHT) {
			handleContainer.setLayoutParams(new LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT, 1));
			_contentPlaceHolder.setLayoutParams(new LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT, 1));
		} else {
			handleContainer.setLayoutParams(new LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1));
			_contentPlaceHolder.setLayoutParams(new LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1));
		}

		// adding the view to the parent layout according to docking position
		if (_position == DockPosition.RIGHT || _position == DockPosition.BOTTOM) {
			this.addView(handleContainer);
			this.addView(_contentPlaceHolder);
		} else {
			this.addView(_contentPlaceHolder);
			this.addView(handleContainer);
		}

		if (!_isOpen) {
			_contentPlaceHolder.setVisibility(GONE);
		}
	}

	private void setDefaultValues(AttributeSet attrs) {
		// set default values
		_isOpen = true;
		_animationRunning = false;
		_animationDuration = 500;
		setPosition(DockPosition.RIGHT);

		// Try to load values set by xml markup
		if (attrs != null) {
			String namespace = "http://com.MobileAnarchy.Android.Widgets";

			_animationDuration = attrs.getAttributeIntValue(namespace,
					"animationDuration", 500);
			_contentLayoutId = attrs.getAttributeResourceValue(namespace,
					"contentLayoutId", 0);
			_handleButtonDrawableId = attrs.getAttributeResourceValue(
					namespace, "handleButtonDrawableResourceId", 0);
			_isOpen = attrs.getAttributeBooleanValue(namespace, "isOpen", true);

			// Enums are a bit trickier (needs to be parsed)
			try {
				_position = DockPosition.valueOf(attrs.getAttributeValue(
						namespace, "dockPosition").toUpperCase());
				setPosition(_position);
			} catch (Exception ex) {
				// Docking to the left is the default behavior
				setPosition(DockPosition.LEFT);
			}
		}
	}

	private void createHandleToggleButton() {
		_toggleButton = new ImageButton(getContext());
		_toggleButton.setPadding(0, 0, 0, 0);
		_toggleButton.setLayoutParams(new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER));
		_toggleButton.setBackgroundColor(Color.TRANSPARENT);
		_toggleButton.setImageResource(_handleButtonDrawableId);
		_toggleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggle();
			}
		});
	}

	private void setPosition(DockPosition position) {
		_position = position;
		switch (position) {
		case TOP:
			setOrientation(LinearLayout.VERTICAL);
			setGravity(Gravity.TOP);
			break;
		case RIGHT:
			setOrientation(LinearLayout.HORIZONTAL);
			setGravity(Gravity.RIGHT);
			break;
		case BOTTOM:
			setOrientation(LinearLayout.VERTICAL);
			setGravity(Gravity.BOTTOM);
			break;
		case LEFT:
			setOrientation(LinearLayout.HORIZONTAL);
			setGravity(Gravity.LEFT);
			break;
		}
	}

	// =========================================
	// Public methods
	// =========================================

	public int getAnimationDuration() {
		return _animationDuration;
	}

	public void setAnimationDuration(int milliseconds) {
		_animationDuration = milliseconds;
	}

	public Boolean getIsRunning() {
		return _animationRunning;
	}

	public void open() {
		if (!_animationRunning) {
			Log.d(TAG, "Opening...");

			Animation animation = createShowAnimation();
			this.setAnimation(animation);
			animation.start();

			_isOpen = true;
		}
	}

	public void close() {
		if (!_animationRunning) {
			Log.d(TAG, "Closing...");

			Animation animation = createHideAnimation();
			this.setAnimation(animation);
			animation.start();
			_isOpen = false;
		}
	}

	public void toggle() {
		if (_isOpen) {
			close();
		} else {
			open();
		}
	}

	// =========================================
	// Private methods
	// =========================================

	private Animation createHideAnimation() {
		Animation animation = null;
		switch (_position) {
		case TOP:
			animation = new TranslateAnimation(0, 0, 0, -_contentPlaceHolder
					.getHeight());
			break;
		case RIGHT:
			animation = new TranslateAnimation(0, _contentPlaceHolder
					.getWidth(), 0, 0);
			break;
		case BOTTOM:
			animation = new TranslateAnimation(0, 0, 0, _contentPlaceHolder
					.getHeight());
			break;
		case LEFT:
			animation = new TranslateAnimation(0, -_contentPlaceHolder
					.getWidth(), 0, 0);
			break;
		}

		animation.setDuration(_animationDuration);
		animation.setInterpolator(new AccelerateInterpolator());
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				_animationRunning = true;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				_contentPlaceHolder.setVisibility(View.GONE);
				_animationRunning = false;
			}
		});
		return animation;
	}

	private Animation createShowAnimation() {
		Animation animation = null;
		switch (_position) {
		case TOP:
			animation = new TranslateAnimation(0, 0, -_contentPlaceHolder
					.getHeight(), 0);
			break;
		case RIGHT:
			animation = new TranslateAnimation(_contentPlaceHolder.getWidth(),
					0, 0, 0);
			break;
		case BOTTOM:
			animation = new TranslateAnimation(0, 0, _contentPlaceHolder
					.getHeight(), 0);
			break;
		case LEFT:
			animation = new TranslateAnimation(-_contentPlaceHolder.getWidth(),
					0, 0, 0);
			break;
		}
		Log.d(TAG, "Animation duration: " + _animationDuration);
		animation.setDuration(_animationDuration);
		animation.setInterpolator(new DecelerateInterpolator());
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				_animationRunning = true;
				_contentPlaceHolder.setVisibility(View.VISIBLE);
				Log.d(TAG, "\"Show\" Animation started");
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				_animationRunning = false;
				Log.d(TAG, "\"Show\" Animation ended");
			}
		});
		return animation;
	}

}
