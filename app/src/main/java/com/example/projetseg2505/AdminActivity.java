package com.example.projetseg2505;

import android.annotation.SuppressLint;
import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    // Add requester variables
    private EditText emailNewRequester, passwordNewRequester, firstNameNewRequester, lastNameNewRequester;
    private Button addRequesterButton, addFromJsonButton;
    private DatabaseReference databaseRef;
    private TextView errorTextAddRequester, textAddedRequester;

    // Requester search for modification or suppression
    private TextView errorTextEmailInput;

    // Edit/Delete variables
    private Button sendToAddRequesterLayoutButton, senToEditDeleteRequesterButton, logoutButton, applyChangesButton, deleteRequesterButton;
    private EditText emailEditText, firstNameEditText, lastNameEditText, passwordEditText;
    private DatabaseReference databaseReference;

    // Found requester details
    private String foundFirstName, foundLastName, foundEmail, foundPassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialization of views in the admin layout
        sendToAddRequesterLayoutButton = findViewById(R.id.sendToAddRequesterLayoutButton);
        senToEditDeleteRequesterButton = findViewById(R.id.senToEditDeleteRequesterButton);
        logoutButton = findViewById(R.id.logoutButton);
        emailEditText = findViewById(R.id.emailEditText);
        errorTextEmailInput = findViewById(R.id.errorTextEmailInput);
        addFromJsonButton = findViewById(R.id.addFromJsonButton);

        // Firebase reference initialization
        databaseRef = FirebaseDatabase.getInstance().getReference().child("User");
        databaseReference = FirebaseDatabase.getInstance().getReference("User");

        // Log Out functionality
        logoutButton.setOnClickListener(v -> finish());

        // ADD requesters from JSON
        addFromJsonButton.setOnClickListener(view -> {
            // Appel de la méthode pour charger les requesters depuis le fichier JSON

            loadRequestersFromJson(view.getContext(), "requesters.json");

        });

        // ADD a requester manually
        sendToAddRequesterLayoutButton.setOnClickListener(v -> {
            // Switch to the Add Requester layout
            setContentView(R.layout.activity_admin_add_requester);

            // Initialize views for adding requester (from the add requester layout)
            firstNameNewRequester = findViewById(R.id.firstNameNewRequester);
            lastNameNewRequester = findViewById(R.id.lastNameNewRequester);
            emailNewRequester = findViewById(R.id.emailNewRequester);
            passwordNewRequester = findViewById(R.id.passwordNewRequester);
            addRequesterButton = findViewById(R.id.addRequesterButton);
            errorTextAddRequester = findViewById(R.id.errorTextAddRequester);
            textAddedRequester = findViewById(R.id.textAddedRequester);

            addRequesterButton.setOnClickListener(view -> {
                String passwordNewRequesterString = passwordNewRequester.getText().toString();
                String emailNewRequesterString = emailNewRequester.getText().toString();
                String userType = "requester";
                String firstNameNewRequesterString = firstNameNewRequester.getText().toString();
                String lastNameNewRequesterString = lastNameNewRequester.getText().toString();
                String dateCreationRequester = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                String dateModificationRequester = "";

                errorTextAddRequester.setVisibility(View.GONE);
                textAddedRequester.setVisibility(View.GONE);

                HashMap<String, String> hashMap = new HashMap<>();

                if (!emailNewRequesterString.isEmpty() && !passwordNewRequesterString.isEmpty()) {
                    databaseRef.child(emailNewRequesterString.replace(".", ",")).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            errorTextAddRequester.setText("This requester already has an account");
                            errorTextAddRequester.setVisibility(View.VISIBLE);
                        } else {
                            hashMap.put("email", emailNewRequesterString);
                            hashMap.put("password", passwordNewRequesterString);
                            hashMap.put("userType", userType);
                            hashMap.put("first name", firstNameNewRequesterString);
                            hashMap.put("last name", lastNameNewRequesterString);
                            hashMap.put("date of creation", dateCreationRequester);
                            hashMap.put("date of the last modification", dateModificationRequester);

                            databaseRef.child(emailNewRequesterString.replace(".", ",")).setValue(hashMap)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            textAddedRequester.setText("Requester Added");
                                            textAddedRequester.setVisibility(View.VISIBLE);
                                            errorTextAddRequester.setVisibility(View.GONE);
                                        } else {
                                            textAddedRequester.setText("Error while adding requester");
                                            textAddedRequester.setVisibility(View.VISIBLE);
                                            errorTextAddRequester.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    });
                } else {
                    errorTextAddRequester.setText("Please enter email and password");
                    errorTextAddRequester.setVisibility(View.VISIBLE);
                }
            });
        });

        // Requester Search and sending to edit/delete layout
        senToEditDeleteRequesterButton.setOnClickListener(v -> {
            String emailToSearch = emailEditText.getText().toString().trim();
            if (!emailToSearch.isEmpty()) {
                searchRequesterByEmail(emailToSearch);
            } else {
                errorTextEmailInput.setText("Please enter email");
                errorTextEmailInput.setVisibility(View.VISIBLE);
            }
        });
    }

    // Method to search requester by email
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

                        switchToModifyDeleteLayout();
                    }
                } else {
                    errorTextEmailInput.setText("Please enter a valid email");
                    errorTextEmailInput.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Switch to modify/delete layout
    private void switchToModifyDeleteLayout() {
        setContentView(R.layout.activity_admin_edit_delete_requester);

        // Initialize views for modifying/deleting requesters (from the edit/delete layout)
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        applyChangesButton = findViewById(R.id.applyRequesterButton);
        deleteRequesterButton = findViewById(R.id.addRequesterButton);

        // Populate the fields with found requester data
        firstNameEditText.setText(foundFirstName);
        lastNameEditText.setText(foundLastName);
        emailEditText.setText(foundEmail);
        passwordEditText.setText(foundPassword);

        // Save changes to requester
        applyChangesButton.setOnClickListener(v -> applyChanges());

        // Delete requester
        deleteRequesterButton.setOnClickListener(v -> deleteRequester());
    }

    // Method to apply changes to a requester
    private void applyChanges() {
        String updatedFirstName = firstNameEditText.getText().toString().trim();
        String updatedLastName = lastNameEditText.getText().toString().trim();
        String updatedEmail = emailEditText.getText().toString().trim();
        String updatedPassword = passwordEditText.getText().toString().trim();
        String updateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        databaseReference.orderByChild("email").equalTo(foundEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userId = userSnapshot.getKey();

                        if (userId != null) {
                            // Update fields in Firebase
                            userSnapshot.getRef().child("first name").setValue(updatedFirstName);
                            userSnapshot.getRef().child("last name").setValue(updatedLastName);
                            userSnapshot.getRef().child("email").setValue(updatedEmail);
                            userSnapshot.getRef().child("password").setValue(updatedPassword);
                            userSnapshot.getRef().child("date of the last modification").setValue(updateTime);
                            Toast.makeText(AdminActivity.this, "Requester updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Error while updating", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to delete a requester
    private void deleteRequester() {
        String currentFirstName = firstNameEditText.getText().toString().trim();
        String currentLastName = lastNameEditText.getText().toString().trim();
        String currentPassword = passwordEditText.getText().toString().trim();

        // Search for the requester by email
        databaseReference.orderByChild("email").equalTo(foundEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Fetch the information stored in the database and compare it
                        if (foundFirstName.equals(currentFirstName) &&
                                foundLastName.equals(currentLastName) &&
                                foundPassword.equals(currentPassword)) {

                            // If all information matches, proceed to delete the requester
                            userSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                Toast.makeText(AdminActivity.this, "Requester deleted successfully", Toast.LENGTH_SHORT).show();
                                setContentView(R.layout.activity_admin);  // Return to the main layout after deletion
                            }).addOnFailureListener(e -> {
                                Toast.makeText(AdminActivity.this, "Error while deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                        } else {
                            // If the information does not match, show an error message
                            Toast.makeText(AdminActivity.this, "Requester's information does not match", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(AdminActivity.this, "No requester found with this email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lire un fichier JSON
    public String readJsonFile(Context context, String fileName) {
        StringBuilder jsonContent = new StringBuilder();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonContent.toString();
    }

    // Charger les requesters du fichier JSON
    public void loadRequestersFromJson(Context context, String fileName) {
        try {
            // Lire le fichier JSON
            String jsonString = readJsonFile(context, fileName);
            JSONObject jsonObject = new JSONObject(jsonString);  // C'est un objet JSON, pas un tableau

            // Récupérer la section "User"
            JSONObject usersObject = jsonObject.getJSONObject("User");

            // Parcourir chaque utilisateur
            for (Iterator<String> it = usersObject.keys(); it.hasNext();) {
                String key = it.next();
                JSONObject userObject = usersObject.getJSONObject(key);

                // Extraire les informations du JSON
                String emailNewRequesterString = userObject.getString("email");
                String passwordNewRequesterString = userObject.getString("password");
                String firstNameNewRequesterString = userObject.getString("first name");
                String lastNameNewRequesterString = userObject.getString("last name");
                String userType = userObject.getString("userType");

                // Vérification sans collision avant d'ajouter dans la base
                databaseRef.child(emailNewRequesterString.replace(".", ",")).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // Si le requester existe déjà, ne pas l'ajouter
                        Log.d("Firebase", "Collision détectée pour le requester avec l'email : " + emailNewRequesterString);
                    } else {
                        // Ajouter le requester s'il n'existe pas
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("email", emailNewRequesterString);
                        hashMap.put("password", passwordNewRequesterString);
                        hashMap.put("userType", userType);
                        hashMap.put("first name", firstNameNewRequesterString);
                        hashMap.put("last name", lastNameNewRequesterString);
                        hashMap.put("date of creation", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
                        hashMap.put("date of the last modification", userObject.optString("last modification", "")); // Optionnel

                        databaseRef.child(emailNewRequesterString.replace(".", ",")).setValue(hashMap)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d("Firebase", "Requester ajouté avec succès : " + emailNewRequesterString);
                                        Toast.makeText(AdminActivity.this, "Requesters added successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.e("Firebase", "Erreur lors de l'ajout du requester : " + emailNewRequesterString);
                                    }
                                });
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


}
}
