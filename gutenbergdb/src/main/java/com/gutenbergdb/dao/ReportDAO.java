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

    // -----------------------------------------------------------------------
    // Number and total price of copies of each publication bought per week
    // -----------------------------------------------------------------------
    public void reportPerWeek() throws SQLException {
        String sql =
            "SELECT p.PubID, p.Title AS publication_title," +
            "       YEARWEEK(o.date_ordered, 1) AS year_week," +
            "       SUM(copies) AS total_copies, SUM(total_price) AS total_price" +
            " FROM Orders o" +
            " JOIN (" +
            "   SELECT OID, PubID, number_of_copies AS copies, number_of_copies * unit_price AS total_price" +
            "   FROM Orders_books" +
            "   UNION ALL" +
            "   SELECT oi.OID, mo.PubID, oi.number_of_copies, oi.number_of_copies * oi.unit_price" +
            "   FROM Orders_issues oi JOIN Made_of mo ON oi.IID = mo.IID" +
            " ) combined ON o.OID = combined.OID" +
            " JOIN Publications p ON combined.PubID = p.PubID" +
            " GROUP BY p.PubID, p.Title, YEARWEEK(o.date_ordered, 1)" +
            " ORDER BY year_week, p.PubID";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Publications Sold Per Week =====");
            System.out.printf("%-6s %-25s %-10s %-12s %-10s%n",
                "PubID", "Title", "Year-Week", "Copies", "Total Price");
            System.out.println("-".repeat(70));

            while (rs.next()) {
                System.out.printf("%-6d %-25s %-10s %-12d $%-10.2f%n",
                    rs.getInt("PubID"),
                    rs.getString("publication_title"),
                    rs.getString("year_week"),
                    rs.getInt("total_copies"),
                    rs.getDouble("total_price"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Number and total price of copies of each publication bought per month
    // -----------------------------------------------------------------------
    public void reportPerMonth() throws SQLException {
        String sql =
            "SELECT p.PubID, p.Title AS publication_title," +
            "       YEAR(o.date_ordered) AS year, MONTH(o.date_ordered) AS month," +
            "       SUM(copies) AS total_copies, SUM(total_price) AS total_price" +
            " FROM Orders o" +
            " JOIN (" +
            "   SELECT OID, PubID, number_of_copies AS copies, number_of_copies * unit_price AS total_price" +
            "   FROM Orders_books" +
            "   UNION ALL" +
            "   SELECT oi.OID, mo.PubID, oi.number_of_copies, oi.number_of_copies * oi.unit_price" +
            "   FROM Orders_issues oi JOIN Made_of mo ON oi.IID = mo.IID" +
            " ) combined ON o.OID = combined.OID" +
            " JOIN Publications p ON combined.PubID = p.PubID" +
            " GROUP BY p.PubID, p.Title, YEAR(o.date_ordered), MONTH(o.date_ordered)" +
            " ORDER BY year, month, p.PubID";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Publications Sold Per Month =====");
            System.out.printf("%-6s %-25s %-6s %-6s %-12s %-10s%n",
                "PubID", "Title", "Year", "Month", "Copies", "Total Price");
            System.out.println("-".repeat(72));

            while (rs.next()) {
                System.out.printf("%-6d %-25s %-6d %-6d %-12d $%-10.2f%n",
                    rs.getInt("PubID"),
                    rs.getString("publication_title"),
                    rs.getInt("year"),
                    rs.getInt("month"),
                    rs.getInt("total_copies"),
                    rs.getDouble("total_price"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Weekly revenue and expenses (shipping + salaries)
    // -----------------------------------------------------------------------
    public void weeklyRevenueExpenses() throws SQLException {
        String sql =
            "SELECT YEARWEEK(period, 1) AS year_week," +
            "       SUM(revenue) AS total_revenue," +
            "       SUM(shipping) AS total_shipping," +
            "       SUM(salaries) AS total_salaries," +
            "       SUM(shipping) + SUM(salaries) AS total_expenses" +
            " FROM (" +
            "   SELECT o.date_ordered AS period, ob.number_of_copies * ob.unit_price AS revenue, 0 AS shipping, 0 AS salaries" +
            "   FROM Orders o JOIN Orders_books ob ON o.OID = ob.OID" +
            "   UNION ALL" +
            "   SELECT o.date_ordered, oi.number_of_copies * oi.unit_price, 0, 0" +
            "   FROM Orders o JOIN Orders_issues oi ON o.OID = oi.OID" +
            "   UNION ALL" +
            "   SELECT o.date_ordered, 0, o.shipping_fee, 0" +
            "   FROM Orders o" +
            "   UNION ALL" +
            "   SELECT wp.pay_issue_date, 0, 0, wp.amount" +
            "   FROM Worker_Payments wp" +
            " ) combined" +
            " GROUP BY YEARWEEK(period, 1)" +
            " ORDER BY year_week";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Weekly Revenue & Expenses =====");
            System.out.printf("%-10s %-14s %-14s %-14s %-14s%n",
                "Year-Week", "Revenue", "Shipping", "Salaries", "Total Expenses");
            System.out.println("-".repeat(70));

            while (rs.next()) {
                System.out.printf("%-10s $%-13.2f $%-13.2f $%-13.2f $%-13.2f%n",
                    rs.getString("year_week"),
                    rs.getDouble("total_revenue"),
                    rs.getDouble("total_shipping"),
                    rs.getDouble("total_salaries"),
                    rs.getDouble("total_expenses"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Monthly revenue and expenses (shipping + salaries)
    // -----------------------------------------------------------------------
    public void monthlyRevenueExpenses() throws SQLException {
        String sql =
            "SELECT YEAR(period) AS year, MONTH(period) AS month," +
            "       SUM(revenue) AS total_revenue," +
            "       SUM(shipping) AS total_shipping," +
            "       SUM(salaries) AS total_salaries," +
            "       SUM(shipping) + SUM(salaries) AS total_expenses" +
            " FROM (" +
            "   SELECT o.date_ordered AS period, ob.number_of_copies * ob.unit_price AS revenue, 0 AS shipping, 0 AS salaries" +
            "   FROM Orders o JOIN Orders_books ob ON o.OID = ob.OID" +
            "   UNION ALL" +
            "   SELECT o.date_ordered, oi.number_of_copies * oi.unit_price, 0, 0" +
            "   FROM Orders o JOIN Orders_issues oi ON o.OID = oi.OID" +
            "   UNION ALL" +
            "   SELECT o.date_ordered, 0, o.shipping_fee, 0" +
            "   FROM Orders o" +
            "   UNION ALL" +
            "   SELECT wp.pay_issue_date, 0, 0, wp.amount" +
            "   FROM Worker_Payments wp" +
            " ) combined" +
            " GROUP BY YEAR(period), MONTH(period)" +
            " ORDER BY year, month";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Monthly Revenue & Expenses =====");
            System.out.printf("%-6s %-6s %-14s %-14s %-14s %-14s%n",
                "Year", "Month", "Revenue", "Shipping", "Salaries", "Total Expenses");
            System.out.println("-".repeat(74));

            while (rs.next()) {
                System.out.printf("%-6d %-6d $%-13.2f $%-13.2f $%-13.2f $%-13.2f%n",
                    rs.getInt("year"),
                    rs.getInt("month"),
                    rs.getDouble("total_revenue"),
                    rs.getDouble("total_shipping"),
                    rs.getDouble("total_salaries"),
                    rs.getDouble("total_expenses"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Total current number of distributors
    // -----------------------------------------------------------------------
    public void totalDistributors() throws SQLException {
        String sql = "SELECT COUNT(*) AS total_distributors FROM Distributors";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("\n===== Total Distributors =====");
                System.out.println("Total: " + rs.getInt("total_distributors"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Total revenue since inception per city
    // -----------------------------------------------------------------------
    public void revenuePerCity() throws SQLException {
        String sql =
            "SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(d.addr, ',', 2), ',', -1)) AS city," +
            "       COALESCE(SUM(combined.total_price), 0) AS total_revenue" +
            " FROM Distributors d" +
            " JOIN Places pl ON d.DID = pl.DID" +
            " JOIN Orders o ON pl.OID = o.OID" +
            " JOIN (" +
            "   SELECT OID, number_of_copies * unit_price AS total_price FROM Orders_books" +
            "   UNION ALL" +
            "   SELECT OID, number_of_copies * unit_price AS total_price FROM Orders_issues" +
            " ) combined ON o.OID = combined.OID" +
            " GROUP BY city" +
            " ORDER BY total_revenue DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Total Revenue Per City =====");
            System.out.printf("%-25s %-12s%n", "City", "Total Revenue");
            System.out.println("-".repeat(40));

            while (rs.next()) {
                System.out.printf("%-25s $%-12.2f%n",
                    rs.getString("city"),
                    rs.getDouble("total_revenue"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Total revenue since inception per distributor
    // -----------------------------------------------------------------------
    public void revenuePerDistributor() throws SQLException {
        String sql =
            "SELECT d.DID, d.name AS distributor_name," +
            "       COALESCE(SUM(combined.total_price), 0) AS total_revenue" +
            " FROM Distributors d" +
            " JOIN Places pl ON d.DID = pl.DID" +
            " JOIN Orders o ON pl.OID = o.OID" +
            " JOIN (" +
            "   SELECT OID, number_of_copies * unit_price AS total_price FROM Orders_books" +
            "   UNION ALL" +
            "   SELECT OID, number_of_copies * unit_price AS total_price FROM Orders_issues" +
            " ) combined ON o.OID = combined.OID" +
            " GROUP BY d.DID, d.name" +
            " ORDER BY total_revenue DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Total Revenue Per Distributor =====");
            System.out.printf("%-6s %-25s %-12s%n", "DID", "Distributor", "Total Revenue");
            System.out.println("-".repeat(48));

            while (rs.next()) {
                System.out.printf("%-6d %-25s $%-12.2f%n",
                    rs.getInt("DID"),
                    rs.getString("distributor_name"),
                    rs.getDouble("total_revenue"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Total payments to editors and authors per month
    // -----------------------------------------------------------------------
    public void paymentsPerMonth() throws SQLException {
        String sql =
            "SELECT YEAR(wp.pay_issue_date) AS year, MONTH(wp.pay_issue_date) AS month," +
            "       SUM(wp.amount) AS total_payments" +
            " FROM Worker_Payments wp" +
            " JOIN Get_Paid gp ON wp.PID = gp.PID" +
            " JOIN Workers w ON gp.EID = w.EID" +
            " WHERE w.worker_type IN ('editor', 'writer')" +
            " GROUP BY YEAR(wp.pay_issue_date), MONTH(wp.pay_issue_date)" +
            " ORDER BY year, month";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Total Payments to Editors & Authors Per Month =====");
            System.out.printf("%-6s %-6s %-14s%n", "Year", "Month", "Total Payments");
            System.out.println("-".repeat(30));

            while (rs.next()) {
                System.out.printf("%-6d %-6d $%-13.2f%n",
                    rs.getInt("year"),
                    rs.getInt("month"),
                    rs.getDouble("total_payments"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Total payments to editors and authors per work type
    // -----------------------------------------------------------------------
    public void paymentsPerWorkType() throws SQLException {
        String sql =
            "SELECT work_type, SUM(total_payments) AS total_payments" +
            " FROM (" +
            "   SELECT 'Editorial Work' AS work_type, wp.amount AS total_payments" +
            "   FROM Worker_Payments wp" +
            "   JOIN Get_Paid gp ON wp.PID = gp.PID" +
            "   JOIN Workers w ON gp.EID = w.EID" +
            "   WHERE w.worker_type = 'editor'" +
            "   UNION ALL" +
            "   SELECT 'Book Authorship', wp.amount" +
            "   FROM Worker_Payments wp" +
            "   JOIN Get_Paid gp ON wp.PID = gp.PID" +
            "   JOIN Workers w ON gp.EID = w.EID" +
            "   WHERE w.worker_type = 'writer'" +
            "     AND EXISTS (SELECT 1 FROM Works_on_books wob WHERE wob.EID = w.EID)" +
            "   UNION ALL" +
            "   SELECT 'Article Authorship', wp.amount" +
            "   FROM Worker_Payments wp" +
            "   JOIN Get_Paid gp ON wp.PID = gp.PID" +
            "   JOIN Workers w ON gp.EID = w.EID" +
            "   WHERE w.worker_type = 'writer'" +
            "     AND EXISTS (SELECT 1 FROM Works_on_articles woa WHERE woa.EID = w.EID)" +
            " ) combined" +
            " GROUP BY work_type" +
            " ORDER BY work_type";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Total Payments Per Work Type =====");
            System.out.printf("%-22s %-14s%n", "Work Type", "Total Payments");
            System.out.println("-".repeat(38));

            while (rs.next()) {
                System.out.printf("%-22s $%-13.2f%n",
                    rs.getString("work_type"),
                    rs.getDouble("total_payments"));
            }
        }
    }
}
