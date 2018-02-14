package com.example.versuspc.snake;


import android.os.Bundle;
import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

/**
 * Created by VersusPc on 13/02/2018.
 */

public class SnakeActivity extends Activity {
    SnakeEngine serp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        serp = new SnakeEngine(this, size);
        setContentView(serp);
    }
    @Override
    protected void onResume() {
        super.onResume();
        serp.resume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        serp.pause();
    }


}
