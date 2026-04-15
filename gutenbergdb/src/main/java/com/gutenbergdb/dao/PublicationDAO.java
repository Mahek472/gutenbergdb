package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;

import java.sql.*;
import java.util.Scanner;

/**
 * PublicationDAO
 *
 * Handles editing and publishing operations for publications, books, periodicals,
 * worker assignments, and periodic publication table-of-contents updates.
 */
public class PublicationDAO {

    private final Scanner scanner = new Scanner(System.in);

    /**
     * Inserts a new periodical into Publications and Periodicals.
     */
    public void insertNewPeriodical() throws SQLException {
        System.out.println("\nInsert New Periodical");

        System.out.print("Enter PubID: ");
        int pubId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter Title: ");
        String title = scanner.nextLine();

        System.out.print("Enter Periodicity: ");
        String periodicity = scanner.nextLine();

        System.out.print("Enter Topic: ");
        String topic = scanner.nextLine();

        System.out.print("Enter Periodical Type: ");
        String type = scanner.nextLine();

        String checkSql = "SELECT COUNT(*) FROM Publications WHERE PubID = ?";
        String pubSql = "INSERT INTO Publications (PubID, Title, periodicity, topic) VALUES (?, ?, ?, ?)";
        String perSql = "INSERT INTO Periodicals (PubID, type) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 PreparedStatement pubStmt = conn.prepareStatement(pubSql);
                 PreparedStatement perStmt = conn.prepareStatement(perSql)) {

                checkStmt.setInt(1, pubId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Publication with PubID " + pubId + " already exists.");
                        conn.rollback();
                        return;
                    }
                }

                pubStmt.setInt(1, pubId);
                pubStmt.setString(2, title);
                pubStmt.setString(3, periodicity);
                pubStmt.setString(4, topic);
                int pubRows = pubStmt.executeUpdate();

                perStmt.setInt(1, pubId);
                perStmt.setString(2, type);
                int perRows = perStmt.executeUpdate();

                conn.commit();
                System.out.println("Periodical inserted successfully. Publications rows: " +
                        pubRows + ", Periodicals rows: " + perRows);

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Insert failed: " + e.getMessage());
            }
        }
    }

    /**
     * Inserts a new book into Publications and Books.
     */
    public void insertNewBook() throws SQLException {
        System.out.println("\nInsert New Book");

        System.out.print("Enter PubID: ");
        int pubId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter Title: ");
        String title = scanner.nextLine();

        System.out.print("Enter Topic: ");
        String topic = scanner.nextLine();

        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine();

        System.out.print("Enter Full Text: ");
        String fullText = scanner.nextLine();

        System.out.print("Enter Edition Number: ");
        int editionNumber = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter Publication Date (YYYY-MM-DD): ");
        String publicationDate = scanner.nextLine();

        System.out.print("Enter Written Date (YYYY-MM-DD): ");
        String writtenDate = scanner.nextLine();

        String checkSql = "SELECT COUNT(*) FROM Publications WHERE PubID = ?";
        String pubSql = "INSERT INTO Publications (PubID, Title, periodicity, topic) VALUES (?, ?, ?, ?)";
        String bookSql = "INSERT INTO Books (PubID, ISBN, full_text, edition_number, publication_date, written_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 PreparedStatement pubStmt = conn.prepareStatement(pubSql);
                 PreparedStatement bookStmt = conn.prepareStatement(bookSql)) {

                checkStmt.setInt(1, pubId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Publication with PubID " + pubId + " already exists.");
                        conn.rollback();
                        return;
                    }
                }

                pubStmt.setInt(1, pubId);
                pubStmt.setString(2, title);
                pubStmt.setNull(3, Types.VARCHAR);
                pubStmt.setString(4, topic);
                int pubRows = pubStmt.executeUpdate();

                bookStmt.setInt(1, pubId);
                bookStmt.setString(2, isbn);
                bookStmt.setString(3, fullText);
                bookStmt.setInt(4, editionNumber);
                bookStmt.setDate(5, Date.valueOf(publicationDate));
                bookStmt.setDate(6, Date.valueOf(writtenDate));
                int bookRows = bookStmt.executeUpdate();

                conn.commit();
                System.out.println("Book inserted successfully. Publications rows: " +
                        pubRows + ", Books rows: " + bookRows);

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Insert failed: " + e.getMessage());
            }
        }
    }

    /**
     * Shows book details for a given PubID.
     */
    public void showBookDetails() throws SQLException {
        System.out.println("\nShow Book Details");
        System.out.print("Enter PubID: ");
        int pubId = Integer.parseInt(scanner.nextLine());
        showBookDetailsById(pubId);
    }

    private void showBookDetailsById(int pubId) throws SQLException {
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
                    System.out.println("Book not found for PubID: " + pubId);
                }
            }
        }
    

    /**
     * Shows periodical/publication details for a given PubID.
     */
    public void showPeriodical() throws SQLException {
        System.out.println("\nShow Periodical Details");
        System.out.print("Enter PubID: ");
        int pubId = Integer.parseInt(scanner.nextLine());
        showPeriodicalById(pubId);
    }

    private void showPeriodicalById(int pubId) throws SQLException {
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
                    System.out.println("Publication not found for PubID: " + pubId);
                }
            }
        }
    }

    /**
     * Updates periodical fields in Publications.
     */
    public void updatePeriodical() throws SQLException {
        System.out.println("\nUpdate Periodical ");

        System.out.print("Enter PubID to update: ");
        int pubId = Integer.parseInt(scanner.nextLine());

        System.out.println("\nCurrent details:");
        showPeriodicalById(pubId);

        System.out.print("Enter new periodicity: ");
        String periodicity = scanner.nextLine();

        System.out.print("Enter new topic: ");
        String topic = scanner.nextLine();

        String sql = "UPDATE Publications SET periodicity = ?, topic = ? WHERE PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, periodicity);
            stmt.setString(2, topic);
            stmt.setInt(3, pubId);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.out.println("Publication not found for PubID: " + pubId);
            } else {
                System.out.println("Periodical updated successfully.");
                System.out.println("\nUpdated details:");
                showPeriodicalById(pubId);
            }
        }
    }

    /**
     * Updates book edition number.
     */
    public void updateBookEdition() throws SQLException {
        System.out.println("\nUpdate Book Edition ");

        System.out.print("Enter PubID of the book: ");
        int pubId = Integer.parseInt(scanner.nextLine());

        System.out.println("\nCurrent details:");
        showBookDetailsById(pubId);

        System.out.print("Enter new edition number: ");
        int editionNumber = Integer.parseInt(scanner.nextLine());

        String sql = "UPDATE Books SET edition_number = ? WHERE PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, editionNumber);
            stmt.setInt(2, pubId);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.out.println("Book not found for PubID: " + pubId);
            } else {
                System.out.println("Book edition updated successfully.");
                System.out.println("\nUpdated details:");
                showBookDetailsById(pubId);
            }
        }
    }

    /**
     * Assigns a worker/editor/designer to a publication through Works_on_books.
     */
    public void assignWorkerToPublication() throws SQLException {
        System.out.println("\nAssign Worker To Publication");

        System.out.print("Enter EID: ");
        int eid = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter PubID: ");
        int pubId = Integer.parseInt(scanner.nextLine());

        String checkSql = "SELECT COUNT(*) FROM Works_on_books WHERE EID = ? AND PubID = ?";
        String insertSql = "INSERT INTO Works_on_books (EID, PubID) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setInt(1, eid);
            checkStmt.setInt(2, pubId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Assignment already exists for EID " + eid + " and PubID " + pubId);
                    return;
                }
            }

            insertStmt.setInt(1, eid);
            insertStmt.setInt(2, pubId);

            int rows = insertStmt.executeUpdate();
            if (rows == 0) {
                System.out.println("Assignment failed.");
            } else {
                System.out.println("Worker assigned successfully.");
                showWorkersAssignedToPublicationById(pubId);
            }
        } catch (SQLException e) {
            System.out.println("Assignment failed: " + e.getMessage());
        }
    }

    /**
     * Removes a worker/editor/designer from a publication.
     */
    public void removeWorkerFromPublication() throws SQLException {
        System.out.println("\nRemove Worker From Publication");

        System.out.print("Enter EID: ");
        int eid = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter PubID: ");
        int pubId = Integer.parseInt(scanner.nextLine());

        String deleteSql = "DELETE FROM Works_on_books WHERE EID = ? AND PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {

            stmt.setInt(1, eid);
            stmt.setInt(2, pubId);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.out.println("Assignment not found for EID " + eid + " and PubID " + pubId);
            } else {
                System.out.println("Worker removed successfully.");
                showWorkersAssignedToPublicationById(pubId);
            }
        }
    }

    /**
     * Shows all workers assigned to a publication.
     */
    public void showWorkersAssignedToPublication() throws SQLException {
        System.out.println("\nShow Workers Assigned To Publication");
        System.out.print("Enter PubID: ");
        int pubId = Integer.parseInt(scanner.nextLine());
        showWorkersAssignedToPublicationById(pubId);
    }

    private void showWorkersAssignedToPublicationById(int pubId) throws SQLException {
        String sql = "SELECT w.EID, w.worker_name, w.worker_type, p.PubID, p.Title " +
                     "FROM Works_on_books wob " +
                     "JOIN Workers w ON wob.EID = w.EID " +
                     "JOIN Publications p ON wob.PubID = p.PubID " +
                     "WHERE wob.PubID = ? " +
                     "ORDER BY w.EID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pubId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nWorkers Assigned To Publication");
                System.out.printf("%-6s %-20s %-20s %-6s %-25s%n",
                        "EID", "Worker Name", "Worker Type", "PubID", "Title");
                System.out.println("-".repeat(85));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-6d %-20s %-20s %-6d %-25s%n",
                            rs.getInt("EID"),
                            rs.getString("worker_name"),
                            rs.getString("worker_type"),
                            rs.getInt("PubID"),
                            rs.getString("Title"));
                }

                if (!found) {
                    System.out.println("No workers assigned to PubID: " + pubId);
                }
            }
        }
    }

    /**
     * Shows all publications assigned to a worker/editor.
     * Combines direct publication assignments and indirect article-based assignments.
     */
    public void showPublicationsForWorker() throws SQLException {
        System.out.println("\nShow Publications For Worker");
        System.out.print("Enter EID: ");
        int eid = Integer.parseInt(scanner.nextLine());

        String sql =
                "SELECT w.EID, w.worker_name, p.PubID, p.Title, p.periodicity, p.topic, " +
                "'direct (book/pub)' AS assigned_via " +
                "FROM Works_on_books wob " +
                "JOIN Workers w ON wob.EID = w.EID " +
                "JOIN Publications p ON wob.PubID = p.PubID " +
                "WHERE wob.EID = ? " +
                "UNION " +
                "SELECT w.EID, w.worker_name, p.PubID, p.Title, p.periodicity, p.topic, " +
                "'via article in issue' AS assigned_via " +
                "FROM Works_on_articles woa " +
                "JOIN Workers w ON woa.EID = w.EID " +
                "JOIN Issues i ON woa.IID = i.IID " +
                "JOIN Made_of mo ON i.IID = mo.IID " +
                "JOIN Publications p ON mo.PubID = p.PubID " +
                "WHERE woa.EID = ? " +
                "ORDER BY PubID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, eid);
            stmt.setInt(2, eid);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nPublications Assigned To Worker");
                System.out.printf("%-6s %-20s %-6s %-25s %-15s %-20s %-22s%n",
                        "EID", "Worker Name", "PubID", "Title", "Periodicity", "Topic", "Assigned Via");
                System.out.println("-".repeat(130));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-6d %-20s %-6d %-25s %-15s %-20s %-22s%n",
                            rs.getInt("EID"),
                            rs.getString("worker_name"),
                            rs.getInt("PubID"),
                            rs.getString("Title"),
                            rs.getString("periodicity"),
                            rs.getString("topic"),
                            rs.getString("assigned_via"));
                }

                if (!found) {
                    System.out.println("No publications found for EID: " + eid);
                }
            }
        }
    }

    /**
     * Adds a new issue and a new article to a periodic publication.
     */
    public void addArticleToPeriodicPublication() throws SQLException {
        System.out.println("\nAdd Article To Periodic Publication");

        System.out.print("Enter Publication PubID: ");
        int pubId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter new Issue ID (IID): ");
        int iid = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter issue subtitle: ");
        String subtitle = scanner.nextLine();

        System.out.print("Enter issue publication date (YYYY-MM-DD): ");
        String issuePubDate = scanner.nextLine();

        System.out.print("Enter article title: ");
        String articleTitle = scanner.nextLine();

        System.out.print("Enter article full text: ");
        String fullText = scanner.nextLine();

        System.out.print("Enter article written date (YYYY-MM-DD): ");
        String writtenDate = scanner.nextLine();

        System.out.print("Enter article topic: ");
        String topic = scanner.nextLine();

        String issueCheckSql = "SELECT COUNT(*) FROM Issues WHERE IID = ?";
        String issueInsertSql = "INSERT INTO Issues (IID, subtitle, Publication_Date) VALUES (?, ?, ?)";
        String madeOfInsertSql = "INSERT INTO Made_of (PubID, IID) VALUES (?, ?)";
        String articleInsertSql = "INSERT INTO Articles (IID, Title, full_text, written_date, topic) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement issueCheckStmt = conn.prepareStatement(issueCheckSql);
                 PreparedStatement issueInsertStmt = conn.prepareStatement(issueInsertSql);
                 PreparedStatement madeOfInsertStmt = conn.prepareStatement(madeOfInsertSql);
                 PreparedStatement articleInsertStmt = conn.prepareStatement(articleInsertSql)) {

                issueCheckStmt.setInt(1, iid);
                try (ResultSet rs = issueCheckStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Issue with IID " + iid + " already exists.");
                        conn.rollback();
                        return;
                    }
                }

                issueInsertStmt.setInt(1, iid);
                issueInsertStmt.setString(2, subtitle);
                issueInsertStmt.setDate(3, Date.valueOf(issuePubDate));
                int issueRows = issueInsertStmt.executeUpdate();

                madeOfInsertStmt.setInt(1, pubId);
                madeOfInsertStmt.setInt(2, iid);
                int madeOfRows = madeOfInsertStmt.executeUpdate();

                articleInsertStmt.setInt(1, iid);
                articleInsertStmt.setString(2, articleTitle);
                articleInsertStmt.setString(3, fullText);
                articleInsertStmt.setDate(4, Date.valueOf(writtenDate));
                articleInsertStmt.setString(5, topic);
                int articleRows = articleInsertStmt.executeUpdate();

                conn.commit();
                System.out.println("Article added successfully. Issues rows: " + issueRows +
                        ", Made_of rows: " + madeOfRows + ", Articles rows: " + articleRows);

                showTableOfContentsByPublication(pubId);

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Add article failed: " + e.getMessage());
            }
        }
    }

    /**
     * Deletes an article from a periodic publication in reverse dependency order.
     */
    public void deleteArticleFromPeriodicPublication() throws SQLException {
        System.out.println("\n Delete Article From Periodic Publication ");

        System.out.print("Enter Publication PubID: ");
        int pubId = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter Issue ID (IID): ");
        int iid = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter Article Title: ");
        String title = scanner.nextLine();

        String deleteAssignmentsSql = "DELETE FROM Works_on_articles WHERE IID = ? AND Title = ?";
        String deleteArticleSql = "DELETE FROM Articles WHERE IID = ? AND Title = ?";
        String deleteMadeOfSql = "DELETE FROM Made_of WHERE IID = ? AND PubID = ?";
        String deleteIssueSql = "DELETE FROM Issues WHERE IID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteAssignmentsStmt = conn.prepareStatement(deleteAssignmentsSql);
                 PreparedStatement deleteArticleStmt = conn.prepareStatement(deleteArticleSql);
                 PreparedStatement deleteMadeOfStmt = conn.prepareStatement(deleteMadeOfSql);
                 PreparedStatement deleteIssueStmt = conn.prepareStatement(deleteIssueSql)) {

                deleteAssignmentsStmt.setInt(1, iid);
                deleteAssignmentsStmt.setString(2, title);
                int assignmentRows = deleteAssignmentsStmt.executeUpdate();

                deleteArticleStmt.setInt(1, iid);
                deleteArticleStmt.setString(2, title);
                int articleRows = deleteArticleStmt.executeUpdate();

                deleteMadeOfStmt.setInt(1, iid);
                deleteMadeOfStmt.setInt(2, pubId);
                int madeOfRows = deleteMadeOfStmt.executeUpdate();

                deleteIssueStmt.setInt(1, iid);
                int issueRows = deleteIssueStmt.executeUpdate();

                if (articleRows == 0 && madeOfRows == 0 && issueRows == 0) {
                    conn.rollback();
                    System.out.println("No matching article/issue/publication record found.");
                    return;
                }

                conn.commit();
                System.out.println("Delete completed. Works_on_articles rows: " + assignmentRows +
                        ", Articles rows: " + articleRows +
                        ", Made_of rows: " + madeOfRows +
                        ", Issues rows: " + issueRows);

                showTableOfContentsByPublication(pubId);

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Delete article failed: " + e.getMessage());
            }
        }
    }

    /**
     * Shows the table of contents for a publication.
     */
    public void showTableOfContents() throws SQLException {
        System.out.println("\nShow Table Of Contents ");
        System.out.print("Enter Publication PubID: ");
        int pubId = Integer.parseInt(scanner.nextLine());
        showTableOfContentsByPublication(pubId);
    }

    private void showTableOfContentsByPublication(int pubId) throws SQLException {
        String sql = "SELECT p.Title AS publication_title, i.IID, i.subtitle, i.Publication_Date, " +
                     "a.Title AS article_title, a.topic " +
                     "FROM Publications p " +
                     "JOIN Made_of mo ON p.PubID = mo.PubID " +
                     "JOIN Issues i ON mo.IID = i.IID " +
                     "LEFT JOIN Articles a ON i.IID = a.IID " +
                     "WHERE p.PubID = ? " +
                     "ORDER BY i.IID, a.Title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pubId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nTable Of Contents");
                System.out.printf("%-25s %-6s %-30s %-15s %-30s %-20s%n",
                        "Publication", "IID", "Issue Subtitle", "Issue Date", "Article", "Topic");
                System.out.println("-".repeat(135));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-25s %-6d %-30s %-15s %-30s %-20s%n",
                            rs.getString("publication_title"),
                            rs.getInt("IID"),
                            rs.getString("subtitle"),
                            rs.getDate("Publication_Date"),
                            rs.getString("article_title"),
                            rs.getString("topic"));
                }

                if (!found) {
                    System.out.println("No table of contents found for PubID: " + pubId);
                }
            }
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
