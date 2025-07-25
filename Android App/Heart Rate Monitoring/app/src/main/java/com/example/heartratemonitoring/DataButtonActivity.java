package com.example.heartratemonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DataButtonActivity extends AppCompatActivity {
    private ImageButton buttonBack;
    private TextView textRaw;
    private TextView textCalibrate;
    private Integer source;
    private String path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_data_button);
        buttonBack = findViewById(R.id.btn_back);
        textRaw=findViewById(R.id.txt_raw);
        textCalibrate=findViewById(R.id.txt_calibrate);
        Intent prevIntent=getIntent();
        source= prevIntent.getIntExtra("source", -1);
        path=prevIntent.getStringExtra("path");


        buttonBack.setOnClickListener(v->{
            Intent backIntent;
            if(source==0){
                backIntent=new Intent(DataButtonActivity.this, HeartRateActivity.class);
                startActivity(backIntent);
            }
            else {
                backIntent=new Intent(DataButtonActivity.this, MeasurementOption.class);
                startActivity(backIntent);
            }
        });

        textRaw.setOnClickListener(v->{
            Intent rawHistoryIntent=new Intent(DataButtonActivity.this, MeasurementHistoryActivity.class);
            rawHistoryIntent.putExtra("path", path);
            rawHistoryIntent.putExtra("type", "raw");
            startActivity(rawHistoryIntent);
        });

        textCalibrate.setOnClickListener(v->{
            Intent caliHistoryIntent=new Intent(DataButtonActivity.this, MeasurementHistoryActivity.class);
            caliHistoryIntent.putExtra("path", path);
            caliHistoryIntent.putExtra("type", "calibrated");
            startActivity(caliHistoryIntent);
        });
    }
}