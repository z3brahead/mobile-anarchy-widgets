package com.MobileAnarchy.Android.Widgets.TilesLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SingleTileLayout extends FrameLayout {

	// =========================================
	// Private members
	// =========================================

	private TilePosition position;
	
	// =========================================
	// Constructors
	// =========================================

	public SingleTileLayout(Context context) {
		super(context);
	}

	public SingleTileLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	
	// =========================================
	// Public Methods
	// =========================================

	public TilePosition getPosition() {
		return position;
	}

	public void setPosition(TilePosition position) {
		this.position = position;
	}

	
	// =========================================
	// Private Methods
	// =========================================

	
}
