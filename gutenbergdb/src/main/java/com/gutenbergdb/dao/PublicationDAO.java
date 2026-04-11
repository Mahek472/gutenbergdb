/**
 * PublicationDAO
 *
 * Handles all database operations related to Publications, Periodicals, and Books.
 *
 * Functionalities:
 * - Insert new periodical (multi-table insert)
 * - Insert new book (multi-table insert)
 * - Retrieve book details using JOIN
 * - Update periodical attributes (periodicity, topic)
 * - Update book edition
 * - Display before/after states for verification
 *
 * Design Decisions:
 * - Used PreparedStatement to prevent SQL injection
 * - Used transactions for multi-table inserts (commit/rollback)
 * - Used try-with-resources to ensure DB connections are closed
 * - Implemented duplicate checks to avoid primary key violations
 *
 * Tables Used:
 * - Publications (parent)
 * - Periodicals (child)
 * - Books (child)
 *
 * Author: [Manusha]
 */
package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;
import java.sql.*;

public class PublicationDAO {

  
    // INSERT NEW PERIODICAL
    public void insertNewPeriodical() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM Publications WHERE PubID = ?";
        String pubSql = "INSERT INTO Publications (PubID, Title, periodicity, topic) VALUES (?, ?, ?, ?)";
        String perSql = "INSERT INTO Periodicals (PubID, type) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                PreparedStatement pubStmt = conn.prepareStatement(pubSql);
                PreparedStatement perStmt = conn.prepareStatement(perSql)
            ) {
                checkStmt.setInt(1, 11);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Skipping periodical insert: PubID 11 already exists.");
                        conn.rollback();
                        return;
                    }
                }

                pubStmt.setInt(1, 11);
                pubStmt.setString(2, "Science Today");
                pubStmt.setString(3, "monthly");
                pubStmt.setString(4, "General Science");
                int pubRows = pubStmt.executeUpdate();

                perStmt.setInt(1, 11);
                perStmt.setString(2, "magazine");
                int perRows = perStmt.executeUpdate();

                conn.commit();
                System.out.println("Inserted periodical: Science Today (Publications rows: "
                        + pubRows + ", Periodicals rows: " + perRows + ")");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }


    // INSERT NEW BOOK
    public void insertNewBook() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM Publications WHERE PubID = ?";
        String pubSql = "INSERT INTO Publications (PubID, Title, periodicity, topic) VALUES (?, ?, ?, ?)";
        String bookSql = "INSERT INTO Books (PubID, ISBN, full_text, edition_number, publication_date, written_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                PreparedStatement pubStmt = conn.prepareStatement(pubSql);
                PreparedStatement bookStmt = conn.prepareStatement(bookSql)
            ) {
                checkStmt.setInt(1, 12);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Skipping book insert: PubID 12 already exists.");
                        conn.rollback();
                        return;
                    }
                }

                pubStmt.setInt(1, 12);
                pubStmt.setString(2, "BossyPants");
                pubStmt.setNull(3, Types.VARCHAR);
                pubStmt.setString(4, "Comedy");
                int pubRows = pubStmt.executeUpdate();

                bookStmt.setInt(1, 12);
                bookStmt.setString(2, "978-0-7432-7357-1");
                bookStmt.setString(3, "Full text of BossyPants...");
                bookStmt.setInt(4, 1);
                bookStmt.setDate(5, Date.valueOf("1925-04-10"));
                bookStmt.setDate(6, Date.valueOf("1924-01-01"));
                int bookRows = bookStmt.executeUpdate();

                conn.commit();
                System.out.println("Inserted book: BossyPants (Publications rows: "
                        + pubRows + ", Books rows: " + bookRows + ")");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

 
    // SHOW BOOK DETAILS
 
    public void showBookDetails(int pubId) throws SQLException {
        String sql = "SELECT p.PubID, p.Title, p.topic, b.ISBN, b.edition_number, b.publication_date " +
                     "FROM Publications p " +
                     "JOIN Books b ON p.PubID = b.PubID " +
                     "WHERE p.PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pubId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nBook Details");
                System.out.printf("%-6s %-20s %-15s %-20s %-10s %-15s%n",
                        "PubID", "Title", "Topic", "ISBN", "Edition", "Pub Date");
                System.out.println("-".repeat(90));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-6d %-20s %-15s %-20s %-10d %-15s%n",
                            rs.getInt("PubID"),
                            rs.getString("Title"),
                            rs.getString("topic"),
                            rs.getString("ISBN"),
                            rs.getInt("edition_number"),
                            rs.getDate("publication_date"));
                }

                if (!found) {
                    System.out.println("No book found for PubID " + pubId);
                }
            }
        }
    }


    // SHOW PERIODICAL DETAILS
  
    public void showPeriodical(int pubId) throws SQLException {
        String sql = "SELECT PubID, Title, periodicity, topic FROM Publications WHERE PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pubId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nPeriodical Details");
                System.out.printf("%-6s %-20s %-15s %-20s%n",
                        "PubID", "Title", "Periodicity", "Topic");
                System.out.println("-".repeat(65));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-6d %-20s %-15s %-20s%n",
                            rs.getInt("PubID"),
                            rs.getString("Title"),
                            rs.getString("periodicity"),
                            rs.getString("topic"));
                }

                if (!found) {
                    System.out.println("No publication found for PubID " + pubId);
                }
            }
        }
    }


    // UPDATE PERIODICAL

    public void updatePeriodical() throws SQLException {
        int pubId = 11;
        System.out.println("\n===== Before Periodical Update =====");
        showPeriodical(pubId);

        String sql = "UPDATE Publications SET periodicity = ?, topic = ? WHERE PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "weekly");
            stmt.setString(2, "Applied Science");
            stmt.setInt(3, pubId);

            int rows = stmt.executeUpdate();
            System.out.println("\nUpdated periodical rows: " + rows);
        }

        System.out.println("\nAfter Periodical Update");
        showPeriodical(pubId);
    }


    // UPDATE BOOK EDITION
   
    public void updateBookEdition() throws SQLException {
        int pubId = 12;

        System.out.println("\nBefore Book Update");
        showBookDetails(pubId);

        String sql = "UPDATE Books SET edition_number = ? WHERE PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, 2);
            stmt.setInt(2, pubId);

            int rows = stmt.executeUpdate();
            System.out.println("\nUpdated book edition rows: " + rows);
        }

        System.out.println("\nAfter Book Update ");
        showBookDetails(pubId);
    }
}