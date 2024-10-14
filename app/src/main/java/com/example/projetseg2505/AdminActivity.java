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

    private EditText email, password, prenom, nom;
    private Button addButton;
    private DatabaseReference rootDatabaseref;
    private TextView textError ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        setContentView(R.layout.activity_admin); // Ensure you have a layout file named activity_admin.xml
        password =  findViewById(R.id.logInText);
        addButton = findViewById(R.id.addbutton);
        email = findViewById(R.id.email);
        prenom = findViewById(R.id.prenom);
        nom = findViewById(R.id.nom);
        textError = findViewById(R.id.errorTextView1);

        rootDatabaseref = FirebaseDatabase.getInstance().getReference().child("User");

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mdp = password.getText().toString();
                String mail = email.getText().toString();
                String usert = "requester";
                String pren = prenom.getText().toString();
                String no = nom.getText().toString();

                HashMap hashMap = new HashMap();

                if(!mail.isEmpty() && !mdp.isEmpty()){
                    rootDatabaseref.child(mail.replace(".", ",")).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            textError.setText("this requester has already an account");
                            textError.setVisibility(View.VISIBLE);
                        } else {
                            hashMap.put("email", mail);
                            hashMap.put("password", mdp);
                            hashMap.put("userType", usert);
                            hashMap.put("first name", pren);
                            hashMap.put("last name", no);
                            rootDatabaseref.child(mail.replace(".", ",")).setValue(hashMap)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(AdminActivity.this, "Utilisateur ajouté avec succès", Toast.LENGTH_SHORT).show();
                                            textError.setVisibility(View.GONE); // Cache le message d'erreur en cas de succès
                                        } else {
                                            textError.setText("Erreur lors de l'ajout de l'utilisateur.");
                                            textError.setVisibility(View.VISIBLE);
                                        }
                                    });
                        }
                    });
                }
                else{
                    textError.setText("Les champs email et mot de passe ne peuvent pas être vides.");
                    textError.setVisibility(View.VISIBLE);
                }

            }
        });

    }
}

