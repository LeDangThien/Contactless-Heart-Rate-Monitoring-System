package com.example.heartratemonitoring;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.TimeZone;

public class MeasurementHistoryActivity extends AppCompatActivity {
    private TextView textTotalTime, textAvrHR, textAvrResp;
    private ImageButton buttonBack;
    private GraphView graphView;
    private LineGraphSeries<DataPoint> seriesHr, seriesRealHr, seriesResp, seriesDistance, seriesBody;
    private String startTime, uid, path, type;
    private SimpleDateFormat sdf;
    private Date initialTime, endTime;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference referenceHR,referenceRealHR,referenceResp,referenceDistance,referenceBody;
    private Integer avrHR=0, avrResp=0, avrRealHR=0;
    private Queue<Date> queueBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_measurement_history);
        textTotalTime=findViewById(R.id.txt_total_time);
        textAvrHR=findViewById(R.id.txt_avr_hr);
        textAvrResp=findViewById(R.id.txt_avr_resp);
        sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        queueBody=new LinkedList<>();
        buttonBack=findViewById(R.id.btn_back);
        firebaseAuth=FirebaseAuth.getInstance();
        uid= Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        Intent prevIntent=getIntent();
        path=prevIntent.getStringExtra("path");
        type=prevIntent.getStringExtra("type");
        startTime=path.substring(11, path.length());
        setUpGraph();
        String measurement="user_infor/"+uid+"/measurement/"+path;
        referenceHR= FirebaseDatabase.getInstance().getReference(measurement+"/radar_heart_rate");
        referenceRealHR= FirebaseDatabase.getInstance().getReference(measurement+"/real_heart_rate");
        referenceResp= FirebaseDatabase.getInstance().getReference(measurement+"/respiratory_rate");
        referenceDistance= FirebaseDatabase.getInstance().getReference(measurement+"/distance");
        referenceBody= FirebaseDatabase.getInstance().getReference(measurement+"/body_signal");

        buttonBack.setOnClickListener(v->{
            Intent backIntent=new Intent(MeasurementHistoryActivity.this, DataButtonActivity.class);
            backIntent.putExtra("path", path);
            startActivity(backIntent);
        });

        referenceBody.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String time;
                Integer value;
                Date prevTime=new Date(initialTime.getTime()+1000);
                Date currentTime;
                for(DataSnapshot childShot: snapshot.getChildren()){
                    time=childShot.getKey();
                    value=childShot.getValue(Integer.class);
                    try {
                        currentTime=sdf.parse(time);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    if(value>90){
                        queueBody.add(currentTime);
                    }
                    if(currentTime.after(endTime)){
                        endTime=new Date(currentTime.getTime());
                    }
                    for(Date i=new Date(prevTime.getTime()); i.before(currentTime) || i.equals(currentTime); i=new Date(i.getTime()+1000)){
                        seriesBody.appendData(new DataPoint(i, value), false, 1000);
                    }
                    prevTime=new Date(currentTime.getTime()+1000);
                }
                int diff= Math.toIntExact((endTime.getTime() - initialTime.getTime()) / 1000);
                Integer second=diff%60;
                Integer minute=(diff/60)%60;
                Integer hour=diff/3600;
                String totalTime=String.format("%02d:%02d:%02d", hour, minute, second);
                textTotalTime.setText(totalTime);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(type.equals("raw")){
            handleRawData();
        }
        else{
            handleCalibratedData();
        }

        referenceResp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String time;
                Integer value, count=0;
                Date prevTime=new Date(initialTime.getTime()+1000);
                Date currentTime;
                for(DataSnapshot childShot: snapshot.getChildren()){
                    count++;
                    time=childShot.getKey();
                    value=childShot.getValue(Integer.class);
                    avrResp+=value;
                    try {
                        currentTime=sdf.parse(time);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    if(currentTime.after(endTime)){
                        endTime=new Date(currentTime.getTime());
                    }
                    for(Date i=new Date(prevTime.getTime()); i.before(currentTime) || i.equals(currentTime); i=new Date(i.getTime()+1000)){
                        seriesResp.appendData(new DataPoint(i, value), false, 1000);
                    }
                    prevTime=new Date(currentTime.getTime()+1000);
                }
                avrResp/=count;
                textAvrResp.setText(avrResp.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenceRealHR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String time;
                Integer value;
                Date prevTime=new Date(initialTime.getTime()+1000);
                Date currentTime;
                for(DataSnapshot childShot: snapshot.getChildren()){
                    time=childShot.getKey();
                    value=childShot.getValue(Integer.class);
                    try {
                        currentTime=sdf.parse(time);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    if(currentTime.after(endTime)){
                        endTime=new Date(currentTime.getTime());
                    }
                    for(Date i=new Date(prevTime.getTime()); i.before(currentTime) || i.equals(currentTime); i=new Date(i.getTime()+1000)){
                        seriesRealHr.appendData(new DataPoint(i, value), false, 1000);
                    }
                    prevTime=new Date(currentTime.getTime()+1000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenceDistance.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String time;
                Integer value, count=0;
                Date prevTime=new Date(initialTime.getTime()+1000);
                Date currentTime;
                for(DataSnapshot childShot: snapshot.getChildren()){
                    count++;
                    time=childShot.getKey();
                    value=childShot.getValue(Integer.class);
                    try {
                        currentTime=sdf.parse(time);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    if(currentTime.after(endTime)){
                        endTime=new Date(currentTime.getTime());
                    }
                    for(Date i=new Date(prevTime.getTime()); i.before(currentTime) || i.equals(currentTime); i=new Date(i.getTime()+1000)){
                        seriesDistance.appendData(new DataPoint(i, value), false, 1000);
                    }
                    prevTime=new Date(currentTime.getTime()+1000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void setUpGraph(){
        graphView=findViewById(R.id.graph);
        seriesHr=new LineGraphSeries<>();
        seriesRealHr= new LineGraphSeries<>();
        seriesResp=new LineGraphSeries<>();
        seriesDistance=new LineGraphSeries<>();
        seriesBody=new LineGraphSeries<>();

        seriesHr.setColor(Color.RED);
        seriesRealHr.setColor(Color.parseColor("#FFB6C1"));
        seriesResp.setColor(Color.BLUE);
        seriesDistance.setColor(Color.GRAY);
        seriesBody.setColor(Color.parseColor("#D7CCC8"));

        graphView.addSeries(seriesHr);
        graphView.addSeries(seriesRealHr);
        graphView.addSeries(seriesResp);
        graphView.addSeries(seriesDistance);
        graphView.addSeries(seriesBody);

        Date max;

        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(MeasurementHistoryActivity.this, sdf));
        try {
            initialTime=sdf.parse(startTime);
            endTime=sdf.parse(startTime);
            max=new Date(initialTime.getTime()+60*1000);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        graphView.getViewport().setXAxisBoundsManual(true);
//        graphView.getViewport().setMinX(0);
//        graphView.getViewport().setMaxX(60);
        graphView.getViewport().setMinX(initialTime.getTime());
        graphView.getViewport().setMaxX(max.getTime());
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(160);
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setScalable(true);
    }

    void handleRawData(){
        referenceHR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String time;
                Integer value, count=0;
                Date prevTime=new Date(initialTime.getTime()+1000);
                Date currentTime;
                for(DataSnapshot childShot: snapshot.getChildren()){
                    count++;
                    time=childShot.getKey();
                    value=childShot.getValue(Integer.class);
                    avrHR+=value;
                    try {
                        currentTime=sdf.parse(time);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    if(currentTime.after(endTime)){
                        endTime=new Date(currentTime.getTime());
                    }
                    for(Date i=new Date(prevTime.getTime()); i.before(currentTime) || i.equals(currentTime); i=new Date(i.getTime()+1000)){
                        seriesHr.appendData(new DataPoint(i, value), false, 1000);
                    }
                    prevTime=new Date(currentTime.getTime()+1000);
                }
                avrHR/=count;
                textAvrHR.setText(avrHR.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void handleCalibratedData(){
        referenceHR.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String time;
                double kalmanK, kalmanQ=0.01, kalmanR=400;
                double kalmanP=100+kalmanQ;
                double resultHR=80;
                int errorData=0;
                Integer value, count=0;
                Date prevTime=new Date(initialTime.getTime()+1000);
                Date currentTime;
                for(DataSnapshot childShot: snapshot.getChildren()){
                    time=childShot.getKey();
                    value=childShot.getValue(Integer.class);
                    try {
                        currentTime=sdf.parse(time);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    if(!queueBody.isEmpty()){
                        if(currentTime.after(queueBody.peek()) || currentTime.equals(queueBody.peek())){
                            errorData=3;
                            queueBody.remove();
                        }
                    }
                    if(value!=0 && errorData==0){
                        kalmanK=kalmanP/(kalmanP+kalmanR);
                        resultHR=(resultHR + (kalmanK * (value - resultHR)));
                        kalmanP=(1-kalmanK)*kalmanP+kalmanQ;
                    }
                    else if(errorData>0){
                        errorData--;
                    }

                    if(currentTime.after(endTime)){
                        endTime=new Date(currentTime.getTime());
                    }
                    for(Date i=new Date(prevTime.getTime()); i.before(currentTime) || i.equals(currentTime); i=new Date(i.getTime()+1000)){
                        seriesHr.appendData(new DataPoint(i, (int) resultHR), false, 1000);
                        avrRealHR+=(int) resultHR;
                        count++;
                    }
                    prevTime=new Date(currentTime.getTime()+1000);
                }
                avrRealHR/=count;
                textAvrHR.setText(avrRealHR.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}