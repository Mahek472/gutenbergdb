package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DistributorDAO {

    // -------------------------------------------------------------------------
    // Helper: generate next DPID in DP### format
    // -------------------------------------------------------------------------
    private String generateNextDPID() throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(DPID, 3) AS UNSIGNED)) AS max_num FROM Distributor_payments WHERE DPID LIKE 'DP%'";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                Object maxNum = rs.getObject("max_num");
                int nextNum = (maxNum == null) ? 1 : ((Number) maxNum).intValue() + 1;
                return String.format("DP%03d", nextNum);
            }
            return "DP001";
        }
    }

    // -------------------------------------------------------------------------
    // Helper: generate next OID in O### format
    // -------------------------------------------------------------------------
    private String generateNextOID() throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(OID, 2) AS UNSIGNED)) AS max_num FROM Orders WHERE OID LIKE 'O%'";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                Object maxNum = rs.getObject("max_num");
                int nextNum = (maxNum == null) ? 1 : ((Number) maxNum).intValue() + 1;
                return String.format("O%03d", nextNum);
            }
            return "O001";
        }
    }

    // -------------------------------------------------------------------------
    // Helper: open a connection (uses DBConnection to load properties)
    // -------------------------------------------------------------------------
    private Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // -------------------------------------------------------------------------
    // 1. Enter a new distributor
    // -------------------------------------------------------------------------
    public void enterNewDistributor(String did_choice, String iname, String iphone_number,
                                    String icategory,
                                    String iaddr, String icontact) throws SQLException {

        String sql = "INSERT INTO Distributors (DID, name, phone_number, category, " +
                     "outstanding_balance, addr, contact, balance_as_of) " +
                     "VALUES (?, ?, ?, ?, 0.0, ?, ?, CURDATE())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, did_choice);
            stmt.setString(2, iname);
            stmt.setString(3, iphone_number);
            stmt.setString(4, icategory);
            stmt.setString(5, iaddr);
            stmt.setString(6, icontact);
            stmt.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // 2. Update distributor info
    // -------------------------------------------------------------------------
    public void updateDistributorInfo(String did_choice, String iname, String iphone_number,
                                      String icategory, float ioutstanding_balance,
                                      String iaddr, String icontact) throws SQLException {

        // Debug output
        System.out.println("DEBUG: Updating distributor with DID=" + did_choice + 
                          ", name=" + iname +
                          ", phone=" + iphone_number +
                          ", category=" + icategory +
                          ", balance=" + ioutstanding_balance +
                          ", addr=" + iaddr +
                          ", contact=" + icontact);

        String sql = "UPDATE Distributors " +
                     "SET name = ?, phone_number = ?, category = ?, " +
                     "outstanding_balance = ?, addr = ?, contact = ? " +
                     "WHERE DID = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, iname);
            stmt.setString(2, iphone_number);
            stmt.setString(3, icategory);
            stmt.setFloat(4, ioutstanding_balance);
            stmt.setString(5, iaddr);
            stmt.setString(6, icontact);
            stmt.setString(7, did_choice);

            System.out.println("DEBUG: About to execute UPDATE...");
            int rows = stmt.executeUpdate();
            System.out.println("DEBUG: Update completed, rows affected: " + rows);
        } catch (SQLException e) {
            System.out.println("DEBUG: SQLException occurred: " + e.getMessage());
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // 3. Delete a distributor
    // -------------------------------------------------------------------------
    public void deleteDistributor(String did_choice) throws SQLException {

        String sql = "DELETE FROM Distributors WHERE DID = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, did_choice);
            stmt.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // 4. Input a single order  (multi-statement transaction)
    //    OID is generated in O### format to match existing data.
    //    For books: use iIdentifier as ISBN; for issues: use iIdentifier as IID
    // -------------------------------------------------------------------------
    public void inputOrder(String iDID, String iIdentifier, String idate_ordered,
                           float ishipping_fee, float iunit_price, int inumber_of_copies, 
                           boolean is_book) throws SQLException {

        // Generate next OID in O### format
        String generatedOID = generateNextOID();

        String sqlOrder = "INSERT INTO Orders (OID, date_ordered, shipping_fee, DID) " +
                          "VALUES (?, ?, ?, ?)";
        String sqlOrderStuff;
        if (is_book) {
            sqlOrderStuff = "INSERT INTO Orders_books (OID, ISBN, unit_price, number_of_copies) " +
                            "VALUES (?, ?, ?, ?)";
        }
        else {
            sqlOrderStuff = "INSERT INTO Orders_issues (OID, IID, unit_price, number_of_copies) " +
                            "VALUES (?, ?, ?, ?)";
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // begin transaction
            try {
                // Insert into Orders
                try (PreparedStatement stmt = conn.prepareStatement(sqlOrder)) {
                    stmt.setString(1, generatedOID);
                    stmt.setString(2, idate_ordered);
                    stmt.setFloat(3, ishipping_fee);
                    stmt.setString(4, iDID);
                    stmt.executeUpdate();
                }

                // Insert into Orders_books or Orders_issues
                try (PreparedStatement stmt = conn.prepareStatement(sqlOrderStuff)) {
                    stmt.setString(1, generatedOID);
                    stmt.setString(2, iIdentifier);  // ISBN or IID
                    stmt.setFloat(3, iunit_price);
                    stmt.setInt(4, inumber_of_copies);
                    stmt.executeUpdate();
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }


    // -------------------------------------------------------------------------
    // 6. Bill a distributor  (adds to outstanding_balance)
    //    DPID is generated in DP### format to match existing data.
    //    Distributor_payments now has DID directly (no Make table).
    // -------------------------------------------------------------------------
    public void billDistributor(String did_choice, float ipayment_amount,
                                String payment_date) throws SQLException {

        // Generate next DPID in DP### format
        String generatedDPID = generateNextDPID();

        String sqlPayment = "INSERT INTO Distributor_payments (DPID, payment_amount, payment_date, DID) " +
                            "VALUES (?, ?, ?, ?)";
        String sqlUpdate  = "UPDATE Distributors " +
                            "SET outstanding_balance = outstanding_balance + ? " +
                            "WHERE DID = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlPayment)) {
                    stmt.setString(1, generatedDPID);
                    stmt.setFloat(2, ipayment_amount);
                    stmt.setString(3, payment_date);
                    stmt.setString(4, did_choice);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                    stmt.setFloat(1, ipayment_amount);
                    stmt.setString(2, did_choice);
                    stmt.executeUpdate();
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 7. Change distributor balance  (subtracts from outstanding_balance)
    // -------------------------------------------------------------------------
    public void changeDistributorBalance(String did_choice, float ipayment_amount,
                                         String payment_date) throws SQLException {

        // Generate next DPID in DP### format
        String generatedDPID = generateNextDPID();

        String sqlPayment = "INSERT INTO Distributor_payments (DPID, payment_amount, payment_date, DID) " +
                            "VALUES (?, ?, ?, ?)";
        String sqlUpdate  = "UPDATE Distributors " +
                            "SET outstanding_balance = outstanding_balance - ? " +
                            "WHERE DID = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlPayment)) {
                    stmt.setString(1, generatedDPID);
                    stmt.setFloat(2, ipayment_amount);
                    stmt.setString(3, payment_date);
                    stmt.setString(4, did_choice);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                    stmt.setFloat(1, ipayment_amount);
                    stmt.setString(2, did_choice);
                    stmt.executeUpdate();
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 8. Identify non-matching distributor balances
    // -------------------------------------------------------------------------
    public String identifyNonMatchingDistributorBalances() throws SQLException {

        String sql = "SELECT d.DID, d.name, d.outstanding_balance, " +
                     "COALESCE(SUM(dp.payment_amount), 0) AS total_payments " +
                     "FROM Distributors d " +
                     "LEFT JOIN Distributor_payments dp ON d.DID = dp.DID " +
                     "GROUP BY d.DID, d.name, d.outstanding_balance " +
                     "HAVING d.outstanding_balance != COALESCE(SUM(dp.payment_amount), 0)";

        StringBuilder result = new StringBuilder();
        result.append(String.format("%-10s %-20s %-20s %-20s%n",
                      "DID", "Name", "Outstanding Balance", "Total Payments"));
        result.append("-".repeat(70)).append("\n");

        try (Connection conn        = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs           = stmt.executeQuery()) {

            List<String> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(String.format("%-10s %-20s %-20.2f %-20.2f%n",
                          rs.getString("DID"),
                          rs.getString("name"),
                          rs.getFloat("outstanding_balance"),
                          rs.getFloat("total_payments")));
            }

            if (rows.isEmpty()) {
                result.append("No mismatched distributor balances found.\n");
            } else {
                rows.forEach(result::append);
            }
        }

        return result.toString();
    }

    // -------------------------------------------------------------------------
    // 9. Identify distributors in a location
    // -------------------------------------------------------------------------
    public String identifyDistributorInLocation(String location,
                                                String type) throws SQLException {

        String sql = "SELECT DID, name, phone_number, category, outstanding_balance, addr, contact " +
                     "FROM Distributors " +
                     "WHERE addr LIKE ? AND category = ?";

        StringBuilder result = new StringBuilder();
        result.append(String.format("%-10s %-20s %-15s %-15s %-20s %-30s %-20s%n",
                      "DID", "Name", "Phone", "Category", "Balance", "Address", "Contact"));
        result.append("-".repeat(130)).append("\n");

        try (Connection conn        = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + location + "%");
            stmt.setString(2, type);

            try (ResultSet rs = stmt.executeQuery()) {
                List<String> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(String.format("%-10s %-20s %-15s %-15s %-20.2f %-30s %-20s%n",
                              rs.getString("DID"),
                              rs.getString("name"),
                              rs.getString("phone_number"),
                              rs.getString("category"),
                              rs.getFloat("outstanding_balance"),
                              rs.getString("addr"),
                              rs.getString("contact")));
                }

                if (rows.isEmpty()) {
                    result.append("No distributors found for location '")
                          .append(location).append("' and category '").append(type).append("'.\n");
                } else {
                    rows.forEach(result::append);
                }
            }
        }

        return result.toString();
    }

    public void inputOrder(int did_choice, int pid_choice, String date_ordered, float shipping_fee, String date_due,
            float unit_price, int num_copies, boolean is_book) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'inputOrder'");
    }
}