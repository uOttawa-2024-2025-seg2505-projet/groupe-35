package com.example.testlogin1;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private EditText emailText, passwordText;
    private Button loginButton;
    private FirebaseFirestore db;
    private TextView errorTextView;
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
        db = FirebaseFirestore.getInstance();

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
            errorTextView.setText("Please enter email and password");  // Set the error message
            errorTextView.setVisibility(View.VISIBLE);
            return;
        }

        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete( Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String dbPassword = document.getString("password");
                            String userType = document.getString("userType");

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
                } else {
                    errorTextView.setText("Error" + task.getException());
                    errorTextView.setVisibility(View.VISIBLE);
                }
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