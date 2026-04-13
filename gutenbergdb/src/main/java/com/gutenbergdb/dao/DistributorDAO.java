package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;
import java.sql.*;

public class DistributorDAO {
    public void enterNewDistributor(int iDID, String iname, String iphone_number, String icategory, float ioutstanding_balance, String iaddr, String icontact) throws SQLException {
        String sql = "INSERT INTO Distributors (DID, name, phone_number, category, outstanding_balance, addr, contact) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, iDID);
            pstmt.setString(2, iname);
            pstmt.setString(3, iphone_number);
            pstmt.setString(4, icategory);
            pstmt.setFloat(5, ioutstanding_balance);
            pstmt.setString(6, iaddr);
            pstmt.setString(7, icontact);
            pstmt.executeUpdate();
        }
    }

    public void updateDistributorInfo(int iDID, String iphone_number, String iaddr, String icontact) throws SQLException {
        String sql = "UPDATE Distributors SET phone_number = ?, addr = ?, contact = ? WHERE DID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, iphone_number);
            pstmt.setString(2, iaddr);
            pstmt.setString(3, icontact);
            pstmt.setInt(4, iDID);
            pstmt.executeUpdate();
        }
    }

    public void deleteDistributor(int iDID) throws SQLException {
        String sql = "DELETE FROM Distributors WHERE DID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, iDID);
            pstmt.executeUpdate();
        }
    }

    public void inputOrder(int iOID, int iDID, int iPubID, String idate_ordered, float ishipping_fee, String idate_due, float iunit_price, int inumber_of_copies) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql1 = "INSERT INTO Orders (OID, date_ordered, shipping_fee, date_due, is_produced) VALUES (?, ?, ?, ?, FALSE)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
                    pstmt.setInt(1, iOID);
                    pstmt.setString(2, idate_ordered);
                    pstmt.setFloat(3, ishipping_fee);
                    pstmt.setString(4, idate_due);
                    pstmt.executeUpdate();
                }
                String sql2 = "INSERT INTO Places (OID, DID) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                    pstmt.setInt(1, iOID);
                    pstmt.setInt(2, iDID);
                    pstmt.executeUpdate();
                }
                String sql3 = "INSERT INTO Orders_books (OID, PubID, unit_price, number_of_copies) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql3)) {
                    pstmt.setInt(1, iOID);
                    pstmt.setInt(2, iPubID);
                    pstmt.setFloat(3, iunit_price);
                    pstmt.setInt(4, inumber_of_copies);
                    pstmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void inputMultipleOrders(int numOfOrders) throws SQLException {
        // This requires actual order data for each iteration. 
        // Placeholder to fix syntax error:
        for (int i = 0; i < numOfOrders; i++) {
            // inputOrder(...) call logic would go here
        }
    }

    public void billDistributor(int iDBID, int iDID, float ipayment_amount, String ipayment_date) throws SQLException {
        updateBalance(iDBID, iDID, ipayment_amount, ipayment_date, true);
    }

    public void changeDistributorBalance(int iDBID, int iDID, float ipayment_amount, String ipayment_date) throws SQLException {
        updateBalance(iDBID, iDID, ipayment_amount, ipayment_date, false);
    }

    private void updateBalance(int iDBID, int iDID, float ipayment_amount, String ipayment_date, boolean isAddition) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql1 = "INSERT INTO Distributor_payments (DBID, payment_date, payment_amount) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
                    pstmt.setInt(1, iDBID);
                    pstmt.setString(2, ipayment_date);
                    pstmt.setFloat(3, ipayment_amount);
                    pstmt.executeUpdate();
                }
                String sql2 = "INSERT INTO Make (DBID, DID) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                    pstmt.setInt(1, iDBID);
                    pstmt.setInt(2, iDID);
                    pstmt.executeUpdate();
                }
                String op = isAddition ? "+" : "-";
                String sql3 = "UPDATE Distributors SET outstanding_balance = outstanding_balance " + op + " ? WHERE DID = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql3)) {
                    pstmt.setFloat(1, ipayment_amount);
                    pstmt.setInt(2, iDID);
                    pstmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public String identifyNonMatchingDistributorBalances() throws SQLException {
        String sql = "SELECT d.DID, d.name, d.outstanding_balance, COALESCE(SUM(dp.payment_amount), 0) AS total_payments " +
                     "FROM Distributors d " +
                     "LEFT JOIN Make m ON d.DID = m.DID " +
                     "LEFT JOIN Distributor_payments dp ON m.DBID = dp.DBID " +
                     "GROUP BY d.DID, d.name, d.outstanding_balance " +
                     "HAVING d.outstanding_balance != COALESCE(SUM(dp.payment_amount), 0)";

        StringBuilder output = new StringBuilder();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            output.append(String.format("%-10s %-20s %-20s %-20s\n", "DID", "Name", "Balance", "Total Payments"));
            output.append("-".repeat(70)).append("\n");
            while (rs.next()) {
                output.append(String.format("%-10d %-20s %-20.2f %-20.2f\n",
                        rs.getInt("DID"),
                        rs.getString("name"),
                        rs.getFloat("outstanding_balance"),
                        rs.getFloat("total_payments")));
            }
        }
        return output.toString();
    }

    public String identifyDistributorInLocation(String location, String type) throws SQLException {
        String sql = "SELECT DID, name, phone_number, category, outstanding_balance, addr, contact " +
                     "FROM Distributors WHERE addr LIKE ? AND category = ?";
        
        StringBuilder output = new StringBuilder();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + location + "%");
            pstmt.setString(2, type);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                output.append(String.format("%-5s %-15s %-15s %-10s %-10s %-20s\n", "DID", "Name", "Phone", "Cat", "Bal", "Addr"));
                output.append("-".repeat(80)).append("\n");
                while (rs.next()) {
                    output.append(String.format("%-5d %-15s %-15s %-10s %-10.2f %-20s\n",
                            rs.getInt("DID"),
                            rs.getString("name"),
                            rs.getString("phone_number"),
                            rs.getString("category"),
                            rs.getFloat("outstanding_balance"),
                            rs.getString("addr")));
                }
            }
        }
        return output.toString();
    }
}
