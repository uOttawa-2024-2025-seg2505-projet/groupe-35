package com.example.projetseg2505;

public class HardwareComponent extends Component{

    public HardwareComponent(String subType, String description, int quantity) {

        super("Hardware", subType, description, quantity);
    }

    public HardwareComponent(String subType, String description, int quantity, String comment) {

        super("Hardware", subType, description, quantity, comment);
    }
}
