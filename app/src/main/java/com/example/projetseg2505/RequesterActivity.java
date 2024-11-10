package com.example.projetseg2505;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RequesterActivity extends AppCompatActivity {
    //logout
    private Button logoutButton;
    //add order
    private Button sendToPlaceOrderLayout, createOrderButton, returnButtonViewOrderStatus, returnButtonCreateOrder;
    private Spinner computerCase, motherboard, memoryStick, hardDrive, monitor, keyboardMouse, webBrowser, officeSuite, developmentTools;
    private EditText numberOfMonitors,  numberOfMemorySticks ;
    private TextView errorTextNewOrderLayout;
    private ArrayList<String> hardDriveArray, developmentToolsArray, itemsWithSameSubtype;
    private String computerCaseDescription, motherboardDescription, memoryStickDescription, monitorDescription, keyboardMouseDescription, webBrowserDescription, requesterEmail, officeSuiteDescription;
    private int numberOfMonitorsInt,numberOfMemorySticksInt;

    //view orders and delete
    private DatabaseReference ordersDatabaseRef = FirebaseDatabase.getInstance().getReference("Orders");
    private DatabaseReference componentDatabaseRef = FirebaseDatabase.getInstance().getReference("Components");
    private boolean orderIsDeletable;

    private Button viewMyOrderButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requester);
        initializeMainLayout();
    }

    //methods for initializing
    private void initializeMainLayout() {

        logoutButton = findViewById(R.id.logoutButton);
        sendToPlaceOrderLayout = findViewById(R.id.sendToPlaceOrderLayout);
        viewMyOrderButton = findViewById(R.id.viewMyOrderButton);

        requesterEmail = getIntent().getStringExtra("userEmail");
        if (requesterEmail != null) {
            getSharedPreferences("RequesterPrefs", MODE_PRIVATE).edit().putString("requesterEmail", requesterEmail).apply();
        } else {
            requesterEmail = getSharedPreferences("RequesterPrefs", MODE_PRIVATE).getString("requesterEmail", null);
        }

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(RequesterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        sendToPlaceOrderLayout.setOnClickListener(v -> {
            setContentView(R.layout.activity_requester_new_order);
            initializeOrderLayout();
        });

        viewMyOrderButton.setOnClickListener(v -> {
            setContentView(R.layout.activity_requester_orders_status);
            loadOrdersDataForRequester();
            initializeOrderStatusLayout();
        });
    }


    private void initializeOrderStatusLayout() {
        returnButtonViewOrderStatus = findViewById(R.id.returnButtonViewOrderStatus);
        returnButtonViewOrderStatus.setOnClickListener(v -> {
            setContentView(R.layout.activity_requester);
            initializeMainLayout();
        });
    }

    private void initializeOrderLayout() {

        computerCase = findViewById(R.id.computerCase);
        motherboard = findViewById(R.id.motherboard);
        memoryStick = findViewById(R.id.memoryStick);
        hardDrive = findViewById(R.id.hardDrive);
        monitor = findViewById(R.id.monitors);
        keyboardMouse = findViewById(R.id.keyboardMouse);
        webBrowser = findViewById(R.id.webBrowser);
        officeSuite = findViewById(R.id.officeSuite);
        developmentTools = findViewById(R.id.developmentTools);
        numberOfMonitors = findViewById(R.id.inputMonitorAmount);
        numberOfMemorySticks = findViewById(R.id.inputMemoryAmount);
        errorTextNewOrderLayout = findViewById(R.id.errorTextNewOrderLayout);
        createOrderButton = findViewById(R.id.createOrderButton);
        returnButtonCreateOrder = findViewById(R.id.returnButtonCreateOrder);
        hardDriveArray = new ArrayList<>();
        developmentToolsArray = new ArrayList<>();


        returnButtonCreateOrder.setOnClickListener(v -> {
            setContentView(R.layout.activity_requester);
            initializeMainLayout();
        });



        searchItemBySubtypeAndCreateSpinner("case", computerCase);
        searchItemBySubtypeAndCreateSpinner("motherboard", motherboard);
        searchItemBySubtypeAndCreateSpinner("memory", memoryStick);
        searchItemBySubtypeAndCreateSpinner("hard drive", hardDrive);
        searchItemBySubtypeAndCreateSpinner("monitor", monitor);
        searchItemBySubtypeAndCreateSpinner("Keyboard_Mouse", keyboardMouse);
        searchItemBySubtypeAndCreateSpinner("Web Browser", webBrowser);
        searchItemBySubtypeAndCreateSpinner("Office Suite", officeSuite);
        searchItemBySubtypeAndCreateSpinner("Development Tool", developmentTools);

        hardDrive.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                handleHardDriveSelection(selectedItem);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected: ");
            }
        });

        developmentTools.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                handleDevelopmentToolsSelection(selectedItem);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected: ");
            }
        });

        createOrderButton.setOnClickListener(view -> {
            createOrder();
        });
    }


    private boolean checkItemSelection(){
        computerCaseDescription = computerCase.getSelectedItem().toString();
        motherboardDescription = motherboard.getSelectedItem().toString();
        memoryStickDescription = memoryStick.getSelectedItem().toString();
        monitorDescription = monitor.getSelectedItem().toString();
        keyboardMouseDescription = keyboardMouse.getSelectedItem().toString();
        webBrowserDescription = webBrowser.getSelectedItem().toString();

        if (computerCaseDescription.equals("Select an option") || motherboardDescription.equals("Select an option") ||memoryStickDescription.equals("Select an option") ||monitorDescription.equals("Select an option") || keyboardMouseDescription.equals("Select an option") || webBrowserDescription.equals("Select an option") || hardDriveArray.get(0) == null) {
            errorTextNewOrderLayout.setText("The computer can't be built with only these items");
            errorTextNewOrderLayout.setVisibility(View.VISIBLE);
            return false;
        }



        return true;
    }
    private boolean checkItemInput(){
        numberOfMonitorsInt =0;
        if ((!numberOfMonitors.getText().toString().isEmpty())) {
            numberOfMonitorsInt = Integer.parseInt(numberOfMonitors.getText().toString());
            if(numberOfMonitorsInt> 3 || numberOfMonitorsInt < 1) {
                errorTextNewOrderLayout.setText("You will need to enter a number of monitors between 1 and 3");
                errorTextNewOrderLayout.setVisibility(View.VISIBLE);
                return false;
            }
        } else {
            errorTextNewOrderLayout.setText("You will need to enter a number for monitors");
            errorTextNewOrderLayout.setVisibility(View.VISIBLE);
            return false;
        }
        numberOfMemorySticksInt=0;
        if (!numberOfMemorySticks.getText().toString().isEmpty()) {
            numberOfMemorySticksInt = Integer.parseInt(numberOfMemorySticks.getText().toString());
            if(numberOfMemorySticksInt> 4 || numberOfMemorySticksInt < 1) {
                errorTextNewOrderLayout.setText("You will need to enter a number of memory sticks between 1 and 4");
                errorTextNewOrderLayout.setVisibility(View.VISIBLE);
                return false;
            }
        } else {
            errorTextNewOrderLayout.setText("You will need to enter a number for memory sticks");
            errorTextNewOrderLayout.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }


    private boolean isOrderBeingProcessed = false;

    private void createOrder() {
        if (isOrderBeingProcessed) {
            return; // Bloque l'exécution si une commande est déjà en cours de traitement
        }

        if (checkItemInput() && checkItemSelection()) {
            isOrderBeingProcessed = true; // Marque le début du processus de commande

            officeSuiteDescription = officeSuite.getSelectedItem().toString();
            if (officeSuiteDescription.equals("Select an option")) {
                officeSuiteDescription = null;
            }

            Orders newOrder = new Orders(requesterEmail, computerCaseDescription, motherboardDescription, memoryStickDescription,
                    numberOfMemorySticksInt, hardDriveArray, monitorDescription, numberOfMonitorsInt,
                    keyboardMouseDescription, webBrowserDescription, officeSuiteDescription, developmentToolsArray);

            newOrder.checkIfItemsExist(new ItemExistenceCallback() {
                @Override
                public void onResult(boolean exists, Integer quantity) {
                    isOrderBeingProcessed = false; // Réinitialise le flag à la fin du traitement

                    if (exists) {
                        newOrder.refreshDatabaseInfo();
                        newOrder.pushOrderToDatabase();

                        errorTextNewOrderLayout.setText("Computer built successfully");
                        errorTextNewOrderLayout.setVisibility(View.VISIBLE);

                        clearCreateOrderLayout(); // Efface les champs seulement si la commande est réussie
                    } else {
                        errorTextNewOrderLayout.setText("Not enough stock to build your computer");
                        errorTextNewOrderLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private void clearCreateOrderLayout(){
        computerCase.setSelection(0);
        computerCase.setSelection(0);
        motherboard.setSelection(0);
        memoryStick.setSelection(0);
        hardDrive.setSelection(0);
        monitor.setSelection(0);
        keyboardMouse.setSelection(0);
        webBrowser.setSelection(0);
        officeSuite.setSelection(0);
        developmentTools.setSelection(0);
        numberOfMonitors.setText("");
        numberOfMemorySticks.setText("");
        errorTextNewOrderLayout.setVisibility(View.GONE);
    }
    //visualize orders and delete
    public void orderIsDeletable(String orderID, OnCheckDeletableCallback callback) {
        ordersDatabaseRef.child(requesterEmail.replace('.', ',')).child(orderID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status = dataSnapshot.child("Status").getValue(String.class);
                boolean isDeletable = status.equals("Waiting for acceptance") || status.equals("Rejected") || status.equals("Delivered");
                callback.onCheckComplete(isDeletable);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: ");
                callback.onCheckComplete(false);
            }
        });
    }
    private void loadOrdersDataForRequester() {
        TableLayout OrdersTableLayout = findViewById(R.id.requesterOrdersTableLayout);

        TableRow headerRow = new TableRow(RequesterActivity.this);
        headerRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        headerRow.setPadding(16, 16, 16, 16);

        String[] headers = {"OrderID", "Date Of Creation", "Status", "Action"};
        for (int i = 0; i < headers.length; i++) {
            TextView textView = new TextView(RequesterActivity.this);
            textView.setText(headers[i]);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(5, 5, 2, 2);
            textView.setBackgroundColor(0xFFD1C4E9);
            textView.setTextColor(0xFF000000);

            textView.setMinHeight((int) getResources().getDimension(R.dimen.header_min_height));

            TableRow.LayoutParams params;
            if (i == headers.length - 1) {
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f);
            } else {
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            }
            textView.setLayoutParams(params);

            headerRow.addView(textView);
        }
        OrdersTableLayout.addView(headerRow);

        ordersDatabaseRef.child(requesterEmail.replace('.', ',')).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ordersSnapshot : dataSnapshot.getChildren()) {
                    String orderID = ordersSnapshot.getKey();
                    String dateOfCreation = ordersSnapshot.child("Date Of Creation").getValue(String.class);
                    String status = ordersSnapshot.child("Status").getValue(String.class);

                    TableRow componentRow = new TableRow(RequesterActivity.this);
                    componentRow.setLayoutParams(new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));
                    componentRow.setPadding(2, 5, 2, 5);
                    orderIsDeletable(orderID, (isDeletable) -> {
                        String[] orderDetails = {orderID, dateOfCreation, status};
                        for (int i = 0; i < orderDetails.length; i++) {
                            TextView detailTextView = new TextView(RequesterActivity.this);
                            detailTextView.setText(orderDetails[i]);
                            detailTextView.setGravity(Gravity.CENTER);
                            detailTextView.setPadding(2, 5, 2, 5);
                            detailTextView.setMaxWidth(150);
                            detailTextView.setBackgroundColor(0xFFE3F2FD);
                            detailTextView.setTextColor(0xFF000000);

                            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                            detailTextView.setLayoutParams(params);
                            componentRow.addView(detailTextView);
                        }
                        if (isDeletable) {
                            Button deleteButton = new Button(RequesterActivity.this);
                            deleteButton.setText("X");
                            deleteButton.setTextColor(Color.WHITE);
                            deleteButton.setTextSize(10);
                            deleteButton.setBackgroundColor(Color.BLACK);

                            TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.5f);
                            deleteButton.setLayoutParams(buttonParams);
                            deleteButton.setPadding(2, 2, 2, 2);
                            deleteButton.setWidth(60);
                            deleteButton.setHeight(60);

                            deleteButton.setOnClickListener(v -> {
                                ordersDatabaseRef.child(requesterEmail.replace('.', ',')).child(orderID).removeValue()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                OrdersTableLayout.removeView(componentRow);
                                            }
                                        });
                            });
                            componentRow.addView(deleteButton);
                        }
                        OrdersTableLayout.addView(componentRow);
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RequesterActivity.this, "Erreur : " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    // methods for spinner creation
    public void searchItemBySubtypeAndCreateSpinner(String subtype, Spinner spinner) {

        ArrayList<String> subtypeItems = new ArrayList<>();
        if (subtype.equals("case") || subtype.equals("motherboard") || subtype.equals("memory") || subtype.equals("monitor") || subtype.equals("Keyboard_Mouse") || subtype.equals("Web Browser") || subtype.equals("Office Suite")){
            subtypeItems.add("Select an option");
        }
        else if(subtype.equals("hard drive")){
            subtypeItems.add("Select one or two items");
        }
        else{
            subtypeItems.add("Select from 0 to 3 items");
        }

        searchItemBySubtypeInHardware(subtype, spinner, subtypeItems);

        searchItemBySubtypeInSoftware(subtype, spinner, subtypeItems);
    }
    public void searchItemBySubtypeInHardware(String subtype, Spinner spinner, ArrayList<String> subtypeItems){
        DatabaseReference hardwareRef = componentDatabaseRef.child("Hardware");
        hardwareRef.orderByChild("subType").equalTo(subtype.replace(".", ",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        String itemDescription = itemSnapshot.child("description").getValue(String.class);
                        subtypeItems.add(itemDescription);
                    }
                }

                initializeSpinner(spinner, new ArrayList<>(subtypeItems));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }
    public void searchItemBySubtypeInSoftware(String subtype, Spinner spinner, ArrayList<String> subtypeItems){
        DatabaseReference softwareRef = componentDatabaseRef.child("Software");
        softwareRef.orderByChild("subType").equalTo(subtype.replace(".", ",")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        String itemDescription = itemSnapshot.child("description").getValue(String.class);
                        subtypeItems.add(itemDescription);
                    }
                }

                initializeSpinner(spinner, new ArrayList<>(subtypeItems));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }
    private void initializeSpinner(Spinner spinner, ArrayList<String> dataList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dataList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


    }
    private void handleHardDriveSelection(String selectedItem) {
        if (hardDriveArray.size() < 2) {
            if (!selectedItem.equals("Select one or two items")) {
                hardDriveArray.add(selectedItem);
            }
        } else {
            errorTextNewOrderLayout.setText("You can choose a maximum of 2 Hard Drives.");
            errorTextNewOrderLayout.setVisibility(View.VISIBLE);
        }
    }
    private void handleDevelopmentToolsSelection(String selectedItem) {
        if (!selectedItem.equals("Select from 0 to 3 items")) {
            if (developmentToolsArray.size() < 3) {
                if (!developmentToolsArray.contains(selectedItem)) {
                    developmentToolsArray.add(selectedItem);
                } else {
                    errorTextNewOrderLayout.setText("You need to choose different Development Tools.");
                    errorTextNewOrderLayout.setVisibility(View.VISIBLE);
                }
            } else {
                errorTextNewOrderLayout.setText("You can choose a maximum of 3 different Development Tools.");
                errorTextNewOrderLayout.setVisibility(View.VISIBLE);
            }
        }
    }





}



