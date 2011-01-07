package com.MobileAnarchy.Android.WidgetsDemo;

import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class JoystickActivity extends Activity {

	TextView _txtX, _txtY;
	JoystickView _joystick;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joystick);

		_txtX = (TextView)findViewById(R.id.TextViewX);
        _txtY = (TextView)findViewById(R.id.TextViewY);
        _joystick = (JoystickView)findViewById(R.id.joystickView);
        
        _joystick.setOnJostickMovedListener(_listener);
	}

    private JoystickMovedListener _listener = new JoystickMovedListener() {

		@Override
		public void OnMoved(int pan, int tilt) {
			_txtX.setText(Integer.toString(pan));
			_txtY.setText(Integer.toString(tilt));
		}

		@Override
		public void OnReleased() {
			_txtX.setText("stopped");
			_txtY.setText("stopped");
		}
	}; 

}
