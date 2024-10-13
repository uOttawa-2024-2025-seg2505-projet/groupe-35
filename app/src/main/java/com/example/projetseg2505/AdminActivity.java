package com.example.projetseg2505;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {

    private Button logoutButton;
    private Button removeRequesterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

//bouton logout
        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });


        removeRequesterButton = findViewById(R.id.removeRequesterButton);

        removeRequesterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Charger le layout de suppression d'un requester
                setContentView(R.layout.activity_admin_remove_requester);

                // Référence à la base de données Firebase
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("User");

                // Récupérer le bouton de confirmation et l'EditText pour l'email
                EditText emailDeleteEditText = findViewById(R.id.input_email_delete);
                Button confirmDeleteButton = findViewById(R.id.confirm_delete_button);

                // Listener pour le bouton de suppression
                confirmDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String emailToDelete = emailDeleteEditText.getText().toString().trim();

                        if (!emailToDelete.isEmpty()) {
                            // Méthode pour supprimer un utilisateur en fonction de l'email
                            databaseReference.orderByChild("email").equalTo(emailToDelete).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                            // Supprimer l'utilisateur trouvé
                                            userSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                                Toast.makeText(AdminActivity.this, "Utilisateur supprimé avec succès", Toast.LENGTH_SHORT).show();
                                            }).addOnFailureListener(e -> {
                                                Toast.makeText(AdminActivity.this, "Erreur lors de la suppression: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    } else {
                                        Toast.makeText(AdminActivity.this, "Aucun utilisateur trouvé avec cet email", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(AdminActivity.this, "Erreur: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(AdminActivity.this, "Veuillez entrer un email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


    }



}