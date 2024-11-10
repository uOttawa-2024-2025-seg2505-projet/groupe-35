package com.example.projetseg2505;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@RunWith(RobolectricTestRunner.class)
public class OrdersTest {

    private Orders order;
    private String requesterID;
    private String computerCase;
    private String motherboard;
    private String memoryStick;
    private int numberOfMemorySticks;
    private ArrayList<String> listOfHardDrive;
    private String monitor;
    private int numberOfMonitors;
    private String keyboardMouse;
    private String webBrowser;
    private String officeSuite;
    private ArrayList<String> listOfDevelopmentTools;


    @Test
    public void testOrdersConstructor_ShouldInitializeCorrectly() {

        requesterID = "12345";
        computerCase = "Standard Case";
        motherboard = "ASUS Motherboard";
        memoryStick = "Corsair 8GB";
        numberOfMemorySticks = 2;
        listOfHardDrive = new ArrayList<>(Arrays.asList("HDD1", "HDD2"));
        monitor = "Dell Monitor";
        numberOfMonitors = 1;
        keyboardMouse = "Logitech Combo";
        webBrowser = "Chrome";
        officeSuite = "Microsoft Office";
        listOfDevelopmentTools = new ArrayList<>(Arrays.asList("Eclipse", "IntelliJ"));

        // Create Orders instance
        order = new Orders(requesterID, computerCase, motherboard, memoryStick, numberOfMemorySticks,
                listOfHardDrive, monitor, numberOfMonitors, keyboardMouse, webBrowser,
                officeSuite, listOfDevelopmentTools);

        assertEquals(requesterID, order.getRequesterID());
        assertEquals(computerCase, order.getComputerCase());
        assertEquals(motherboard, order.getMotherboard());
        assertEquals(memoryStick, order.getMemoryStick());
        assertEquals(numberOfMemorySticks, order.getNumberOfMemorySticks());
        assertEquals(listOfHardDrive, order.getListOfHardDrive());
        assertEquals(monitor, order.getMonitor());
        assertEquals(numberOfMonitors, order.getNumberOfMonitors());
        assertEquals(keyboardMouse, order.getKeyboardMouse());
        assertEquals(webBrowser, order.getWebBrowser());
        assertEquals(officeSuite, order.getOfficeSuite());
        assertEquals(listOfDevelopmentTools, order.getListOfDevelopmentTools());
        assertEquals("Waiting for acceptance", order.getStatus());
        assertEquals("", order.getDateTimeModification());
    }

    @BeforeEach
    public void setup() {
        order = new Orders("123", "Case1", "Motherboard1", "Memory1", 2, new ArrayList<>(), "Monitor1", 1, "Keyboard1", "Chrome", "Office", new ArrayList<>());

        order.listOfHardDrive.add("HDD1");
        order.listOfHardDrive.add("HDD2");
        order.listOfDevelopmentTools.add("Eclipse");
        order.listOfDevelopmentTools.add("IntelliJ");

        order.quantityTable = new ArrayList<>();

        // add quantities
        order.quantityTable.add(1); // computerCaseQuantityDatabase
        order.quantityTable.add(1); // motherboardQuantityDatabase
        order.quantityTable.add(3); // memoryStickQuantityDatabase
        order.quantityTable.add(1); // monitorQuantityDatabase
        order.quantityTable.add(1); // keyboardMouseQuantityDatabase
        order.quantityTable.add(1); // webBrowserQuantityDatabase
        order.quantityTable.add(2); // listOfHardDriveQuantityDatabase pour HDD1
        order.quantityTable.add(2); // listOfHardDriveQuantityDatabase pour HDD2
        order.quantityTable.add(1); // listOfDevelopmentToolsQuantityDatabase pour Eclipse
        order.quantityTable.add(1); // listOfDevelopmentToolsQuantityDatabase pour IntelliJ
        order.quantityTable.add(1); // officeSuiteQuantityDatabase
    }

    @Test
    public void testCheckAvailableQuantities_AllQuantitiesSufficient() {
        order.processQuantities();


        boolean result = order.checkAvailableQuantities();
        assertTrue(result);
    }

    @Test
    public void testCheckAvailableQuantities_InsufficientComputerCaseQuantity() {

        order.quantityTable.set(0, 0);
        order.processQuantities();

        boolean result = order.checkAvailableQuantities();
        assertFalse(result);
    }

    @Test
    public void testCheckAvailableQuantities_InsufficientMemoryStickQuantity() {
        order.quantityTable.set(2, 1);
        order.processQuantities();

        boolean result = order.checkAvailableQuantities();
        assertFalse(result);
    }

    @Test
    public void testCheckAvailableQuantities_InsufficientHardDriveQuantity() {

        order.quantityTable.set(6, 1);
        order.quantityTable.set(7, 0);
        order.processQuantities();

        boolean result = order.checkAvailableQuantities();
        assertFalse(result);
    }

    @Test
    public void testCheckAvailableQuantities_InsufficientDevelopmentToolsQuantity() {
        order.quantityTable.set(8, 0);
        order.processQuantities();

        boolean result = order.checkAvailableQuantities();
        assertFalse(result);
    }
    @Test
    public void testMinimalHardwareConfiguration() {
        order = new Orders("001", "Case1", "Motherboard1", "Memory1", 1,
                new ArrayList<>(Arrays.asList("HDD1")), "Monitor1", 1, "Keyboard1", "Chrome",
                null, new ArrayList<>());

        order.quantityTable = new ArrayList<>(Arrays.asList(1, 1, 1, 1, 1, 1, 1));
        order.processQuantities();

        assertTrue(order.checkAvailableQuantities());
    }
    @Test
    public void testMaximalHardwareConfiguration() {

        order = new Orders("002", "Case2", "Motherboard2", "Memory2", 4,
                new ArrayList<>(Arrays.asList("HDD1", "HDD2")), "Monitor2", 3, "Keyboard2", "Firefox",
                "Office Suite", new ArrayList<>(Arrays.asList("Eclipse", "IntelliJ", "VSCode")));

        order.quantityTable = new ArrayList<>(Arrays.asList(1, 1, 4, 3, 1, 1, 1, 1, 1, 1, 1, 1));
        order.processQuantities();

        assertTrue(order.checkAvailableQuantities());
    }

    @Test
    public void testSoftwareConfigurationOnlyBrowser() {
        order = new Orders("003", "Case3", "Motherboard3", "Memory3", 2,
                new ArrayList<>(Arrays.asList("HDD3")), "Monitor3", 1, "Keyboard3", "Safari",
                null, new ArrayList<>());
        order.quantityTable = new ArrayList<>(Arrays.asList(1, 1, 2, 1, 1, 1, 1));
        order.processQuantities();

        assertTrue(order.checkAvailableQuantities());
    }

    @Test
    public void testSoftwareConfigurationFull() {
        order = new Orders("004", "Case4", "Motherboard4", "Memory4", 2,
                new ArrayList<>(Arrays.asList("HDD4")), "Monitor4", 1, "Keyboard4", "Opera",
                "LibreOffice", new ArrayList<>(Arrays.asList("Eclipse", "IntelliJ", "VSCode")));

        order.quantityTable = new ArrayList<>(Arrays.asList(1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1));
        order.processQuantities();

        assertTrue(order.checkAvailableQuantities());
    }

    @Test
    public void testTwoIdenticalHardDrives() {
        order = new Orders("005", "Case5", "Motherboard5", "Memory5", 1,
                new ArrayList<>(Arrays.asList("HDD1", "HDD1")), "Monitor5", 1, "Keyboard5", "Edge",
                null, new ArrayList<>());
        order.quantityTable = new ArrayList<>(Arrays.asList(1, 1, 1, 1, 1, 1, 2, 1));
        order.processQuantities();

        assertTrue(order.checkAvailableQuantities());
    }

    @Test
    public void testTwoDifferentHardDrives() {
        order = new Orders("006", "Case6", "Motherboard6", "Memory6", 1,
                new ArrayList<>(Arrays.asList("HDD1", "HDD2")), "Monitor6", 1, "Keyboard6", "Brave",
                null, new ArrayList<>());
        order.quantityTable = new ArrayList<>(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 1));
        order.processQuantities();

        assertTrue(order.checkAvailableQuantities());
    }

    @Test
    public void testInsufficientDevelopmentToolsQuantity() {
        order = new Orders("007", "Case7", "Motherboard7", "Memory7", 1,
                new ArrayList<>(Arrays.asList("HDD1")), "Monitor7", 1, "Keyboard7", "Chrome",
                null, new ArrayList<>(Arrays.asList("Eclipse", "IntelliJ", "VSCode")));
        order.quantityTable = new ArrayList<>(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1));
        order.processQuantities();

        assertFalse(order.checkAvailableQuantities());
    }

}


