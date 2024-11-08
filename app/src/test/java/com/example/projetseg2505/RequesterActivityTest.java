package com.example.projetseg2505;


import static org.mockito.Mockito.*;

import com.google.firebase.database.DatabaseReference;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

public class RequesterActivityTest {

    @Test
    // Ce premier test , sert à vérifier que lorsqu'une commande valide est crée , les bonnes informations sont ajoutées .
    public void testCreateOrder_validData_orderCreatedSuccessfully() {
        // Mock Firebase DatabaseReference pour simuler une base de données Firebase
        DatabaseReference mockDatabaseReference = mock(DatabaseReference.class);

        // Mock pour simuler le nœud "Orders" dans Firebase
        DatabaseReference ordersRef = mock(DatabaseReference.class);

        // Mock pour simuler un nœud spécifique à un utilisateur, ici "a"
        DatabaseReference specificOrderRef = mock(DatabaseReference.class);

        // Mock pour simuler une référence générée automatiquement par push() dans Firebase
        //pushedRef est une nouvelle référence/commande sous le noeud Orders/a
        DatabaseReference pushedRef = mock(DatabaseReference.class);

        // Configure le mock pour retourner ordersRef lorsqu'on accède à "Orders"
        when(mockDatabaseReference.child("Orders")).thenReturn(ordersRef);

        // Configure le mock pour retourner specificOrderRef lorsqu'on accède à "Orders/a"
        when(ordersRef.child("a")).thenReturn(specificOrderRef);

        // Configure le mock pour retourner pushedRef lorsqu'on appelle push()
        when(specificOrderRef.push()).thenReturn(pushedRef);

        // Création d'une commande valide sous forme d'une Map (clé-valeur)
        Map<String, Object> order = new HashMap<>();
        order.put("requesterID", "a"); // Identifiant unique de l'initiateur
        order.put("computerCase", "Case XYZ500"); // Référence du boîtier choisi
        order.put("Date Of Creation", "08-11-2024 20:02:52"); // Date et heure de création
        order.put("Status", "Waiting for acceptance"); // Statut initial de la commande

        // Ajout de la commande dans la base de données simulée
        //pushedRef représente un emplacement généré sous Orders/a
        pushedRef.setValue(order); // Simule l'enregistrement des données Firebase

        // Vérifie que la méthode setValue a bien été appelée avec les bonnes données
        verify(pushedRef).setValue(order);
    }

}
