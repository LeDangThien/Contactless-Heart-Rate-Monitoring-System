package com.example.heartratemonitoring;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.series.DataPoint;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserInfoActivity extends AppCompatActivity {
    private int STATE_EDIT=0, STATE_SAVE=1;
    private ImageButton buttonBack;
    private Button buttonEdit;
    private EditText textName, textWeight, textHeight;
    private TextView textDate;
    private String uid;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference referenceName, referenceDate, referenceHeight, referenceWeight;
    private int state=STATE_EDIT;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_info);
        buttonBack=findViewById(R.id.btn_back);
        buttonEdit=findViewById(R.id.btn_edit);
        textName=findViewById(R.id.txt_name);
        textDate=findViewById(R.id.txt_date);
        textWeight=findViewById(R.id.txt_weigh);
        textHeight=findViewById(R.id.txt_height);
        firebaseAuth=FirebaseAuth.getInstance();
        uid= Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        referenceName=FirebaseDatabase.getInstance().getReference("user_infor/"+uid+"/name");
        referenceDate=FirebaseDatabase.getInstance().getReference("user_infor/"+uid+"/dateOfBirth");
        referenceWeight=FirebaseDatabase.getInstance().getReference("user_infor/"+uid+"/weigh");
        referenceHeight=FirebaseDatabase.getInstance().getReference("user_infor/"+uid+"/height");

        buttonBack.setOnClickListener(v->{
            Intent backIntent=new Intent(UserInfoActivity.this, HeartRateActivity.class);
            startActivity(backIntent);
        });

        referenceName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name=snapshot.getValue(String.class);
                textName.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenceDate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String date=snapshot.getValue(String.class);
                textDate.setText(date);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenceWeight.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer weight=snapshot.getValue(Integer.class);
                textWeight.setText(weight.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        referenceHeight.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer height=snapshot.getValue(Integer.class);
                textHeight.setText(height.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        buttonEdit.setOnClickListener(v->{
            if(state == STATE_EDIT){
                handleEditButton();
            }
            else {
                handleSaveButton();
            }
        });
    }

    void handleEditButton(){
        enableEditText(textName);
        enableEditText(textWeight);
        enableEditText(textHeight);
        textDate.setEnabled(true);
        textDate.setClickable(true);
        textDate.setFocusable(true);
        buttonEdit.setText("Save");
        buttonEdit.setBackgroundColor(Color.GREEN);
        state=STATE_SAVE;
        textDate.setOnClickListener(v->{
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create and show DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    UserInfoActivity.this, // replace with "getActivity()" if in a Fragment
                    new DatePickerDialog.OnDateSetListener() {
                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            // Set selected date on TextView
                            textDate.setText(String.format("%04d-%02d-%02d", year, month, day));
                        }
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    void handleSaveButton(){
        String name, date, weighStr, heighStr;
        int weigh, heigh;
        disableEditText(textName);
        disableEditText(textWeight);
        disableEditText(textHeight);
        textDate.setEnabled(false);
        textDate.setClickable(false);
        textDate.setFocusable(false);
        buttonEdit.setText("Edit");
        buttonEdit.setBackgroundColor(Color.parseColor("#ff6750a4"));
        state=STATE_EDIT;
        name=textName.getText().toString().trim();
        date=textDate.getText().toString().trim();
        weighStr=textWeight.getText().toString().trim();
        heighStr=textHeight.getText().toString().trim();
        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(date) || TextUtils.isEmpty(weighStr) || TextUtils.isEmpty(heighStr)){
            Toast.makeText(UserInfoActivity.this, "Missing Information", Toast.LENGTH_SHORT).show();
            return;
        }
        weigh=Integer.parseInt(weighStr);
        heigh=Integer.parseInt(heighStr);
        DatabaseReference referenceUID=FirebaseDatabase.getInstance().getReference("user_infor/"+uid);
        Map<String, Object> map=new HashMap<>();
        map.put("dateOfBirth", date);
        map.put("name", name);
        map.put("height", heigh);
        map.put("weigh", weigh);
        referenceUID.updateChildren(map)
                .addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        Toast.makeText(UserInfoActivity.this,
                                dbTask.getException() != null
                                        ? dbTask.getException().getMessage()
                                        : "Information updated successfully",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserInfoActivity.this,
                                dbTask.getException() != null
                                        ? dbTask.getException().getMessage()
                                        : "Error: Can't change your informations",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
    }

    void enableEditText(EditText txt){
        txt.setEnabled(true);
        txt.setFocusable(true);
        txt.setFocusableInTouchMode(true);
    }

    void disableEditText(EditText txt){
        txt.setEnabled(false);
        txt.setFocusable(false);
        txt.setFocusableInTouchMode(false);
    }
}