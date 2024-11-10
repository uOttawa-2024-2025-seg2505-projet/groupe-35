package com.example.projetseg2505;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class StorekeeperActivity extends AppCompatActivity {
    // Logout Variables
    private Button logoutButton;
    private boolean showTextToClient = true;

    // Add Item Variables
    private DatabaseReference databaseRef;
    private Button sendToAddItemLayoutButton, addItemButton;
    private EditText  textInputDescriptionNewItem, textInputQuantityNewItem, textInputCommentNewItem;
    private TextView infoTextAddItem;
    private Spinner typeSpinner, subtypeSpinner;

    // Modify/Delete Item variables
    private Button sendToRemoveEditItemLayoutButton, modifyItemButton, deleteItemButton, incrementButton, decrementButton;
    private String foundComment, foundDescription, foundSubType, foundQuantity;
    private TextView errorTextDescriptionItemInput, errorTextModifyDeleteItem;
    private EditText textSubtypeModificationItem, textDescriptionModificationItem, textQuantityModificationItem, textCommentModificationItem, modifyRemoveDescriptionItemInput;
    private int quantity;

    // view informations of Item variables:
    private Button viewItemInformationsButton;

    // tabular view Variable
    private Button tabularListButton;

    // Add return Variable
    private Button returnButton;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storekeeper);

        // Initialization
        logoutButton = findViewById(R.id.logoutButton);
        sendToAddItemLayoutButton = findViewById(R.id.sendToAddItemLayoutButton);
        modifyRemoveDescriptionItemInput = findViewById(R.id.modifyRemoveDescriptionItemInput);
        sendToRemoveEditItemLayoutButton = findViewById(R.id.sendToModifyRemoveItemLayoutButton);
        tabularListButton = findViewById(R.id.sendToTabularList);
        viewItemInformationsButton = findViewById(R.id.viewItemInformationsButton);
        errorTextDescriptionItemInput = findViewById(R.id.errorTextSubtypeItemInput);

        // Initialize the Database Reference globally for Components
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Components");

        // Logout button logic
        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(StorekeeperActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Add item layout redirection logic
        sendToAddItemLayoutButton.setOnClickListener(v -> {
            setContentView(R.layout.activity_storekeeper_add_item);

            // Initialization for add layout
            subtypeSpinner = findViewById(R.id.subTypeSpinner);
            textInputDescriptionNewItem = findViewById(R.id.textInputDescriptionNewItem);
            textInputQuantityNewItem = findViewById(R.id.textInputQuantityNewItem);
            textInputCommentNewItem = findViewById(R.id.textInputCommentNewItem);
            infoTextAddItem = findViewById(R.id.infoTextAddItem);
            addItemButton = findViewById(R.id.addItemButton);
            typeSpinner = findViewById(R.id.typeSpinner);

            returnButton = findViewById(R.id.returnButton);

            returnButton.setOnClickListener(view -> {
                Intent intent = new Intent(this, StorekeeperActivity.class);
                startActivity(intent);
                finish();




            });

            // Configure Spinner for Component Type selection
            /*if (typeSpinner != null) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                        StorekeeperActivity.this, R.array.component_types, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                typeSpinner.setAdapter(adapter);
            } else {
                Toast.makeText(StorekeeperActivity.this, "Spinner is null", Toast.LENGTH_SHORT).show();
            }*/
            ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                    R.array.component_types, android.R.layout.simple_spinner_item);
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(typeAdapter);
            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    updateSubTypeSpinner(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });



            // Add item button click logic
            addItemButton.setOnClickListener(view -> {
                String subType = subtypeSpinner.getSelectedItem().toString();
                String description = textInputDescriptionNewItem.getText().toString().trim();
                String quantityStr = textInputQuantityNewItem.getText().toString().trim();
                String comment = textInputCommentNewItem.getText().toString().trim();
                String componentType = typeSpinner.getSelectedItem().toString();

                // Validate inputs
                if (description.isEmpty() || quantityStr.isEmpty() ||subtypeSpinner.equals("Choose Type") ||typeSpinner.equals("Choose Type")) {
                    infoTextAddItem.setText("Please fill in all required fields.");
                    infoTextAddItem.setVisibility(View.VISIBLE);
                    return;
                }

                // Parse quantity
                int quantity;
                try {
                    quantity = Integer.parseInt(quantityStr);
                } catch (NumberFormatException e) {
                    infoTextAddItem.setText("Please enter a valid quantity.");
                    infoTextAddItem.setVisibility(View.VISIBLE);
                    return;
                }

                // Check if the item already exists based on description
                checkIfItemExists(description, databaseRef, infoTextAddItem, new ItemExistenceCallback() {
                    @Override
                    public void onResult(boolean exists, Integer x) {
                        if (!exists) {
                            addNewItem(subType, description, quantity, comment, componentType, databaseRef,infoTextAddItem);
                        }
                    }
                });

                clearInputFields();
            });
        });

        // Modify/Delete item layout redirection logic
        sendToRemoveEditItemLayoutButton.setOnClickListener(v -> {
            // Load the layout that contains `returnButton` if necessary
            setContentView(R.layout.activity_storekeeper_modify_remove_item);

            // Initialize `returnButton` after the layout is set
            returnButton = findViewById(R.id.returnButton);
            returnButton.setOnClickListener(view -> {
                // Use StorekeeperActivity.this to get the correct context
                Intent intent = new Intent(StorekeeperActivity.this, StorekeeperActivity.class);
                startActivity(intent);
                finish();
            });

            // Continue with the search functionality
            String descriptionToSearch = modifyRemoveDescriptionItemInput.getText().toString().trim();
            if (!descriptionToSearch.isEmpty()) {
                searchItemByDescription(descriptionToSearch);
            } else {
                errorTextDescriptionItemInput.setText("Please enter a valid description title.");
                errorTextDescriptionItemInput.setVisibility(View.VISIBLE);
            }
        });

        //view item
        viewItemInformationsButton.setOnClickListener(v -> {

            String descriptionToSearch = modifyRemoveDescriptionItemInput.getText().toString().trim();

            if (!descriptionToSearch.isEmpty()) {
                searchItemForInformation(descriptionToSearch);

            } else {
                errorTextDescriptionItemInput.setText("Please enter a valid item description.");
                errorTextDescriptionItemInput.setVisibility(View.VISIBLE);
            }
        });

        //Tabular view
        tabularListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch to the Welcome layout
                setContentView(R.layout.activity_storekeeper_tabular_list);

                // After switching the layout, load components data
                loadComponentsData();

                returnButton = findViewById(R.id.returnButton);
                returnButton.setOnClickListener(view -> {
                    // Use StorekeeperActivity.this to get the correct context
                    Intent intent = new Intent(StorekeeperActivity.this, StorekeeperActivity.class);
                    startActivity(intent);

                    finish();
                });
            }
        });


    }

    // Add item method
    public static void checkIfItemExists(String description, DatabaseReference databaseRef, TextView textView, ItemExistenceCallback callback) {
        // Search in Hardware
        databaseRef.child("Hardware").orderByChild("description").equalTo(description).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot hardwareSnapshot) {
                if (hardwareSnapshot.exists()) {
                    if(textView != null) {
                        textView.setText("An item with this description already exists in Hardware.");
                        textView.setVisibility(View.VISIBLE);
                    } else {
                        Log.d(TAG, "An item with this description already exists in Hardware.");
                    }
                    DataSnapshot firstMatch = hardwareSnapshot.getChildren().iterator().next();
                    Integer quantity = firstMatch.child("quantity").getValue(Integer.class);

                    callback.onResult(true, quantity);
                } else {
                    // Check in Software
                    databaseRef.child("Software").orderByChild("description").equalTo(description).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot softwareSnapshot) {
                            if (softwareSnapshot.exists()) {
                                if(textView != null) {
                                    textView.setText("An item with this description already exists in Software.");
                                    textView.setVisibility(View.VISIBLE);
                                } else {
                                    Log.d(TAG, "An item with this description already exists in Software.");
                                }
                                DataSnapshot firstMatch = softwareSnapshot.getChildren().iterator().next();
                                Integer quantity = firstMatch.child("quantity").getValue(Integer.class);
                                callback.onResult(true, quantity);
                            } else {
                                // Item does not exist in Hardware or Software
                                callback.onResult(false, 0);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "Error checking Software: " + databaseError.getMessage());
                            callback.onResult(false, 0);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error checking Hardware: " + databaseError.getMessage());
                callback.onResult(false, 0);
            }
        });
    }




    public static void addNewItem(String subType, String description, int quantity, String comment, String componentType, DatabaseReference databaseRef, TextView textView) {
        DatabaseReference componentRef = databaseRef.child(componentType);

        if (componentType.equals("Hardware")) {
            HardwareComponent newItem = new HardwareComponent(subType, description, quantity, comment);
            componentRef.child(description.replace(".", ",")).setValue(newItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if(textView != null){
                        textView.setText("Hardware item added successfully!");
                        textView.setVisibility(View.VISIBLE);
                    }

                } else {
                    if(textView != null) {
                        textView.setText("Failed to add hardware item. Please try again.");
                        textView.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else if (componentType.equals("Software")) {
            SoftwareComponent newItem = new SoftwareComponent(subType, description, quantity, comment);
            componentRef.child(description.replace(".", ",")).setValue(newItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if(textView != null) {
                        textView.setText("Software item added successfully!");
                        textView.setVisibility(View.VISIBLE);
                    }

                } else {
                    if(textView != null) {
                        textView.setText("Failed to add software item. Please try again.");
                        textView.setVisibility(View.VISIBLE);
                    }

                }
            });
        } else {
            if(textView != null) {
                textView.setText("Invalid component type selected.");
                textView.setVisibility(View.VISIBLE);
            }
        }
    }

    // Clear input fields after adding an item
    private void clearInputFields() {
        textInputDescriptionNewItem.setText("");
        textInputQuantityNewItem.setText("");
        textInputCommentNewItem.setText("");
        typeSpinner.setSelection(0);
        subtypeSpinner.setSelection(0);
    }

    //view methods

    // Method to search item and display information
    private void searchItemForInformation(String description) {
        DatabaseReference hardwareRef = databaseRef.child("Hardware");


        // Search in 'Hardware' components
        hardwareRef.orderByChild("description").equalTo(description.replace(".", ",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String type = "Hardware";
                        String subType = snapshot.child("subType").getValue(String.class);
                        String description = snapshot.child("description").getValue(String.class);
                        String quantity = String.valueOf(snapshot.child("quantity").getValue(Long.class));
                        String comment = snapshot.child("comment").getValue(String.class);
                        String creationDate = snapshot.child("dateTimeCreation").getValue(String.class);
                        String modificationDate = snapshot.child("dateTimeModification").getValue(String.class);

                        displayItemInformation(type, subType, description, quantity, comment, creationDate, modificationDate);

                        // Initialize and set up the return button
                        returnButton = findViewById(R.id.returnButton);
                        returnButton.setOnClickListener(view -> {
                            Intent intent = new Intent(StorekeeperActivity.this, StorekeeperActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }
                } else {
                    // If not found in 'Hardware', search in 'Software'
                    searchInSoftwareForInformation(description);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StorekeeperActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Method to search item in 'Software'
    private void searchInSoftwareForInformation(String description) {
        DatabaseReference softwareRef = databaseRef.child("Software");

        softwareRef.orderByChild("description").equalTo(description.replace(".", ",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String type = "Software";
                        String subType = snapshot.child("subType").getValue(String.class);
                        String description = snapshot.child("description").getValue(String.class);
                        String quantity = String.valueOf(snapshot.child("quantity").getValue(Long.class));
                        String comment = snapshot.child("comment").getValue(String.class);
                        String creationDate = snapshot.child("dateTimeCreation").getValue(String.class);
                        String modificationDate = snapshot.child("dateTimeModification").getValue(String.class);

                        displayItemInformation(type, subType, description, quantity, comment, creationDate, modificationDate);
                    }
                } else {
                    // Item not found
                    errorTextDescriptionItemInput.setText("Item with this description not found.");
                    errorTextDescriptionItemInput.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StorekeeperActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to display item information in the second layout
    private void displayItemInformation(String type, String subType, String description, String quantity, String comment, String creationDate, String modificationDate) {
        // Switch to the information display layout
        setContentView(R.layout.activity_storekeeper_view_item_informations);

        // Initialize TextViews
        EditText textType = findViewById(R.id.textType);
        EditText textSubType = findViewById(R.id.textSubType);
        EditText textDescription = findViewById(R.id.textDescription);
        EditText textQuantity = findViewById(R.id.textQuantity);
        EditText textComment = findViewById(R.id.textComment);
        EditText textCreationDate = findViewById(R.id.textCreationDate);
        EditText textModificationDate = findViewById(R.id.textModificationDate);

        // Set the item details
        textType.setText(type);
        textSubType.setText(subType);
        textDescription.setText(description);
        textQuantity.setText(quantity);
        textComment.setText(comment);
        textCreationDate.setText(creationDate);
        textModificationDate.setText(modificationDate);

    }


    //  modification or deletion methods
    private void searchItemByDescription(String description) {
        DatabaseReference hardwareRef = databaseRef.child("Hardware");
        DatabaseReference softwareRef = databaseRef.child("Software");


        hardwareRef.orderByChild("description").equalTo(description.replace(".", ",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        foundComment = userSnapshot.child("comment").getValue(String.class);
                        foundDescription = userSnapshot.child("description").getValue(String.class);
                        foundSubType = userSnapshot.child("subType").getValue(String.class);
                        foundQuantity = String.valueOf(userSnapshot.child("quantity").getValue(Long.class));


                        switchToModifyDeleteLayout();
                    }
                } else {

                    searchInSoftware(description);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StorekeeperActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Méthode pour rechercher dans "Software" si l'élément n'est pas trouvé dans "Hardware"
    private void searchInSoftware(String description) {
        DatabaseReference softwareRef = databaseRef.child("Software");

        softwareRef.orderByChild("description").equalTo(description.replace(".", ",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        foundComment = userSnapshot.child("comment").getValue(String.class);
                        foundDescription = userSnapshot.child("description").getValue(String.class);
                        foundSubType = userSnapshot.child("subType").getValue(String.class);
                        foundQuantity = String.valueOf(userSnapshot.child("quantity").getValue(Long.class));

                        // Basculer vers la mise en page de modification/suppression
                        switchToModifyDeleteLayout();
                    }
                } else {
                    // Si l'élément n'est trouvé ni dans "Hardware" ni dans "Software", afficher un message d'erreur
                    errorTextDescriptionItemInput.setText("Item with this description not found.");
                    errorTextDescriptionItemInput.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StorekeeperActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("MissingInflatedId")

    private void switchToModifyDeleteLayout() {
        setContentView(R.layout.activity_storekeeper_modify_remove_item);

        // Initialize inputs for modification layout
        textSubtypeModificationItem = findViewById(R.id.textSubtypeModificationItem);
        textDescriptionModificationItem = findViewById(R.id.textDescriptionModificationItem);
        textQuantityModificationItem = findViewById(R.id.textQuantityModificationItem);
        textCommentModificationItem = findViewById(R.id.textCommentModificationItem);
        errorTextModifyDeleteItem = findViewById(R.id.errorTextModifyDeleteItem);
        modifyItemButton = findViewById(R.id.modifyItemButton);
        deleteItemButton = findViewById(R.id.deleteItemButton);


        // Initialize buttons for increment/decrement
        incrementButton = findViewById(R.id.incrementButton);
        decrementButton = findViewById(R.id.decrementButton);

        // Populate fields with found item data
        textSubtypeModificationItem.setText(foundSubType); // Display the subType
        textDescriptionModificationItem.setText(foundDescription);
        textCommentModificationItem.setText(foundComment);

        // Check if the quantity is valid and not empty
        if (foundQuantity != null && !foundQuantity.isEmpty()) {
            quantity = Integer.parseInt(foundQuantity);
        } else {
            quantity = 0; // Default value if no quantity is found
        }

        textQuantityModificationItem.setText(String.valueOf(quantity));

        // Disable the editing of subType and description fields since they are not modifiable
        textSubtypeModificationItem.setEnabled(false);  // Make subType non-editable
        textDescriptionModificationItem.setEnabled(false);  // Make description non-editable
        textQuantityModificationItem.setEnabled(false);  // Disable manual editing of the quantity

        // Set up the listeners for increment and decrement buttons
        incrementButton.setOnClickListener(v -> incrementQuantity());
        decrementButton.setOnClickListener(v -> decrementQuantity());

        // Modify button logic
        modifyItemButton.setOnClickListener(v -> {
            String updatedComment = textCommentModificationItem.getText().toString().trim();
            applyChanges(databaseRef, foundDescription, quantity,updatedComment,true,errorTextModifyDeleteItem);
        });


        // Delete button logic
        deleteItemButton.setOnClickListener(v -> deleteItem());
    }


    // Increment quantity
    private void incrementQuantity() {
        quantity++;
        textQuantityModificationItem.setText(String.valueOf(quantity));
    }

    // Decrement quantity
    private void decrementQuantity() {
        if (quantity > 0) {
            quantity--;
            textQuantityModificationItem.setText(String.valueOf(quantity));
        } else {
            errorTextModifyDeleteItem.setText("Quantity can't be less than 0");
            errorTextModifyDeleteItem.setVisibility(View.VISIBLE);

        }
    }

    // Modify item method
    public static void applyChanges(DatabaseReference databaseRef, String description, int quantity, String comment, boolean isStorekeeper, TextView infoText) {
        
        // Update the item in 'Hardware' first, based on description
        DatabaseReference hardwareRef = databaseRef.child("Hardware");
        hardwareRef.orderByChild("description").equalTo(description).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        if (comment != null){
                            userSnapshot.getRef().child("comment").setValue(comment);
                        }
                        userSnapshot.getRef().child("quantity").setValue(quantity);
                        if(isStorekeeper) {
                            userSnapshot.getRef().child("dateTimeModification").setValue(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

                        }
                    }
                } else {
                    // If not found in 'Hardware', check 'Software'
                    updateSoftwareComponent(databaseRef, description,quantity, comment,isStorekeeper, infoText);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: error");
            }
        });
    }

    // Update 'Software' components if not found in 'Hardware'
    public static void updateSoftwareComponent(DatabaseReference databaseRef,String description, int quantity, String comment, boolean isStorekeeper, TextView infoText) {
        DatabaseReference softwareRef = databaseRef.child("Software");
        softwareRef.orderByChild("description").equalTo(description).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Only update comment and quantity
                        if (comment != null){
                            userSnapshot.getRef().child("comment").setValue(comment);
                        }
                        userSnapshot.getRef().child("quantity").setValue(quantity);
                        if(isStorekeeper) {
                            userSnapshot.getRef().child("dateTimeModification").setValue(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));

                        }
                    }
                } else {
                    if(isStorekeeper) {
                        if(infoText != null){
                            infoText.setText("Item not found.");
                            infoText.setVisibility(View.VISIBLE);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(isStorekeeper) {
                    Log.d(TAG, "onCancelled: error");
                }
            }
        });
    }

    public void deleteItem() {

        DatabaseReference hardwareRef = databaseRef.child("Hardware");
        hardwareRef.orderByChild("subType").equalTo(foundSubType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Item found in 'Hardware', delete it
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        userSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                            Toast.makeText(StorekeeperActivity.this, "Hardware item deleted successfully", Toast.LENGTH_SHORT).show();
                            setContentView(R.layout.activity_storekeeper);
                        }).addOnFailureListener(e -> {
                            Toast.makeText(StorekeeperActivity.this, "Error while deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    // If not found in 'Hardware', check in 'Software'
                    deleteSoftwareComponent();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StorekeeperActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteSoftwareComponent() {
        DatabaseReference softwareRef = databaseRef.child("Software");
        softwareRef.orderByChild("subType").equalTo(foundSubType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Item found in 'Software', delete it
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        userSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                            Toast.makeText(StorekeeperActivity.this, "Software item deleted successfully", Toast.LENGTH_SHORT).show();
                            setContentView(R.layout.activity_storekeeper); // Return to initial layout
                        }).addOnFailureListener(e -> {
                            Toast.makeText(StorekeeperActivity.this, "Error while deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    errorTextModifyDeleteItem.setText("Item not found.");
                    errorTextModifyDeleteItem.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StorekeeperActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tabular List Methods
    private void loadComponentsData() {
        // Reference to the TableLayout in the new "Welcome" layout
        TableLayout componentsTableLayout = findViewById(R.id.componentsTableLayout);

        // Create header row
        TableRow headerRow = new TableRow(StorekeeperActivity.this);
        headerRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        headerRow.setPadding(16, 16, 16, 16);

        String[] headers = {"Category", "Component Name", "Description", "Quantity"};
        for (String header : headers) {
            TextView textView = new TextView(StorekeeperActivity.this);
            textView.setText(header);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(8, 8, 8, 8);
            textView.setBackgroundColor(0xFFD1C4E9); // Light purple background for header
            textView.setTextColor(0xFF000000);
            headerRow.addView(textView);
        }
        componentsTableLayout.addView(headerRow);


        // Fetch hardware and software components from Firebase
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Loop through the "Components" node
                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    String category = categorySnapshot.getKey(); // e.g., "Hardware" or "Software"

                    // Loop through the items in each category
                    for (DataSnapshot componentSnapshot : categorySnapshot.getChildren()) {
                        String componentName = componentSnapshot.getKey();
                        String description = componentSnapshot.child("description").getValue(String.class);
                        Long quantity = componentSnapshot.child("quantity").getValue(Long.class);

                        // Create a row for each component
                        TableRow componentRow = new TableRow(StorekeeperActivity.this);
                        componentRow.setLayoutParams(new TableLayout.LayoutParams(
                                TableLayout.LayoutParams.MATCH_PARENT,
                                TableLayout.LayoutParams.WRAP_CONTENT));
                        componentRow.setPadding(8, 8, 8, 8);

                        // Add category, component name, description, and quantity to the row
                        String[] componentDetails = {category, componentName, description, String.valueOf(quantity)};
                        for (String detail : componentDetails) {
                            TextView detailTextView = new TextView(StorekeeperActivity.this);
                            detailTextView.setText(detail);
                            detailTextView.setGravity(Gravity.CENTER);
                            detailTextView.setPadding(2, 2, 2, 2);
                            detailTextView.setBackgroundColor(0xFFE3F2FD); // Light blue background for rows
                            detailTextView.setTextColor(0xFF000000);
                            detailTextView.setSingleLine(false);
                            detailTextView.setMaxWidth(150);
                            componentRow.addView(detailTextView);
                        }
                        componentsTableLayout.addView(componentRow);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StorekeeperActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //change subtype spinner depending on type
    private void updateSubTypeSpinner(int typePosition) {
        int subTypeArrayId;
        if (typePosition == 1) {
            subTypeArrayId = R.array.hardware_subtypes_array;
        }
        else if(typePosition == 2) {
            subTypeArrayId = R.array.software_subtypes_array;
        }
        else{
            subTypeArrayId = R.array.subtypes_array;
        }

        ArrayAdapter<CharSequence> subTypeAdapter = ArrayAdapter.createFromResource(this,
                subTypeArrayId, android.R.layout.simple_spinner_item);
        subTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subtypeSpinner.setAdapter(subTypeAdapter);
    }
}
