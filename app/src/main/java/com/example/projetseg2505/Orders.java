package com.example.projetseg2505;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class Orders {
    // database ref
    private DatabaseReference userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("User");
    private DatabaseReference componentDatabaseRef = FirebaseDatabase.getInstance().getReference("Components");
    private DatabaseReference ordersDatabaseRef = FirebaseDatabase.getInstance().getReference("Orders");


    //order info
    private String requesterID;
    private String dateTimeOrder;
    private String dateTimeModification;
    private String status;

    //Hardware components
    private String computerCase;
    private String motherboard;
    private String memoryStick;
    private int numberOfMemorySticks;
    private ArrayList<String> listOfHardDrive;
    private String monitor;
    private int numberOfMonitors;
    private String keyboardMouse;

    //Software components
    private String webBrowser;
    private String officeSuite;
    private ArrayList<String> listOfDevelopmentTools;

    //componenet info
    private ArrayList<Integer> quantityTable;
    private int computerCaseQuantityDatabase;
    private int motherboardQuantityDatabase;
    private int memoryStickQuantityDatabase;
    private int[] listOfHardDriveQuantityDatabase;
    private int monitorQuantityDatabase;
    private int keyboardMouseQuantityDatabase;
    private int webBrowserQuantityDatabase;
    private int officeSuiteQuantityDatabase;
    private int[] listOfDevelopmentToolsQuantityDatabase;


    public Orders(String requesterID, String computerCase, String motherboard, String memoryStick, int numberOfMemorySticks, ArrayList<String> listOfHardDrive, String monitor, int numberOfMonitors, String keyboardMouse, String webBrowser, String officeSuite, ArrayList<String> listOfDevelopmentTools) {
        this.requesterID = requesterID;
        this.computerCase = computerCase;
        this.motherboard = motherboard;
        this.memoryStick = memoryStick;
        this.numberOfMemorySticks = numberOfMemorySticks;
        this.listOfHardDrive = listOfHardDrive;
        this.monitor = monitor;
        this.numberOfMonitors = numberOfMonitors;
        this.keyboardMouse = keyboardMouse;
        this.webBrowser = webBrowser;
        this.officeSuite = officeSuite;
        this.listOfDevelopmentTools = listOfDevelopmentTools;
        this.dateTimeOrder = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        this.dateTimeModification = "";
        this.status = "Waiting for acceptance";

    }

    public void pushOrderToDatabase() {
        HashMap<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("requesterID", requesterID);
        orderDetails.put("computerCase", computerCase);
        orderDetails.put("motherboard", motherboard);
        orderDetails.put("memoryStick", memoryStick);
        orderDetails.put("numberOfMemorySticks", numberOfMemorySticks);
        for (String hardDrive : listOfHardDrive) {
            orderDetails.put("hardDrive", hardDrive);
        }
        orderDetails.put("monitor", monitor);
        orderDetails.put("numberOfMonitors", numberOfMonitors);
        orderDetails.put("keyboardMouse", keyboardMouse);
        orderDetails.put("webBrowser", webBrowser);
        if (officeSuite != null) {
            orderDetails.put("officeSuite", officeSuite);
        }
        if (listOfDevelopmentTools.get(0) != null) {
            for (String devTools : listOfDevelopmentTools) {
                orderDetails.put("developmentTools", devTools);
            }
        }
        orderDetails.put("Date Of Creation", dateTimeOrder);
        orderDetails.put("Date Of Modification", dateTimeModification);
        orderDetails.put("Status", status);
        ordersDatabaseRef.child(requesterID.replace('.', ',')).push().setValue(orderDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("Database", "Order successfully added to the database.");
            } else {
                Log.d("Database", "Failed to add order to the database: " + task.getException().getMessage());
            }
        });
    }


    public boolean checkIfItemsExist(final ItemExistenceCallback finalCallback) {
        ArrayList<Boolean> existingTable = new ArrayList<>();
        quantityTable = new ArrayList<>();
        AtomicInteger totalItems = new AtomicInteger(6 + listOfHardDrive.size() + listOfDevelopmentTools.size());
        if (officeSuite != null) {
            totalItems.incrementAndGet();
        }
        AtomicInteger completedChecks = new AtomicInteger(0);

        ItemExistenceCallback itemCallback = new ItemExistenceCallback() {
            @Override
            public void onResult(boolean exists, Integer quantity) {
                synchronized (existingTable) {
                    existingTable.add(exists);
                    quantityTable.add(quantity);
                    completedChecks.incrementAndGet();

                    if (completedChecks.get() == totalItems.get()) {
                        boolean result = !existingTable.contains(Boolean.FALSE);
                        boolean quantityCheck = processQuantities();
                        if(result && quantityCheck){
                            finalCallback.onResult(true, -1);
                        }
                        else{
                            finalCallback.onResult(false, -1);
                        }


                    }
                }
            }
        };

        StorekeeperActivity.checkIfItemExists(computerCase, componentDatabaseRef, null, itemCallback);
        StorekeeperActivity.checkIfItemExists(motherboard, componentDatabaseRef, null, itemCallback);
        StorekeeperActivity.checkIfItemExists(memoryStick, componentDatabaseRef, null, itemCallback);
        StorekeeperActivity.checkIfItemExists(monitor, componentDatabaseRef, null, itemCallback);
        StorekeeperActivity.checkIfItemExists(keyboardMouse, componentDatabaseRef, null, itemCallback);
        StorekeeperActivity.checkIfItemExists(webBrowser, componentDatabaseRef, null, itemCallback);

        for (int i = 0; i < listOfHardDrive.size(); i++) {
            StorekeeperActivity.checkIfItemExists(listOfHardDrive.get(i), componentDatabaseRef, null, itemCallback);
        }
        for (int i = 0; i < listOfDevelopmentTools.size(); i++) {
            StorekeeperActivity.checkIfItemExists(listOfDevelopmentTools.get(i), componentDatabaseRef, null, itemCallback);
        }
        if (officeSuite != null) {
            StorekeeperActivity.checkIfItemExists(officeSuite, componentDatabaseRef, null, itemCallback);
        }

        return false;
    }

    public boolean processQuantities() {
        computerCaseQuantityDatabase = quantityTable.get(0);
        motherboardQuantityDatabase = quantityTable.get(1);
        memoryStickQuantityDatabase = quantityTable.get(2);
        monitorQuantityDatabase = quantityTable.get(3);
        keyboardMouseQuantityDatabase = quantityTable.get(4);
        webBrowserQuantityDatabase = quantityTable.get(5);
        listOfHardDriveQuantityDatabase = new int[2];
        for (int i = 1; i <= listOfHardDrive.size(); i++) {
            listOfHardDriveQuantityDatabase[i - 1] = quantityTable.get(5 + i);
        }
        listOfDevelopmentToolsQuantityDatabase = new int[3];
        for (int i = 1; i <= listOfDevelopmentTools.size(); i++) {
            listOfDevelopmentToolsQuantityDatabase[i - 1] = quantityTable.get(5 + listOfHardDrive.size() + i);
        }
        int indexOfFinalElem = 5 + listOfHardDrive.size() + listOfDevelopmentTools.size() + 1;
        if (officeSuite != null) {
            officeSuiteQuantityDatabase = quantityTable.get(indexOfFinalElem);
        }
        return checkAvailableQuantities();
    }

    public boolean checkAvailableQuantities(){
        if(computerCaseQuantityDatabase < 1 && motherboardQuantityDatabase < 1 && memoryStickQuantityDatabase < numberOfMemorySticks && motherboardQuantityDatabase < numberOfMonitors && keyboardMouseQuantityDatabase < 1 && webBrowserQuantityDatabase < 1){
            return false;
        }
        else{
            for(int i = 0; i < listOfDevelopmentTools.size();i++){
                if(listOfDevelopmentToolsQuantityDatabase[i] < 1){
                    return false;
                }
            }
            for (int i =0; i<listOfHardDrive.size();i++){
                if(listOfHardDrive.size() > 1){
                    if(listOfHardDrive.get(0).equals(listOfHardDrive.get(1))){
                        if(listOfHardDriveQuantityDatabase[0]<2){
                            return false;
                        }
                    }
                    else{
                        if(listOfHardDriveQuantityDatabase[0]<1 || listOfHardDriveQuantityDatabase[1]<1 ){
                            return false;
                        }
                    }
                }
                else{
                    if(listOfHardDriveQuantityDatabase[0] < 1 ){
                        return false;
                    }
                }

            }
            if(officeSuite != null){
                if(officeSuiteQuantityDatabase<1){
                    return false;
                }
            }
        }
        return true;
    }





    public void refreshDatabaseInfo(){

        int newComputerCaseQuantityDatabase = computerCaseQuantityDatabase-1;
        int newMotherboardQuantityDatabase = motherboardQuantityDatabase-1;
        int newMemoryStickQuantityDatabase = memoryStickQuantityDatabase - numberOfMemorySticks;
        int newMonitorQuantityDatabase = monitorQuantityDatabase - numberOfMonitors;
        int newKeyboardMouseQuantityDatabase = keyboardMouseQuantityDatabase -1;
        int newWebBrowserQuantityDatabase = webBrowserQuantityDatabase-1;
        int newOfficeSuiteQuantityDatabase;
        if(officeSuite != null){
            newOfficeSuiteQuantityDatabase = officeSuiteQuantityDatabase-1;
            StorekeeperActivity.applyChanges(componentDatabaseRef,officeSuite,newOfficeSuiteQuantityDatabase,null,false,null);
        }

        int[] newListOfHardDriveQuantityDatabase = new int[2];
        for (int i = 0; i < listOfHardDrive.size();i++ ){
            newListOfHardDriveQuantityDatabase[i] = listOfHardDriveQuantityDatabase[i]-1;
            StorekeeperActivity.applyChanges(componentDatabaseRef,listOfHardDrive.get(i),newListOfHardDriveQuantityDatabase[i],null,false,null);
        }
        if(listOfDevelopmentTools.get(0) != null){
            int[] newListOfDevelopmentToolsQuantityDatabase = new int[4];
            for (int i = 0; i < listOfDevelopmentTools.size();i++ ){
                newListOfDevelopmentToolsQuantityDatabase[i] = listOfDevelopmentToolsQuantityDatabase[i]-1;
                StorekeeperActivity.applyChanges(componentDatabaseRef,listOfDevelopmentTools.get(i),newListOfDevelopmentToolsQuantityDatabase[i],null,false,null);
            }
        }

        StorekeeperActivity.applyChanges(componentDatabaseRef,computerCase,newComputerCaseQuantityDatabase,null,false,null);
        StorekeeperActivity.applyChanges(componentDatabaseRef,motherboard,newMotherboardQuantityDatabase,null,false,null);
        StorekeeperActivity.applyChanges(componentDatabaseRef,memoryStick,newMemoryStickQuantityDatabase,null,false,null);
        StorekeeperActivity.applyChanges(componentDatabaseRef,monitor,newMonitorQuantityDatabase,null,false,null);
        StorekeeperActivity.applyChanges(componentDatabaseRef,keyboardMouse,newKeyboardMouseQuantityDatabase,null,false,null);
        StorekeeperActivity.applyChanges(componentDatabaseRef,webBrowser,newWebBrowserQuantityDatabase,null,false,null);

    }
}

