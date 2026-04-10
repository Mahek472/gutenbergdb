package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;
import java.sql.*;

public class PublicationDAO {

    // =========================
    // INSERT NEW PERIODICAL
    // =========================
    public void insertNewPeriodical() throws SQLException {
        String pubSql = "INSERT INTO Publications (PubID, Title, periodicity, topic) VALUES (?, ?, ?, ?)";
        String perSql = "INSERT INTO Periodicals (PubID, type) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (
                PreparedStatement p1 = conn.prepareStatement(pubSql);
                PreparedStatement p2 = conn.prepareStatement(perSql)
            ) {
                p1.setInt(1, 11);
                p1.setString(2, "Science Today");
                p1.setString(3, "monthly");
                p1.setString(4, "General Science");
                p1.executeUpdate();

                p2.setInt(1, 11);
                p2.setString(2, "magazine");
                p2.executeUpdate();

                conn.commit();
                System.out.println("Inserted Periodical: Science Today");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // =========================
    // INSERT NEW BOOK
    // =========================
    public void insertNewBook() throws SQLException {
        String pubSql = "INSERT INTO Publications (PubID, Title, periodicity, topic) VALUES (?, ?, ?, ?)";
        String bookSql = "INSERT INTO Books (PubID, ISBN, full_text, edition_number, publication_date, written_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (
                PreparedStatement p1 = conn.prepareStatement(pubSql);
                PreparedStatement p2 = conn.prepareStatement(bookSql)
            ) {
                p1.setInt(1, 12);
                p1.setString(2, "BossyPants");
                p1.setNull(3, Types.VARCHAR);
                p1.setString(4, "Comedy");
                p1.executeUpdate();

                p2.setInt(1, 12);
                p2.setString(2, "978-0-7432-7357-1");
                p2.setString(3, "Full text...");
                p2.setInt(4, 1);
                p2.setDate(5, Date.valueOf("1925-04-10"));
                p2.setDate(6, Date.valueOf("1924-01-01"));
                p2.executeUpdate();

                conn.commit();
                System.out.println("Inserted Book: BossyPants");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // =========================
    // SHOW BOOK DETAILS
    // =========================
    public void showBookDetails(int pubId) throws SQLException {
        String sql = "SELECT p.PubID, p.Title, p.topic, b.ISBN, b.edition_number, b.publication_date " +
                     "FROM Publications p JOIN Books b ON p.PubID = b.PubID WHERE p.PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pubId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nBook Details");
            System.out.printf("%-6s %-20s %-15s %-20s %-10s %-15s%n",
                    "PubID", "Title", "Topic", "ISBN", "Edition", "Pub Date");
            System.out.println("-".repeat(90));

            while (rs.next()) {
                System.out.printf("%-6d %-20s %-15s %-20s %-10d %-15s%n",
                        rs.getInt("PubID"),
                        rs.getString("Title"),
                        rs.getString("topic"),
                        rs.getString("ISBN"),
                        rs.getInt("edition_number"),
                        rs.getDate("publication_date")
                );
            }
        }
    }

    // =========================
    // UPDATE PERIODICAL
    // =========================
    public void updatePeriodical() throws SQLException {
        String sql = "UPDATE Publications SET periodicity = ?, topic = ? WHERE PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "weekly");
            stmt.setString(2, "Applied Science");
            stmt.setInt(3, 11);

            int rows = stmt.executeUpdate();
            System.out.println("Updated Periodical Rows: " + rows);
        }
    }

    // =========================
    // UPDATE BOOK EDITION
    // =========================
    public void updateBookEdition() throws SQLException {
        String sql = "UPDATE Books SET edition_number = ? WHERE PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, 2);
            stmt.setInt(2, 12);

            int rows = stmt.executeUpdate();
            System.out.println("Updated Book Edition Rows: " + rows);
        }
    }

    // =========================
    // SHOW PERIODICAL DETAILS
    // =========================
    public void showPeriodical(int pubId) throws SQLException {
        String sql = "SELECT * FROM Publications WHERE PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pubId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n===== Periodical Details =====");
            System.out.printf("%-6s %-20s %-15s %-20s%n",
                    "PubID", "Title", "Periodicity", "Topic");
            System.out.println("-".repeat(65));

            while (rs.next()) {
                System.out.printf("%-6d %-20s %-15s %-20s%n",
                        rs.getInt("PubID"),
                        rs.getString("Title"),
                        rs.getString("periodicity"),
                        rs.getString("topic"));
            }
        }
    }

    // =========================
    // EDITOR MANAGEMENT
    // =========================

    public void assignEditor(int pubId, int eid) throws SQLException {
        // Assuming a junction table 'Works_on_publications' or similar exists for editors
        String sql = "INSERT INTO Works_on_books (PubID, EID) VALUES (?, ?)"; 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pubId);
            stmt.setInt(2, eid);
            stmt.executeUpdate();
            System.out.println("Assigned Editor EID " + eid + " to Publication " + pubId);
        }
    }

    public void removeEditor(int pubId, int eid) throws SQLException {
        String sql = "DELETE FROM Works_on_books WHERE PubID = ? AND EID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pubId);
            stmt.setInt(2, eid);
            stmt.executeUpdate();
            System.out.println("Removed Editor EID " + eid + " from Publication " + pubId);
        }
    }

    public void viewPublicationsByEditor(int eid) throws SQLException {
        String sql = "SELECT p.PubID, p.Title FROM Publications p " +
                     "JOIN Works_on_books wb ON p.PubID = wb.PubID WHERE wb.EID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eid);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\nPublications for Editor EID: " + eid);
            while (rs.next()) {
                System.out.printf("ID: %d | Title: %s%n", 
                    rs.getInt("PubID"), 
                    rs.getString("Title"));
            }
        }
    }
}
