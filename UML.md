interface Connect {
    + login(): void
    + signup(): void
    + logout(): void
}

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
    
    + getEmailAddress(): String
    + getId(): int
    + getPassword(): String
    + getCreationDate(): LocalDateTime
    + getTimeCreation(): LocalDateTime
    + getFirstName(): String
    + getLastName(): String
    + setEmailAddress(email: String): void
    + setId(id: int): void
    + setPassword(password: String): void
    + setCreationDate(date: LocalDateTime): void
    + setTimeCreation(time: LocalDateTime): void
    + setFirstName(name: String): void
    + setLastName(name: String): void
}

class StockKeeper extends User {
    + addStock(Order o): void
    + deleteStock(Order o): void
    + getOrderStatus(Order o): void
}

class Administrator extends User {
    + createClient(Requester q): void
    + deleteClient(Requester q): void
}

class Assembler extends User {
    + acceptOrder(Order o): void
    + refuseOrder(Order o): void
    + getOrderStatus(Order o): void
    + closeOrder(Order o): void
}

class Requester extends User {
    + createOrder(Order o): void
    + getOrderStatus(Order o): void
    + cancelOrder(orderId: String): void
}

Order <--> Assembler
Order <--> Requester

class Order {
    - orderId: String
    - requesterId: String
    - orderDate: Date
    - status: String
    - components: List<Component>

    + addComponent(component: Component): void
    + removeComponent(componentId: String): void
    + updateStatus(status: String): void
}

class Users_Coordinates {
    - hardwareComponents: List<HardwareComponent>
    - softwareComponents: List<SoftwareComponent>
    - stockKeeper: List<StockKeeper>
    - requester: Requester
    - assembler: Assembler
}

class MaterialComponents extends StockKeeper {
    + addHardwareComponent(component: Component): void
    + removeHardwareComponent(componentId: String): void
}

class SoftwareComponents extends StockKeeper {
    + addSoftwareComponent(component: Component): void
    + removeSoftwareComponent(componentId: String): void
}

User ..|> Connect

