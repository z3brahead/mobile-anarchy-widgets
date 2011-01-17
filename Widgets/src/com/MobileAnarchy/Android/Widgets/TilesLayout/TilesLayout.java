package com.MobileAnarchy.Android.Widgets.TilesLayout;

import java.util.ArrayList;
import java.util.List;

import android.R;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;

public class TilesLayout extends FrameLayout {

	// =========================================
	// Private members
	// =========================================

	private static final String TAG = "TilesLayout";
	private int animatedTransitionDuration;
	private List<SingleTileLayout> tiles;
	private TilesLayoutPreset preset;
	private Handler handler;
	
	// =========================================
	// Constructors
	// =========================================

	public TilesLayout(Context context) {
		super(context);
		Init(null);
	}

	public TilesLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		Init(attrs);
	}

	
	// =========================================
	// Public Methods
	// =========================================

	public void setPreset(TilesLayoutPreset preset) {
		try {
			rebuildLayout(preset);
			this.preset = preset;
		}
		catch (Exception ex) {
			Log.e(TAG, "Failed to set layout preset", ex);
		}
	}
	
	
	// =========================================
	// Private Methods
	// =========================================

	private void Init(AttributeSet attrs) {
		animatedTransitionDuration = 2000;
		handler = new Handler();
		tiles = new ArrayList<SingleTileLayout>();
	}
	
	
	private void rebuildLayout(TilesLayoutPreset preset) {
		ArrayList<TilePosition> positions = buildViewsPositions(preset);
		
		// We need to transform the current layout, to the new layout
		int extraViews = tiles.size() - positions.size();
		if (extraViews > 0) {
			// Remove the extra views
			while(tiles.size() - positions.size() > 0) {
				int lastViewPisition = tiles.size() - 1;
				removeView(tiles.get(lastViewPisition));
				tiles.remove(lastViewPisition);	
			}
		} else {
			// Add the extra views
			for (int i = tiles.size(); i< positions.size(); i++) {
				TilePosition newTilePisition = positions.get(i);
				SingleTileLayout tile = new SingleTileLayout(getContext());
				tile.setBackgroundResource(R.drawable.edit_text);
				FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
						(int)newTilePisition.getWidth(), 
						(int)newTilePisition.getHeight(), 
						Gravity.TOP + Gravity.LEFT);
				lp.setMargins((int)newTilePisition.getX(), 
						(int)newTilePisition.getY(), 0, 0);
				
				tile.setLayoutParams(lp);
				
				tiles.add(tile);
				addView(tile);
			}
		}
		// There is a bug in the animation-set, so we'll not animate
		//animateChange(positions);

		// Regular repositioning (no animation)
		processChange(positions);
	}



	private ArrayList<TilePosition> buildViewsPositions(TilesLayoutPreset preset) {
		int width = getWidth();
		int height = getHeight();

		Log.d(TAG, "Container's Dimensions = Width: " + width + ", Height: " + height);
		ArrayList<TilePosition> actualPositions = new ArrayList<TilePosition>();
		for (TilePosition position : preset.getTilePositions()) {
			
			int tileX = (int) Math.round(width * ((float)position.getX() / 100.0));
			int tileY = (int) Math.round(height * ((float)position.getY() / 100.0));
			int tileWidth = (int) Math.round(width * ((float)position.getWidth() / 100.0));
			int tileHeight = (int) Math.round(height * ((float)position.getHeight() / 100.0));
			
			TilePosition actualPosition = new TilePosition(tileX, tileY, tileWidth, tileHeight);
			actualPositions.add(actualPosition);
			
			Log.d(TAG, "New tile created - X: " + tileX + ", Y: " + tileY + ", Width: " + tileWidth + ", Height: " + tileHeight);
		}
		return actualPositions;
	}


	private void processChange(ArrayList<TilePosition> positions) {
		for (int i = 0; i < tiles.size(); i++) {
			final SingleTileLayout currentTile = tiles.get(i);
			final TilePosition targetPosition = positions.get(i);
			currentTile.setPosition(targetPosition);

			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					(int)targetPosition.getWidth(), 
					(int)targetPosition.getHeight(), 
					Gravity.TOP + Gravity.LEFT);
			lp.setMargins((int)targetPosition.getX(), 
					(int)targetPosition.getY(), 0, 0);
			
			currentTile.setLayoutParams(lp);
		}		
	}

	
	private void animateChange(ArrayList<TilePosition> positions) {
		AnimationSet animationSet = new AnimationSet(true);
		DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
		
		for (int i = 0; i < tiles.size(); i++) {
			AnimationSet scaleAndMove = new AnimationSet(true);

			final SingleTileLayout currentTile = tiles.get(i);
			TilePosition currentPosition = currentTile.getPosition(); 
			final TilePosition targetPosition = positions.get(i);
			
			if (currentPosition == null) {
				AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
				alphaAnimation.setDuration(animatedTransitionDuration);
				alphaAnimation.setStartOffset(0);
				currentTile.setAnimation(alphaAnimation);
				
				scaleAndMove.addAnimation(alphaAnimation);
			}

			currentTile.setPosition(targetPosition);

			if (!targetPosition.equals(currentPosition)) {
				float toXDelta = 0, toYDelta = 0;
				if (currentPosition != null) {
					toXDelta = targetPosition.getX() - currentPosition.getX();
					toYDelta = targetPosition.getY() - currentPosition.getY();
					Log.d(TAG, "From = " + currentPosition + ", To = " + targetPosition);
					Log.d(TAG, "toXDelta = " + toXDelta + ", toYDelta = " + toYDelta);
				}

				scaleAndMove.setStartOffset(0);
				
				// Move
				TranslateAnimation moveAnimation = new TranslateAnimation(0, toXDelta, 0, toYDelta);
				moveAnimation.setDuration(animatedTransitionDuration);
				//moveAnimation.setFillAfter(true);
				moveAnimation.setStartOffset(0);
				//moveAnimation.setStartOffset(animatedTransitionDuration);
				moveAnimation.setInterpolator(decelerateInterpolator);
				scaleAndMove.addAnimation(moveAnimation);
				
				// Physically move the tile when the animation ends
				scaleAndMove.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {	}
					
					@Override
					public void onAnimationRepeat(Animation animation) { }
					
					@Override
					public void onAnimationEnd(Animation animation) {
						FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
								(int)targetPosition.getWidth(), 
								(int)targetPosition.getHeight(), 
								Gravity.TOP + Gravity.LEFT);
						lp.setMargins((int)targetPosition.getX(), 
								(int)targetPosition.getY(), 0, 0);
						
						currentTile.setLayoutParams(lp);
					}
				});
				
				// Scale
				if (currentPosition != null) {
					ScaleAnimation scaleAnimation = 
						new ScaleAnimation(1, 
								targetPosition.getWidth() / currentPosition.getWidth(), 
								1, 
								targetPosition.getHeight() / currentPosition.getHeight(),
								Animation.ABSOLUTE, 0,
								Animation.ABSOLUTE, 0);
					
					scaleAnimation.setDuration(animatedTransitionDuration);
					//scaleAnimation.setStartOffset(animatedTransitionDuration);
					scaleAnimation.setStartOffset(0);
					//scaleAnimation.setFillAfter(true);
					scaleAndMove.addAnimation(scaleAnimation);
				}
				
				// Set animation to the tile
				currentTile.setAnimation(scaleAndMove);
			}
			// Add to the total animation set
			animationSet.addAnimation(scaleAndMove);
		}
		
		if (animationSet.getAnimations().size() > 0) {
			Log.d(TAG, "Starting animation");
			animationSet.start();
		}
	}

	
}
