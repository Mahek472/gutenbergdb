package com.gutenbergdb.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DistributorDAO {

    private static final String URL      = "jdbc:mysql://localhost:3306/your_database";
    private static final String USERNAME = "x";
    private static final String PASSWORD = "y";

    // -------------------------------------------------------------------------
    // Helper: open a connection
    // -------------------------------------------------------------------------
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    // -------------------------------------------------------------------------
    // 1. Enter a new distributor
    // -------------------------------------------------------------------------
    public void enterNewDistributor(int iDID, String iname, String iphone_number,
                                    String icategory, float ioutstanding_balance,
                                    String iaddr, String icontact) throws SQLException {

        String sql = "INSERT INTO Distributors (DID, name, phone_number, category, " +
                     "outstanding_balance, addr, contact) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, iDID);
            stmt.setString(2, iname);
            stmt.setString(3, iphone_number);
            stmt.setString(4, icategory);
            stmt.setFloat(5, ioutstanding_balance);
            stmt.setString(6, iaddr);
            stmt.setString(7, icontact);
            stmt.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // 2. Update distributor info
    // -------------------------------------------------------------------------
    public void updateDistributorInfo(int iDID, String iname, String iphone_number,
                                      String icategory, float ioutstanding_balance,
                                      String iaddr, String icontact) throws SQLException {

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
            stmt.setInt(7, iDID);
            stmt.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // 3. Delete a distributor
    // -------------------------------------------------------------------------
    public void deleteDistributor(int iDID) throws SQLException {

        String sql = "DELETE FROM Distributors WHERE DID = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, iDID);
            stmt.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // 4. Input a single order  (multi-statement transaction)
    //    NOTE: iOID is auto-generated here via LAST_INSERT_ID() to avoid
    //    the undefined iOID variable that was in the original code.
    // -------------------------------------------------------------------------
    public void inputOrder(int iDID, int iPubID, String idate_ordered,
                           float ishipping_fee, String idate_due,
                           float iunit_price, int inumber_of_copies) throws SQLException {

        String sqlOrder = "INSERT INTO Orders (date_ordered, shipping_fee, date_due, is_produced) " +
                          "VALUES (?, ?, ?, FALSE)";
        String sqlPlaces = "INSERT INTO Places (OID, DID) VALUES (?, ?)";
        String sqlOrderBooks = "INSERT INTO Orders_books (OID, PubID, unit_price, number_of_copies) " +
                               "VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // begin transaction
            try {
                int generatedOID;

                // Insert into Orders and retrieve the generated OID
                try (PreparedStatement stmt = conn.prepareStatement(sqlOrder,
                                             Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, idate_ordered);
                    stmt.setFloat(2, ishipping_fee);
                    stmt.setString(3, idate_due);
                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Failed to retrieve generated OID.");
                        generatedOID = keys.getInt(1);
                    }
                }

                // Insert into Places
                try (PreparedStatement stmt = conn.prepareStatement(sqlPlaces)) {
                    stmt.setInt(1, generatedOID);
                    stmt.setInt(2, iDID);
                    stmt.executeUpdate();
                }

                // Insert into Orders_books
                try (PreparedStatement stmt = conn.prepareStatement(sqlOrderBooks)) {
                    stmt.setInt(1, generatedOID);
                    stmt.setInt(2, iPubID);
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
    // 5. Input multiple orders
    //    Callers must now supply full order details for each order.
    //    Orders are passed in as a simple inner class / record.
    // -------------------------------------------------------------------------
    public static class OrderRequest {
        public final int    DID;
        public final int    PubID;
        public final String date_ordered;
        public final float  shipping_fee;
        public final String date_due;
        public final float  unit_price;
        public final int    number_of_copies;

        public OrderRequest(int DID, int PubID, String date_ordered, float shipping_fee,
                            String date_due, float unit_price, int number_of_copies) {
            this.DID             = DID;
            this.PubID           = PubID;
            this.date_ordered    = date_ordered;
            this.shipping_fee    = shipping_fee;
            this.date_due        = date_due;
            this.unit_price      = unit_price;
            this.number_of_copies = number_of_copies;
        }
    }

    public void inputMultipleOrders(List<OrderRequest> orders) throws SQLException {
        for (OrderRequest order : orders) {
            inputOrder(order.DID, order.PubID, order.date_ordered,
                       order.shipping_fee, order.date_due,
                       order.unit_price, order.number_of_copies);
        }
    }

    // -------------------------------------------------------------------------
    // 6. Bill a distributor  (adds to outstanding_balance)
    //    iDBID is auto-generated; retrieved via RETURN_GENERATED_KEYS.
    // -------------------------------------------------------------------------
    public void billDistributor(int iDID, float ipayment_amount,
                                String ipayment_date) throws SQLException {

        String sqlPayment = "INSERT INTO Distributor_payments (payment_date, payment_amount) " +
                            "VALUES (?, ?)";
        String sqlMake    = "INSERT INTO Make (DBID, DID) VALUES (?, ?)";
        String sqlUpdate  = "UPDATE Distributors " +
                            "SET outstanding_balance = outstanding_balance + ? " +
                            "WHERE DID = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int generatedDBID;

                try (PreparedStatement stmt = conn.prepareStatement(sqlPayment,
                                             Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, ipayment_date);
                    stmt.setFloat(2, ipayment_amount);
                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Failed to retrieve generated DBID.");
                        generatedDBID = keys.getInt(1);
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlMake)) {
                    stmt.setInt(1, generatedDBID);
                    stmt.setInt(2, iDID);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                    stmt.setFloat(1, ipayment_amount);
                    stmt.setInt(2, iDID);
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
    public void changeDistributorBalance(int iDID, float ipayment_amount,
                                         String ipayment_date) throws SQLException {

        String sqlPayment = "INSERT INTO Distributor_payments (payment_date, payment_amount) " +
                            "VALUES (?, ?)";
        String sqlMake    = "INSERT INTO Make (DBID, DID) VALUES (?, ?)";
        String sqlUpdate  = "UPDATE Distributors " +
                            "SET outstanding_balance = outstanding_balance - ? " +
                            "WHERE DID = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int generatedDBID;

                try (PreparedStatement stmt = conn.prepareStatement(sqlPayment,
                                             Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, ipayment_date);
                    stmt.setFloat(2, ipayment_amount);
                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Failed to retrieve generated DBID.");
                        generatedDBID = keys.getInt(1);
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlMake)) {
                    stmt.setInt(1, generatedDBID);
                    stmt.setInt(2, iDID);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                    stmt.setFloat(1, ipayment_amount);
                    stmt.setInt(2, iDID);
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
                     "LEFT JOIN Make m ON d.DID = m.DID " +
                     "LEFT JOIN Distributor_payments dp ON m.DBID = dp.DBID " +
                     "GROUP BY d.DID, d.name, d.outstanding_balance " +
                     "HAVING d.outstanding_balance != COALESCE(SUM(dp.payment_amount), 0)";

        StringBuilder result = new StringBuilder();
        result.append(String.format("%-6s %-20s %-20s %-20s%n",
                      "DID", "Name", "Outstanding Balance", "Total Payments"));
        result.append("-".repeat(68)).append("\n");

        try (Connection conn        = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs           = stmt.executeQuery()) {

            List<String> rows = new ArrayList<>();
            while (rs.next()) {
                rows.add(String.format("%-6d %-20s %-20.2f %-20.2f%n",
                          rs.getInt("DID"),
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
        result.append(String.format("%-6s %-20s %-15s %-15s %-20s %-30s %-20s%n",
                      "DID", "Name", "Phone", "Category", "Balance", "Address", "Contact"));
        result.append("-".repeat(128)).append("\n");

        try (Connection conn        = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + location + "%"); // LIKE wildcard applied here, not in SQL string
            stmt.setString(2, type);

            try (ResultSet rs = stmt.executeQuery()) {
                List<String> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(String.format("%-6d %-20s %-15s %-15s %-20.2f %-30s %-20s%n",
                              rs.getInt("DID"),
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
}