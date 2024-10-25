package com.example.projetseg2505;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    private Button addRequesterButton, editDeleteRequesterButton, logoutButton, applyChangesButton, deleteRequesterButton;
    private EditText emailEditText, firstNameEditText, lastNameEditText, passwordEditText;
    private DatabaseReference databaseReference;
    private View clearDatabaseButton;



    private EditText email, password, prenom, nom;
    private Button addButton;
    private DatabaseReference rootDatabaseref;
    private TextView textError ;

    private String foundFirstName, foundLastName, foundEmail, foundPassword;




    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);  // Charger le layout admin principal

        // Initialisation des vues pour le premier layout (admin panel)
        addRequesterButton = findViewById(R.id.addRequesterButton);
        editDeleteRequesterButton = findViewById(R.id .edit_deleteButton);
        logoutButton = findViewById(R.id.logoutButton);
        emailEditText = findViewById(R.id.emailEditText);

        // Référence à Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("User");
        clearDatabaseButton = findViewById(R.id.button_clear_database);

        // Bouton pour ajouter un nouveau requester



        // Recherche d'un utilisateur et redirection vers le layout de modification/suppression
        editDeleteRequesterButton.setOnClickListener(v -> {
            String emailToSearch = emailEditText.getText().toString().trim();
            if (!emailToSearch.isEmpty()) {
                searchRequesterByEmail(emailToSearch);
            } else {
                Toast.makeText(AdminActivity.this, "Veuillez entrer un email pour chercher", Toast.LENGTH_SHORT).show();
            }
        });


        clearDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearRequesters(); // Appeler la méthode pour vider tous les requesters
                clearSoftwareComponents();
                clearHardwareComponents();
            }
        });

        // Déconnexion
        logoutButton.setOnClickListener(v -> {
            finish();  // Quitter l'application ou revenir à l'écran de connexion
        });


        addRequesterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change le layout pour afficher celui de addrequester
                setContentView(R.layout.activity_admin_add_requester);
                password =  findViewById(R.id.passwordEditText);
                addButton = findViewById(R.id.addRequesterButton);
                email = findViewById(R.id.emailEditText);
                prenom = findViewById(R.id.firstNameEditText);
                nom = findViewById(R.id.lastNameEditText);
                textError = findViewById(R.id.errorTextView);
                rootDatabaseref = FirebaseDatabase.getInstance().getReference().child("User");

//bouton pour ajouter des requesters

                addButton.setOnClickListener(new View.OnClickListener() {


                    public void onClick(View view) {
                        setContentView(R.layout.activity_admin_add_requester);
                        String mdp = password.getText().toString();
                        String mail = email.getText().toString();
                        String user = "requester";
                        String pren = prenom.getText().toString();
                        String no = nom.getText().toString();
                        String currentDateAndTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());


                        HashMap hashMap = new HashMap();

                        if(!mail.isEmpty() && !mdp.isEmpty()){
                            rootDatabaseref.child(mail.replace(".", ",")).get().addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult().exists()) {
                                    textError.setText("this requester has already an account");
                                    textError.setVisibility(View.VISIBLE);
                                } else {
                                    hashMap.put("email", mail);
                                    hashMap.put("password", mdp);
                                    hashMap.put("userType", user);
                                    hashMap.put("first name", pren);
                                    hashMap.put("last name", no);
                                    hashMap.put("last modification", currentDateAndTime);
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
        });
    }

    // Méthode pour rechercher un requester par email
    private void searchRequesterByEmail(String email) {
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        foundFirstName = userSnapshot.child("first name").getValue(String.class);
                        foundLastName = userSnapshot.child("last name").getValue(String.class);
                        foundEmail = userSnapshot.child("email").getValue(String.class);
                        foundPassword = userSnapshot.child("password").getValue(String.class);

                        // Une fois que l'utilisateur est trouvé, changer le layout
                        switchToModifyDeleteLayout();
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
    }

    // Méthode pour basculer vers le layout de modification/suppression
    private void switchToModifyDeleteLayout() {
        setContentView(R.layout.activity_admin_edit_delete_requester);  // Charger le layout modify/delete

        // Initialisation des vues pour le layout de modification/suppression
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);  // Réutilisé pour afficher l'email
        passwordEditText = findViewById(R.id.passwordEditText);
        applyChangesButton = findViewById(R.id.applyRequesterButton);
        deleteRequesterButton = findViewById(R.id.addRequesterButton);  // Bouton pour suppression

        // Remplir les champs avec les données récupérées
        firstNameEditText.setText(foundFirstName);
        lastNameEditText.setText(foundLastName);
        emailEditText.setText(foundEmail);
        passwordEditText.setText(foundPassword);

        // Sauvegarder les modifications
        applyChangesButton.setOnClickListener(v -> applyChanges());

        // Supprimer l'utilisateur
        deleteRequesterButton.setOnClickListener(v -> deleteRequester());
    }

    // Méthode pour appliquer les modifications
    private void applyChanges() {
        String updatedFirstName = firstNameEditText.getText().toString().trim();
        String updatedLastName = lastNameEditText.getText().toString().trim();
        String updatedEmail = emailEditText.getText().toString().trim();
        String updatedPassword = passwordEditText.getText().toString().trim();
        String updatetime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        databaseReference.orderByChild("email").equalTo(foundEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userId = userSnapshot.getKey();

                        if (userId != null) {
                            // Mettre à jour les champs dans Firebase
                            userSnapshot.getRef().child("first name").setValue(updatedFirstName);
                            userSnapshot.getRef().child("last name").setValue(updatedLastName);
                            userSnapshot.getRef().child("email").setValue(updatedEmail);
                            userSnapshot.getRef().child("password").setValue(updatedPassword);
                            userSnapshot.getRef().child("last modification").setValue(updatetime);
                            Toast.makeText(AdminActivity.this, "Utilisateur modifié avec succès", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Méthode pour supprimer l'utilisateur
    private void deleteRequester() {
        databaseReference.orderByChild("email").equalTo(foundEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Supprimer l'utilisateur de Firebase
                        userSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                            Toast.makeText(AdminActivity.this, "Utilisateur supprimé avec succès", Toast.LENGTH_SHORT).show();
                            setContentView(R.layout.activity_admin);  // Retourner au layout principal après suppression
                        }).addOnFailureListener(e -> {
                            Toast.makeText(AdminActivity.this, "Erreur lors de la suppression : " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
    }

    public void clearRequesters() {
        // Références à la base de données
        DatabaseReference requesterRef = FirebaseDatabase.getInstance().getReference("User");

        // Lire tous les requesters
        requesterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Vérifier si le userType est 'requester'
                        String userType = userSnapshot.child("userType").getValue(String.class);
                        if ("requester".equals(userType)) {
                            userSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                Log.d("AdminActivity", "Requester deleted: " + userSnapshot.getKey());
                            }).addOnFailureListener(e -> {
                                Log.e("AdminActivity", "Failed to delete requester: " + e.getMessage());
                            });
                        }
                    }
                    Toast.makeText(AdminActivity.this, "Tous les requesters ont été supprimés avec succès.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminActivity.this, "Aucun requester à supprimer.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminActivity", "Error while checking requesters: " + databaseError.getMessage());
            }
        });
    }


    // Méthode séparée pour supprimer les composants logiciels
    private void clearSoftwareComponents() {
        DatabaseReference componentsRef = FirebaseDatabase.getInstance().getReference("Components").child("Software");

        componentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot softwareSnapshot) {
                if (softwareSnapshot.exists()) {
                    for (DataSnapshot softwareComponent : softwareSnapshot.getChildren()) {
                        String subType = softwareComponent.child("subType").getValue(String.class);
                        if ("processeur".equals(subType) || "carte mère".equals(subType)) {
                            softwareComponent.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                Log.d("AdminActivity", "Software component deleted: " + softwareComponent.getKey());
                            }).addOnFailureListener(e -> {
                                Log.e("AdminActivity", "Failed to delete software component: " + e.getMessage());
                            });

                            DatabaseReference componentsRef = FirebaseDatabase.getInstance().getReference("Components");
                            DatabaseReference softwareRef = componentsRef.child("Software");

                            // Utilisez une clé fixe "status" pour le nouveau composant
                            String softwareId = "status"; // Clé fixe

                            // Créer un HashMap pour stocker les détails du logiciel
                            HashMap<String, String> softwareDetails = new HashMap<>();
                            softwareDetails.put("status", "le stockage de software:");
                            // Vous pouvez ajouter d'autres attributs ici

                            // Ajouter le logiciel à la base de données
                            softwareRef.child(softwareId).setValue(softwareDetails)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("AdminActivity", "Software component added successfully: " + softwareId);
                                        Toast.makeText(AdminActivity.this, "Composant Software ajouté avec succès.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("AdminActivity", "Failed to add software component: " + e.getMessage());
                                        Toast.makeText(AdminActivity.this, "Erreur lors de l'ajout du composant Software: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                    Toast.makeText(AdminActivity.this, "Tous les composants Software ont été supprimés.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("AdminActivity", "No software components found.");
                    Toast.makeText(AdminActivity.this, "Aucun composant Software à supprimer.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminActivity", "Error while checking software components: " + databaseError.getMessage());
            }
        });
    }

    private void clearHardwareComponents() {
        DatabaseReference componentsRef = FirebaseDatabase.getInstance().getReference("Components").child("Hardware");

        componentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot hardwareSnapshot) {
                if (hardwareSnapshot.exists()) {
                    for (DataSnapshot hardwareComponent : hardwareSnapshot.getChildren()) {
                        String subType = hardwareComponent.child("subType").getValue(String.class);
                        if ("boitier".equals(subType) ) {
                            hardwareComponent.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                Log.d("AdminActivity", "Hardware component deleted: " + hardwareComponent.getKey());
                            }).addOnFailureListener(e -> {
                                Log.e("AdminActivity", "Failed to delete Hardware component: " + e.getMessage());
                            });

                            DatabaseReference componentsRef = FirebaseDatabase.getInstance().getReference("Components");
                            DatabaseReference hardwareRef = componentsRef.child("Hardware");

                            // Utilisez une clé fixe "status" pour le nouveau composant
                            String hardwareId = "status"; // Clé fixe

                            // Créer un HashMap pour stocker les détails du logiciel
                            HashMap<String, String> hardwareDetails = new HashMap<>();
                            hardwareDetails.put("status", "le stockage de Hardware:");
                            // Vous pouvez ajouter d'autres attributs ici

                            // Ajouter le logiciel à la base de données
                            hardwareRef.child(hardwareId).setValue(hardwareDetails)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("AdminActivity", "Hardware component added successfully: " + hardwareId);
                                        Toast.makeText(AdminActivity.this, "Composant Software ajouté avec succès.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("AdminActivity", "Failed to add hardware component: " + e.getMessage());
                                        Toast.makeText(AdminActivity.this, "Erreur lors de l'ajout du composant Hardware: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                    Toast.makeText(AdminActivity.this, "Tous les composants Software ont été supprimés.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("AdminActivity", "No software components found.");
                    Toast.makeText(AdminActivity.this, "Aucun composant Software à supprimer.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminActivity", "Error while checking hardware components: " + databaseError.getMessage());
            }
        });
    }




}



