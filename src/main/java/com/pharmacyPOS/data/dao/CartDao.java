package com.pharmacyPOS.data.dao;

import com.mysql.cj.Session;
import com.pharmacyPOS.data.database.DatabaseConnection;
import com.pharmacyPOS.data.entities.Cart;
import com.pharmacyPOS.data.entities.CartItem;
import com.pharmacyPOS.data.entities.Inventory;
import com.pharmacyPOS.data.entities.SaleItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDao {

    private DatabaseConnection dbConnection;

    public CartDao(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public int createCart(Cart cart) throws SQLException {
        String sql = "INSERT INTO carts (user_id) VALUES (?)";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, cart.getUserId());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating cart failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating cart failed, no ID obtained.");
                }
            }
        }
    }

    public Cart getCartById(int cartId) throws SQLException {
        String sql = "SELECT * FROM cart_items WHERE cart_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartId);
            ResultSet rs = pstmt.executeQuery();

            Cart cart = new Cart();
            cart.setCartId(cartId);
            while (rs.next()) {
                SaleItem item = new SaleItem(
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price_at_time_of_addition")
                );
                cart.addItem(item);
            }
            return cart;
        }
    }

    // Helper method to remove an item from a cart
    public void removeItemFromCart(int cartId, int productId) throws SQLException {
        String sql = "SELECT quantity FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartId);
            pstmt.setInt(2, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int currentQuantity = rs.getInt("quantity");

                    if (currentQuantity > 1) {
                        // If quantity is more than 1, decrement the quantity
                        updateCartItemQuantity(cartId, productId, currentQuantity - 1);
                    } else {
                        // If quantity is 1, remove the item from the cart
                        String deleteSql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";
                        try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
                            deletePstmt.setInt(1, cartId);
                            deletePstmt.setInt(2, productId);
                            deletePstmt.executeUpdate();
                        }
                    }
                }
            }
        }
    }


    public void updateCartItemQuantity(int cartId, int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, cartId);
            pstmt.setInt(3, productId);

            pstmt.executeUpdate();
        }
    }
    // Delete a Cart by ID
    public void deleteCart(int cartId) throws SQLException {
        String sql = "DELETE FROM carts WHERE cart_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartId);
            pstmt.executeUpdate();
        }
    }

    public int getCartItemQuantity(int cartId, int productId) throws SQLException {
        String sql = "SELECT quantity FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartId);
            pstmt.setInt(2, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                } else {
                    return 0; // Return 0 if the item is not found in the cart
                }
            }
        }
    }

    // Helper method to add an item to a cart
    public void addItemToCart(int cartId, SaleItem item) throws SQLException {
        String sql = "INSERT INTO cart_items (cart_id, product_id, quantity, price_at_time_of_addition) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartId);
            pstmt.setInt(2, item.getProductId());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setDouble(4, item.getPrice());
            pstmt.executeUpdate();
        }
    }

    // Helper method to remove an item from a cart
    /*public void removeItemFromCart(int cartId, int productId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartId);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
        }
    }

     */

    // Check if a cart exists
    private boolean doesCartExist(int cartId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM carts WHERE cart_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public List<CartItem> getCartItems(int cartId) throws SQLException
    {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT ci.product_id, p.name, ci.quantity, ci.price_at_time_of_addition " +
                "FROM products p " +
                "JOIN cart_items ci ON p.product_id = ci.product_id " +
                "JOIN carts c ON ci.cart_id = c.cart_id " +
                "JOIN users u ON c.user_id = u.user_id " +
                "WHERE u.user_id = ?";


        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cartId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    String productName = rs.getString("name");
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price_at_time_of_addition");

                    CartItem item = new CartItem(productId, productName, quantity, price);
                    items.add(item);
                }
            }
        }
        return items;
    }

    public Cart getCurrentCart(int userId) throws SQLException {
        String sql = "SELECT * FROM carts WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int cartId = rs.getInt("cart_id");
                return getCartById(cartId);
            } else {
                // If no current cart exists for the user, create a new one
                Cart newCart = new Cart(userId);
                int newCartId = createCart(newCart);
                newCart.setCartId(newCartId);
                return newCart;
            }
        }
    }
    // ... other methods ...

    /**
     * Removes all items from the specified cart.
     * @param cartId The ID of the cart to be cleared.
     * @throws SQLException If a database access error occurs.
     */
    public void clearCartItems(int cartId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartId);
            pstmt.executeUpdate();
        }
    }
}
