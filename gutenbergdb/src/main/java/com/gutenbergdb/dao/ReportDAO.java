package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;
import java.sql.*;

public class ReportDAO {

    public void reportPerDistributor() throws SQLException {
    	String sql = "SELECT\n" +
                "    d.DID,\n" +
                "    d.name AS distributor_name,\n" +
                "    p.PubID,\n" +
                "    p.Title AS publication_title,\n" +
                "    SUM(copies) AS total_copies,\n" +
                "    SUM(total_price) AS total_price\n" +
                "FROM Orders o\n" +
                "JOIN Places pl ON o.OID = pl.OID\n" +
                "JOIN Distributors d ON pl.DID = d.DID\n" +
                "JOIN (\n" +
                "    SELECT OID, PubID, number_of_copies AS copies, number_of_copies * unit_price AS total_price\n" +
                "    FROM Orders_books\n" +
                "    UNION ALL\n" +
                "    SELECT oi.OID, mo.PubID, oi.number_of_copies, oi.number_of_copies * oi.unit_price\n" +
                "    FROM Orders_issues oi\n" +
                "    JOIN Made_of mo ON oi.IID = mo.IID\n" +
                ") combined ON o.OID = combined.OID\n" +
                "JOIN Publications p ON combined.PubID = p.PubID\n" +
                "GROUP BY d.DID, d.name, p.PubID, p.Title\n" +
                "ORDER BY d.DID, p.PubID";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Publications Per Distributor =====");
            System.out.printf("%-6s %-20s %-6s %-25s %-12s %-10s%n",
                "DID", "Distributor", "PubID", "Title", "Copies", "Total Price");
            System.out.println("-".repeat(85));

            while (rs.next()) {
                System.out.printf("%-6d %-20s %-6d %-25s %-12d $%-10.2f%n",
                    rs.getInt("DID"),
                    rs.getString("distributor_name"),
                    rs.getInt("PubID"),
                    rs.getString("publication_title"),
                    rs.getInt("total_copies"),
                    rs.getDouble("total_price")
                );
            }
        }
    }
}