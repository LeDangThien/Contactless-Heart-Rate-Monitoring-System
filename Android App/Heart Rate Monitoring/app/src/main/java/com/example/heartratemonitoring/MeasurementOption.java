package com.example.heartratemonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MeasurementOption extends AppCompatActivity {
    private ImageButton buttonBack;
    private DatabaseReference referenceMeasure;
    private FirebaseAuth firebaseAuth;
    private String uid;
    private FirebaseRecyclerOptions<RecyclerModel> options;
    private FirebaseRecyclerAdapter<RecyclerModel, MyViewHolder> adapter;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_measurement_option);
        buttonBack=findViewById(R.id.btn_back);
        firebaseAuth=FirebaseAuth.getInstance();
        uid= Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        referenceMeasure= FirebaseDatabase.getInstance().getReference().child("user_infor/"+uid+"/measurement");
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        buttonBack.setOnClickListener(v->{
            Intent backIntent=new Intent(MeasurementOption.this, HeartRateActivity.class);
            startActivity(backIntent);
        });

        options=new FirebaseRecyclerOptions.Builder<RecyclerModel>().setQuery(referenceMeasure, RecyclerModel.class).build();
        adapter=new FirebaseRecyclerAdapter<RecyclerModel, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull RecyclerModel model) {
                String key=getRef(position).getKey();
                String display=key.replace("_","  ");
                holder.textView.setText(display);
                holder.view.setOnClickListener(v->{
                    Intent intent=new Intent(MeasurementOption.this, DataButtonActivity.class);
                    intent.putExtra("source", 1);
                    intent.putExtra("path", key);
                    startActivity(intent);
                });
                holder.imageButtonDelete.setOnClickListener(v->{
                    referenceMeasure.child(key).removeValue()
                            .addOnCompleteListener(aVoid -> Toast.makeText(MeasurementOption.this, "Deleted successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(aVoid -> Toast.makeText(MeasurementOption.this, "Failed to delete", Toast.LENGTH_SHORT).show());
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_recycle_view_layout, parent, false);
                return new MyViewHolder(v);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }
}