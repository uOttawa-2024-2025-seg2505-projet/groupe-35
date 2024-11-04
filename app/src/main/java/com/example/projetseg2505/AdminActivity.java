package com.example.projetseg2505;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

public class AdminActivity extends AppCompatActivity {

    //dataBase refs
    private DatabaseReference userDatabaseRef, componentDatabaseRef, ordersDatabaseRef;

    // Add requester variables
    private EditText emailNewRequester, passwordNewRequester, firstNameNewRequester, lastNameNewRequester;
    private Button addRequesterButton;
    private TextView errorTextAddRequester, textAddedRequester;

    // Requester search for modification or suppression
    private TextView errorTextEmailInput;

    // Edit/Delete variables
    private Button sendToAddRequesterLayoutButton, senToEditDeleteRequesterButton, logoutButton, applyChangesButton, deleteRequesterButton;
    private EditText emailEditText, firstNameEditText, lastNameEditText, passwordEditText;
    private View clearDatabaseButton;

    // Found requester details
    private String foundFirstName, foundLastName, foundEmail, foundPassword;

    //reset methods variable
    private Button resetDatabase, resetStock;
    private ArrayList<String> listJsonFileNames;
    private Spinner fileNameSpinner;
    private boolean fileSelected;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        //Database refs
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("User");
        componentDatabaseRef = FirebaseDatabase.getInstance().getReference("Components");
        ordersDatabaseRef = FirebaseDatabase.getInstance().getReference("Orders");



        // Initialization of views in the admin layout

        sendToAddRequesterLayoutButton = findViewById(R.id.sendToAddRequesterLayoutButton);
        senToEditDeleteRequesterButton = findViewById(R.id.senToEditDeleteRequesterButton);
        logoutButton = findViewById(R.id.logoutButton);
        emailEditText = findViewById(R.id.emailEditText);
        errorTextEmailInput = findViewById(R.id.errorTextEmailInput);
        resetDatabase = findViewById(R.id.resetDatabase);
        resetStock = findViewById(R.id.resetStock);
        fileNameSpinner = findViewById(R.id.fileNameSpinner);
        listJsonFileNames = new ArrayList<>();

        // Log Out functionality
        logoutButton.setOnClickListener(v -> finish());


        //Select file to read from spinner
        fileNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFileName = parent.getItemAtPosition(position).toString();
                if (!selectedFileName.equals("Select one file or more")) {
                    addFileNameToJsonFilesArray(selectedFileName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "Nothing selected ");
            }

        });

        // resetdatabase and load from JSON
        resetDatabase.setOnClickListener(view -> {
            if(listJsonFileNames.isEmpty()){
                Toast.makeText(AdminActivity.this, "Error: You will need to select at least one file" , Toast.LENGTH_SHORT).show();
            }
            else{
                clearRequesters(userDatabaseRef);
                clearSoftwareComponents(componentDatabaseRef);
                clearHardwareComponents(componentDatabaseRef);
                loadRequestersFromManyJsonFiles(view.getContext());
                loadStockFromManyJsonFiles(view.getContext());
                fileNameSpinner.setSelection(0);
                listJsonFileNames.clear();

            }


        });

        //reset Stock from json file
        resetStock.setOnClickListener(view -> {
            if(listJsonFileNames.isEmpty()){
                Toast.makeText(AdminActivity.this, "Error: You will need to select at least one file" , Toast.LENGTH_SHORT).show();
            }
            else {
                clearSoftwareComponents(componentDatabaseRef);
                clearHardwareComponents(componentDatabaseRef);
                loadStockFromManyJsonFiles(view.getContext());
                fileNameSpinner.setSelection(0);
                listJsonFileNames.clear();

            }

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
                    userDatabaseRef.child(emailNewRequesterString.replace(".", ",")).get().addOnCompleteListener(task -> {
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

                            userDatabaseRef.child(emailNewRequesterString.replace(".", ",")).setValue(hashMap)
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

        // Firebase reference initialization
        clearDatabaseButton = findViewById(R.id.button_clear_database);
        clearDatabaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearRequesters(userDatabaseRef);
                clearSoftwareComponents(componentDatabaseRef);
                clearHardwareComponents(componentDatabaseRef);
                clearOrders(ordersDatabaseRef);
            }
        });
    }

    // Method to search requester by email
    private void searchRequesterByEmail(String email) {
        userDatabaseRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
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

        userDatabaseRef.orderByChild("email").equalTo(foundEmail).addListenerForSingleValueEvent(new ValueEventListener() {
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
        userDatabaseRef.orderByChild("email").equalTo(foundEmail).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void clearRequesters(DatabaseReference requesterRef) {

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


    // Methods to clear Software Components
    private void clearSoftwareComponents(DatabaseReference componentRef) {
        DatabaseReference softwareRef = componentRef.child("Software");

        softwareRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot softwareSnapshot) {
                if (softwareSnapshot.exists()) {
                    for (DataSnapshot softwareComponent : softwareSnapshot.getChildren()) {
                        // Skip the component named "status"
                        if ("status".equals(softwareComponent.getKey())) {

                            continue; // Skip this entry
                        }
                        // Delete other software components
                        softwareComponent.getRef().removeValue().addOnSuccessListener(aVoid -> {

                        }).addOnFailureListener(e -> {

                        });
                    }
                    Toast.makeText(AdminActivity.this, "All the Software components were deleted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminActivity.this, "Software components Not CLeared.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminActivity", "Error while deleting software components: " + databaseError.getMessage());
            }
        });
    }

    //Method to clear Hardware Components
    public void clearHardwareComponents(DatabaseReference componentRef) {
        // Reference to the Hardware components in the database
        DatabaseReference hardwareRef = componentRef.child("Hardware");

        // Retrieve all hardware components and selectively delete them
        hardwareRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot hardwareSnapshot : dataSnapshot.getChildren()) {
                        // Check if the current child is named "status" and skip it
                        if ("status".equals(hardwareSnapshot.getKey())) {
                            Log.d("AdminActivity", "Skipping preserved hardware component with key: " + hardwareSnapshot.getKey());
                            continue; // Skip this entry
                        }
                        // Delete other hardware components
                        hardwareSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                        }).addOnFailureListener(e -> {
                        });
                    }
                    Toast.makeText(AdminActivity.this, "All Hardware componetns were deleted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminActivity.this, "Hardware components were not deleted.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminActivity", "Error while deleting hardware components: " + databaseError.getMessage());
            }
        });
    }


    //Clear Components
    public void clearOrders(DatabaseReference ordersRef) {

        // Retrieve all orders and selectively delete them
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {

                        if ("Orders".equals(orderSnapshot.getKey())) {
                            continue;
                        }
                        // Delete other orders
                        orderSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                            Log.d("AdminActivity", "Order deleted: " + orderSnapshot.getKey());
                        }).addOnFailureListener(e -> {
                            Log.e("AdminActivity", "Failed to delete order: " + e.getMessage());
                        });
                    }
                    Toast.makeText(AdminActivity.this, "All the orders have been deleted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminActivity.this, "Clearing unsuccesful.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminActivity", "Error while deleting orders: " + databaseError.getMessage());
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

    //load requesters from many json files
    public void loadRequestersFromManyJsonFiles(Context context){
        if(listJsonFileNames.size() == 0){
            Toast.makeText(context, "You will need to select at least a file", Toast.LENGTH_SHORT).show();
        }
        else{
            for(int i=0; i < listJsonFileNames.size(); i++){
                String fileName = listJsonFileNames.get(i);
                loadRequestersFromJson(context,fileName);
            }

        }

    }


    public void addFileNameToJsonFilesArray(String newJsonFileName){
        if (!listJsonFileNames.contains(newJsonFileName)) {
            listJsonFileNames.add(newJsonFileName);
        }
    }

    // load requesters from a json file
    public void loadRequestersFromJson(Context context, String fileName) {
        try {
            String jsonString = readJsonFile(context, fileName);
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONObject usersObject = jsonObject.getJSONObject("User");
            for (Iterator<String> it = usersObject.keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject userObject = usersObject.getJSONObject(key);

                String emailNewRequesterString = userObject.getString("email");
                String passwordNewRequesterString = userObject.getString("password");
                String firstNameNewRequesterString = userObject.getString("first name");
                String lastNameNewRequesterString = userObject.getString("last name");
                String userType = userObject.getString("userType");

                if (userType.equals("requester")) {
                    String finalEmail = emailNewRequesterString.replace(".", ",");
                    userDatabaseRef.orderByChild("email").equalTo(finalEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Log.d("Firebase", "Requester already exists: " + emailNewRequesterString);
                            } else{
                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("email", emailNewRequesterString);
                                hashMap.put("password", passwordNewRequesterString);
                                hashMap.put("userType", userType);
                                hashMap.put("first name", firstNameNewRequesterString);
                                hashMap.put("last name", lastNameNewRequesterString);
                                hashMap.put("date of creation", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
                                hashMap.put("date of the last modification", userObject.optString("last modification", ""));
                                userDatabaseRef.child(finalEmail).setValue(hashMap)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Log.d("Firebase", "Requester successfully added: " + emailNewRequesterString);
                                                Toast.makeText(context, "Requester added successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.e("Firebase", "Error adding requester: " + emailNewRequesterString + ". " + task1.getException().getMessage());
                                            }
                                        });

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(context, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }catch (JSONException e){
            Toast.makeText(context, "JSON Parsing Error", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadStockFromManyJsonFiles(Context context){
        if(listJsonFileNames.size() == 0){
            Toast.makeText(context, "You will need to select at least a file", Toast.LENGTH_SHORT).show();
        }
        else {
            for (int i = 0; i < listJsonFileNames.size(); i++) {
                String fileName = listJsonFileNames.get(i);
                loadStockFromJson(context, fileName);
            }
        }
    }

    private void loadStockFromJson(Context context, String fileName){
        loadHarwareComponentsFromJson(context,fileName);
        loadSoftwareComponentsFromJson(context,fileName);
    }

    private void loadHarwareComponentsFromJson(Context context, String fileName) {
        try {
            String jsonString = readJsonFile(context, fileName);
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONObject hardwareComponents = jsonObject.getJSONObject("Components").getJSONObject("Hardware");
            Queue<JSONObject> componentQueue = new LinkedList<>();
            for (Iterator<String> it = hardwareComponents.keys(); it.hasNext();) {
                String key = it.next();
                JSONObject hardwareComponent = hardwareComponents.getJSONObject(key);
                componentQueue.add(hardwareComponent);
            }
            processComponentsWithHandler(context, componentQueue);

        }catch (JSONException e){
            Toast.makeText(context, "JSON Parsing Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSoftwareComponentsFromJson(Context context, String fileName) {
        try {
            String jsonString = readJsonFile(context, fileName);
            JSONObject jsonObject = new JSONObject(jsonString);
            Queue<JSONObject> componentQueue = new LinkedList<>();
            JSONObject softwareComponents = jsonObject.getJSONObject("Components").getJSONObject("Software");
            for (Iterator<String> it = softwareComponents.keys(); it.hasNext();) {
                String key = it.next();
                JSONObject softwareComponent = softwareComponents.getJSONObject(key);
                componentQueue.add(softwareComponent);
            }
            processComponentsWithHandler(context, componentQueue);


        }catch (JSONException e){
            Toast.makeText(context, "JSON Parsing Error", Toast.LENGTH_SHORT).show();
        }
    }

    private void processComponentsWithHandler(Context context, Queue<JSONObject> componentQueue) {
        Handler handler = new Handler();

        Runnable processNext = new Runnable() {
            @Override
            public void run() {
                if (componentQueue.isEmpty()) {
                    return;
                }

                JSONObject hardwareComponent = componentQueue.poll();
                try {
                    String componentTypeJsonObject = hardwareComponent.getString("type");
                    String descriptionJsonObject = hardwareComponent.getString("description");
                    String componentSubtypeJsonObject = hardwareComponent.getString("subType");
                    String quantityJsonObject = hardwareComponent.getString("quantity");
                    int intQuantity = Integer.parseInt(quantityJsonObject);
                    String commentJsonObject = hardwareComponent.getString("comment");


                    StorekeeperActivity.checkIfItemExistsAndAdd(descriptionJsonObject, componentTypeJsonObject, componentSubtypeJsonObject, intQuantity, commentJsonObject, componentDatabaseRef, null);


                    handler.postDelayed(this, 100);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };


        handler.post(processNext);
    }






    /*private void loadStockFromJson(Context context, String fileName) {
        try {
            // Lire le fichier JSON
            String jsonString = readJsonFile(context, fileName);
            JSONObject jsonObject = new JSONObject(jsonString);  // C'est un objet JSON, pas un tableau

            // Référence vers la base de données Firebase pour les composants
            DatabaseReference componentsRef = FirebaseDatabase.getInstance().getReference("Components");

            // Vérifier si le noeud "Components" existe
            componentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        // Créer le noeud "Components" s'il n'existe pas
                        componentsRef.setValue(new HashMap<String, Object>())
                                .addOnSuccessListener(aVoid -> Log.d("AdminActivity", "Node 'Components' created."))
                                .addOnFailureListener(e -> Log.e("AdminActivity", "Failed to create 'Components' node: " + e.getMessage()));
                    }

                    // Charger les composants hardware et software après avoir vérifié la création du noeud
                    loadHardwareAndSoftwareComponents(componentsRef, jsonObject);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("AdminActivity", "Database error: " + databaseError.getMessage());
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadHardwareAndSoftwareComponents(DatabaseReference componentsRef, JSONObject jsonObject) {
        try {
            // Récupérer la section "Components"
            JSONObject components = jsonObject.getJSONObject("Components");

            // Charger les composants hardware
            JSONObject hardwareComponents = components.getJSONObject("Hardware");
            for (Iterator<String> it = hardwareComponents.keys(); it.hasNext();) {
                String key = it.next();
                JSONObject component = hardwareComponents.getJSONObject(key);

                // Récupérer la description pour l'utiliser comme clé unique
                String description = component.getString("description");

                // Utiliser un HashMap pour stocker les détails du composant hardware
                HashMap<String, Object> hardwareDetails = new HashMap<>();
                for (Iterator<String> compKeys = component.keys(); compKeys.hasNext();) {
                    String compKey = compKeys.next();
                    hardwareDetails.put(compKey, component.get(compKey));
                }

                // Ajouter le composant dans Firebase en utilisant la description comme clé unique
                componentsRef.child("Hardware").child(description).setValue(hardwareDetails)
                        .addOnSuccessListener(aVoid -> Log.d("AdminActivity", "Hardware component added: " + description))
                        .addOnFailureListener(e -> Log.e("AdminActivity", "Failed to add hardware component: " + e.getMessage()));
            }

            // Charger les composants software
            JSONObject softwareComponents = components.getJSONObject("Software");
            for (Iterator<String> it = softwareComponents.keys(); it.hasNext();) {
                String key = it.next();
                JSONObject component = softwareComponents.getJSONObject(key);

                // Récupérer la description pour l'utiliser comme clé unique
                String description = component.getString("description");

                // Créer un HashMap pour stocker les détails du composant software
                HashMap<String, Object> softwareDetails = new HashMap<>();
                for (Iterator<String> compKeys = component.keys(); compKeys.hasNext();) {
                    String compKey = compKeys.next();
                    softwareDetails.put(compKey, component.get(compKey));
                }

                // Ajouter le composant dans Firebase en utilisant la description comme clé unique
                componentsRef.child("Software").child(description).setValue(softwareDetails)
                        .addOnSuccessListener(aVoid -> Log.d("AdminActivity", "Software component added: " + description))
                        .addOnFailureListener(e -> Log.e("AdminActivity", "Failed to add software component: " + e.getMessage()));
            }

            Log.i("AdminActivity", "Chargement des composants du stock depuis le JSON terminé.");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/



}