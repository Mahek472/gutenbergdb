package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;
import java.sql.*;

/**
 * ProductionDAO handles core production operations including publications, 
 * books, issues, and worker payments.
 */
public class ProductionDAO {

    // --- 1. Enter a new book edition or new issue ---

    /**
     * Supports an ISBN-first workflow by letting callers verify that a book
     * already exists before collecting the new edition details.
     */
    public boolean isbnExists(String isbn) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return isbnExists(conn, isbn);
        }
    }

    /**
     * Adds a new edition for an existing ISBN and reuses the existing topic when
     * the caller only wants to provide the new edition title and number.
     */
    public void enterBookEdition(int pubID, String isbn, String title, int edition, String pubDate, String writtenDate, String fullText) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!isbnExists(conn, isbn)) {
                    throw new SQLException("ISBN " + isbn + " does not exist. Add the original book before adding a new edition.");
                }

                String existingTopic = findTopicByIsbn(conn, isbn);
                insertBookEdition(conn, pubID, title, existingTopic, isbn, edition, pubDate, writtenDate, fullText);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void enterBookEdition(int pubID, String title, String topic, String isbn, int edition, String pubDate, String writtenDate, String fullText) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!isbnExists(conn, isbn)) {
                    throw new SQLException("ISBN " + isbn + " does not exist. Add the original book before adding a new edition.");
                }

                String existingTopic = findTopicByIsbn(conn, isbn);
                String resolvedTopic = hasText(topic) ? topic : existingTopic;
                insertBookEdition(conn, pubID, title, resolvedTopic, isbn, edition, pubDate, writtenDate, fullText);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void insertBookEdition(Connection conn, int pubID, String title, String topic, String isbn, int edition, String pubDate, String writtenDate, String fullText) throws SQLException {
        String pubSQL = "INSERT INTO Publications (PubID, Title, periodicity, topic) VALUES (?, ?, NULL, ?)";
        String bookSQL = "INSERT INTO Books (PubID, ISBN, full_text, edition_number, publication_date, written_date) VALUES (?, ?, ?, ?, ?, ?)";

        validateNewEdition(conn, pubID, isbn, edition);

        try (PreparedStatement psPub = conn.prepareStatement(pubSQL);
             PreparedStatement psBook = conn.prepareStatement(bookSQL)) {

            psPub.setInt(1, pubID);
            psPub.setString(2, title);
            if (hasText(topic)) {
                psPub.setString(3, topic);
            } else {
                psPub.setNull(3, Types.VARCHAR);
            }
            psPub.executeUpdate();

            psBook.setInt(1, pubID);
            psBook.setString(2, isbn);
            psBook.setString(3, fullText);
            psBook.setInt(4, edition);
            psBook.setString(5, emptyToNull(pubDate));
            psBook.setString(6, emptyToNull(writtenDate));
            psBook.executeUpdate();

            System.out.println("\n--- New Book Edition Entered ---");
            System.out.printf("Existing ISBN Verified: %s%n", isbn);
            System.out.printf("PubID: %d | Title: %s | Topic: %s%n", pubID, title, topic);
            System.out.printf("Edition: %d | Pub Date: %s | Written Date: %s%n", edition, pubDate, writtenDate);
            System.out.println("Full Text (partial): " + previewText(fullText));
        }
    }

    private void validateNewEdition(Connection conn, int pubID, String isbn, int edition) throws SQLException {
        if (publicationExists(conn, pubID)) {
            throw new SQLException("Publication with PubID " + pubID + " already exists.");
        }

        if (editionExists(conn, isbn, edition)) {
            throw new SQLException("Edition " + edition + " already exists for ISBN " + isbn + ".");
        }
    }

    private boolean publicationExists(Connection conn, int pubID) throws SQLException {
        String sql = "SELECT 1 FROM Publications WHERE PubID = ? LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, pubID);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean isbnExists(Connection conn, String isbn) throws SQLException {
        String sql = "SELECT 1 FROM Books WHERE ISBN = ? LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean editionExists(Connection conn, String isbn, int edition) throws SQLException {
        String sql = "SELECT 1 FROM Books WHERE ISBN = ? AND edition_number = ? LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            pstmt.setInt(2, edition);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String findTopicByIsbn(Connection conn, String isbn) throws SQLException {
        String sql = "SELECT p.topic FROM Books b JOIN Publications p ON p.PubID = b.PubID WHERE b.ISBN = ? LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("topic");
                }
                return null;
            }
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String emptyToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String previewText(String value) {
        String safeValue = value == null ? "" : value;
        return safeValue.length() > 50 ? safeValue.substring(0, 50) + "..." : safeValue;
    }

    public void enterIssue(int iid, int pubID, String subtitle, String pubDate) throws SQLException {
        String issueSQL = "INSERT INTO Issues (IID, subtitle, Publication_Date) VALUES (?, ?, ?)";
        String madeOfSQL = "INSERT INTO Made_of (PubID, IID) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psIssue = conn.prepareStatement(issueSQL);
                 PreparedStatement psMadeOf = conn.prepareStatement(madeOfSQL)) {
                
                psIssue.setInt(1, iid);
                psIssue.setString(2, subtitle);
                psIssue.setString(3, pubDate);
                psIssue.executeUpdate();

                psMadeOf.setInt(1, pubID);
                psMadeOf.setInt(2, iid);
                psMadeOf.executeUpdate();

                conn.commit();
                System.out.println("\n--- New Issue Entered ---");
                System.out.printf("IID: %d | PubID: %d | Subtitle: %s | Pub Date: %s%n", iid, pubID, subtitle, pubDate);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // --- 2. Update, delete a book edition or publication issue ---

    public void updateBookEdition(int pubID, String isbn, Integer edition, String pubDate) throws SQLException {
        String sql = "UPDATE Books SET ISBN = COALESCE(?, ISBN), edition_number = COALESCE(?, edition_number), publication_date = COALESCE(?, publication_date) WHERE PubID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            if (edition != null) pstmt.setInt(2, edition); else pstmt.setNull(2, Types.INTEGER);
            pstmt.setString(3, pubDate);
            pstmt.setInt(4, pubID);
            pstmt.executeUpdate();
        }
    }

    public void deleteBookEdition(int pubID) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String[] sqls = {
                    "DELETE FROM Works_on_books WHERE PubID = ?",
                    "DELETE FROM Orders_books WHERE PubID = ?",
                    "DELETE FROM Books WHERE PubID = ?",
                    "DELETE FROM Publications WHERE PubID = ?"
                };
                for (String sql : sqls) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, pubID);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void deleteIssue(int iid) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String[] sqls = {
                    "DELETE FROM Works_on_articles WHERE IID = ?",
                    "DELETE FROM Orders_issues WHERE IID = ?",
                    "DELETE FROM Articles WHERE IID = ?",
                    "DELETE FROM Made_of WHERE IID = ?",
                    "DELETE FROM Issues WHERE IID = ?"
                };
                for (String sql : sqls) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, iid);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
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
            
            String bookSQL = "SELECT p.Title, b.ISBN FROM Publications p JOIN Books b ON p.PubID = b.PubID WHERE p.topic LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(bookSQL)) {
                ps.setString(1, keyword);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) System.out.println("[Book] " + rs.getString("Title") + " (ISBN: " + rs.getString("ISBN") + ")");
            }

            String artSQL = "SELECT Title FROM Articles WHERE topic LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(artSQL)) {
                ps.setString(1, keyword);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) System.out.println("[Article] " + rs.getString("Title"));
            }
        }
    }

    public void findByDateRange(String start, String end) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("\n--- Content between " + start + " and " + end + " ---");
            
            String bookSQL = "SELECT Title, publication_date FROM Publications p JOIN Books b ON p.PubID = b.PubID WHERE b.publication_date BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(bookSQL)) {
                ps.setString(1, start);
                ps.setString(2, end);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) System.out.println("[Book] " + rs.getString("Title") + " Date: " + rs.getString("publication_date"));
            }

            String artSQL = "SELECT Title, written_date FROM Articles WHERE written_date BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(artSQL)) {
                ps.setString(1, start);
                ps.setString(2, end);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) System.out.println("[Article] " + rs.getString("Title") + " Date: " + rs.getString("written_date"));
            }
        }
    }

    public void findByAuthor(String name) throws SQLException {
        String keyword = "%" + name + "%";
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("\n--- Content by Author: " + name + " ---");
            
            String bookSQL = "SELECT p.Title FROM Workers w JOIN Works_on_books wb ON w.EID = wb.EID JOIN Publications p ON wb.PubID = p.PubID WHERE w.worker_name LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(bookSQL)) {
                ps.setString(1, keyword);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) System.out.println("[Book] " + rs.getString("Title"));
            }

            String artSQL = "SELECT a.Title FROM Workers w JOIN Works_on_articles wa ON w.EID = wa.EID JOIN Articles a ON wa.IID = a.IID AND wa.Title = a.Title WHERE w.worker_name LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(artSQL)) {
                ps.setString(1, keyword);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) System.out.println("[Article] " + rs.getString("Title"));
            }
        }
    }

    // --- 6. Enter payment; update when payment was claimed ---

    public void enterWorkerPayment(int pid, int eid, double amount, String type, String issueDate) throws SQLException {
        String paySQL = "INSERT INTO Worker_Payments (PID, amount, work_payment_type, pay_claim_date, pay_issue_date) VALUES (?, ?, ?, NULL, ?)";
        String getPaidSQL = "INSERT INTO Get_Paid (PID, EID) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psPay = conn.prepareStatement(paySQL);
                 PreparedStatement psLink = conn.prepareStatement(getPaidSQL)) {
                
                psPay.setInt(1, pid);
                psPay.setDouble(2, amount);
                psPay.setString(3, type);
                psPay.setString(4, issueDate);
                psPay.executeUpdate();

                psLink.setInt(1, pid);
                psLink.setInt(2, eid);
                psLink.executeUpdate();

                conn.commit();
                System.out.println("\n--- New Worker Payment Entered ---");
                System.out.printf("PID: %d | EID: %d | Amount: $%.2f | Type: %s | Issue Date: %s%n", pid, eid, amount, type, issueDate);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void updatePaymentClaimed(int pid, String claimDate) throws SQLException {
        String sql = "UPDATE Worker_Payments SET pay_claim_date = ? WHERE PID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, claimDate);
            pstmt.setInt(2, pid);
            int rows = pstmt.executeUpdate();
            if (rows > 0) System.out.println("Payment " + pid + " marked as claimed on " + claimDate);
        }
    }

    // --- 7. List unclaimed payments within a specified time window ---

    public void listUnclaimedPayments(String start, String end) throws SQLException {
        String sql = "SELECT wp.PID, w.worker_name, wp.amount, wp.pay_issue_date " +
                     "FROM Worker_Payments wp " +
                     "JOIN Get_Paid gp ON wp.PID = gp.PID " +
                     "JOIN Workers w ON gp.EID = w.EID " +
                     "WHERE wp.pay_claim_date IS NULL AND wp.pay_issue_date BETWEEN ? AND ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, start);
            pstmt.setString(2, end);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n--- Unclaimed Payments (" + start + " to " + end + ") ---");
            System.out.printf("%-5s %-20s %-10s %-15s%n", "PID", "Worker", "Amount", "Issued Date");
            while (rs.next()) {
                System.out.printf("%-5d %-20s $%-9.2f %-15s%n",
                    rs.getInt("PID"), rs.getString("worker_name"), 
                    rs.getDouble("amount"), rs.getString("pay_issue_date"));
            }
        }
    }

    // --- 8. Compare two issues by listing their associated articles ---

    public void compareIssues(int iid1, int iid2) throws SQLException {
        String sql = "SELECT a.IID, i.subtitle, a.Title, a.topic " +
                     "FROM Articles a JOIN Issues i ON a.IID = i.IID " +
                     "WHERE a.IID IN (?, ?) " +
                     "ORDER BY a.IID, a.Title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, iid1);
            pstmt.setInt(2, iid2);
            ResultSet rs = pstmt.executeQuery();

            int currentIID = -1;
            System.out.println("\n--- Issue Comparison ---");
            while (rs.next()) {
                int iid = rs.getInt("IID");
                if (iid != currentIID) {
                    currentIID = iid;
                    System.out.println("\nIssue ID: " + iid + " (" + rs.getString("subtitle") + ")");
                    System.out.printf("  %-30s %-20s%n", "Article Title", "Topic");
                    System.out.println("  " + "-".repeat(50));
                }
                System.out.printf("  %-30s %-20s%n", rs.getString("Title"), rs.getString("topic"));
            }
        }
    }
}
