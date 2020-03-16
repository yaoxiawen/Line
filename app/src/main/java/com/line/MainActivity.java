package com.line;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Button progress = findViewById(R.id.progress);
        Button line = findViewById(R.id.line);
        progress.setOnClickListener(v -> {
            startActivity(new Intent(this,ProgressActivity.class));
        });
        line.setOnClickListener(v -> {
            startActivity(new Intent(this,LineActivity.class));
        });
    }
}
