package com.example.projetseg2505;

public class SoftwareComponent extends Component{
    public SoftwareComponent(String subType, String description, int quantity, String comment) {
        // Le type est toujours "Logiciel" pour les composants logiciels
        super("Software", subType, description, quantity, comment);
    }
    public SoftwareComponent(String subType, String description, int quantity) {
        // Le type est toujours "Logiciel" pour les composants logiciels
        super("Software", subType, description, quantity);
    }
}
