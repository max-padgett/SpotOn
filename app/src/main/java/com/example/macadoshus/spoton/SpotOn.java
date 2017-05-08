package com.example.macadoshus.spoton;

import android.app.Activity;
import android.content.Context;
import android.widget.RelativeLayout;
import android.os.Bundle;

public class SpotOn extends Activity {

    private SpotOnView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayout);
        view = new SpotOnView(this, getPreferences(Context.MODE_PRIVATE), layout);
        layout.addView(view, 0);

    }

    @Override
    public void onPause(){
        super.onPause();
        view.pause();
    }

    public void onResume(){
        super.onResume();
        view.resume(this);
    }

}
