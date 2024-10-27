package com.example.projetseg2505;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Component {
    private static String type;
    private static String subType;
    private static String description;
    private int quantity;
    private String comment; // not required
    private String dateTimeCreation;
    private String dateTimeModification;

    public Component(String type, String subType, String description, int quantity) {
        this.type = type;
        this.subType = subType;
        this.description = description;
        this.quantity = quantity;
        this.comment = "";
        this.dateTimeCreation = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        this.dateTimeModification = "";
    }

    public Component(String type, String subType, String description, int quantity, String comment) {
        this.type = type;
        this.subType = subType;
        this.description = description;
        this.quantity = quantity;
        this.comment = comment;
        this.dateTimeCreation = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        this.dateTimeModification = "";
    }

    // Getters
    public String getType() { return type; }
    public String getSubType() { return subType; }
    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public String getComment() { return comment; }
    public String getDateTimeCreation() { return dateTimeCreation; }
    public String getDateTimeModification() { return dateTimeModification; }


    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateDateTimeModification();
    }

    public void setComment(String comment) {
        this.comment = comment;
        updateDateTimeModification();
    }

    // Méthode pour mettre à jour la date de modification
    private void updateDateTimeModification() {
        this.dateTimeModification = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
    }






}
