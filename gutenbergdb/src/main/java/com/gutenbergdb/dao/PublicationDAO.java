package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;

import java.sql.*;
import java.util.Scanner;

public class PublicationDAO {

    private final Scanner scanner = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // 1. Insert New Periodical
    // -------------------------------------------------------------------------
    public void insertNewPeriodical() throws SQLException {
        System.out.println("\nInsert New Periodical");

        System.out.print("Enter PubID (e.g. PUB004): ");
        String pubId = scanner.nextLine().trim();

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

                checkStmt.setString(1, pubId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Publication with PubID " + pubId + " already exists.");
                        conn.rollback();
                        return;
                    }
                }

                pubStmt.setString(1, pubId);
                pubStmt.setString(2, title);
                pubStmt.setString(3, periodicity);
                pubStmt.setString(4, topic);
                int pubRows = pubStmt.executeUpdate();

                perStmt.setString(1, pubId);
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

    // -------------------------------------------------------------------------
    // 2. Insert New Book
    // Inserts into Publications and Books
    // -------------------------------------------------------------------------
    public void insertNewBook() throws SQLException {
        System.out.println("\nInsert New Book");

        System.out.print("Enter PubID (e.g. PUB004): ");
        String pubId = scanner.nextLine().trim();

        System.out.print("Enter Publication Title: ");
        String publicationTitle = scanner.nextLine();

        System.out.print("Enter Topic: ");
        String topic = scanner.nextLine();

        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();

        System.out.print("Enter Book Title: ");
        String bookTitle = scanner.nextLine();

        System.out.print("Enter Full Text (or leave blank for NULL): ");
        String fullText = scanner.nextLine();

        System.out.print("Enter Edition Number: ");
        int editionNumber = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter Publication Date (YYYY-MM-DD): ");
        String publicationDate = scanner.nextLine();

        String checkPubSql = "SELECT COUNT(*) FROM Publications WHERE PubID = ?";
        String checkBookSql = "SELECT COUNT(*) FROM Books WHERE ISBN = ?";
        String pubSql = "INSERT INTO Publications (PubID, Title, periodicity, topic) VALUES (?, ?, ?, ?)";
        String bookSql = "INSERT INTO Books (PubID, ISBN, title, full_text, edition_number, publication_date) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement checkPubStmt = conn.prepareStatement(checkPubSql);
                 PreparedStatement checkBookStmt = conn.prepareStatement(checkBookSql);
                 PreparedStatement pubStmt = conn.prepareStatement(pubSql);
                 PreparedStatement bookStmt = conn.prepareStatement(bookSql)) {

                checkPubStmt.setString(1, pubId);
                try (ResultSet rs = checkPubStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Publication with PubID " + pubId + " already exists.");
                        conn.rollback();
                        return;
                    }
                }

                checkBookStmt.setString(1, isbn);
                try (ResultSet rs = checkBookStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Book with ISBN " + isbn + " already exists.");
                        conn.rollback();
                        return;
                    }
                }

                pubStmt.setString(1, pubId);
                pubStmt.setString(2, publicationTitle);
                pubStmt.setNull(3, Types.VARCHAR); // books have no periodicity
                pubStmt.setString(4, topic);
                int pubRows = pubStmt.executeUpdate();

                bookStmt.setString(1, pubId);
                bookStmt.setString(2, isbn);
                bookStmt.setString(3, bookTitle);

                if (fullText == null || fullText.trim().isEmpty()) {
                    bookStmt.setNull(4, Types.LONGVARCHAR);
                } else {
                    bookStmt.setString(4, fullText);
                }

                bookStmt.setInt(5, editionNumber);
                bookStmt.setDate(6, Date.valueOf(publicationDate));
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

    // -------------------------------------------------------------------------
    // 3. Show Book Details
    // -------------------------------------------------------------------------
    public void showBookDetails() throws SQLException {
        System.out.println("\nShow Book Details");
        System.out.print("Enter PubID: ");
        String pubId = scanner.nextLine().trim();
        showBookDetailsByPubId(pubId);
    }

    private void showBookDetailsByPubId(String pubId) throws SQLException {
        String sql = "SELECT p.PubID, p.Title AS publication_title, p.topic, " +
                     "b.ISBN, b.title AS book_title, b.edition_number, b.publication_date " +
                     "FROM Publications p " +
                     "JOIN Books b ON p.PubID = b.PubID " +
                     "WHERE p.PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pubId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nBook Details");
                System.out.printf("%-10s %-30s %-35s %-20s %-22s %-10s %-15s%n",
                        "PubID", "Publication", "Book Title", "Topic", "ISBN", "Edition", "Pub Date");
                System.out.println("-".repeat(150));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-10s %-30s %-35s %-20s %-22s %-10d %-15s%n",
                            rs.getString("PubID"),
                            rs.getString("publication_title"),
                            rs.getString("book_title"),
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
    }

    private void showBookDetailsByIsbn(String isbn) throws SQLException {
        String sql = "SELECT p.PubID, p.Title AS publication_title, p.topic, " +
                     "b.ISBN, b.title AS book_title, b.edition_number, b.publication_date " +
                     "FROM Publications p " +
                     "JOIN Books b ON p.PubID = b.PubID " +
                     "WHERE b.ISBN = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nBook Details");
                System.out.printf("%-10s %-30s %-35s %-20s %-22s %-10s %-15s%n",
                        "PubID", "Publication", "Book Title", "Topic", "ISBN", "Edition", "Pub Date");
                System.out.println("-".repeat(150));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-10s %-30s %-35s %-20s %-22s %-10d %-15s%n",
                            rs.getString("PubID"),
                            rs.getString("publication_title"),
                            rs.getString("book_title"),
                            rs.getString("topic"),
                            rs.getString("ISBN"),
                            rs.getInt("edition_number"),
                            rs.getDate("publication_date"));
                }

                if (!found) {
                    System.out.println("Book not found for ISBN: " + isbn);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 4. Show Periodical Details
    // -------------------------------------------------------------------------
    public void showPeriodical() throws SQLException {
        System.out.println("\nShow Periodical Details");
        System.out.print("Enter PubID: ");
        String pubId = scanner.nextLine().trim();
        showPeriodicalById(pubId);
    }

    private void showPeriodicalById(String pubId) throws SQLException {
        String sql = "SELECT p.PubID, p.Title, p.periodicity, p.topic, pr.type " +
                     "FROM Publications p " +
                     "JOIN Periodicals pr ON p.PubID = pr.PubID " +
                     "WHERE p.PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pubId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nPeriodical Details");
                System.out.printf("%-8s %-30s %-15s %-20s %-15s%n",
                        "PubID", "Title", "Periodicity", "Topic", "Type");
                System.out.println("-".repeat(95));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-8s %-30s %-15s %-20s %-15s%n",
                            rs.getString("PubID"),
                            rs.getString("Title"),
                            rs.getString("periodicity"),
                            rs.getString("topic"),
                            rs.getString("type"));
                }

                if (!found) {
                    System.out.println("Periodical not found for PubID: " + pubId);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 5. Update Periodical
    // -------------------------------------------------------------------------
    public void updatePeriodical() throws SQLException {
        System.out.println("\nUpdate Periodical");

        System.out.print("Enter PubID to update: ");
        String pubId = scanner.nextLine().trim();

        System.out.println("\nCurrent details:");
        showPeriodicalById(pubId);

        System.out.print("Enter new periodicity: ");
        String periodicity = scanner.nextLine();

        System.out.print("Enter new topic: ");
        String topic = scanner.nextLine();

        System.out.print("Enter new type: ");
        String type = scanner.nextLine();

        String pubSql = "UPDATE Publications SET periodicity = ?, topic = ? WHERE PubID = ?";
        String perSql = "UPDATE Periodicals SET type = ? WHERE PubID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pubStmt = conn.prepareStatement(pubSql);
                 PreparedStatement perStmt = conn.prepareStatement(perSql)) {

                pubStmt.setString(1, periodicity);
                pubStmt.setString(2, topic);
                pubStmt.setString(3, pubId);

                perStmt.setString(1, type);
                perStmt.setString(2, pubId);

                int pubRows = pubStmt.executeUpdate();
                int perRows = perStmt.executeUpdate();

                if (pubRows == 0 || perRows == 0) {
                    conn.rollback();
                    System.out.println("Periodical not found for PubID: " + pubId);
                } else {
                    conn.commit();
                    System.out.println("Periodical updated successfully.");
                    System.out.println("\nUpdated details:");
                    showPeriodicalById(pubId);
                }

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Update failed: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // 6. Update Book Edition
    // -------------------------------------------------------------------------
    public void updateBookEdition() throws SQLException {
        System.out.println("\nUpdate Book Edition");

        System.out.print("Enter PubID of the book: ");
        String pubId = scanner.nextLine().trim();

        System.out.println("\nCurrent details:");
        showBookDetailsByPubId(pubId);

        System.out.print("Enter new edition number: ");
        int editionNumber = Integer.parseInt(scanner.nextLine());

        String sql = "UPDATE Books SET edition_number = ? WHERE PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, editionNumber);
            stmt.setString(2, pubId);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.out.println("Book not found for PubID: " + pubId);
            } else {
                System.out.println("Book edition updated successfully.");
                System.out.println("\nUpdated details:");
                showBookDetailsByPubId(pubId);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 7. Assign Worker To Publication
    // -------------------------------------------------------------------------
    public void assignWorkerToPublication() throws SQLException {
        System.out.println("\nAssign Worker To Publication");

        System.out.print("Enter EID (e.g. E008): ");
        String eid = scanner.nextLine().trim();

        System.out.print("Enter PubID (e.g. PUB001): ");
        String pubId = scanner.nextLine().trim();

        String checkSql = "SELECT COUNT(*) FROM Edits WHERE EID = ? AND PubID = ?";
        String insertSql = "INSERT INTO Edits (EID, PubID) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setString(1, eid);
            checkStmt.setString(2, pubId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Assignment already exists for EID " + eid + " and PubID " + pubId);
                    return;
                }
            }

            insertStmt.setString(1, eid);
            insertStmt.setString(2, pubId);

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

    // -------------------------------------------------------------------------
    // 8. Remove Worker From Publication
    // -------------------------------------------------------------------------
    public void removeWorkerFromPublication() throws SQLException {
        System.out.println("\nRemove Worker From Publication");

        System.out.print("Enter EID: ");
        String eid = scanner.nextLine().trim();

        System.out.print("Enter PubID: ");
        String pubId = scanner.nextLine().trim();

        String deleteSql = "DELETE FROM Edits WHERE EID = ? AND PubID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {

            stmt.setString(1, eid);
            stmt.setString(2, pubId);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                System.out.println("Assignment not found for EID " + eid + " and PubID " + pubId);
            } else {
                System.out.println("Worker removed successfully.");
                showWorkersAssignedToPublicationById(pubId);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 9. Show Workers Assigned To Publication
    // -------------------------------------------------------------------------
    public void showWorkersAssignedToPublication() throws SQLException {
        System.out.println("\nShow Workers Assigned To Publication");
        System.out.print("Enter PubID: ");
        String pubId = scanner.nextLine().trim();
        showWorkersAssignedToPublicationById(pubId);
    }

    private void showWorkersAssignedToPublicationById(String pubId) throws SQLException {
        String sql = "SELECT w.EID, w.worker_name, w.worker_type, p.PubID, p.Title " +
                     "FROM Edits e " +
                     "JOIN Workers w ON e.EID = w.EID " +
                     "JOIN Publications p ON e.PubID = p.PubID " +
                     "WHERE e.PubID = ? " +
                     "ORDER BY w.EID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pubId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nWorkers Assigned To Publication");
                System.out.printf("%-8s %-25s %-15s %-8s %-35s%n",
                        "EID", "Worker Name", "Worker Type", "PubID", "Title");
                System.out.println("-".repeat(100));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-8s %-25s %-15s %-8s %-35s%n",
                            rs.getString("EID"),
                            rs.getString("worker_name"),
                            rs.getString("worker_type"),
                            rs.getString("PubID"),
                            rs.getString("Title"));
                }

                if (!found) {
                    System.out.println("No workers assigned to PubID: " + pubId);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 10. Show Publications For Worker
    // -------------------------------------------------------------------------
    public void showPublicationsForWorker() throws SQLException {
        System.out.println("\nShow Publications For Worker");
        System.out.print("Enter EID: ");
        String eid = scanner.nextLine().trim();

        String sql =
                "SELECT w.EID, w.worker_name, p.PubID, p.Title, p.periodicity, p.topic, " +
                "'direct via edits' AS assigned_via " +
                "FROM Edits e " +
                "JOIN Workers w ON e.EID = w.EID " +
                "JOIN Publications p ON e.PubID = p.PubID " +
                "WHERE e.EID = ? " +
                "UNION " +
                "SELECT w.EID, w.worker_name, p.PubID, p.Title, p.periodicity, p.topic, " +
                "'via article in issue' AS assigned_via " +
                "FROM Works_on_articles woa " +
                "JOIN Workers w ON woa.EID = w.EID " +
                "JOIN Contains_articles ca ON woa.AID = ca.AID " +
                "JOIN Issues i ON ca.IID = i.IID " +
                "JOIN Publications p ON i.PubID = p.PubID " +
                "WHERE woa.EID = ? " +
                "ORDER BY PubID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, eid);
            stmt.setString(2, eid);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nPublications Assigned To Worker");
                System.out.printf("%-8s %-20s %-8s %-35s %-15s %-20s %-22s%n",
                        "EID", "Worker Name", "PubID", "Title", "Periodicity", "Topic", "Assigned Via");
                System.out.println("-".repeat(140));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-8s %-20s %-8s %-35s %-15s %-20s %-22s%n",
                            rs.getString("EID"),
                            rs.getString("worker_name"),
                            rs.getString("PubID"),
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

    // -------------------------------------------------------------------------
    // 11. Show Table Of Contents
    // -------------------------------------------------------------------------
    public void showTableOfContents() throws SQLException {
        System.out.println("\nShow Table Of Contents");
        System.out.print("Enter Publication PubID: ");
        String pubId = scanner.nextLine().trim();
        showTableOfContentsByPublication(pubId);
    }

    private void showTableOfContentsByPublication(String pubId) throws SQLException {
        String sql = "SELECT p.Title AS publication_title, i.IID, i.Subtitle, i.publication_date, " +
                     "a.AID, a.Title AS article_title, a.topic " +
                     "FROM Publications p " +
                     "JOIN Issues i ON p.PubID = i.PubID " +
                     "LEFT JOIN Contains_articles ca ON i.IID = ca.IID " +
                     "LEFT JOIN Articles a ON ca.AID = a.AID " +
                     "WHERE p.PubID = ? " +
                     "ORDER BY i.IID, a.AID";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pubId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nTable Of Contents");
                System.out.printf("%-30s %-8s %-35s %-15s %-8s %-30s %-20s%n",
                        "Publication", "IID", "Issue Subtitle", "Issue Date", "AID", "Article", "Topic");
                System.out.println("-".repeat(160));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-30s %-8s %-35s %-15s %-8s %-30s %-20s%n",
                            rs.getString("publication_title"),
                            rs.getString("IID"),
                            rs.getString("Subtitle"),
                            rs.getDate("publication_date"),
                            rs.getString("AID"),
                            rs.getString("article_title"),
                            rs.getString("topic"));
                }

                if (!found) {
                    System.out.println("No table of contents found for PubID: " + pubId);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 12. Add Article To Periodic Publication
    // Adds article to an existing issue
    // -------------------------------------------------------------------------
    public void addArticleToPeriodicPublication() throws SQLException {
        System.out.println("\nAdd Article To Periodic Publication");

        System.out.print("Enter Publication PubID: ");
        String pubId = scanner.nextLine().trim();

        System.out.print("Enter existing Issue ID (IID): ");
        String iid = scanner.nextLine().trim();

        System.out.print("Enter new Article ID (AID): ");
        String aid = scanner.nextLine().trim();

        System.out.print("Enter article title: ");
        String articleTitle = scanner.nextLine();

        System.out.print("Enter article full text: ");
        String fullText = scanner.nextLine();

        System.out.print("Enter article written date (YYYY-MM-DD): ");
        String writtenDate = scanner.nextLine();

        System.out.print("Enter article topic: ");
        String topic = scanner.nextLine();

        String issueCheckSql = "SELECT COUNT(*) FROM Issues WHERE IID = ? AND PubID = ?";
        String articleCheckSql = "SELECT COUNT(*) FROM Articles WHERE AID = ?";
        String articleInsertSql = "INSERT INTO Articles (AID, Title, full_text, written_date, topic) VALUES (?, ?, ?, ?, ?)";
        String containsInsertSql = "INSERT INTO Contains_articles (IID, AID) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement issueCheckStmt = conn.prepareStatement(issueCheckSql);
                 PreparedStatement articleCheckStmt = conn.prepareStatement(articleCheckSql);
                 PreparedStatement articleInsertStmt = conn.prepareStatement(articleInsertSql);
                 PreparedStatement containsInsertStmt = conn.prepareStatement(containsInsertSql)) {

                issueCheckStmt.setString(1, iid);
                issueCheckStmt.setString(2, pubId);
                try (ResultSet rs = issueCheckStmt.executeQuery()) {
                    if (!(rs.next() && rs.getInt(1) > 0)) {
                        System.out.println("Issue " + iid + " does not exist for publication " + pubId + ".");
                        conn.rollback();
                        return;
                    }
                }

                articleCheckStmt.setString(1, aid);
                try (ResultSet rs = articleCheckStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Article with AID " + aid + " already exists.");
                        conn.rollback();
                        return;
                    }
                }

                articleInsertStmt.setString(1, aid);
                articleInsertStmt.setString(2, articleTitle);
                articleInsertStmt.setString(3, fullText);
                articleInsertStmt.setDate(4, Date.valueOf(writtenDate));
                articleInsertStmt.setString(5, topic);
                int articleRows = articleInsertStmt.executeUpdate();

                containsInsertStmt.setString(1, iid);
                containsInsertStmt.setString(2, aid);
                int containsRows = containsInsertStmt.executeUpdate();

                conn.commit();
                System.out.println("Article added successfully. Articles rows: " + articleRows +
                        ", Contains_articles rows: " + containsRows);

                showTableOfContentsByPublication(pubId);

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Add article failed: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // 13. Delete Article From Periodic Publication
    // -------------------------------------------------------------------------
    public void deleteArticleFromPeriodicPublication() throws SQLException {
        System.out.println("\nDelete Article From Periodic Publication");

        System.out.print("Enter Issue ID (IID): ");
        String iid = scanner.nextLine().trim();

        System.out.print("Enter Article ID (AID): ");
        String aid = scanner.nextLine().trim();

        String pubIdLookupSql = "SELECT PubID FROM Issues WHERE IID = ?";
        String deleteAssignmentSql = "DELETE FROM Works_on_articles WHERE AID = ?";
        String deleteContainsSql = "DELETE FROM Contains_articles WHERE IID = ? AND AID = ?";
        String deleteArticleSql = "DELETE FROM Articles WHERE AID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pubIdStmt = conn.prepareStatement(pubIdLookupSql);
                 PreparedStatement deleteAssignmentStmt = conn.prepareStatement(deleteAssignmentSql);
                 PreparedStatement deleteContainsStmt = conn.prepareStatement(deleteContainsSql);
                 PreparedStatement deleteArticleStmt = conn.prepareStatement(deleteArticleSql)) {

                String pubId = null;
                pubIdStmt.setString(1, iid);
                try (ResultSet rs = pubIdStmt.executeQuery()) {
                    if (rs.next()) {
                        pubId = rs.getString("PubID");
                    }
                }

                deleteAssignmentStmt.setString(1, aid);
                int assignmentRows = deleteAssignmentStmt.executeUpdate();

                deleteContainsStmt.setString(1, iid);
                deleteContainsStmt.setString(2, aid);
                int containsRows = deleteContainsStmt.executeUpdate();

                deleteArticleStmt.setString(1, aid);
                int articleRows = deleteArticleStmt.executeUpdate();

                if (containsRows == 0 && articleRows == 0) {
                    conn.rollback();
                    System.out.println("No matching article/issue record found.");
                    return;
                }

                conn.commit();
                System.out.println("Delete completed. Works_on_articles rows: " + assignmentRows +
                        ", Contains_articles rows: " + containsRows +
                        ", Articles rows: " + articleRows);

                if (pubId != null) {
                    showTableOfContentsByPublication(pubId);
                }

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Delete article failed: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // 14. Add Chapter To Book
    // -------------------------------------------------------------------------
    public void addChapterToBook() throws SQLException {
        System.out.println("\nAdd Chapter To Book");

        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();

        System.out.print("Enter Chapter Title: ");
        String chapterTitle = scanner.nextLine();

        System.out.print("Enter Chapter Topic: ");
        String topic = scanner.nextLine();

        System.out.print("Enter Written Date (YYYY-MM-DD): ");
        String writtenDate = scanner.nextLine();

        System.out.print("Enter Full Text: ");
        String fullText = scanner.nextLine();

        String bookCheckSql = "SELECT COUNT(*) FROM Books WHERE ISBN = ?";
        String chapterCheckSql = "SELECT COUNT(*) FROM Chapters WHERE ISBN = ? AND title = ?";
        String insertSql = "INSERT INTO Chapters (ISBN, title, topic, written_date, full_text) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement bookCheckStmt = conn.prepareStatement(bookCheckSql);
                 PreparedStatement chapterCheckStmt = conn.prepareStatement(chapterCheckSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                bookCheckStmt.setString(1, isbn);
                try (ResultSet rs = bookCheckStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("Book not found for ISBN: " + isbn);
                        conn.rollback();
                        return;
                    }
                }

                chapterCheckStmt.setString(1, isbn);
                chapterCheckStmt.setString(2, chapterTitle);
                try (ResultSet rs = chapterCheckStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Chapter already exists for ISBN " + isbn + " and title '" + chapterTitle + "'.");
                        conn.rollback();
                        return;
                    }
                }

                insertStmt.setString(1, isbn);
                insertStmt.setString(2, chapterTitle);
                insertStmt.setString(3, topic);
                insertStmt.setDate(4, Date.valueOf(writtenDate));
                insertStmt.setString(5, fullText);

                int rows = insertStmt.executeUpdate();
                conn.commit();

                System.out.println("Chapter added successfully. Chapters rows: " + rows);
                showBookChaptersByIsbn(isbn);

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Add chapter failed: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // 15. Delete Chapter From Book
    // -------------------------------------------------------------------------
    public void deleteChapterFromBook() throws SQLException {
        System.out.println("\nDelete Chapter From Book");

        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine().trim();

        System.out.print("Enter Chapter Title: ");
        String chapterTitle = scanner.nextLine();

        String deleteAssignmentsSql = "DELETE FROM Works_on_chapters WHERE ISBN = ? AND chapter_title = ?";
        String deleteChapterSql = "DELETE FROM Chapters WHERE ISBN = ? AND title = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteAssignmentsStmt = conn.prepareStatement(deleteAssignmentsSql);
                 PreparedStatement deleteChapterStmt = conn.prepareStatement(deleteChapterSql)) {

                deleteAssignmentsStmt.setString(1, isbn);
                deleteAssignmentsStmt.setString(2, chapterTitle);
                int assignmentRows = deleteAssignmentsStmt.executeUpdate();

                deleteChapterStmt.setString(1, isbn);
                deleteChapterStmt.setString(2, chapterTitle);
                int chapterRows = deleteChapterStmt.executeUpdate();

                if (chapterRows == 0) {
                    conn.rollback();
                    System.out.println("No matching chapter found for ISBN " + isbn + " and title '" + chapterTitle + "'.");
                    return;
                }

                conn.commit();
                System.out.println("Delete completed. Works_on_chapters rows: " + assignmentRows +
                        ", Chapters rows: " + chapterRows);

                showBookChaptersByIsbn(isbn);

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Delete chapter failed: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helper: Show Chapters For Book
    // -------------------------------------------------------------------------
    private void showBookChaptersByIsbn(String isbn) throws SQLException {
        String sql = "SELECT c.ISBN, c.title, c.topic, c.written_date " +
                     "FROM Chapters c " +
                     "WHERE c.ISBN = ? " +
                     "ORDER BY c.title";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\nBook Chapters");
                System.out.printf("%-18s %-40s %-25s %-15s%n",
                        "ISBN", "Chapter Title", "Topic", "Written Date");
                System.out.println("-".repeat(105));

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-18s %-40s %-25s %-15s%n",
                            rs.getString("ISBN"),
                            rs.getString("title"),
                            rs.getString("topic"),
                            rs.getDate("written_date"));
                }

                if (!found) {
                    System.out.println("No chapters found for ISBN: " + isbn);
                }
            }
        }
    }

    // legacy helper
    public void showPeriodical(String pubId) throws SQLException {
        showPeriodicalById(pubId);
    }
}