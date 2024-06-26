package com.pharmacyPOS.presentation.controllers;

import com.pharmacyPOS.data.dao.InventoryDao;
import com.pharmacyPOS.data.database.DatabaseConnection;
import com.pharmacyPOS.data.entities.Inventory;
import com.pharmacyPOS.data.entities.Product;
import com.pharmacyPOS.service.InventoryService;

import java.sql.SQLException;
import java.util.List;

public class InventoryController {

    private InventoryDao inventoryDao;
    private InventoryService inventoryService;

    public InventoryController(InventoryDao inventoryDao, InventoryService inventoryService) {
        this.inventoryDao = inventoryDao;
        this.inventoryService = inventoryService;
    }

    public InventoryController(InventoryService inventoryService)
    {
        this.inventoryService = inventoryService;
        DatabaseConnection conn = new DatabaseConnection();
        conn.connect();
        this.inventoryDao = new InventoryDao(conn);
    }

    public List<Inventory> getInventoryList() {
        try {
            return inventoryDao.getAllInventory();
        } catch (Exception e) {
            // Log error
            e.printStackTrace();
            return null;
        }
    }

    public Inventory getInventoryItem(int productId) {
        try {
            return inventoryDao.getInventoryByProductId(productId);
        } catch (Exception e) {
            // Log error
            e.printStackTrace();
            return null;
        }
    }

    public Inventory getInventoryById(int inventoryId) {
        return inventoryService.getInventoryById(inventoryId);
    }

    public boolean addInventoryItem(Inventory inventory) {
        try {
            inventoryDao.createInventoryItem(inventory);
            return true;
        } catch (Exception e) {
            // Log error
            e.printStackTrace();
            return false;
        }
    }

    public void updateInventoryItemWithProductInfo(Inventory inventory, Product product)
    {
        inventoryService.updateInventoryItemWithProductInfo(inventory,product);
    }

    public void replenishInventory()
    {
        inventoryService.replenishInventory();
    }

    public boolean updateInventoryItem(Inventory inventory) {
        try {
            inventoryDao.updateInventoryItem(inventory);
            return true;
        } catch (Exception e) {
            // Log error
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteInventoryItem(int productId) {
        try {
            inventoryDao.deleteInventoryItem(productId);
            return true;
        } catch (Exception e) {
            // Log error
            e.printStackTrace();
            return false;
        }
    }

    public void checkAndAlertLowStock() {
        List<Inventory> allInventoryItems = getInventoryList();
        if (allInventoryItems == null) {
            // Handle error scenario, possibly with logging and user notification.
            return;
        }
        for (Inventory item : allInventoryItems) {
            if (inventoryService.isStockLow(item)) {
                // Trigger low stock alert, which could be an email notification, etc.
                // This assumes inventoryService has a method isStockLow to check stock levels.
                alertLowStock(item);
            }
        }
    }

    private void alertLowStock(Inventory item) {
        // Implementation of alert, could be an email, a log, or a UI notification.
        System.out.println("Low stock alert for product ID: " + item.getProductId());
        // You would insert more robust notification logic here.
    }

    public String getProductNameById(int productId)
    {
        return (inventoryService.getProductNameById(productId));
    }

    public Inventory getInventoryByProductId(int productId) {
        return inventoryService.getInventoryByProductId(productId);
    }

    public int getQuantityByProductId(int productId) {
        // Call the inventoryService to retrieve the quantity by product ID
        return inventoryService.getQuantityByProductId(productId);
    }

    public void decrementQuantityByProductId(int productId) throws SQLException {
        inventoryService.decrementQuantityByProductId(productId);
    }

    public void incrementInventory(int productId, int amount) {
        try {
            // Get the current inventory quantity
            int currentQuantity = inventoryDao.getQuantityByProductId(productId);
            if (currentQuantity >= 0) {
                // Calculate the new quantity after incrementing
                int newQuantity = currentQuantity + amount;
                // Update the inventory with the new quantity
                inventoryDao.updateInventoryQuantity(productId, newQuantity);
            } else {
                // Handle the case where the product is not found in the inventory
                System.out.println("Product not found in inventory.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateInventoryQuantity(int productId, int newQuantity)
    {
        inventoryDao.updateInventoryQuantity(productId,newQuantity);
    }

    public int getCurrentInventoryQuantity(int productId)
    {
        return (inventoryService.getCurrentInventoryQuantity(productId));
    }
}
