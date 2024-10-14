package com.example.projetseg2505;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    // Declare UI elements
    private Button addRequesterButton, editDeleteRequesterButton, logoutButton;
    private EditText emailEditText, firstNameEditText, lastNameEditText, passwordEditText, currentDateTimeEditText;
    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_requester);  // Ensure the correct layout is set

        // Initialize UI elements
        addRequesterButton = findViewById(R.id.addRequesterButton);
        editDeleteRequesterButton = findViewById(R.id.edit_deleteButton);
        logoutButton = findViewById(R.id.logoutButton);
        emailEditText = findViewById(R.id.emailEditText);
        currentDateTimeEditText = findViewById(R.id.currentDateTimeEditText); // Initialize currentDateTimeEditText

        // Set the current date and time
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        currentDateTimeEditText.setText(currentDateTime);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("User");

        // Add Requester Button click event
        addRequesterButton.setOnClickListener(v -> {
            // Logic for adding a requester
            Toast.makeText(AdminActivity.this, "Add Requester Clicked", Toast.LENGTH_SHORT).show();
        });

        // Edit/Delete Requester Button click event
        editDeleteRequesterButton.setOnClickListener(v -> {
            // Logic for editing or deleting a requester
        });

        // Logout Button click event
        logoutButton.setOnClickListener(v -> finish()); // Finish activity to simulate logout
    }
}
