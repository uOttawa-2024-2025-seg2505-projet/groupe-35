package com.example.projetseg2505;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private EditText emailText, passwordText;
    private Button loginButton;
    private DatabaseReference db;
    private TextView errorTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Point to the correct "User" node in Firebase
        db = FirebaseDatabase.getInstance().getReference("User");

        emailText = findViewById(R.id.emailEditText);
        passwordText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        errorTextView = findViewById(R.id.errorTextView);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        errorTextView.setVisibility(View.GONE);

        if (email.isEmpty() || password.isEmpty()) {
            errorTextView.setText("Please enter email and password");
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

        // Query Firebase for the email
        db.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Debugging log
                Log.d("Login", "DataSnapshot: " + dataSnapshot.getValue());

                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String dbPassword = userSnapshot.child("password").getValue(String.class);
                        String userType = userSnapshot.child("userType").getValue(String.class);

                        // Debugging log for found user
                        Log.d("Login", "Found user: " + userSnapshot.getKey() + ", userType: " + userType);

                        if (dbPassword != null && dbPassword.equals(password)) {
                            redirectToUserPage(userType);
                        } else {
                            errorTextView.setText("Invalid password");
                            errorTextView.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    errorTextView.setText("User not found");
                    errorTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                errorTextView.setText("Error: " + databaseError.getMessage());
                errorTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void redirectToUserPage(String userType) {
        Intent intent;
        switch (userType) {
            case "admin":
                intent = new Intent(this, AdminActivity.class);
                break;
            case "requester":
                intent = new Intent(this, RequesterActivity.class);
                break;
            case "storekeeper":
                intent = new Intent(this, StorekeeperActivity.class);
                break;
            case "assembler":
                intent = new Intent(this, AssemblerActivity.class);
                break;
            default:
                Toast.makeText(this, "Unknown user type", Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
    }
}
