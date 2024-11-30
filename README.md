# uOttawa - 2024-2025 - SEG2505A - Projet - Groupe 35

**Nom du projet** : Service à la demande de PC sur mesure

## Membres du projet

| Prénom        | NOM           | Identifiant GitHub |
| ------------- | ------------- | ------------------ |
| Khalil        | YAHYAOUI      | khalil-yahyaoui-015|
| Yassine       | SAHLI         | YassineSahli04     |
| Yassine       | KHELIFI       | Yassine-Khelifi    |
| Othmane       | OUAJJOU       | oth-code           |
| Othman        | NAGIFI        | NagifiOthman       |

## Introduction

Dans le cadre du cours SEG2505 à l’Université d'Ottawa, notre équipe s’attaque au projet Service à la demande de PC sur mesure. Ce projet vise à concevoir une application Android permettant aux utilisateurs d'une organisation de gérer des commandes de PC sur mesure. Cette application est destinée à offrir des configurations de matériel et de logiciels adaptées aux besoins spécifiques des utilisateurs.

Notre application gérera plusieurs rôles d’utilisateurs :

- Administrator : responsable de la gestion des comptes utilisateurs, en particulier ceux qui auront le rôle de Demandeur.
- StoreKeeper : en charge de la gestion des composants en stock, qu’il pourra ajouter, modifier ou supprimer.
- Assembler : responsable de l'assemblage des commandes, en s’assurant que les composants nécessaires sont disponibles.
- Requester : utilisateur ayant la possibilité de passer des commandes de PC personnalisés selon une configuration de composants choisis.

Le projet sera développé en utilisant Android Studio, avec une base de données locale (SQLite ou Firebase) pour le stockage, et inclura les étapes suivantes :

Configuration du dépôt GitHub et mise en place de la gestion des utilisateurs.
Développement des fonctionnalités propres à chaque rôle.
Création d'un rapport final détaillant nos choix de conception, les défis rencontrés et les contributions de chaque membre de l’équipe.

## Clarifications sur les exigences

### Exigences explicites reformulées

<à compléter (optionnel)>
 
### Exigences implicites proposées

<à compléter (optionnel)>

### Hypothèses

<à compléter (optionnel)>

## Modélisation

### Diagramme de classes

<à compléter>

```plantuml
@startuml
class User {
    - email_address: String
    - id: int
    - password: String
    - firstName: String
    - lastName: String
    - dateCreation: LocalDateTime
    - timeCreation: LocalDateTime
    - modification_date: LocalDateTime
    - modification_time: LocalDateTime

    + login(email: String, password: String): boolean
    + logout(): void
    + redirectToUserPage(userType: String): void
    + getEmailAddress(): String
    + getId(): int
    + getPassword(): String
    + getCreationDate(): LocalDateTime
    + getTimeCreation(): LocalDateTime
    + getFirstName(): String
    + getLastName(): String
}

class StoreKeeper extends User {
    + addStock(component: Component): void
    + deleteStock(description: String): void
    + viewItem(description: String): Component
    + modifyItem(description: String, quantity: int, comment: String): void
    + loadComponentsData(): void
    + incrementQuantity(description: String): void
    + decrementQuantity(description: String): void
    + checkIfItemExists(description: String, componentRef: DatabaseReference, quantity: Integer, callback: ItemExistenceCallback): void
    + applyChanges(databaseRef: DatabaseReference, description: String, newQuantity: int, comment: String, isDecrement: boolean, callback: ItemExistenceCallback): void
}

class Administrator extends User {
    + createClient(email: String, password: String, firstName: String, lastName: String): void
    + deleteClient(email: String): void
    + clearDatabase(): void
    + resetDatabase(fileName: String): void
    + resetStock(fileName: String): void
}

class Assembler extends User {
    + acceptOrder(order: Orders): void
    + refuseOrder(order: Orders): void
    + getOrderStatus(order: Orders): void
    + closeOrder(order: Orders): void
}

class Requester extends User {
    + createOrder(order: Orders): void
    + getOrderStatus(order: Orders): void
    + cancelOrder(orderId: String): void
}

class Orders {
    - requesterID: String
    - dateTimeOrder: String
    - dateTimeModification: String
    - status: String
    - components: List<Component>
    - componentDatabaseRef: DatabaseReference
    - ordersDatabaseRef: DatabaseReference

    + pushOrderToDatabase(): void
    + checkIfItemsExist(finalCallback: ItemExistenceCallback): boolean
    + processQuantities(): boolean
    + checkAvailableQuantities(): boolean
    + refreshDatabaseInfo(): void
}

class Component {
    - type: String
    - subType: String
    - description: String
    - quantity: int
    - comment: String
    - dateTimeCreation: String
    - dateTimeModification: String

    + Component(type: String, subType: String, description: String, quantity: int)
    + setQuantity(quantity: int): void
    + decrementQuantity(): void
    + updateDateTimeModification(): void
}

class HardwareComponent extends Component {
    + HardwareComponent(subType: String, description: String, quantity: int)
    + HardwareComponent(subType: String, description: String, quantity: int, comment: String)
}

class SoftwareComponent extends Component {
    + SoftwareComponent(subType: String, description: String, quantity: int)
    + SoftwareComponent(subType: String, description: String, quantity: int, comment: String)
}

interface ItemExistenceCallback {
    + onResult(exists: boolean, quantity: Integer): void
}

Orders <-- Assembler
Orders <-- Requester
Orders "1" *-- "1..*" Component
StoreKeeper "1" *-- "1..*" Component : Gère

@enduml


### Diagrammes d'utilisation (optionnel)

```plantuml
@startuml
package "CustomDesktopService" #DDDDDD {
    actor Administrator
    actor StoreKeeper
    actor Assembler
    actor Requester

    "Register a requester" as (registerRequester)
    "Unegister a requester" as (unregisterRequester)

    Administrator --> (registerRequester)
    Administrator --> (unregisterRequester)
@enduml

### Diagrammes d'états

#### Commandes

```plantuml
@startuml
    [*] -->  WaitingForApproval : "Création de la commande par un Requester"

    WaitingForApproval --> AcceptedAssembling : "Acceptation de la commande par l'Assembler"

    AcceptedAssembling -> Delivered : "Livraison de la commande"
    
    WaitingForApproval --> Rejected : "Rejet de la commande par l'Assembler"
    
    Delivered --> [*]
    
    Rejected --> [*]
@enduml

### Diagrammes d'activités

#### Accueil et authentification

```plantuml
@startuml
    title Authentification

    start
        :Initialiser l'application;

        :Se connecter à la base de données;
            
        while (Appui sur la touche de retour ?) is (Non) 
            :Afficher de la fenêtre d'accueil;
        
            if (Appui sur le bouton "OK" ?) is (Oui)
                :Valider de l'identifiant et du mot de passe;
        
                if (Authentification validée) then (Oui)
                    If (L'utilisateur est un Administrator) then (Oui)
                        :Afficher la fenêtre d'un Administrator;

                        :...;
                    elseif (L'utilisateur est un StoreKeeper) then (Oui)
                        :Afficher la fenêtre d'un StoreKeeper;

                        :...;
                    elseif (L'utilisateur est un Assembler) then (Oui)
                        :Afficher la fenêtre d'un Assembler;

                        :...;
                    elseif (L'utilisateur a le rôle Requester) then (Oui)
                        :Afficher la fenêtre d'un Requester;

                        :...;
                    else
                    :EAfficher une rreur de conception: rôle inconnu;
                    endif
                else (Non)
                    :Afficher une erreur d'authentification;
                endif
            endif
        endwhile (Oui)

        :Libérer les ressources (base de données...);
    stop
@enduml

#### Gestion des utilisateurs

<à compléter>

@startuml
    title Authentification et Gestion des Utilisateurs

    start
        :Initialiser l'application;
        :Se connecter à la base de données;

        while (Appui sur la touche de retour ?) is (Non) 
            :Afficher la fenêtre d'accueil;

            if (Appui sur le bouton "OK" ?) then (Oui)
                :Valider l'identifiant et le mot de passe;

                if (Authentification validée) then (Oui)
                    if (L'utilisateur est un Administrator) then (Oui)
                        :Afficher la fenêtre d'un Administrator;
                        
                        fork
                            :Créer ou Supprimer un Utilisateur;
                        fork again
                            :Effacer la Base de Données;
                        fork again
                            :Réinitialiser la Base de Données à partir d'un fichier;
                        fork again
                            :Réinitialiser le Stock à partir d'un fichier;
                        end fork

                    elseif (L'utilisateur est un StoreKeeper) then (Oui)
                        :Afficher la fenêtre d'un StoreKeeper;
                    elseif (L'utilisateur est un Assembler) then (Oui)
                        :Afficher la fenêtre d'un Assembler;
                    elseif (L'utilisateur a le rôle Requester) then (Oui)
                        :Afficher la fenêtre d'un Requester;
                    else
                        :Afficher une erreur de conception : rôle inconnu;
                    endif
                else (Non)
                    :Afficher une erreur d'authentification;
                endif
            endif
        endwhile (Oui)

        :Libérer les ressources (base de données...);
    stop
@enduml


#### Gestion du stock

<à compléter>

@startuml
    title Authentification, Gestion des Utilisateurs et Gestion du Stock

    start
        :Initialiser l'application;
        :Se connecter à la base de données;

        while (Appui sur la touche de retour ?) is (Non) 
            :Afficher la fenêtre d'accueil;

            if (Appui sur le bouton "OK" ?) then (Oui)
                :Valider l'identifiant et le mot de passe;

                if (Authentification validée) then (Oui)
                    if (L'utilisateur est un Administrator) then (Oui)
                        :Afficher la fenêtre d'un Administrator;
                        
                        fork
                            :Créer ou Supprimer un Utilisateur;
                        fork again
                            :Effacer la Base de Données;
                        fork again
                            :Réinitialiser la Base de Données à partir d'un fichier;
                        fork again
                            :Réinitialiser le Stock à partir d'un fichier;
                        end fork

                    elseif (L'utilisateur est un StoreKeeper) then (Oui)
                        :Afficher la fenêtre de gestion du stock;

                        fork
                            :Ajouter des articles au stock;
                        fork again
                            :Mettre à jour les quantités de stock;
                        fork again
                            :Supprimer des articles du stock;
                        fork again
                            :Générer un rapport d'inventaire;
                        end fork

                    elseif (L'utilisateur est un Assembler) then (Oui)
                        :Afficher la fenêtre d'un Assembler;
                    elseif (L'utilisateur a le rôle Requester) then (Oui)
                        :Afficher la fenêtre d'un Requester;
                    else
                        :Afficher une erreur de conception : rôle inconnu;
                    endif
                else (Non)
                    :Afficher une erreur d'authentification;
                endif
            endif
        endwhile (Oui)

        :Libérer les ressources (base de données...);
    stop
@enduml


#### Passage d'une commande

<à compléter>

@startuml
    title Authentification, Gestion des Utilisateurs, Gestion du Stock et Passage d'une Commande

    start
        :Initialiser l'application;
        :Se connecter à la base de données;

        while (Appui sur la touche de retour ?) is (Non) 
            :Afficher la fenêtre d'accueil;

            if (Appui sur le bouton "OK" ?) then (Oui)
                :Valider l'identifiant et le mot de passe;

                if (Authentification validée) then (Oui)
                    if (L'utilisateur est un Administrator) then (Oui)
                        :Afficher la fenêtre d'un Administrator;
                        
                        fork
                            :Créer ou Supprimer un Utilisateur;
                        fork again
                            :Effacer la Base de Données;
                        fork again
                            :Réinitialiser la Base de Données à partir d'un fichier;
                        fork again
                            :Réinitialiser le Stock à partir d'un fichier;
                        end fork

                    elseif (L'utilisateur est un StoreKeeper) then (Oui)
                        :Afficher la fenêtre de gestion du stock;

                        fork
                            :Ajouter des articles au stock;
                        fork again
                            :Mettre à jour les quantités de stock;
                        fork again
                            :Supprimer des articles du stock;
                        fork again
                            :Générer un rapport d'inventaire;
                        end fork

                    elseif (L'utilisateur est un Assembler) then (Oui)
                        :Afficher la fenêtre d'un Assembler;

                    elseif (L'utilisateur a le rôle Requester) then (Oui)
                        :Afficher la fenêtre de passage de commande;
                        fork
                        :Sélectionner des articles à commander;
                        fork again
                        :Définir les quantités pour chaque article sélectionné;

                        if (Stock disponible pour chaque article ?) then (Oui)
                            :Valider la commande;

                            :Envoyer la commande pour approbation;
                        else (Non)
                            :Afficher une erreur : stock insuffisant pour certains articles;
                        endif
                        end fork

                    else
                        :Afficher une erreur de conception : rôle inconnu;
                    endif
                else (Non)
                    :Afficher une erreur d'authentification;
                endif
            endif
        endwhile (Oui)

        :Libérer les ressources (base de données...);
    stop
@enduml


#### Traitement d'une commande

<à compléter>
 @startuml
    title Authentification, Gestion des Utilisateurs, Gestion du Stock, Passage d'une Commande et Traitement d'une commancd

    start
        :Initialiser l'application;
        :Se connecter à la base de données;

        while (Appui sur la touche de retour ?) is (Non) 
            :Afficher la fenêtre d'accueil;

            if (Appui sur le bouton "OK" ?) then (Oui)
                :Valider l'identifiant et le mot de passe;

                if (Authentification validée) then (Oui)
                    if (L'utilisateur est un Administrator) then (Oui)
                        :Afficher la fenêtre d'un Administrator;
                        
                        fork
                            :Créer ou Supprimer un Utilisateur;
                        fork again
                            :Effacer la Base de Données;
                        fork again
                            :Réinitialiser la Base de Données à partir d'un fichier;
                        fork again
                            :Réinitialiser le Stock à partir d'un fichier;
                        end fork

                    elseif (L'utilisateur est un StoreKeeper) then (Oui)
                        :Afficher la fenêtre de gestion du stock;

                        fork
                            :Ajouter des articles au stock;
                        fork again
                            :Mettre à jour les quantités de stock;
                        fork again
                            :Supprimer des articles du stock;
                        fork again
                            :Générer un rapport d'inventaire;
                        end fork

                    elseif (L'utilisateur est un Assembler) then (Oui)
                        :Afficher la fenêtre d'un Assembler;
                        fork
                            :Afficher les commandes avec le statut en attente;
                        fork again
                            :Afficher les commandes avec le statut accepté;
                        fork again
                            :Afficher toutes les commandes;
                        end fork
                        

                    elseif (L'utilisateur a le rôle Requester) then (Oui)
                        :Afficher la fenêtre de passage de commande;
                        fork
                        :Sélectionner des articles à commander;
                        fork again
                        :Définir les quantités pour chaque article sélectionné;

                        if (Stock disponible pour chaque article ?) then (Oui)
                            :Valider la commande;

                            :Envoyer la commande pour approbation;
                        else (Non)
                            :Afficher une erreur : stock insuffisant pour certains articles;
                        endif
                        end fork

                    else
                        :Afficher une erreur de conception : rôle inconnu;
                    endif
                else (Non)
                    :Afficher une erreur d'authentification;
                endif
            endif
        endwhile (Oui)

        :Libérer les ressources (base de données...);
    stop
@enduml

### Diagrammes de séquences

#### Pour l'accueil et l'authentification

```plantuml
@startuml
    actor Inconnu

    control MainActivity
    control AdministratorActivity
    control StoreKeeperActivity
    control AssemblerActivity
    control RequesterActivity

    Inconnu --> MainActivity : Demande d'authentification\n(avec identifiant et mot de passe)

    MainActivity <--> Database : Rechercher un utilisateur\navec un identifiant et un mot de passe

    alt L'utilisateur existe
        MainActivity <--> Database : Obtenir des informations sur l'utilisateur\n(dont son rôle) 
        
        alt Le rôle de l'utilisateur est Administror
            MainActivity --> AdministratorActivity
        else Le rôle de l'utilisateur est StoreKeeper
            MainActivity --> StoreKeeperActivity
        else Le rôle de l'utilisateur est Assembler
            MainActivity --> AssemblerActivity
        else Le rôle de l'utilisateur est Requester
            MainActivity --> RequesterActivity
        else Rôle inconnu 
            MainActivity --> Inconnu : Afficher une erreur de conception
        end
    else Sinon
        MainActivity --> Inconnu : Afficher une erreur d'authentification
    end

    database Database
@enduml

#### Pour le rôle Administrator

```plantuml
@startuml
    actor Administrator

    control AdministratorActivity
    
    database Database

    Administrator --> AdministratorActivity : Créer un utilisateur

    AdministratorActivity <--> Database : Obtenir la liste des utilisateurs

    alt L'utilisateur existe déjà
        AdministratorActivity --> Administrator: Afficher une erreur
    else Sinon
        AdministratorActivity --> Database : Ajouter une ligne à la table Users
    end
@enduml

#### Pour le rôle StoreKeeper

<à compléter>

@startuml
    actor StoreKeeper

    control StoreKeeperActivity
    
    database Database

    StoreKeeper --> StoreKeeperActivity : Ajouter des articles au stock

    StoreKeeperActivity <--> Database : Rechercher l'article dans le stock

    alt L'article existe déjà
        StoreKeeperActivity --> Database : Mettre à jour la quantité de l'article
    else Sinon
        StoreKeeperActivity --> Database : Ajouter un nouvel article au stock
    end

    StoreKeeper --> StoreKeeperActivity : Générer un rapport d'inventaire

    StoreKeeperActivity <--> Database : Obtenir toutes les données de stock

    StoreKeeperActivity --> StoreKeeper : Afficher le rapport d'inventaire
@enduml


#### Pour le rôle Assembler

<à compléter>

@startuml
    actor Assembler

    control AssemblerActivity
    
    database Database

    Assembler --> AssemblerActivity : Demande d'assemblage

    AssemblerActivity <--> Database : Vérifier la disponibilité des articles nécessaires

    alt Tous les articles sont disponibles
        AssemblerActivity --> Database : Déduire les quantités nécessaires du stock
        AssemblerActivity --> Assembler : Confirmer l'assemblage
    else Certains articles sont insuffisants
        AssemblerActivity --> Assembler : Afficher une erreur de stock insuffisant
    end
@enduml

#### Pour le rôle Requester

<à compléter>

@startuml
    actor Requester

    control RequesterActivity
    
    database Database

    Requester --> RequesterActivity : Passer une commande

    RequesterActivity <--> Database : Vérifier la disponibilité des articles commandés

    alt Stock disponible pour chaque article
        RequesterActivity --> Database : Enregistrer la commande
        RequesterActivity --> Requester : Confirmer la commande
    else Certains articles sont insuffisants
        RequesterActivity --> Requester : Afficher une erreur de stock insuffisant
    end
@enduml


## Eléments de conception

<à compléter>

- Modèles UML : Utilisation de diagrammes UML pour représenter le système de manière visuelle et structurée.
- Architecture de l’application : Structure globale de l'application, incluant la manière dont les différents composants (interface utilisateur, gestion de la base de données, logique métier) interagissent.
- Interfaces utilisateur (UI) : Les écrans de l’application Android, en détaillant l'interface et l'expérience utilisateur pour chaque rôle (Administrator, StoreKeeper, Assembler, Requester).
- Conception de la base de données : La structure des données, comme les tables et leurs relations pour gérer les composants, les utilisateurs, et les commandes, particulièrement en utilisant SQLite ou Firebase.
- APIs et Interface de communication : La définition des interfaces, méthodes et fonctions pour l’interaction entre les composants de l'application (par exemple, comment l'interface utilisateur interagit avec la base de données).
- Règles et contraintes de gestion : Par exemple, les règles de validation pour s'assurer qu’un utilisateur peut seulement accéder aux fonctionnalités correspondant à son rôle.

## Eléments d'implémentation

<à compléter (optionnel)>

## Eléments de tests unitaires

<à compléter (outils utiliser, comment les lancer, etc.)>

Voici une explication simple de chaque test dans cette classe OrdersTest :

testOrdersConstructor_ShouldInitializeCorrectly : Vérifie que le constructeur de la classe Orders initialise correctement toutes les propriétés de l'objet order avec les valeurs fournies. Ce test s’assure que les valeurs initiales et les valeurs par défaut (statut et date de modification) sont correctement définies.

testCheckAvailableQuantities_AllQuantitiesSufficient : Vérifie que la méthode checkAvailableQuantities retourne true lorsque toutes les quantités sont suffisantes pour chaque composant nécessaire.

testCheckAvailableQuantities_InsufficientComputerCaseQuantity : Modifie la quantité de boîtiers d'ordinateur à zéro dans quantityTable et vérifie que checkAvailableQuantities retourne false, ce qui indique qu’une quantité insuffisante de boîtiers est détectée.

testCheckAvailableQuantities_InsufficientMemoryStickQuantity : Définit une quantité de barrettes mémoire insuffisante et vérifie que checkAvailableQuantities retourne false, validant que la méthode détecte ce manque.

testCheckAvailableQuantities_InsufficientHardDriveQuantity : Définit des quantités insuffisantes pour les disques durs et vérifie que checkAvailableQuantities retourne false, indiquant qu’il manque des disques durs.

testCheckAvailableQuantities_InsufficientDevelopmentToolsQuantity : Définit une quantité insuffisante pour un des outils de développement et vérifie que checkAvailableQuantities retourne false.

testMinimalHardwareConfiguration : Crée une configuration minimale d'ordinateur avec une seule barrette mémoire et un seul disque dur. Vérifie que checkAvailableQuantities retourne true pour cette configuration de base.

testMaximalHardwareConfiguration : Crée une configuration maximale avec plusieurs composants (4 barrettes mémoire, 2 disques durs, 3 moniteurs, etc.). Vérifie que checkAvailableQuantities retourne true lorsque tous les composants nécessaires sont présents en quantités maximales.

testSoftwareConfigurationOnlyBrowser : Crée une configuration où seul un navigateur est installé, sans suite bureautique ni outils de développement. Vérifie que checkAvailableQuantities retourne true.

testSoftwareConfigurationFull : Crée une configuration complète avec un navigateur, une suite bureautique et trois outils de développement. Vérifie que checkAvailableQuantities retourne true pour cette configuration.

testTwoIdenticalHardDrives : Vérifie que la méthode accepte deux disques durs identiques si les quantités nécessaires sont disponibles.

testTwoDifferentHardDrives : Vérifie que la méthode accepte deux disques durs différents avec des quantités suffisantes.

testInsufficientDevelopmentToolsQuantity : Modifie quantityTable pour rendre une des quantités des outils de développement insuffisante et vérifie que checkAvailableQuantities retourne false.

## Comment reconstruire la solution

<à compléter (optionnel)>

## Comment installer et utiliser la solution

<à compléter (optionnel)>

## Eléments de démonstration

### Scénario ("storyboard") suggéré

<à compléter (optionnel)>

### Valeurs de test

#### Utilisateurs

| Rôle           | Identifiant de connexion | Mot de passe  |
| -------------- | ------------------------ | ------------- |
| Administrateur | admin@gmail.com          | admin         |
| StoreKeeper    | storekeeper@gmail.com    | storekeeper   |
| Assembler      | assembler@gmail.com      | assembler     |

#### Fichier de données exemple

<à compléter : où se trouve-t-il ? Quel est son format ? ...>

Les fichiers de données se trouvent dans le sous-dossier 'assets'

## Limites et problèmes connus

<à compléter (fonctions non implémentées ou non terminées, limites connues, bugs connus...)>

Nous n'avons pas rencontré de problème particulier pour le livrable 3.

## Information destinées aux correcteurs

<à compléter (optionnel)>