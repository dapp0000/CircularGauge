package com.nuraytec.circulargauge;

import java.util.Random;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	private Gauge mGaugeView1;
	private final Random RAND = new Random();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_main);
		
		mGaugeView1 = (Gauge) findViewById(R.id.gauge1);
		
		mTimer.start();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private final CountDownTimer mTimer = new CountDownTimer(300000, 1000) {

		@Override
		public void onTick(final long millisUntilFinished) {
			mGaugeView1.setTargetValue(RAND.nextInt(101));		
			
		}

		@Override
		public void onFinish() {}
	};

}
