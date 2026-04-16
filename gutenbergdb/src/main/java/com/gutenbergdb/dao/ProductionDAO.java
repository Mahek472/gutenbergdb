package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;
import java.sql.*;

/**
 * ProductionDAO handles core production operations including publications,
 * books, issues, and worker payments.
 * All IDs (PubID, IID, PID, EID) are VARCHAR strings (e.g. "PUB001", "I001", "P001", "E001").
 */
public class ProductionDAO {

    // --- 1. Enter a new issue ---

    public void enterIssue(String iid, String pubID, String subtitle, String pubDate) throws SQLException {
        String checkPubSql = "SELECT COUNT(*) FROM Periodicals WHERE PubID = ?";
        String checkIidSql = "SELECT COUNT(*) FROM Issues WHERE IID = ?";
        String issueSQL    = "INSERT INTO Issues (IID, publication_date, Subtitle, PubID) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(checkPubSql)) {
                    ps.setString(1, pubID);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            throw new SQLException("No periodical found for PubID: " + pubID);
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(checkIidSql)) {
                    ps.setString(1, iid);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("Issue with IID " + iid + " already exists.");
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(issueSQL)) {
                    ps.setString(1, iid);
                    ps.setDate(2, Date.valueOf(pubDate));
                    ps.setString(3, subtitle);
                    ps.setString(4, pubID);
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println("\n--- New Issue Entered ---");
                System.out.printf("IID: %s | PubID: %s | Subtitle: %s | Pub Date: %s%n",
                        iid, pubID, subtitle, pubDate);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // --- 1b. PubID-first new book edition workflow ---

    /**
     * Prints all publications so the user can pick a PubID before entering a new edition.
     */
    public void listAllPublications() throws SQLException {
        String sql = "SELECT PubID, title, topic FROM Publications ORDER BY PubID";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("\nAvailable Publications:");
            System.out.printf("  %-10s %-40s %-20s%n", "PubID", "Title", "Topic");
            System.out.println("  " + "-".repeat(72));
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  %-10s %-40s %-20s%n",
                        rs.getString("PubID"),
                        rs.getString("title"),
                        rs.getString("topic"));
            }
            if (!found) {
                System.out.println("  (no publications found)");
            }
        }
    }

    /**
     * Returns [PubID, title, topic] for the given PubID, or null if not found.
     */
    public String[] getPublicationInfo(String pubID) throws SQLException {
        String sql = "SELECT PubID, title, topic FROM Publications WHERE PubID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pubID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("PubID"),
                        rs.getString("title"),
                        rs.getString("topic")
                    };
                }
                return null;
            }
        }
    }

    /**
     * Prints all existing editions for the given PubID so the user can see
     * what already exists before adding a new one.
     */
    public void listEditionsForPub(String pubID) throws SQLException {
        String sql = "SELECT ISBN, title, edition_number, publication_date " +
                     "FROM Books WHERE PubID = ? ORDER BY edition_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pubID);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\nExisting editions for PubID " + pubID + ":");
                System.out.printf("  %-20s %-40s %-10s %-15s%n",
                        "ISBN", "Title", "Edition", "Pub Date");
                System.out.println("  " + "-".repeat(90));
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("  %-20s %-40s %-10d %-15s%n",
                            rs.getString("ISBN"),
                            rs.getString("title"),
                            rs.getInt("edition_number"),
                            rs.getDate("publication_date"));
                }
                if (!found) {
                    System.out.println("  (no editions found — this will be the first)");
                }
            }
        }
    }

    /**
     * Inserts a new row into Books for an already-existing PubID.
     * Does NOT touch Publications — the publication record already exists.
     */
    public void addBookEdition(String pubID, String isbn, String title, int edition, String pubDate) throws SQLException {
        String checkIsbnSql    = "SELECT COUNT(*) FROM Books WHERE ISBN = ?";
        String checkEditionSql = "SELECT COUNT(*) FROM Books WHERE PubID = ? AND edition_number = ?";
        String insertSql       = "INSERT INTO Books (ISBN, title, edition_number, publication_date, PubID) " +
                                 "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(checkIsbnSql)) {
                    ps.setString(1, isbn);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("ISBN " + isbn + " already exists in Books.");
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(checkEditionSql)) {
                    ps.setString(1, pubID);
                    ps.setInt(2, edition);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("Edition " + edition + " already exists for PubID " + pubID + ".");
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, isbn);
                    ps.setString(2, title);
                    ps.setInt(3, edition);
                    ps.setDate(4, Date.valueOf(pubDate));
                    ps.setString(5, pubID);
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println("\n--- New Book Edition Added ---");
                System.out.printf("PubID: %s | ISBN: %s | Title: %s | Edition: %d | Pub Date: %s%n",
                        pubID, isbn, title, edition, pubDate);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // --- 2. Update, delete a book edition or issue ---

    public void updateBookEdition(String isbn, Integer edition, String pubDate) throws SQLException {
        String sql = "UPDATE Books " +
                     "SET edition_number   = COALESCE(?, edition_number), " +
                     "    publication_date = COALESCE(?, publication_date) " +
                     "WHERE ISBN = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (edition != null) ps.setInt(1, edition); else ps.setNull(1, Types.INTEGER);
            if (pubDate != null) ps.setDate(2, Date.valueOf(pubDate)); else ps.setNull(2, Types.DATE);
            ps.setString(3, isbn);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                System.out.println("No book found for ISBN: " + isbn);
            } else {
                System.out.println("Book edition updated for ISBN: " + isbn);
            }
        }
    }

    public void deleteBookEdition(String isbn) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Detach chapters (ISBN is a nullable FK in Chapters)
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Chapters SET ISBN = NULL WHERE ISBN = ?")) {
                    ps.setString(1, isbn);
                    ps.executeUpdate();
                }
                // Remove order line items
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM Orders_books WHERE ISBN = ?")) {
                    ps.setString(1, isbn);
                    ps.executeUpdate();
                }
                // Delete the book row
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM Books WHERE ISBN = ?")) {
                    ps.setString(1, isbn);
                    ps.executeUpdate();
                }
                conn.commit();
                System.out.println("Book edition deleted for ISBN: " + isbn);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void deleteIssue(String iid) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Detach articles (IID is a nullable FK in Articles)
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Articles SET IID = NULL WHERE IID = ?")) {
                    ps.setString(1, iid);
                    ps.executeUpdate();
                }
                // Remove order line items
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM Orders_issues WHERE IID = ?")) {
                    ps.setString(1, iid);
                    ps.executeUpdate();
                }
                // Delete the issue row
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM Issues WHERE IID = ?")) {
                    ps.setString(1, iid);
                    ps.executeUpdate();
                }
                conn.commit();
                System.out.println("Issue deleted for IID: " + iid);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // --- 5. Find books and articles by topic, date range, author's name ---

    public void findByTopic(String topic) throws SQLException {
        String keyword = "%" + topic + "%";
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("\n--- Search Results for Topic: " + topic + " ---");

            String bookSQL = "SELECT p.title, b.ISBN " +
                             "FROM Publications p JOIN Books b ON p.PubID = b.PubID " +
                             "WHERE p.topic LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(bookSQL)) {
                ps.setString(1, keyword);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        System.out.println("[Book] " + rs.getString("title") +
                                " (ISBN: " + rs.getString("ISBN") + ")");
                }
            }

            String artSQL = "SELECT title FROM Articles WHERE topic LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(artSQL)) {
                ps.setString(1, keyword);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        System.out.println("[Article] " + rs.getString("title"));
                }
            }
        }
    }

    public void findByDateRange(String start, String end) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("\n--- Content between " + start + " and " + end + " ---");

            String bookSQL = "SELECT p.title, b.publication_date " +
                             "FROM Publications p JOIN Books b ON p.PubID = b.PubID " +
                             "WHERE b.publication_date BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(bookSQL)) {
                ps.setString(1, start);
                ps.setString(2, end);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        System.out.println("[Book] " + rs.getString("title") +
                                " | Date: " + rs.getDate("publication_date"));
                }
            }

            String artSQL = "SELECT title, written_date FROM Articles WHERE written_date BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(artSQL)) {
                ps.setString(1, start);
                ps.setString(2, end);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        System.out.println("[Article] " + rs.getString("title") +
                                " | Date: " + rs.getDate("written_date"));
                }
            }
        }
    }

    public void findByAuthor(String name) throws SQLException {
        String keyword = "%" + name + "%";
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("\n--- Content by Author: " + name + " ---");

            // Books via Works_on_chapters -> Chapters -> Books -> Publications
            String bookSQL = "SELECT DISTINCT p.title " +
                             "FROM Workers w " +
                             "JOIN Works_on_chapters wc ON w.EID = wc.EID " +
                             "JOIN Chapters c ON wc.CID = c.CID " +
                             "JOIN Books b ON c.ISBN = b.ISBN " +
                             "JOIN Publications p ON b.PubID = p.PubID " +
                             "WHERE w.worker_name LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(bookSQL)) {
                ps.setString(1, keyword);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        System.out.println("[Book] " + rs.getString("title"));
                }
            }

            // Articles via Works_on_articles -> Articles
            String artSQL = "SELECT a.title " +
                            "FROM Workers w " +
                            "JOIN Works_on_articles wa ON w.EID = wa.EID " +
                            "JOIN Articles a ON wa.AID = a.AID " +
                            "WHERE w.worker_name LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(artSQL)) {
                ps.setString(1, keyword);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        System.out.println("[Article] " + rs.getString("title"));
                }
            }
        }
    }

    // --- 6. Enter payment; update when payment was claimed ---

    public void enterWorkerPayment(String pid, String eid, double amount, String type, String issueDate) throws SQLException {
        String checkEidSql = "SELECT COUNT(*) FROM Workers WHERE EID = ?";
        String checkPidSql = "SELECT COUNT(*) FROM Worker_Payments WHERE PID = ?";
        String paySQL      = "INSERT INTO Worker_Payments " +
                             "(PID, amount, payment_type, pay_issue_date, pay_claim_date, EID) " +
                             "VALUES (?, ?, ?, ?, NULL, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(checkEidSql)) {
                    ps.setString(1, eid);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            throw new SQLException("Worker with EID " + eid + " does not exist.");
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(checkPidSql)) {
                    ps.setString(1, pid);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException("Payment with PID " + pid + " already exists.");
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(paySQL)) {
                    ps.setString(1, pid);
                    ps.setDouble(2, amount);
                    ps.setString(3, type);
                    ps.setDate(4, Date.valueOf(issueDate));
                    ps.setString(5, eid);
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println("\n--- New Worker Payment Entered ---");
                System.out.printf("PID: %s | EID: %s | Amount: $%.2f | Type: %s | Issue Date: %s%n",
                        pid, eid, amount, type, issueDate);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void updatePaymentClaimed(String pid, String claimDate) throws SQLException {
        String sql = "UPDATE Worker_Payments SET pay_claim_date = ? WHERE PID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(claimDate));
            ps.setString(2, pid);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Payment " + pid + " marked as claimed on " + claimDate);
            } else {
                System.out.println("No payment found for PID: " + pid);
            }
        }
    }

    // --- 7. List unclaimed payments within a specified time window ---

    public void listUnclaimedPayments(String start, String end) throws SQLException {
        String sql = "SELECT wp.PID, w.worker_name, wp.amount, wp.pay_issue_date " +
                     "FROM Worker_Payments wp " +
                     "JOIN Workers w ON wp.EID = w.EID " +
                     "WHERE wp.pay_claim_date IS NULL " +
                     "  AND wp.pay_issue_date BETWEEN ? AND ? " +
                     "ORDER BY wp.pay_issue_date";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n--- Unclaimed Payments (" + start + " to " + end + ") ---");
                System.out.printf("%-8s %-25s %-12s %-15s%n", "PID", "Worker", "Amount", "Issued Date");
                System.out.println("-".repeat(65));
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-8s %-25s $%-11.2f %-15s%n",
                            rs.getString("PID"),
                            rs.getString("worker_name"),
                            rs.getDouble("amount"),
                            rs.getDate("pay_issue_date"));
                }
                if (!found) System.out.println("No unclaimed payments found in this date range.");
            }
        }
    }

    // --- 8. Compare two issues by listing their associated articles ---

    public void compareIssues(String iid1, String iid2) throws SQLException {
        String sql = "SELECT a.IID, i.Subtitle, a.title, a.topic " +
                     "FROM Articles a " +
                     "JOIN Issues i ON a.IID = i.IID " +
                     "WHERE a.IID IN (?, ?) " +
                     "ORDER BY a.IID, a.title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, iid1);
            ps.setString(2, iid2);
            try (ResultSet rs = ps.executeQuery()) {
                String currentIID = null;
                System.out.println("\n--- Issue Comparison ---");
                while (rs.next()) {
                    String iid = rs.getString("IID");
                    if (!iid.equals(currentIID)) {
                        currentIID = iid;
                        System.out.println("\nIssue ID: " + iid + " (" + rs.getString("Subtitle") + ")");
                        System.out.printf("  %-35s %-20s%n", "Article Title", "Topic");
                        System.out.println("  " + "-".repeat(55));
                    }
                    System.out.printf("  %-35s %-20s%n", rs.getString("title"), rs.getString("topic"));
                }
                if (currentIID == null) System.out.println("No articles found for the given issue IDs.");
            }
        }
    }
}
