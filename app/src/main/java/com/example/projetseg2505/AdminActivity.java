package com.example.projetseg2505;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.badge.BadgeUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class AdminActivity extends AppCompatActivity {

    private EditText email, password;
    private Button addButton;
    private DatabaseReference rootDatabaseref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // Ensure you have a layout file named activity_admin.xml
        password =  findViewById(R.id.logInText);
        addButton = findViewById(R.id.addbutton);
        email = findViewById(R.id.email);

        rootDatabaseref = FirebaseDatabase.getInstance().getReference().child("User");

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mdp = password.getText().toString();
                String mail = email.getText().toString();

                HashMap hashMap = new HashMap();
                hashMap.put("email", mail);
                hashMap.put("password", mdp);

                rootDatabaseref.child(mail.replace(".", ",")).setValue(hashMap);
            }
        });

    }
}

