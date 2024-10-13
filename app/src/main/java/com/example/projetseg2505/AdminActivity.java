package com.example.projetseg2505;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.badge.BadgeUtils;

public class AdminActivity extends AppCompatActivity {

    private EditText input;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // Ensure you have a layout file named activity_admin.xml

    }
}

