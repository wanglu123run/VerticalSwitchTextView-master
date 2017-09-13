package com.viclee.verticalswitchtextview.scrolltextview;


import android.app.Activity;
import android.os.Bundle;

import com.viclee.verticalswitchtextview.R;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);




		String temp = "今今天落实到加福禄寿天落实今天落实到加福禄寿今天落实到加福禄寿今天落实到加福禄寿今天落实到加福禄寿今天落实到加福禄寿今天落实到加福禄寿到加福禄寿";
	    ScrollTextView switcher1 = (ScrollTextView) findViewById(R.id.switcher1);
		switcher1.setText(temp);
		switcher1.beginScroll();
		
	    ScrollTextView switcher2 = (ScrollTextView) findViewById(R.id.switcher2);
		switcher2.setText(temp);
		switcher2.beginScroll();
	}
	


}
