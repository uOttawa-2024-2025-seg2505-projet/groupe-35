package com.example.projetseg2505;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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

    //return buttons
    private Button returnButtonModifyDeleteLayout, returnButtonAddRequester;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        initializeMainLayout();

    }


    //methods for initialization
    private void initializeMainLayout() {
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("User");
        componentDatabaseRef = FirebaseDatabase.getInstance().getReference("Components");
        ordersDatabaseRef = FirebaseDatabase.getInstance().getReference("Orders");

        sendToAddRequesterLayoutButton = findViewById(R.id.sendToAddRequesterLayoutButton);
        senToEditDeleteRequesterButton = findViewById(R.id.senToEditDeleteRequesterButton);
        logoutButton = findViewById(R.id.logoutButton);
        emailEditText = findViewById(R.id.emailEditText);
        errorTextEmailInput = findViewById(R.id.errorTextEmailInput);
        resetDatabase = findViewById(R.id.resetDatabase);
        resetStock = findViewById(R.id.resetStock);
        fileNameSpinner = findViewById(R.id.fileNameSpinner);
        clearDatabaseButton = findViewById(R.id.button_clear_database);

        listJsonFileNames = new ArrayList<>();

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        sendToAddRequesterLayoutButton.setOnClickListener(v -> {
            setContentView(R.layout.activity_admin_add_requester);
            initializeAddRequesterLayout();
        });
        senToEditDeleteRequesterButton.setOnClickListener(v -> {
            initializeEditDeleteRequester();
        });


        // Initialization of listeners
        initializeFileNameSpinnerListener();
        initializeResetDatabaseButtonListener();
        initializeResetStockButtonListener();
        initializeClearDatabaseButtonListener();
    }

    private void initializeAddRequesterLayout() {
        firstNameNewRequester = findViewById(R.id.firstNameNewRequester);
        lastNameNewRequester = findViewById(R.id.lastNameNewRequester);
        emailNewRequester = findViewById(R.id.emailNewRequester);
        passwordNewRequester = findViewById(R.id.passwordNewRequester);
        addRequesterButton = findViewById(R.id.addRequesterButton);
        errorTextAddRequester = findViewById(R.id.errorTextAddRequester);
        textAddedRequester = findViewById(R.id.textAddedRequester);


        returnButtonAddRequester = findViewById(R.id.returnButtonAddRequester);
        returnButtonAddRequester.setOnClickListener(v -> {
            setContentView(R.layout.activity_admin);
            initializeMainLayout();
        });

        addRequesterButton.setOnClickListener(view -> {
            String passwordNewRequesterString = passwordNewRequester.getText().toString();
            String emailNewRequesterString = emailNewRequester.getText().toString();
            String userType = "requester";
            String firstNameNewRequesterString = firstNameNewRequester.getText().toString();
            String lastNameNewRequesterString = lastNameNewRequester.getText().toString();
            addRequester(emailNewRequesterString, passwordNewRequesterString, userType, firstNameNewRequesterString, lastNameNewRequesterString, errorTextAddRequester, textAddedRequester);
        });
    }
    private void initializeEditDeleteRequester() {
        String emailToSearch = emailEditText.getText().toString().trim();
        if (!emailToSearch.isEmpty()) {
            searchRequesterByEmail(emailToSearch);
        } else {
            errorTextEmailInput.setText("Please enter email");
            errorTextEmailInput.setVisibility(View.VISIBLE);
        }
    }

    private void initializeClearDatabaseButtonListener() {
        clearDatabaseButton.setOnClickListener(v -> {
            clearRequesters(userDatabaseRef, () -> {
                clearSoftwareComponents(componentDatabaseRef, () -> {
                    clearHardwareComponents(componentDatabaseRef, () -> {
                        clearOrders(ordersDatabaseRef);
                    });
                });
            });
        });
    }
    private void initializeFileNameSpinnerListener() {
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
                Log.d(TAG, "Nothing selected");
            }
        });
    }
    private void initializeResetDatabaseButtonListener() {
        resetDatabase.setOnClickListener(view -> {
            if (listJsonFileNames.isEmpty()) {
                Toast.makeText(AdminActivity.this, "Error: You will need to select at least one file", Toast.LENGTH_SHORT).show();
            } else {
                clearRequesters(userDatabaseRef, () -> {
                    clearSoftwareComponents(componentDatabaseRef, () -> {
                        clearHardwareComponents(componentDatabaseRef, () -> {
                            loadRequestersFromManyJsonFiles(view.getContext());
                            loadStockFromManyJsonFiles(view.getContext());
                            fileNameSpinner.setSelection(0);
                            listJsonFileNames.clear();
                        });
                    });
                });
            }
        });
    }
    private void initializeResetStockButtonListener() {
        resetStock.setOnClickListener(view -> {
            if (listJsonFileNames.isEmpty()) {
                Toast.makeText(AdminActivity.this, "Error: You will need to select at least one file", Toast.LENGTH_SHORT).show();
            } else {
                clearSoftwareComponents(componentDatabaseRef, () -> {
                    clearHardwareComponents(componentDatabaseRef, () -> {
                        loadStockFromManyJsonFiles(view.getContext());
                        fileNameSpinner.setSelection(0);
                        listJsonFileNames.clear();
                    });
                });
            }
        });
    }

    //Method to add a requester
    private void addRequester( String email, String password, String userType,String firstName,String lastName,TextView errorText, TextView succesText) {

        String dateCreationRequester = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        String dateModificationRequester = "";
        if (errorText != null) {
            errorText.setVisibility(View.GONE);
            succesText.setVisibility(View.GONE);
        }

        HashMap<String, String> hashMap = new HashMap<>();

        if (!email.isEmpty() && !password.isEmpty()) {
            userDatabaseRef.child(email.replace(".", ",")).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    if (errorText != null){
                        errorText.setText("This requester already has an account");
                        errorText.setVisibility(View.VISIBLE);
                    }

                } else {
                    hashMap.put("email", email);
                    hashMap.put("password", password);
                    hashMap.put("userType", userType);
                    hashMap.put("first name", firstName);
                    hashMap.put("last name", lastName);
                    hashMap.put("date of creation", dateCreationRequester);
                    hashMap.put("date of the last modification", dateModificationRequester);

                    userDatabaseRef.child(email.replace(".", ",")).setValue(hashMap)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    if (errorText != null) {
                                        succesText.setText("Requester Added");
                                        succesText.setTextColor(Color.parseColor("#0000FF"));
                                        succesText.setVisibility(View.VISIBLE);
                                        errorText.setVisibility(View.GONE);
                                        new android.os.Handler().postDelayed(() -> {
                                            clearAddRequesterInputs(succesText);
                                            succesText.setTextColor(Color.parseColor("#FF0000"));
                                            succesText.setVisibility(View.GONE);
                                        }, 2000);

                                    }
                                } else {
                                    if (errorText != null) {
                                        succesText.setText("Error while adding requester");
                                        succesText.setVisibility(View.VISIBLE);
                                        errorText.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            });
        } else {
            if (errorText != null) {
                errorText.setText("Please enter email and password");
                errorText.setVisibility(View.VISIBLE);
            }
        }

    }

    private void clearAddRequesterInputs(TextView succesText){
        firstNameNewRequester.setText("");
        lastNameNewRequester.setText("");
        emailNewRequester.setText("");
        passwordNewRequester.setText("");

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
    @SuppressLint("MissingInflatedId")
    private void switchToModifyDeleteLayout() {
        setContentView(R.layout.activity_admin_edit_delete_requester);

        // Initialize views for modifying/deleting requesters (from the edit/delete layout)
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        applyChangesButton = findViewById(R.id.applyRequesterButton);
        deleteRequesterButton = findViewById(R.id.addRequesterButton);

        returnButtonModifyDeleteLayout = findViewById(R.id.returnButtonModifyDeleteLayout);
        returnButtonModifyDeleteLayout.setOnClickListener(v -> {
            setContentView(R.layout.activity_admin);
            initializeMainLayout();
        });

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

        userDatabaseRef.orderByChild("email").equalTo(foundEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        if (foundFirstName.equals(currentFirstName) &&
                                foundLastName.equals(currentLastName) &&
                                foundPassword.equals(currentPassword)) {

                            ordersDatabaseRef.child(foundEmail.replace('.', ',')).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        userSnapshot.getRef().removeValue();
                                    });

                        } 
                    }
                } 
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: ");
            }
        });
    }


    public void clearRequesters(DatabaseReference requesterRef, Runnable onComplete) {
        requesterRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Task<Void>> deletionTasks = new ArrayList<>();

                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userType = userSnapshot.child("userType").getValue(String.class);
                        if ("requester".equals(userType)) {
                            deletionTasks.add(userSnapshot.getRef().removeValue()
                                    .addOnSuccessListener(aVoid -> Log.d("AdminActivity", "Requester deleted: " + userSnapshot.getKey()))
                                    .addOnFailureListener(e -> Log.e("AdminActivity", "Failed to delete requester: " + e.getMessage())));
                        }
                    }

                    if (deletionTasks.isEmpty()) {
                        Log.d(TAG, "onDataChange: ");                        
                        if (onComplete != null) {
                            onComplete.run();
                        }
                        return;
                    }

                    // Wait for all deletions to complete
                    Tasks.whenAllComplete(deletionTasks).addOnCompleteListener(task -> {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });

                } else {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminActivity", "Error while checking requesters: " + databaseError.getMessage());
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }



    // Methods to clear Software Components
    private void clearSoftwareComponents(DatabaseReference componentRef, Runnable onComplete) {
        DatabaseReference softwareRef = componentRef.child("Software");

        softwareRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot softwareSnapshot) {
                if (softwareSnapshot.exists()) {
                    List<Task<Void>> deletionTasks = new ArrayList<>();

                    for (DataSnapshot softwareComponent : softwareSnapshot.getChildren()) {
                        if (!"status".equals(softwareComponent.getKey())) {
                            // Collect deletion tasks for each component
                            deletionTasks.add(softwareComponent.getRef().removeValue());
                        }
                    }

                    if (deletionTasks.isEmpty()) {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                        return;
                    }

                    // Wait for all deletions to complete
                    Tasks.whenAllComplete(deletionTasks).addOnCompleteListener(task -> {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });

                } else {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminActivity", "Error while deleting software components: " + databaseError.getMessage());
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }


    //Method to clear Hardware Components
    public void clearHardwareComponents(DatabaseReference componentRef, Runnable onComplete) {
        DatabaseReference hardwareRef = componentRef.child("Hardware");

        hardwareRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Task<Void>> deletionTasks = new ArrayList<>();

                    for (DataSnapshot hardwareSnapshot : dataSnapshot.getChildren()) {
                        if ("status".equals(hardwareSnapshot.getKey())) {
                            Log.d("AdminActivity", "Skipping preserved hardware component with key: " + hardwareSnapshot.getKey());
                            continue;
                        }

                        deletionTasks.add(hardwareSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> Log.d("AdminActivity", "Hardware component deleted: " + hardwareSnapshot.getKey()))
                                .addOnFailureListener(e -> Log.e("AdminActivity", "Failed to delete hardware component: " + e.getMessage())));
                    }

                    if (deletionTasks.isEmpty()) {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                        return;
                    }

                    // Wait for all deletions to complete
                    Tasks.whenAllComplete(deletionTasks).addOnCompleteListener(task -> {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });

                } else {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AdminActivity", "Error while deleting hardware components: " + databaseError.getMessage());
                if (onComplete != null) {
                    onComplete.run();
                }
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
                } else {
                    Log.d(TAG, "onDataChange: ");                
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
                    addRequester(emailNewRequesterString, passwordNewRequesterString,userType, firstNameNewRequesterString, lastNameNewRequesterString, null, null);

                }
            }
        }catch (JSONException e){
            Log.d(TAG, "loadRequestersFromJson: ");
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
            Log.d(TAG, "loadHarwareComponentsFromJson: ");
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
            Log.d(TAG, "loadSoftwareComponentsFromJson: ");
        }
    }

    private void processComponentsWithHandler(Context context, Queue<JSONObject> componentQueue) {
        Handler handler = new Handler(Looper.getMainLooper());

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


                    StorekeeperActivity.checkIfItemExists(descriptionJsonObject,  componentDatabaseRef, null,new ItemExistenceCallback() {
                        public void onResult(boolean exists, Integer quantity) {
                            if (exists) {
                                Log.d(TAG, "The item already exists.");
                            } else {
                                StorekeeperActivity.addNewItem(componentSubtypeJsonObject, descriptionJsonObject, intQuantity, commentJsonObject, componentTypeJsonObject, componentDatabaseRef,null);
                            }
                        }
                    });


                    handler.postDelayed(this, 200);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };


        handler.post(processNext);
    }

}