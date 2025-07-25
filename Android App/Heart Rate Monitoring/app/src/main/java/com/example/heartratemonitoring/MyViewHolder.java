package com.example.heartratemonitoring;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    TextView textView;
    ImageButton imageButtonDelete;
    View view;
    public MyViewHolder(@NonNull View itemView){
        super(itemView);
        textView=itemView.findViewById(R.id.txt_DateTime);
        imageButtonDelete=itemView.findViewById(R.id.btn_delete);
        view=itemView;
    }
}
