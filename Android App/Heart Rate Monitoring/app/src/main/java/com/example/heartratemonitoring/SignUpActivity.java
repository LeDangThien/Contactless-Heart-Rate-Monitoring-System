package com.example.heartratemonitoring;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextName, editTextWeigh, editTextHeight;
    private DatePicker dateOfBirth;
    private Button buttonSignUp;
    private TextView textSignIn;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference reference;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        mAuth=FirebaseAuth.getInstance();
        editTextEmail=findViewById(R.id.txt_email);
        editTextPassword=findViewById(R.id.txt_password);
        editTextConfirmPassword=findViewById(R.id.txt_confirm_password);
        editTextHeight=findViewById(R.id.txt_height);
        editTextName=findViewById(R.id.txt_name);
        editTextWeigh=findViewById(R.id.txt_weigh);
        dateOfBirth=findViewById(R.id.date_birth);
        buttonSignUp=findViewById(R.id.btn_sign_up);
        textSignIn=findViewById(R.id.txt_sign_in);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view) {
                String email, password, confirmPass, name, birth, weighStr, heightStr;
                int weigh, height;
                int day, month, year;
                email=editTextEmail.getText().toString().trim();
                password=editTextPassword.getText().toString().trim();
                confirmPass=editTextConfirmPassword.getText().toString().trim();
                name=editTextName.getText().toString().trim();
                weighStr=editTextWeigh.getText().toString().trim();

                heightStr=editTextHeight.getText().toString().trim();


                day=dateOfBirth.getDayOfMonth();
                month=dateOfBirth.getMonth()+1;
                year=dateOfBirth.getYear();
                birth=String.format("%04d-%02d-%02d", year, month, day);

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPass) || TextUtils.isEmpty(name) || TextUtils.isEmpty(weighStr) || TextUtils.isEmpty(heightStr)){
                    Toast.makeText(SignUpActivity.this, "Missing Information", Toast.LENGTH_SHORT).show();
                    return;
                }
                weigh=Integer.parseInt(weighStr);
                height=Integer.parseInt(heightStr);

//                if(Patterns.EMAIL_ADDRESS.matcher(email).matches()){
//                    Toast.makeText(SignUpActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                if(password.length()<6){
                    Toast.makeText(SignUpActivity.this, "Password must has more than 6 chars", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!password.equals(confirmPass)){
                    Toast.makeText(SignUpActivity.this, "Wrong confirm password", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user=new User(name, birth, password, weigh, height);


                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            buttonSignUp.setEnabled(true);
                            if (!task.isSuccessful()) {
                                String msg = task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Auth failed";
                                Toast.makeText(SignUpActivity.this, msg, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String uid = task.getResult().getUser().getUid();
                            String path = "user_infor/"+uid;
                            DatabaseReference measure=FirebaseDatabase.getInstance().getReference(path);
                            measure.setValue(user)
                                    .addOnCompleteListener(dbTask -> {
                                        if (dbTask.isSuccessful()) {
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("measurement", "");
                                            measure.updateChildren(map);
                                            Toast.makeText(SignUpActivity.this, "Account & data saved!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(SignUpActivity.this,
                                                    dbTask.getException() != null
                                                            ? dbTask.getException().getMessage()
                                                            : "DB write failed",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        });


            }
        });

        textSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent=new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(signInIntent);
            }
        });
    }
}