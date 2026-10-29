package com.kiet.inventorymanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class InventoryManagement {

    static final String URL = "jdbc:mysql://localhost:3306/retail_db";
    static final String USER = "root";
    static final String PASSWORD = "YOUR_PASSWORD";

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try (Connection con =
                     DriverManager.getConnection(URL, USER, PASSWORD)) {

            System.out.println("Connected to MySQL successfully!");

            int choice;

            do {
                System.out.println("\n===== INVENTORY MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert 3 Products");
                System.out.println("2. Search Products by Category");
                System.out.println("3. Add Stock");
                System.out.println("4. Update Product Price");
                System.out.println("5. Calculate Total Inventory Value");
                System.out.println("6. Delete Product (Only if Quantity is Zero)");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");

                choice = sc.nextInt();

                switch (choice) {

                    case 1:
                        insertProducts(con);
                        break;

                    case 2:
                        searchProductsByCategory(con);
                        break;

                    case 3:
                        System.out.print("Enter product ID: ");
                        int stockId = sc.nextInt();

                        System.out.print("Enter additional stock: ");
                        int stock = sc.nextInt();

                        addStock(con, stockId, stock);
                        break;

                    case 4:
                        System.out.print("Enter product ID: ");
                        int priceId = sc.nextInt();

                        System.out.print("Enter new price: ");
                        double price = sc.nextDouble();

                        updatePrice(con, priceId, price);
                        break;

                    case 5:
                        calculateInventoryValue(con);
                        break;

                    case 6:
                        System.out.print("Enter product ID to delete: ");
                        int deleteId = sc.nextInt();

                        deleteProduct(con, deleteId);
                        break;

                    case 0:
                        System.out.println("Exiting program...");
                        break;

                    default:
                        System.out.println("Invalid choice!");
                }

            } while (choice != 0);

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (java.util.InputMismatchException e) {
            System.out.println("Invalid input! Please enter the correct data type.");
        } finally {
            sc.close();
        }
    }

    // 1. Insert three products using PreparedStatement
    static void insertProducts(Connection con) throws SQLException {

        String sql = "INSERT INTO Product " +
                "(product_id, product_name, category, quantity, price) " +
                "VALUES (?, ?, ?, ?, ?)";

        int[] ids = {101, 102, 103};
        String[] names = {"Laptop", "Mouse", "Chair"};
        String[] categories = {"Electronics", "Electronics", "Furniture"};
        int[] quantities = {15, 10, 25};
        double[] prices = {55000.0, 500.0, 2500.0};

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 0; i < ids.length; i++) {

                try {
                    ps.setInt(1, ids[i]);
                    ps.setString(2, names[i]);
                    ps.setString(3, categories[i]);
                    ps.setInt(4, quantities[i]);
                    ps.setDouble(5, prices[i]);

                    ps.executeUpdate();

                    System.out.println(names[i] + " inserted successfully.");

                } catch (SQLException e) {

                    if (e.getErrorCode() == 1062) {
                        System.out.println(
                                "Product ID " + ids[i] +
                                        " already exists. Skipping."
                        );
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    // 2. Search category products where quantity < 20, price DESC
    static void searchProductsByCategory(Connection con)
            throws SQLException {

        System.out.print("Enter category: ");
        String category = sc.next();

        String sql = "SELECT * FROM Product " +
                "WHERE category = ? AND quantity < 20 " +
                "ORDER BY price DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {

                boolean found = false;

                System.out.println("\n--- Matching Products ---");

                while (rs.next()) {
                    found = true;

                    System.out.println("ID: " +
                            rs.getInt("product_id"));

                    System.out.println("Name: " +
                            rs.getString("product_name"));

                    System.out.println("Category: " +
                            rs.getString("category"));

                    System.out.println("Quantity: " +
                            rs.getInt("quantity"));

                    System.out.println("Price: " +
                            rs.getDouble("price"));

                    System.out.println("-------------------------");
                }

                if (!found) {
                    System.out.println("No matching products found.");
                }
            }
        }
    }

    // 3. Add stock to an existing product
    static void addStock(Connection con, int productId, int additionalStock)
            throws SQLException {

        if (additionalStock <= 0) {
            System.out.println("Additional stock must be greater than zero.");
            return;
        }

        String sql = "UPDATE Product " +
                "SET quantity = quantity + ? WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, additionalStock);
            ps.setInt(2, productId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Stock added successfully!");
            } else {
                System.out.println("Product not found.");
            }
        }
    }

    // 4. Update price only if product exists and new price > 0
    static void updatePrice(Connection con, int productId, double newPrice)
            throws SQLException {

        if (newPrice <= 0) {
            System.out.println("Price must be greater than zero.");
            return;
        }

        String sql = "UPDATE Product SET price = ? WHERE product_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, newPrice);
            ps.setInt(2, productId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product price updated successfully!");
            } else {
                System.out.println("Product not found.");
            }
        }
    }

    // 5. Calculate total inventory value
    static void calculateInventoryValue(Connection con)
            throws SQLException {

        String sql = "SELECT SUM(quantity * price) AS total_value FROM Product";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double totalValue = rs.getDouble("total_value");

                if (rs.wasNull()) {
                    totalValue = 0.0;
                }

                System.out.printf(
                        "Total Inventory Value: %.2f%n", totalValue
                );
            }
        }
    }

    // 6. Delete a product only if its quantity is zero
    static void deleteProduct(Connection con, int productId)
            throws SQLException {

        String sql = "DELETE FROM Product " +
                "WHERE product_id = ? AND quantity = 0";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productId);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println(
                        "Product not deleted. It may not exist or its quantity is not zero."
                );
            }
        }
    }
}