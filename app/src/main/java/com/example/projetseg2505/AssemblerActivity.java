package com.example.projetseg2505;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.List;

public class   AssemblerActivity extends AppCompatActivity {

    private Button logoutButton , returnButtonViewOrders;

    //database ref
    private DatabaseReference ordersDatabaseRef = FirebaseDatabase.getInstance().getReference("Orders");

    //view orders
    private List<String> ordersList;
    private Button sendToListOfOrderWithWaitingForAcceptanceStatusButton, sendToListOfAllOrdersButton, sendToListOfOrderWithAcceptedStatusButton;
    private Button acceptOrder, rejectOrder, closeOrder;
    private EditText commentEditText;
    private TextView errorTextCommentInput;
    private boolean isTableAllOrders;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assembler);
        initializeMainLayout();







    }

    private void initializeMainLayout(){
        logoutButton = findViewById(R.id.logoutButton);
        sendToListOfOrderWithWaitingForAcceptanceStatusButton = findViewById(R.id.sendToListOfOrderWithWaitingForAcceptanceStatusButton);
        sendToListOfOrderWithAcceptedStatusButton = findViewById(R.id.sendToListOfOrderWithAcceptedStatusButton);
        sendToListOfAllOrdersButton = findViewById(R.id.sendToListOfAllOrdersButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AssemblerActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        sendToListOfOrderWithWaitingForAcceptanceStatusButton.setOnClickListener(v -> {
            setContentView(R.layout.activity_assembler_check_orders);
            initializeCheckOrdersWithWaitingStatusLayout();
        });

        sendToListOfOrderWithAcceptedStatusButton.setOnClickListener(v -> {
            setContentView(R.layout.activity_assembler_check_orders);
            initializeCheckOrdersWithAcceptedStatusLayout();
        });

        sendToListOfAllOrdersButton.setOnClickListener(v -> {
            setContentView(R.layout.activity_assembler_check_orders);
            initializeCheckAllOrdersLayout();
        });
    }

    private void initializeCheckOrdersWithWaitingStatusLayout(){
        isTableAllOrders = false;
        checkOrderWithSpecialStatus("Waiting for acceptance", true);
        returnButtonViewOrders = findViewById(R.id.returnButtonViewOrders);
        returnButtonViewOrders.setOnClickListener(v -> {
            setContentView(R.layout.activity_assembler);
            initializeMainLayout();
        });
    }
    private void initializeCheckOrdersWithAcceptedStatusLayout(){
        isTableAllOrders = false;
        checkOrderWithSpecialStatus("Accepted, currently being assembled", true);
        returnButtonViewOrders = findViewById(R.id.returnButtonViewOrders);
        returnButtonViewOrders.setOnClickListener(v -> {
            setContentView(R.layout.activity_assembler);
            initializeMainLayout();
        });
    }
    private void initializeCheckAllOrdersLayout(){
        isTableAllOrders = true;
        checkOrderWithSpecialStatus("Delivered", true);
        checkOrderWithSpecialStatus("Accepted, currently being assembled", false);
        checkOrderWithSpecialStatus("Rejected", false);
        checkOrderWithSpecialStatus("Waiting for acceptance", false);
        returnButtonViewOrders = findViewById(R.id.returnButtonViewOrders);
        returnButtonViewOrders.setOnClickListener(v -> {
            setContentView(R.layout.activity_assembler);
            initializeMainLayout();
        });
    }

    private void checkOrderWithSpecialStatus(String statusToCheck, boolean includeHeaderRow) {
        TableLayout OrdersTableLayout = findViewById(R.id.assemblerOrdersTableLayout);

        if (includeHeaderRow) {
            TableRow headerRow = new TableRow(AssemblerActivity.this);
            headerRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            headerRow.setPadding(16, 16, 16, 16);

            String[] headers = {"Requester", "OrderID", "Date Of Creation", "Status"};
            float[] columnWeights = {1f, 1f, 1.5f, 1f}; // Proportions des colonnes

            for (int i = 0; i < headers.length; i++) {
                TextView textView = new TextView(AssemblerActivity.this);
                textView.setText(headers[i]);
                textView.setGravity(Gravity.CENTER);
                textView.setPadding(5, 5, 2, 2);
                textView.setBackgroundColor(0xFFD1C4E9);
                textView.setTextColor(0xFF000000);
                textView.setMinHeight((int) getResources().getDimension(R.dimen.header_min_height));

                // Utiliser le poids pour définir la proportion de la colonne
                TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, columnWeights[i]);
                textView.setLayoutParams(params);

                headerRow.addView(textView);
            }
            OrdersTableLayout.addView(headerRow);
        }

        getRequesterEmailThatOrdered(new OnEmailsReceivedListener() {
            @Override
            public void onEmailsReceived(List<String> requesterIds) {
                for (String requesterId : requesterIds) {
                    ordersDatabaseRef.child(requesterId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ordersSnapshot : dataSnapshot.getChildren()) {
                                String status = ordersSnapshot.child("Status").getValue(String.class);
                                if (statusToCheck.equals(status)) {
                                    String orderID = ordersSnapshot.getKey();
                                    String dateOfCreation = ordersSnapshot.child("Date Of Creation").getValue(String.class);

                                    TableRow componentRow = new TableRow(AssemblerActivity.this);
                                    componentRow.setLayoutParams(new TableLayout.LayoutParams(
                                            TableLayout.LayoutParams.MATCH_PARENT,
                                            TableLayout.LayoutParams.WRAP_CONTENT));
                                    componentRow.setPadding(2, 5, 2, 5);

                                    String[] orderDetails = {requesterId, orderID, dateOfCreation, status};
                                    for (int i = 0; i < orderDetails.length; i++) {
                                        TextView detailTextView = new TextView(AssemblerActivity.this);
                                        detailTextView.setText(orderDetails[i]);
                                        detailTextView.setGravity(Gravity.CENTER);
                                        detailTextView.setPadding(2, 5, 2, 5);
                                        detailTextView.setBackgroundColor(0xFFE3F2FD);
                                        detailTextView.setTextColor(0xFF000000);

                                        TableRow.LayoutParams params = new TableRow.LayoutParams(
                                                0,
                                                TableRow.LayoutParams.WRAP_CONTENT
                                        );
                                        detailTextView.setLayoutParams(params);

                                        componentRow.addView(detailTextView);
                                    }
                                    componentRow.setOnClickListener(v -> displayOrderDetails(orderID, status, requesterId));
                                    OrdersTableLayout.addView(componentRow);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(AssemblerActivity.this, "Erreur : " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }



    private void getRequesterEmailThatOrdered(OnEmailsReceivedListener listener) {
        ordersDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> requesterIds = new ArrayList<>();
                for (DataSnapshot requesterSnapshot : snapshot.getChildren()) {
                    requesterIds.add(requesterSnapshot.getKey());
                }
                listener.onEmailsReceived(requesterIds);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }
    public void getOrderFromDatabase(String requesterId, String orderID, OnOrderRetrievedListener listener) {
        ordersDatabaseRef.child(requesterId).child(orderID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String requesterID = dataSnapshot.child("requesterID").getValue(String.class);
                    String computerCase = dataSnapshot.child("computerCase").getValue(String.class);
                    String motherboard = dataSnapshot.child("motherboard").getValue(String.class);
                    String memoryStick = dataSnapshot.child("memoryStick").getValue(String.class);
                    int numberOfMemorySticks = dataSnapshot.child("numberOfMemorySticks").getValue(Integer.class);

                    ArrayList<String> listOfHardDrive = new ArrayList<>();
                    for (DataSnapshot driveSnapshot : dataSnapshot.child("hardDrives").getChildren()) {
                        listOfHardDrive.add(driveSnapshot.getValue(String.class));
                    }

                    String monitor = dataSnapshot.child("monitor").getValue(String.class);
                    int numberOfMonitors = dataSnapshot.child("numberOfMonitors").getValue(Integer.class);
                    String keyboardMouse = dataSnapshot.child("keyboardMouse").getValue(String.class);
                    String webBrowser = dataSnapshot.child("webBrowser").getValue(String.class);
                    String officeSuite = dataSnapshot.child("officeSuite").getValue(String.class);

                    ArrayList<String> listOfDevelopmentTools = new ArrayList<>();
                    for (DataSnapshot toolSnapshot : dataSnapshot.child("developmentTools").getChildren()) {
                        listOfDevelopmentTools.add(toolSnapshot.getValue(String.class));
                    }

                    Orders order = new Orders(
                            requesterID,
                            computerCase,
                            motherboard,
                            memoryStick,
                            numberOfMemorySticks,
                            listOfHardDrive,
                            monitor,
                            numberOfMonitors,
                            keyboardMouse,
                            webBrowser,
                            officeSuite,
                            listOfDevelopmentTools
                    );



                    listener.onOrderRetrieved(order);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: ");
            }
        });
    }
    private void displayOrderDetails(String orderID, String orderStatus, String requesterId) {
        if(orderStatus.equals("Waiting for acceptance" )){
            getOrderFromDatabase(requesterId, orderID, new OnOrderRetrievedListener() {
                @Override
                public void onOrderRetrieved(Orders order) {
                    order.checkIfItemsExist(new ItemExistenceCallback() {
                        @Override
                        public void onResult(boolean exists, Integer quantity) {
                            if(exists) {
                                setupOrderDetailsLayout(orderID, orderStatus, requesterId, exists);
                                if(!isTableAllOrders){
                                    acceptOrder.setOnClickListener(view -> {
                                        order.refreshDatabaseInfo();
                                        updateOrderStatus(requesterId, orderID, "Accepted, currently being assembled", () -> {
                                            refreshOrdersTable();
                                            resetOrderDetailsLayout();
                                        });
                                    });

                                    rejectOrder.setOnClickListener(view -> {
                                        handleRejectOrder(orderID, requesterId, commentEditText, errorTextCommentInput);
                                    });
                                    closeOrder.setOnClickListener(view -> {
                                        showError("Can't close this! Needs to be with an accepted status.", errorTextCommentInput);
                                    });
                                }else{
                                    setupOrderDetailsLayout(orderID, orderStatus,requesterId, true);
                                    acceptOrder.setOnClickListener(view -> showError("Can't Use This Option!", errorTextCommentInput));
                                    rejectOrder.setOnClickListener(view -> showError("Can't Use This Option!", errorTextCommentInput));
                                    closeOrder.setOnClickListener(view -> {
                                        showError("Can't Use This Option!", errorTextCommentInput);
                                    });
                                }


                            }
                            else{
                                if(!isTableAllOrders) {
                                    setupOrderDetailsLayout(orderID, orderStatus, requesterId, false);
                                    acceptOrder.setOnClickListener(view -> showError("Not enough stock. Can't accept this order!", errorTextCommentInput));
                                    rejectOrder.setOnClickListener(view -> handleRejectOrder(orderID, requesterId, commentEditText, errorTextCommentInput));
                                    closeOrder.setOnClickListener(view -> {
                                        showError("Can't close this! Needs to be with an accepted status.", errorTextCommentInput);
                                    });
                                }else{
                                    setupOrderDetailsLayout(orderID, orderStatus,requesterId,false);
                                    acceptOrder.setOnClickListener(view -> showError("Can't Use This Option!", errorTextCommentInput));
                                    rejectOrder.setOnClickListener(view -> showError("Can't Use This Option!", errorTextCommentInput));
                                    closeOrder.setOnClickListener(view -> {
                                        showError("Can't Use This Option!", errorTextCommentInput);
                                    });
                                }
                            }
                        }
                    });

                }
            });
        }
        if(orderStatus.equals("Accepted, currently being assembled")){
            getOrderFromDatabase(requesterId, orderID, new OnOrderRetrievedListener() {
                @Override
                public void onOrderRetrieved(Orders order) {
                    if (!isTableAllOrders) {
                        setupOrderDetailsLayout(orderID, orderStatus, requesterId, true);
                        Button closeOrder = findViewById(R.id.closeButton);
                        closeOrder.setOnClickListener(view -> {
                            handleCloseOrder(orderID, requesterId);
                        });
                        acceptOrder.setOnClickListener(view -> showError("This Order is already accepted!", errorTextCommentInput));
                        rejectOrder.setOnClickListener(view -> showError("This Order is already accepted, it can't be rejected!", errorTextCommentInput));
                    }else{
                        setupOrderDetailsLayout(orderID, orderStatus,requesterId,true);
                        acceptOrder.setOnClickListener(view -> showError("Can't Use This Option!", errorTextCommentInput));
                        rejectOrder.setOnClickListener(view -> showError("Can't Use This Option!", errorTextCommentInput));
                        closeOrder.setOnClickListener(view -> {
                            showError("Can't Use This Option!", errorTextCommentInput);
                        });
                    }
                }
            });
        }
        if(orderStatus.equals("Rejected") || orderStatus.equals("Delivered")){
            setupOrderDetailsLayout(orderID, orderStatus,requesterId,null);
            acceptOrder.setOnClickListener(view -> showError("Can't Use This Option!", errorTextCommentInput));
            rejectOrder.setOnClickListener(view -> showError("Can't Use This Option!", errorTextCommentInput));
            closeOrder.setOnClickListener(view -> {
                showError("Can't Use This Option!", errorTextCommentInput);
            });
        }


    }

    private void setupOrderDetailsLayout(String orderID, String orderStatus, String requesterId, Boolean stockAvailable) {
        LinearLayout orderDetailsLayout = findViewById(R.id.orderDetailsLayout);
        TextView id = findViewById(R.id.orderID);
        TextView status = findViewById(R.id.orderStatus);
        TextView creator = findViewById(R.id.orderCreator);
        TextView orderContentAvailability = findViewById(R.id.orderContentAvailability);
        acceptOrder = findViewById(R.id.approveButton);
        rejectOrder = findViewById(R.id.rejectButton);
        commentEditText = findViewById(R.id.commentEditText);
        errorTextCommentInput = findViewById(R.id.errorTextCommentInput);
        closeOrder = findViewById(R.id.closeButton);

        orderDetailsLayout.setVisibility(View.VISIBLE);
        id.setText(orderID);
        status.setText(orderStatus);
        creator.setText(requesterId);
        if(stockAvailable == null){
            orderContentAvailability.setText("");
        }else{
            if(stockAvailable){
                orderContentAvailability.setText("Stock Available");
            } else{
                orderContentAvailability.setText("Stock Unavailable");
            }
        }


    }


    private void handleRejectOrder(String orderID, String requesterId, EditText commentEditText, TextView errorTextCommentInput) {
        if (commentEditText.getText().toString().isEmpty()) {
            showError("You need to enter a comment before rejecting", errorTextCommentInput);
        } else {
            updateOrderStatus(requesterId, orderID, "Rejected", () -> {
                refreshOrdersTable();
                resetOrderDetailsLayout();
            });
        }
    }
    private void handleCloseOrder(String orderID, String requesterId) {
        updateOrderStatus(requesterId, orderID, "Delivered", () -> {
            refreshOrdersTable();
            resetOrderDetailsLayout();
        });
    }

    private void updateOrderStatus(String requesterId, String orderID, String newStatus, Runnable onSuccess) {

        ordersDatabaseRef.child(requesterId).child(orderID).child("Status").setValue(newStatus)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        onSuccess.run();
                    }
                });


    }

    private void showError(String message, TextView errorTextView) {
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(() -> {
            errorTextView.setVisibility(View.GONE);
        }, 3000);

    }

    private void resetOrderDetailsLayout() {
        new android.os.Handler().postDelayed(() -> {
            LinearLayout orderDetailsLayout = findViewById(R.id.orderDetailsLayout);
            TextView id = findViewById(R.id.orderID);
            TextView status = findViewById(R.id.orderStatus);
            TextView creator = findViewById(R.id.orderCreator);
            TextView orderContentAvailability = findViewById(R.id.orderContentAvailability);
            commentEditText = findViewById(R.id.commentEditText);
            errorTextCommentInput = findViewById(R.id.errorTextCommentInput);

            id.setText("");
            status.setText("");
            creator.setText("");
            orderContentAvailability.setText("");
            commentEditText.setText("");
            errorTextCommentInput.setText("");
            errorTextCommentInput.setVisibility(View.GONE);
            orderDetailsLayout.setVisibility(View.GONE);
        }, 2000);
    }

    private void refreshOrdersTable() {
        TableLayout ordersTableLayout = findViewById(R.id.assemblerOrdersTableLayout);
        ordersTableLayout.removeAllViews();
        checkOrderWithSpecialStatus("Waiting for acceptance", true);
    }



}
