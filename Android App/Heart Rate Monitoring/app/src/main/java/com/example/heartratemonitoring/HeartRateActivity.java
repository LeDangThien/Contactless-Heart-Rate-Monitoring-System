package com.example.heartratemonitoring;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeartRateActivity extends AppCompatActivity {
    private int STATE_RUNNING=1, STATE_IDLE=0;
    private int FLAG_START=0, FLAG_STOP=1, FLAG_NEW_MEASURE=2;
    private int flagButton=FLAG_START;
    private int state=STATE_IDLE;
    private ImageButton imageButtonOption, imageButtonSend;
    private TextView textHr, textBody, textResp, textDistance;
    private EditText editTextRealHR;
    private GraphView graphHr;
    private LineGraphSeries<DataPoint> seriesHr;
    private Button buttonSt;
    private Button buttonShowResult;
    RelativeLayout.LayoutParams params;
    private String uid;
    private FirebaseAuth firebaseAuth;
    private double timeRunning=0.0, timeStart=0.0;
    private double  tickButtonStart=0.0, tickNewHR=0.0;
    private int errorData=0;
    private double kalmanK, kalmanQ=0.01, kalmanR=400;
    private double kalmanP=100+kalmanQ;
    private double resultHR=80;
    private Timer timerCurrent;
    private TimerTask timerTaskCurrent;
    private Queue<Integer> queueHR;
    Integer tempHR=0;
    private DatabaseReference referenceHR, referenceResp, referenceBody, referenceDistance, referenceRealHR, referenceCommunicate;
    private String stringTime;
    private String measureTime;
    private int stateEsp32=STATE_IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_heart_rate);
        textHr=findViewById(R.id.txt_hr);
        textResp=findViewById(R.id.txt_resp);
        textBody=findViewById(R.id.txt_movement);
        textDistance=findViewById(R.id.txt_distance);
        graphHr=findViewById(R.id.graph_hr);
        buttonShowResult=findViewById(R.id.btn_show_result);
        buttonSt=findViewById(R.id.btn_st);
        params = (RelativeLayout.LayoutParams) buttonSt.getLayoutParams();
        imageButtonOption=findViewById(R.id.btn_options);
        imageButtonSend=findViewById(R.id.btn_send);
        editTextRealHR=findViewById(R.id.txt_realHR);
        firebaseAuth=FirebaseAuth.getInstance();
        uid= Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        queueHR=new LinkedList<>();
        timerCurrent=new Timer();

        seriesHr=new LineGraphSeries<>();
        graphHr.addSeries(seriesHr);
        graphHr.getViewport().setXAxisBoundsManual(true);
        graphHr.getViewport().setMinX(0);
        graphHr.getViewport().setMaxX(60);
        graphHr.getViewport().setYAxisBoundsManual(true);
        graphHr.getViewport().setMinY(0);
        graphHr.getViewport().setMaxY(150);
        graphHr.getViewport().setScrollable(true);
        graphHr.getViewport().setScalable(true);

        imageButtonOption.setOnClickListener(v->{
            PopupMenu popupMenu = new PopupMenu(HeartRateActivity.this, imageButtonOption);
            popupMenu.getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if(menuItem.getTitle().equals("Log Out")){
                    FirebaseAuth.getInstance().signOut();
                    Intent signInIntent=new Intent(HeartRateActivity.this, MainActivity.class);
                    startActivity(signInIntent);
                    finish();
                } else if (menuItem.getTitle().equals("User Info")) {
                    Intent userInfoIntent=new Intent(HeartRateActivity.this, UserInfoActivity.class);
                    startActivity(userInfoIntent);
                } else if(menuItem.getTitle().equals("Measurement History")){
                    Intent measureIntent=new Intent(HeartRateActivity.this, MeasurementOption.class);
                    startActivity(measureIntent);
                }
                return true;
            });
            popupMenu.show();
        });

        buttonSt.setOnClickListener(v->{
            if(flagButton==FLAG_START){
                handleStartButton();
            }
            else if(flagButton==FLAG_STOP){
                handleStopButton();
            }
            else{
                handleMeasureButton();
            }
        });

        imageButtonSend.setOnClickListener(v->{
            if(state==STATE_RUNNING)
            {
                String time, stringReal;
                int realHR;
                stringReal = editTextRealHR.getText().toString().trim();
                if (TextUtils.isEmpty(stringReal)) {
                    Toast.makeText(HeartRateActivity.this, "Empty field", Toast.LENGTH_SHORT).show();
                    return;
                }
                realHR = Integer.parseInt(stringReal);
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleMeasure = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                time = simpleMeasure.format(calendar.getTime());
                Map<String, Object> map = new HashMap<>();
                map.put(time, realHR);
                referenceRealHR.updateChildren(map).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        editTextRealHR.setText("");
                        Toast.makeText(HeartRateActivity.this, "Heart rate sent successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HeartRateActivity.this, "Unable to send heart rate", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else{
                Toast.makeText(HeartRateActivity.this, "Press START to begin", Toast.LENGTH_SHORT).show();
            }

        });

        buttonShowResult.setOnClickListener(v->{
            Intent dataButtonIntent=new Intent(HeartRateActivity.this, DataButtonActivity.class);
            dataButtonIntent.putExtra("source", 0);
            dataButtonIntent.putExtra("path", measureTime);
            startActivity(dataButtonIntent);
        });

        referenceCommunicate=FirebaseDatabase.getInstance().getReference("communicate/ESP32_State");
        referenceCommunicate.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String state= Objects.requireNonNull(snapshot.getValue()).toString();
                switch (state){
                    case "RUNNING":
                        stateEsp32=STATE_RUNNING;
                        break;
                    case "IDLE":
                        stateEsp32=STATE_IDLE;
                        break;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        timerTaskCurrent=new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timeStart++;
                        if(state==STATE_RUNNING){
                            if(stateEsp32==STATE_IDLE){
                                if(timeRunning>=5){
                                    handleStopButton();
                                    Toast.makeText(HeartRateActivity.this, "ESP32 Error: No response", Toast.LENGTH_SHORT).show();
                                }
                            }

                            if(timeStart-tickNewHR>18){
                                Toast.makeText(HeartRateActivity.this, "ESP32 Error: No heart rate updated", Toast.LENGTH_SHORT).show();
                            }

                            timeRunning++;
                            if(!queueHR.isEmpty()){
                                tempHR=queueHR.peek();
                                queueHR.remove();
                            }
                            seriesHr.appendData(new DataPoint(timeRunning, tempHR), true, 1000);
                        }
                    }
                });
            }
        };
        timerCurrent.schedule(timerTaskCurrent,0,1000);
    }

    private void handleStartButton() {
        boolean addCommunicate=false;
        AtomicBoolean addMeasure= new AtomicBoolean(false);
        DatabaseReference startRefer = FirebaseDatabase.getInstance().getReference("user_infor/" + uid + "/measurement");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleMeasure = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
        measureTime = simpleMeasure.format(calendar.getTime());
        String startTime = measureTime.substring(11, measureTime.length());
        Map<String, Object> map = new HashMap<>();
        map.put(measureTime, "");
        startRefer.updateChildren(map);
        String path = "user_infor/" + uid + "/measurement/" + measureTime;
        startRefer = FirebaseDatabase.getInstance().getReference(path);
        Measurement measurement = new Measurement();
        startRefer.setValue(measurement)
                .addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        addMeasure.set(true);
                    } else {
                        Toast.makeText(HeartRateActivity.this,
                                dbTask.getException() != null
                                        ? dbTask.getException().getMessage()
                                        : "Wifi error: Please push again",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                });

        startRefer = FirebaseDatabase.getInstance().getReference("communicate");
        Map<String, Object> communicate = new HashMap<>();
        communicate.put("UID", uid);
        communicate.put("start_time", measureTime);
        communicate.put("signal", "RUNNING");
        startRefer.updateChildren(communicate)
                .addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        if(addMeasure.get()){
                            buttonSt.setText("STOP");
                            buttonSt.setBackgroundColor(Color.RED);
                            flagButton = FLAG_STOP;
                            state = STATE_RUNNING;
                            timeRunning=0;
                            tickNewHR=timeStart;
                            seriesHr.resetData(new DataPoint[0]);
                            tickButtonStart=timeStart;
                            addChildListener();
                        }
                    } else {
                        Toast.makeText(HeartRateActivity.this,
                                dbTask.getException() != null
                                        ? dbTask.getException().getMessage()
                                        : "Wifi error: Please push again",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
    }

    private void handleStopButton(){
        DatabaseReference stopRefer = FirebaseDatabase.getInstance().getReference("communicate");
        Map<String, Object> communicate = new HashMap<>();
        communicate.put("signal", "IDLE");
        stopRefer.updateChildren(communicate)
                .addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        buttonShowResult.setVisibility(View.VISIBLE);
                        buttonSt.setText("NEW MEASUREMENT");
                        buttonSt.setBackgroundColor(Color.DKGRAY);
                        flagButton = FLAG_NEW_MEASURE;
                        state = STATE_IDLE;
                        timeRunning=0;
                        params.removeRule(RelativeLayout.ALIGN_PARENT_START);
                        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        params.setMarginStart(0);
                        buttonSt.setLayoutParams(params);
                    } else {
                        Toast.makeText(HeartRateActivity.this,
                                dbTask.getException() != null
                                        ? dbTask.getException().getMessage()
                                        : "Wifi error: Please push again",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                });;

    }

    private void handleMeasureButton(){
        buttonSt.setText("START");
        buttonSt.setBackgroundColor(Color.parseColor("#0099CC"));
        flagButton = FLAG_START;
        buttonShowResult.setVisibility(View.GONE);
//        params.removeRule(RelativeLayout.CENTER_HORIZONTAL);
//        params.addRule(RelativeLayout.ALIGN_PARENT_START);
//        params.setMarginStart(143);
//        buttonSt.setLayoutParams(params);
    }

    private void addChildListener(){
        String stringMeasure="user_infor/"+uid+"/measurement/"+measureTime;
        referenceResp=FirebaseDatabase.getInstance().getReference(stringMeasure+"/respiratory_rate");
        referenceDistance=FirebaseDatabase.getInstance().getReference(stringMeasure+"/distance");
        referenceBody=FirebaseDatabase.getInstance().getReference(stringMeasure+"/body_signal");
        referenceHR=FirebaseDatabase.getInstance().getReference(stringMeasure+"/radar_heart_rate");
        referenceRealHR=FirebaseDatabase.getInstance().getReference(stringMeasure+"/real_heart_rate");

        referenceHR.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                tickNewHR=timeStart;
                Integer hr=snapshot.getValue(Integer.class);
                if(hr!=0 && errorData==0){
                    kalmanK=kalmanP/(kalmanP+kalmanR);
                    resultHR=(resultHR + (kalmanK * (hr - resultHR)));
                    kalmanP=(1-kalmanK)*kalmanP+kalmanQ;
                }
                else if(errorData>0){
                    errorData--;
                }
                Integer temp=(int) resultHR;
                textHr.setText(temp.toString());
                queueHR.add(temp);
                Log.d("OnChildAdded", "New data: "+hr);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenceResp.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Integer resp=snapshot.getValue(Integer.class);
                textResp.setText(resp.toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenceDistance.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Integer distance=snapshot.getValue(Integer.class);
                textDistance.setText(distance.toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenceBody.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Integer body=snapshot.getValue(Integer.class);
                if(body>90){
                    errorData=3;
                }
                textBody.setText(body.toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();                                                                          //Call superclass (AppCompatActivity) onDestroy method
        if (state == STATE_IDLE) {                                              //See if we got past the permission request
            timerCurrent.cancel();
        }
    }
}

