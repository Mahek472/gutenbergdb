package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;
import java.sql.*;
import java.util.Scanner;

public class ReportDAO {

    private static final Scanner scanner = new Scanner(System.in);

    // =========================================================================
    // EXISTING REPORTS (no user input)
    // =========================================================================

    public void reportPerDistributor() throws SQLException {
        String sql =
            "SELECT d.DID, d.name AS distributor_name, " +
            "       p.PubID, p.Title AS publication_title, " +
            "       SUM(copies) AS total_copies, SUM(total_price) AS total_price " +
            "FROM Orders o " +
            "JOIN Distributors d ON o.DID = d.DID " +
            "JOIN (" +
            "    SELECT ob.OID, b.PubID, ob.number_of_copies AS copies, ob.number_of_copies * ob.unit_price AS total_price " +
            "    FROM Orders_books ob JOIN Books b ON ob.ISBN = b.ISBN " +
            "    UNION ALL " +
            "    SELECT oi.OID, i.PubID, oi.number_of_copies, oi.number_of_copies * oi.unit_price " +
            "    FROM Orders_issues oi JOIN Issues i ON oi.IID = i.IID " +
            ") combined ON o.OID = combined.OID " +
            "JOIN Publications p ON combined.PubID = p.PubID " +
            "GROUP BY d.DID, p.PubID " +
            "ORDER BY d.DID, p.PubID";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Publications Per Distributor =====");
            System.out.printf("%-6s %-20s %-6s %-25s %-12s %-10s%n",
                "DID", "Distributor", "PubID", "Title", "Copies", "Total Price");
            System.out.println("-".repeat(85));

            while (rs.next()) {
                System.out.printf("%-6s %-20s %-6s %-25s %-12d $%-10.2f%n",
                    rs.getString("DID"),
                    rs.getString("distributor_name"),
                    rs.getString("PubID"),
                    rs.getString("publication_title"),
                    rs.getInt("total_copies"),
                    rs.getDouble("total_price"));
            }
        }
    }

    public void reportPerWeek() throws SQLException {
        String sql =
            "SELECT p.PubID, p.Title AS publication_title, " +
            "       YEARWEEK(o.date_ordered, 1) AS year_week, " +
            "       SUM(copies) AS total_copies, SUM(total_price) AS total_price " +
            "FROM Orders o " +
            "JOIN (" +
            "   SELECT ob.OID, b.PubID, ob.number_of_copies AS copies, ob.number_of_copies * ob.unit_price AS total_price " +
            "   FROM Orders_books ob JOIN Books b ON ob.ISBN = b.ISBN " +
            "   UNION ALL " +
            "   SELECT oi.OID, i.PubID, oi.number_of_copies, oi.number_of_copies * oi.unit_price " +
            "   FROM Orders_issues oi JOIN Issues i ON oi.IID = i.IID " +
            ") combined ON o.OID = combined.OID " +
            "JOIN Publications p ON combined.PubID = p.PubID " +
            "GROUP BY p.PubID, YEARWEEK(o.date_ordered, 1) " +
            "ORDER BY year_week, p.PubID";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Publications Sold Per Week =====");
            System.out.printf("%-6s %-25s %-10s %-12s %-10s%n",
                "PubID", "Title", "Year-Week", "Copies", "Total Price");
            System.out.println("-".repeat(70));

            while (rs.next()) {
                System.out.printf("%-6s %-25s %-10s %-12d $%-10.2f%n",
                    rs.getString("PubID"),
                    rs.getString("publication_title"),
                    rs.getString("year_week"),
                    rs.getInt("total_copies"),
                    rs.getDouble("total_price"));
            }
        }
    }

    public void reportPerMonth() throws SQLException {
        String sql =
            "SELECT p.PubID, p.Title AS publication_title, " +
            "       YEAR(o.date_ordered) AS year, MONTH(o.date_ordered) AS month, " +
            "       SUM(copies) AS total_copies, SUM(total_price) AS total_price " +
            "FROM Orders o " +
            "JOIN (" +
            "   SELECT ob.OID, b.PubID, ob.number_of_copies AS copies, ob.number_of_copies * ob.unit_price AS total_price " +
            "   FROM Orders_books ob JOIN Books b ON ob.ISBN = b.ISBN " +
            "   UNION ALL " +
            "   SELECT oi.OID, i.PubID, oi.number_of_copies, oi.number_of_copies * oi.unit_price " +
            "   FROM Orders_issues oi JOIN Issues i ON oi.IID = i.IID " +
            ") combined ON o.OID = combined.OID " +
            "JOIN Publications p ON combined.PubID = p.PubID " +
            "GROUP BY p.PubID, YEAR(o.date_ordered), MONTH(o.date_ordered) " +
            "ORDER BY year, month, p.PubID";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Publications Sold Per Month =====");
            System.out.printf("%-6s %-25s %-6s %-6s %-12s %-10s%n",
                "PubID", "Title", "Year", "Month", "Copies", "Total Price");
            System.out.println("-".repeat(72));

            while (rs.next()) {
                System.out.printf("%-6s %-25s %-6d %-6d %-12d $%-10.2f%n",
                    rs.getString("PubID"),
                    rs.getString("publication_title"),
                    rs.getInt("year"),
                    rs.getInt("month"),
                    rs.getInt("total_copies"),
                    rs.getDouble("total_price"));
            }
        }
    }

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

    public void revenuePerCity() throws SQLException {
        String sql =
            "SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(d.addr, ',', 2), ',', -1)) AS city, " +
            "       COALESCE(SUM(combined.total_price), 0) AS total_revenue " +
            "FROM Distributors d " +
            "JOIN Orders o ON d.DID = o.DID " +
            "JOIN (" +
            "    SELECT OID, number_of_copies * unit_price AS total_price FROM Orders_books " +
            "    UNION ALL " +
            "    SELECT OID, number_of_copies * unit_price AS total_price FROM Orders_issues " +
            ") combined ON o.OID = combined.OID " +
            "GROUP BY city " +
            "ORDER BY total_revenue DESC";

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

    public void revenuePerDistributor() throws SQLException {
        String sql =
            "SELECT d.DID, d.name AS distributor_name, " +
            "       COALESCE(SUM(combined.total_price), 0) AS total_revenue " +
            "FROM Distributors d " +
            "JOIN Orders o ON d.DID = o.DID " +
            "JOIN (" +
            "    SELECT OID, number_of_copies * unit_price AS total_price FROM Orders_books " +
            "    UNION ALL " +
            "    SELECT OID, number_of_copies * unit_price AS total_price FROM Orders_issues " +
            ") combined ON o.OID = combined.OID " +
            "GROUP BY d.DID " +
            "ORDER BY total_revenue DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n===== Total Revenue Per Distributor =====");
            System.out.printf("%-6s %-25s %-12s%n", "DID", "Distributor", "Total Revenue");
            System.out.println("-".repeat(48));

            while (rs.next()) {
                System.out.printf("%-6s %-25s $%-12.2f%n",
                    rs.getString("DID"),
                    rs.getString("distributor_name"),
                    rs.getDouble("total_revenue"));
            }
        }
    }

    public void paymentsPerMonth() throws SQLException {
        String sql =
            "SELECT YEAR(wp.pay_issue_date) AS year, MONTH(wp.pay_issue_date) AS month," +
            "       SUM(wp.amount) AS total_payments" +
            " FROM Worker_Payments wp" +
            " JOIN Workers w ON wp.EID = w.EID" +
            " WHERE w.worker_type IN ('Editor', 'Author')" +
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

    public void paymentsPerWorkType() throws SQLException {
        String sql =
            "SELECT work_type, SUM(total_payments) AS total_payments" +
            " FROM (" +
            "   SELECT 'Editorial Work' AS work_type, wp.amount AS total_payments" +
            "   FROM Worker_Payments wp JOIN Workers w ON wp.EID = w.EID" +
            "   WHERE w.worker_type = 'Editor'" +
            "   UNION ALL" +
            "   SELECT 'Book Authorship', wp.amount" +
            "   FROM Worker_Payments wp JOIN Workers w ON wp.EID = w.EID" +
            "   WHERE w.worker_type = 'Author'" +
            "     AND EXISTS (SELECT 1 FROM Works_on_chapters woc WHERE woc.EID = w.EID)" +
            "   UNION ALL" +
            "   SELECT 'Article Authorship', wp.amount" +
            "   FROM Worker_Payments wp JOIN Workers w ON wp.EID = w.EID" +
            "   WHERE w.worker_type = 'Author'" +
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

    // =========================================================================
    // NEW: USER-INPUT REPORTS — input collected inside each method
    // =========================================================================

    /**
     * Prompts for a DID, then reports copies and total price of each
     * publication bought by that distributor.
     */
    public void reportPerDistributorByInput() throws SQLException {
        System.out.print("\nEnter Distributor ID (e.g. D002): ");
        String did = scanner.nextLine().trim();

        String sql =
            "SELECT d.DID, d.name AS distributor_name, " +
            "       p.PubID, p.Title AS publication_title, " +
            "       SUM(combined.copies) AS total_copies, " +
            "       SUM(combined.total_price) AS total_price " +
            "FROM Orders o " +
            "JOIN Distributors d ON o.DID = d.DID " +
            "JOIN (" +
            "    SELECT ob.OID, b.PubID, ob.number_of_copies AS copies, " +
            "           ob.number_of_copies * ob.unit_price AS total_price " +
            "    FROM Orders_books ob JOIN Books b ON ob.ISBN = b.ISBN " +
            "    UNION ALL " +
            "    SELECT oi.OID, i.PubID, oi.number_of_copies, " +
            "           oi.number_of_copies * oi.unit_price " +
            "    FROM Orders_issues oi JOIN Issues i ON oi.IID = i.IID " +
            ") combined ON o.OID = combined.OID " +
            "JOIN Publications p ON combined.PubID = p.PubID " +
            "WHERE d.DID = ? " +
            "GROUP BY d.DID, p.PubID " +
            "ORDER BY p.PubID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, did);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n===== Publications for Distributor: " + did + " =====");
                System.out.printf("%-6s %-22s %-6s %-30s %-12s %-10s%n",
                    "DID", "Distributor", "PubID", "Title", "Copies", "Total Price");
                System.out.println("-".repeat(92));
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("%-6s %-22s %-6s %-30s %-12d $%-10.2f%n",
                        rs.getString("DID"),
                        rs.getString("distributor_name"),
                        rs.getString("PubID"),
                        rs.getString("publication_title"),
                        rs.getInt("total_copies"),
                        rs.getDouble("total_price"));
                }
                if (!any) System.out.println("No records found for distributor: " + did);
            }
        }
    }

    /**
     * Prompts for a year and ISO week number, then reports copies and total
     * price of each publication ordered that week.
     */
    public void reportPerWeekByInput() throws SQLException {
        System.out.print("\nEnter year (e.g. 2026): ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter ISO week number (e.g. 6): ");
        int isoWeek = Integer.parseInt(scanner.nextLine().trim());
        String yearWeekVal = String.format("%04d%02d", year, isoWeek);

        String sql =
            "SELECT p.PubID, p.Title AS publication_title, " +
            "       YEARWEEK(o.date_ordered, 1) AS year_week, " +
            "       SUM(combined.copies) AS total_copies, " +
            "       SUM(combined.total_price) AS total_price " +
            "FROM Orders o " +
            "JOIN (" +
            "    SELECT ob.OID, b.PubID, ob.number_of_copies AS copies, " +
            "           ob.number_of_copies * ob.unit_price AS total_price " +
            "    FROM Orders_books ob JOIN Books b ON ob.ISBN = b.ISBN " +
            "    UNION ALL " +
            "    SELECT oi.OID, i.PubID, oi.number_of_copies, " +
            "           oi.number_of_copies * oi.unit_price " +
            "    FROM Orders_issues oi JOIN Issues i ON oi.IID = i.IID " +
            ") combined ON o.OID = combined.OID " +
            "JOIN Publications p ON combined.PubID = p.PubID " +
            "WHERE YEARWEEK(o.date_ordered, 1) = ? " +
            "GROUP BY p.PubID, YEARWEEK(o.date_ordered, 1) " +
            "ORDER BY p.PubID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yearWeekVal);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n===== Publications Sold: Year " + year + ", ISO Week " + isoWeek + " =====");
                System.out.printf("%-6s %-30s %-10s %-12s %-10s%n",
                    "PubID", "Title", "Year-Week", "Copies", "Total Price");
                System.out.println("-".repeat(74));
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("%-6s %-30s %-10s %-12d $%-10.2f%n",
                        rs.getString("PubID"),
                        rs.getString("publication_title"),
                        rs.getString("year_week"),
                        rs.getInt("total_copies"),
                        rs.getDouble("total_price"));
                }
                if (!any) System.out.println("No orders found for year " + year + ", week " + isoWeek + ".");
            }
        }
    }

    /**
     * Prompts for a year and month, then reports copies and total price of
     * each publication ordered that month.
     */
    public void reportPerMonthByInput() throws SQLException {
        System.out.print("\nEnter year (e.g. 2026): ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine().trim());

        String sql =
            "SELECT p.PubID, p.Title AS publication_title, " +
            "       YEAR(o.date_ordered) AS year, MONTH(o.date_ordered) AS month, " +
            "       SUM(combined.copies) AS total_copies, " +
            "       SUM(combined.total_price) AS total_price " +
            "FROM Orders o " +
            "JOIN (" +
            "    SELECT ob.OID, b.PubID, ob.number_of_copies AS copies, " +
            "           ob.number_of_copies * ob.unit_price AS total_price " +
            "    FROM Orders_books ob JOIN Books b ON ob.ISBN = b.ISBN " +
            "    UNION ALL " +
            "    SELECT oi.OID, i.PubID, oi.number_of_copies, " +
            "           oi.number_of_copies * oi.unit_price " +
            "    FROM Orders_issues oi JOIN Issues i ON oi.IID = i.IID " +
            ") combined ON o.OID = combined.OID " +
            "JOIN Publications p ON combined.PubID = p.PubID " +
            "WHERE YEAR(o.date_ordered) = ? AND MONTH(o.date_ordered) = ? " +
            "GROUP BY p.PubID, YEAR(o.date_ordered), MONTH(o.date_ordered) " +
            "ORDER BY p.PubID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%n===== Publications Sold: %04d-%02d =====%n", year, month);
                System.out.printf("%-6s %-30s %-6s %-6s %-12s %-10s%n",
                    "PubID", "Title", "Year", "Month", "Copies", "Total Price");
                System.out.println("-".repeat(76));
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("%-6s %-30s %-6d %-6d %-12d $%-10.2f%n",
                        rs.getString("PubID"),
                        rs.getString("publication_title"),
                        rs.getInt("year"),
                        rs.getInt("month"),
                        rs.getInt("total_copies"),
                        rs.getDouble("total_price"));
                }
                if (!any) System.out.printf("No orders found for %04d-%02d.%n", year, month);
            }
        }
    }

    /**
     * Prompts for a year and ISO week, then reports revenue and expenses for
     * that week.
     */
    public void weeklyRevenueExpensesByInput() throws SQLException {
        System.out.print("\nEnter year (e.g. 2026): ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter ISO week number (e.g. 6): ");
        int isoWeek = Integer.parseInt(scanner.nextLine().trim());
        String yearWeekVal = String.format("%04d%02d", year, isoWeek);

        String sql =
            "SELECT YEARWEEK(period, 1) AS year_week," +
            "       SUM(revenue)  AS total_revenue," +
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
            "   SELECT o.date_ordered, 0, o.shipping_fee, 0 FROM Orders o" +
            "   UNION ALL" +
            "   SELECT wp.pay_issue_date, 0, 0, wp.amount FROM Worker_Payments wp" +
            " ) combined" +
            " WHERE YEARWEEK(period, 1) = ?" +
            " GROUP BY YEARWEEK(period, 1)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yearWeekVal);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n===== Revenue & Expenses: Year " + year + ", ISO Week " + isoWeek + " =====");
                System.out.printf("%-10s %-14s %-14s %-14s %-14s%n",
                    "Year-Week", "Revenue", "Shipping", "Salaries", "Total Expenses");
                System.out.println("-".repeat(70));
                if (rs.next()) {
                    System.out.printf("%-10s $%-13.2f $%-13.2f $%-13.2f $%-13.2f%n",
                        rs.getString("year_week"),
                        rs.getDouble("total_revenue"),
                        rs.getDouble("total_shipping"),
                        rs.getDouble("total_salaries"),
                        rs.getDouble("total_expenses"));
                } else {
                    System.out.println("No data found for year " + year + ", week " + isoWeek + ".");
                }
            }
        }
    }

    /**
     * Prompts for a year and month, then reports revenue and expenses for
     * that month.
     */
    public void monthlyRevenueExpensesByInput() throws SQLException {
        System.out.print("\nEnter year (e.g. 2026): ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine().trim());

        String sql =
            "SELECT YEAR(period) AS year, MONTH(period) AS month," +
            "       SUM(revenue)  AS total_revenue," +
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
            "   SELECT o.date_ordered, 0, o.shipping_fee, 0 FROM Orders o" +
            "   UNION ALL" +
            "   SELECT wp.pay_issue_date, 0, 0, wp.amount FROM Worker_Payments wp" +
            " ) combined" +
            " WHERE YEAR(period) = ? AND MONTH(period) = ?" +
            " GROUP BY YEAR(period), MONTH(period)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%n===== Revenue & Expenses: %04d-%02d =====%n", year, month);
                System.out.printf("%-6s %-6s %-14s %-14s %-14s %-14s%n",
                    "Year", "Month", "Revenue", "Shipping", "Salaries", "Total Expenses");
                System.out.println("-".repeat(74));
                if (rs.next()) {
                    System.out.printf("%-6d %-6d $%-13.2f $%-13.2f $%-13.2f $%-13.2f%n",
                        rs.getInt("year"),
                        rs.getInt("month"),
                        rs.getDouble("total_revenue"),
                        rs.getDouble("total_shipping"),
                        rs.getDouble("total_salaries"),
                        rs.getDouble("total_expenses"));
                } else {
                    System.out.printf("No data found for %04d-%02d.%n", year, month);
                }
            }
        }
    }

    /**
     * Prompts for a year and ISO week, then reports worker payments issued
     * that week.
     */
    public void paymentsPerWeekByInput() throws SQLException {
        System.out.print("\nEnter year (e.g. 2026): ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter ISO week number (e.g. 6): ");
        int isoWeek = Integer.parseInt(scanner.nextLine().trim());
        String yearWeekVal = String.format("%04d%02d", year, isoWeek);

        String sql =
            "SELECT w.EID, w.worker_name, w.worker_type, " +
            "       wp.PID, wp.payment_type, wp.amount, " +
            "       wp.pay_issue_date, wp.pay_claim_date " +
            "FROM Worker_Payments wp " +
            "JOIN Workers w ON wp.EID = w.EID " +
            "WHERE w.worker_type IN ('Editor', 'Author') " +
            "  AND YEARWEEK(wp.pay_issue_date, 1) = ? " +
            "ORDER BY wp.pay_issue_date, w.worker_type";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yearWeekVal);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n===== Worker Payments: Year " + year + ", ISO Week " + isoWeek + " =====");
                System.out.printf("%-6s %-20s %-8s %-6s %-40s %-10s %-12s %-12s%n",
                    "EID", "Name", "Type", "PID", "Payment For", "Amount", "Issued", "Claimed");
                System.out.println("-".repeat(120));
                boolean any = false;
                double total = 0;
                while (rs.next()) {
                    any = true;
                    double amt = rs.getDouble("amount");
                    total += amt;
                    String claimed = rs.getString("pay_claim_date");
                    System.out.printf("%-6s %-20s %-8s %-6s %-40s $%-9.2f %-12s %-12s%n",
                        rs.getString("EID"), rs.getString("worker_name"),
                        rs.getString("worker_type"), rs.getString("PID"),
                        rs.getString("payment_type"), amt,
                        rs.getString("pay_issue_date"),
                        claimed != null ? claimed : "Unclaimed");
                }
                if (!any) System.out.println("No payments found for year " + year + ", week " + isoWeek + ".");
                else       System.out.printf("Total Paid Out: $%.2f%n", total);
            }
        }
    }

    /**
     * Prompts for a year and month, then reports worker payments issued
     * that month.
     */
    public void paymentsPerMonthByInput() throws SQLException {
        System.out.print("\nEnter year (e.g. 2026): ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine().trim());

        String sql =
            "SELECT w.EID, w.worker_name, w.worker_type, " +
            "       wp.PID, wp.payment_type, wp.amount, " +
            "       wp.pay_issue_date, wp.pay_claim_date " +
            "FROM Worker_Payments wp " +
            "JOIN Workers w ON wp.EID = w.EID " +
            "WHERE w.worker_type IN ('Editor', 'Author') " +
            "  AND YEAR(wp.pay_issue_date) = ? AND MONTH(wp.pay_issue_date) = ? " +
            "ORDER BY wp.pay_issue_date, w.worker_type";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%n===== Worker Payments: %04d-%02d =====%n", year, month);
                System.out.printf("%-6s %-20s %-8s %-6s %-40s %-10s %-12s %-12s%n",
                    "EID", "Name", "Type", "PID", "Payment For", "Amount", "Issued", "Claimed");
                System.out.println("-".repeat(120));
                boolean any = false;
                double total = 0;
                while (rs.next()) {
                    any = true;
                    double amt = rs.getDouble("amount");
                    total += amt;
                    String claimed = rs.getString("pay_claim_date");
                    System.out.printf("%-6s %-20s %-8s %-6s %-40s $%-9.2f %-12s %-12s%n",
                        rs.getString("EID"), rs.getString("worker_name"),
                        rs.getString("worker_type"), rs.getString("PID"),
                        rs.getString("payment_type"), amt,
                        rs.getString("pay_issue_date"),
                        claimed != null ? claimed : "Unclaimed");
                }
                if (!any) System.out.printf("No payments found for %04d-%02d.%n", year, month);
                else       System.out.printf("Total Paid Out: $%.2f%n", total);
            }
        }
    }

    /**
     * Prompts for a year and ISO week, then reports issues published and
     * articles written that week.
     */
    public void articlesIssuesPerWeekByInput() throws SQLException {
        System.out.print("\nEnter year (e.g. 2026): ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter ISO week number (e.g. 6): ");
        int isoWeek = Integer.parseInt(scanner.nextLine().trim());
        String yearWeekVal = String.format("%04d%02d", year, isoWeek);

        System.out.println("\n===== Articles & Issues Published: Year " + year + ", ISO Week " + isoWeek + " =====");

        System.out.println("  Issues:");
        String issSql =
            "SELECT i.IID, i.Subtitle, i.publication_date, p.title AS pub_title " +
            "FROM Issues i " +
            "JOIN Periodicals per ON i.PubID  = per.PubID " +
            "JOIN Publications p  ON per.PubID = p.PubID " +
            "WHERE YEARWEEK(i.publication_date, 1) = ? " +
            "ORDER BY i.publication_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(issSql)) {
            ps.setString(1, yearWeekVal);
            try (ResultSet rs = ps.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("    IID: %-6s | %-42s | Publication: %-28s | Date: %s%n",
                        rs.getString("IID"), rs.getString("Subtitle"),
                        rs.getString("pub_title"), rs.getString("publication_date"));
                }
                if (!any) System.out.println("    No issues published this week.");
            }
        }

        System.out.println("  Articles:");
        String artSql =
            "SELECT a.AID, a.title, a.topic, a.written_date, w.worker_name " +
            "FROM Articles a " +
            "JOIN Works_on_articles wa ON a.AID = wa.AID " +
            "JOIN Workers w            ON wa.EID = w.EID " +
            "WHERE YEARWEEK(a.written_date, 1) = ? " +
            "ORDER BY a.written_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(artSql)) {
            ps.setString(1, yearWeekVal);
            try (ResultSet rs = ps.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("    AID: %-6s | %-38s | Topic: %-25s | Written: %s | Author: %s%n",
                        rs.getString("AID"), rs.getString("title"), rs.getString("topic"),
                        rs.getString("written_date"), rs.getString("worker_name"));
                }
                if (!any) System.out.println("    No articles written this week.");
            }
        }
    }

    /**
     * Prompts for a year and month, then reports issues published and
     * articles written that month.
     */
    public void articlesIssuesPerMonthByInput() throws SQLException {
        System.out.print("\nEnter year (e.g. 2026): ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine().trim());

        System.out.printf("%n===== Articles & Issues Published: %04d-%02d =====%n", year, month);

        System.out.println("  Issues:");
        String issSql =
            "SELECT i.IID, i.Subtitle, i.publication_date, p.title AS pub_title " +
            "FROM Issues i " +
            "JOIN Periodicals per ON i.PubID  = per.PubID " +
            "JOIN Publications p  ON per.PubID = p.PubID " +
            "WHERE YEAR(i.publication_date) = ? AND MONTH(i.publication_date) = ? " +
            "ORDER BY i.publication_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(issSql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("    IID: %-6s | %-42s | Publication: %-28s | Date: %s%n",
                        rs.getString("IID"), rs.getString("Subtitle"),
                        rs.getString("pub_title"), rs.getString("publication_date"));
                }
                if (!any) System.out.println("    No issues published this month.");
            }
        }

        System.out.println("  Articles:");
        String artSql =
            "SELECT a.AID, a.title, a.topic, a.written_date, w.worker_name " +
            "FROM Articles a " +
            "JOIN Works_on_articles wa ON a.AID = wa.AID " +
            "JOIN Workers w            ON wa.EID = w.EID " +
            "WHERE YEAR(a.written_date) = ? AND MONTH(a.written_date) = ? " +
            "ORDER BY a.written_date";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(artSql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("    AID: %-6s | %-38s | Topic: %-25s | Written: %s | Author: %s%n",
                        rs.getString("AID"), rs.getString("title"), rs.getString("topic"),
                        rs.getString("written_date"), rs.getString("worker_name"));
                }
                if (!any) System.out.println("    No articles written this month.");
            }
        }
    }
}
