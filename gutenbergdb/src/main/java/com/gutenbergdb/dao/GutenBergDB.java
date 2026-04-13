package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;

import java.sql.*;
import java.util.Scanner;

/**
 * ProductionOperations.java
 *
 * Covers all "Production of a book edition or issue of a publication" operations:
 *   1.  Enter a new book edition
 *   2.  Enter a new issue of a publication
 *   3.  Update a book edition
 *   4.  Update a publication issue
 *   5.  Delete a book edition
 *   6.  Delete a publication issue
 *   7.  Enter an article (title, author, topic, date) -- uses transaction
 *   8.  Update an article's text
 *   9.  Find books/articles by topic
 *   10. Find books/articles by date range
 *   11. Find books/articles by author name
 *   12. Enter payment for author or editor -- uses transaction
 *   13. Update when a payment was claimed
 *   14. List payments issued but not claimed within a date range
 *   15. Compare two issues by listing their associated articles
 *   16. View all tables
 *
 * Design decisions:
 *   - Every method that can leave the DB in a partial state uses an explicit
 *     transaction with COMMIT / ROLLBACK (operations 7 and 12).
 *   - PreparedStatements are used throughout to prevent SQL injection.
 *   - The single shared Connection is closed in main() via try-with-resources.
 *   - The Scanner (stdin) is never closed inside helper methods to avoid
 *     accidentally closing System.in.
 */
public class GutenBergDB {

    // -------------------------------------------------------------------------
    // MAIN – menu loop
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connected to MariaDB.");
            Scanner sc = new Scanner(System.in);

            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("Choose an option: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1":
                        enterNewBookEdition(conn, sc);
                        break;
                    case "2":
                        enterNewIssue(conn, sc);
                        break;
                    case "3":
                        updateBookEdition(conn, sc);
                        break;
                    case "4":
                        updateIssue(conn, sc);
                        break;
                    case "5":
                        deleteBookEdition(conn, sc);
                        break;
                    case "6":
                        deleteIssue(conn, sc);
                        break;
                    case "7":
                        enterArticle(conn, sc); // transaction
                        break;
                    case "8":
                        updateArticleText(conn, sc);
                        break;
                    case "9":
                        findByTopic(conn, sc);
                        break;
                    case "10":
                        findByDateRange(conn, sc);
                        break;
                    case "11":
                        findByAuthor(conn, sc);
                        break;
                    case "12":
                        enterPayment(conn, sc); // transaction
                        break;
                    case "13":
                        updatePaymentClaimed(conn, sc);
                        break;
                    case "14":
                        listUnclaimedPayments(conn, sc);
                        break;
                    case "15":
                        compareIssues(conn, sc);
                        break;
                    case "16":
                        viewAllTables(conn);
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option, please try again.");
                }
            }
            System.out.println("Goodbye.");
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\n=== Production Operations ===");
        System.out.println(" 1.  Enter a new book edition");
        System.out.println(" 2.  Enter a new issue of a publication");
        System.out.println(" 3.  Update a book edition");
        System.out.println(" 4.  Update a publication issue");
        System.out.println(" 5.  Delete a book edition");
        System.out.println(" 6.  Delete a publication issue");
        System.out.println(" 7.  Enter an article (title, author, topic, date)");
        System.out.println(" 8.  Update the full text of an article");
        System.out.println(" 9.  Find books/articles by topic");
        System.out.println("10.  Find books/articles by date range");
        System.out.println("11.  Find books/articles by author name");
        System.out.println("12.  Enter a payment for author/editor");
        System.out.println("13.  Update when a payment was claimed");
        System.out.println("14.  List payments issued but not claimed (date range)");
        System.out.println("15.  Compare two issues (list their articles)");
        System.out.println("16.  View all tables");
        System.out.println(" 0.  Exit");
    }

    // =========================================================================
    // OPERATION 1 – Enter a new book edition
    // =========================================================================
    /**
     * Inserts a new row into Publications (title, periodicity=NULL for books)
     * and a corresponding row into Books.
     * Assumption: the caller supplies a unique PubID and a unique ISBN.
     */
    private static void enterNewBookEdition(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Enter New Book Edition --");
        System.out.print("PubID: ");           int pubID = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Title: ");           String title = sc.nextLine().trim();
        System.out.print("Topic: ");           String topic = sc.nextLine().trim();
        System.out.print("ISBN: ");            String isbn = sc.nextLine().trim();
        System.out.print("Edition number: ");  int edition = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Publication date (YYYY-MM-DD, blank=NULL): ");
        String pubDate = sc.nextLine().trim();
        System.out.print("Written date (YYYY-MM-DD, blank=NULL): ");
        String writtenDate = sc.nextLine().trim();
        System.out.print("Full text: ");       String fullText = sc.nextLine().trim();

        String pubSQL  = "INSERT INTO Publications (PubID, Title, periodicity, topic) VALUES (?, ?, NULL, ?)";
        String bookSQL = "INSERT INTO Books (PubID, ISBN, full_text, edition_number, publication_date, written_date)"
                       + " VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement psPub  = conn.prepareStatement(pubSQL);
             PreparedStatement psBook = conn.prepareStatement(bookSQL)) {

            psPub.setInt(1, pubID);
            psPub.setString(2, title);
            psPub.setString(3, topic);
            psPub.executeUpdate();

            psBook.setInt(1, pubID);
            psBook.setString(2, isbn);
            psBook.setString(3, fullText);
            psBook.setInt(4, edition);
            psBook.setString(5, pubDate.isEmpty()     ? null : pubDate);
            psBook.setString(6, writtenDate.isEmpty() ? null : writtenDate);
            psBook.executeUpdate();

            System.out.println("Book edition entered successfully (PubID=" + pubID + ").");
        }
    }

    // =========================================================================
    // OPERATION 2 – Enter a new issue of a publication
    // =========================================================================
    /**
     * Inserts a new row into Issues and links it to an existing Publication
     * via Made_of.
     * Assumption: a valid PubID (for a Periodical) already exists.
     */
    private static void enterNewIssue(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Enter New Issue --");
        System.out.print("Issue ID (IID): ");      int iid   = Integer.parseInt(sc.nextLine().trim());
        System.out.print("PubID (parent pub): ");  int pubID = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Subtitle (blank=NULL): "); String sub = sc.nextLine().trim();
        System.out.print("Publication date (YYYY-MM-DD, blank=NULL): ");
        String pubDate = sc.nextLine().trim();

        String issueSQL  = "INSERT INTO Issues (IID, subtitle, Publication_Date) VALUES (?, ?, ?)";
        String madeOfSQL = "INSERT INTO Made_of (PubID, IID) VALUES (?, ?)";

        try (PreparedStatement psIssue  = conn.prepareStatement(issueSQL);
             PreparedStatement psMadeOf = conn.prepareStatement(madeOfSQL)) {

            psIssue.setInt(1, iid);
            psIssue.setString(2, sub.isEmpty() ? null : sub);
            psIssue.setString(3, pubDate.isEmpty() ? null : pubDate);
            psIssue.executeUpdate();

            psMadeOf.setInt(1, pubID);
            psMadeOf.setInt(2, iid);
            psMadeOf.executeUpdate();

            System.out.println("Issue entered successfully (IID=" + iid + ").");
        }
    }

    // =========================================================================
    // OPERATION 3 – Update a book edition
    // =========================================================================
    /**
     * Allows updating ISBN, edition number, publication date, written date,
     * and full text for an existing book (by PubID).
     */
    private static void updateBookEdition(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Update Book Edition --");
        System.out.print("PubID of book to update: "); int pubID = Integer.parseInt(sc.nextLine().trim());
        System.out.print("New ISBN (blank=no change): ");                          String isbn     = sc.nextLine().trim();
        System.out.print("New edition number (blank=no change): ");                String edStr    = sc.nextLine().trim();
        System.out.print("New publication date (YYYY-MM-DD, blank=no change): "); String pubDate  = sc.nextLine().trim();
        System.out.print("New written date (YYYY-MM-DD, blank=no change): ");     String writDate = sc.nextLine().trim();
        System.out.print("New full text (blank=no change): ");                     String fullText = sc.nextLine().trim();

        StringBuilder sb = new StringBuilder("UPDATE Books SET ");
        boolean first = true;
        if (!isbn.isEmpty())     { sb.append("ISBN = ?");                                       first = false; }
        if (!edStr.isEmpty())    { sb.append(first ? "" : ", ").append("edition_number = ?");   first = false; }
        if (!pubDate.isEmpty())  { sb.append(first ? "" : ", ").append("publication_date = ?"); first = false; }
        if (!writDate.isEmpty()) { sb.append(first ? "" : ", ").append("written_date = ?");     first = false; }
        if (!fullText.isEmpty()) { sb.append(first ? "" : ", ").append("full_text = ?");        }
        sb.append(" WHERE PubID = ?");

        try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (!isbn.isEmpty())     ps.setString(idx++, isbn);
            if (!edStr.isEmpty())    ps.setInt(idx++, Integer.parseInt(edStr));
            if (!pubDate.isEmpty())  ps.setString(idx++, pubDate);
            if (!writDate.isEmpty()) ps.setString(idx++, writDate);
            if (!fullText.isEmpty()) ps.setString(idx++, fullText);
            ps.setInt(idx, pubID);
            int rows = ps.executeUpdate();
            System.out.println(rows + " book row(s) updated.");
        }
    }

    // =========================================================================
    // OPERATION 4 – Update a publication issue
    // =========================================================================
    /**
     * Updates subtitle and/or publication date of an existing issue (by IID).
     */
    private static void updateIssue(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Update Publication Issue --");
        System.out.print("IID of issue to update: "); int iid = Integer.parseInt(sc.nextLine().trim());
        System.out.print("New subtitle (blank=no change): ");                          String sub     = sc.nextLine().trim();
        System.out.print("New publication date (YYYY-MM-DD, blank=no change): "); String pubDate = sc.nextLine().trim();

        if (sub.isEmpty() && pubDate.isEmpty()) {
            System.out.println("Nothing to update.");
            return;
        }

        StringBuilder sb = new StringBuilder("UPDATE Issues SET ");
        boolean first = true;
        if (!sub.isEmpty())     { sb.append("subtitle = ?");                                    first = false; }
        if (!pubDate.isEmpty()) { sb.append(first ? "" : ", ").append("Publication_Date = ?"); }
        sb.append(" WHERE IID = ?");

        try (PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (!sub.isEmpty())     ps.setString(idx++, sub);
            if (!pubDate.isEmpty()) ps.setString(idx++, pubDate);
            ps.setInt(idx, iid);
            int rows = ps.executeUpdate();
            System.out.println(rows + " issue row(s) updated.");
        }
    }

    // =========================================================================
    // OPERATION 5 – Delete a book edition
    // =========================================================================
    /**
     * Deletes a book and its parent Publications row.
     * Dependent rows in Works_on_books and Orders_books must be removed first
     * to respect foreign-key constraints.
     * Assumption: caller confirms the PubID belongs to a book.
     */
    private static void deleteBookEdition(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Delete Book Edition --");
        System.out.print("PubID of book to delete: "); int pubID = Integer.parseInt(sc.nextLine().trim());

        // Remove dependents first, then the book and publication rows.
        String[] sqls = {
            "DELETE FROM Works_on_books  WHERE PubID = ?",
            "DELETE FROM Orders_books    WHERE PubID = ?",
            "DELETE FROM Books           WHERE PubID = ?",
            "DELETE FROM Publications    WHERE PubID = ?"
        };

        for (String sql : sqls) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, pubID);
                ps.executeUpdate();
            }
        }
        System.out.println("Book edition (PubID=" + pubID + ") deleted.");
    }

    // =========================================================================
    // OPERATION 6 – Delete a publication issue
    // =========================================================================
    /**
     * Deletes an issue and all dependent articles, assignment rows, and the
     * Made_of link.
     * Assumption: caller confirms the IID exists.
     */
    private static void deleteIssue(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Delete Publication Issue --");
        System.out.print("IID of issue to delete: "); int iid = Integer.parseInt(sc.nextLine().trim());

        String[] sqls = {
            "DELETE FROM Works_on_articles WHERE IID = ?",
            "DELETE FROM Orders_issues      WHERE IID = ?",
            "DELETE FROM Articles           WHERE IID = ?",
            "DELETE FROM Made_of            WHERE IID = ?",
            "DELETE FROM Issues             WHERE IID = ?"
        };

        for (String sql : sqls) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, iid);
                ps.executeUpdate();
            }
        }
        System.out.println("Issue (IID=" + iid + ") and its articles deleted.");
    }

    // =========================================================================
    // OPERATION 7 – Enter an article  *** TRANSACTION ***
    // =========================================================================
    /**
     * TRANSACTION:
     *   Step 1 – INSERT into Articles (title, author link is in Works_on_articles).
     *   Step 2 – INSERT into Works_on_articles to assign the author/editor.
     *   COMMIT on success; ROLLBACK if either insert fails.
     *
     * Assumption: the IID already exists; the EID already exists in Workers.
     */
    private static void enterArticle(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Enter New Article (Transaction) --");
        System.out.print("IID (issue): ");       int iid      = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Article title: ");     String title = sc.nextLine().trim();
        System.out.print("Topic: ");             String topic = sc.nextLine().trim();
        System.out.print("Written date (YYYY-MM-DD, blank=NULL): "); String wDate = sc.nextLine().trim();
        System.out.print("Full text: ");         String text  = sc.nextLine().trim();
        System.out.print("Author/Editor EID: "); int eid      = Integer.parseInt(sc.nextLine().trim());

        String articleSQL = "INSERT INTO Articles (IID, Title, full_text, written_date, topic)"
                          + " VALUES (?, ?, ?, ?, ?)";
        String worksSQL   = "INSERT INTO Works_on_articles (EID, IID, Title) VALUES (?, ?, ?)";

        // Disable auto-commit to begin a manual transaction.
        conn.setAutoCommit(false);
        try (PreparedStatement psArt   = conn.prepareStatement(articleSQL);
             PreparedStatement psWorks = conn.prepareStatement(worksSQL)) {

            // Step 1: insert the article row.
            psArt.setInt(1, iid);
            psArt.setString(2, title);
            psArt.setString(3, text);
            psArt.setString(4, wDate.isEmpty() ? null : wDate);
            psArt.setString(5, topic);
            psArt.executeUpdate();

            // Step 2: assign the worker.
            psWorks.setInt(1, eid);
            psWorks.setInt(2, iid);
            psWorks.setString(3, title);
            psWorks.executeUpdate();

            conn.commit();
            System.out.println("Article entered and author assigned successfully.");

        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Transaction rolled back: " + e.getMessage());
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // =========================================================================
    // OPERATION 8 – Update the full text of an article
    // =========================================================================
    /**
     * Updates the full_text column of an existing article identified by
     * its composite primary key (Title, IID).
     */
    private static void updateArticleText(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Update Article Full Text --");
        System.out.print("Article title: "); String title = sc.nextLine().trim();
        System.out.print("IID: ");           int iid      = Integer.parseInt(sc.nextLine().trim());
        System.out.print("New full text: "); String text  = sc.nextLine().trim();

        String sql = "UPDATE Articles SET full_text = ? WHERE Title = ? AND IID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, text);
            ps.setString(2, title);
            ps.setInt(3, iid);
            int rows = ps.executeUpdate();
            System.out.println(rows + " article row(s) updated.");
        }
    }

    // =========================================================================
    // OPERATION 9 – Find books / articles by topic
    // =========================================================================
    /**
     * Returns matching books (from Publications + Books) and articles
     * (from Articles) whose topic contains the search term.
     */
    private static void findByTopic(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Find Books/Articles by Topic --");
        System.out.print("Topic keyword: "); String keyword = "%" + sc.nextLine().trim() + "%";

        System.out.println("\n[ Books ]");
        String bookSQL = "SELECT p.PubID, p.Title, p.topic, b.ISBN, b.edition_number, b.publication_date"
                       + " FROM Publications p JOIN Books b ON p.PubID = b.PubID"
                       + " WHERE p.topic LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(bookSQL)) {
            ps.setString(1, keyword);
            printResultSet(ps.executeQuery());
        }

        System.out.println("\n[ Articles ]");
        String artSQL = "SELECT a.IID, a.Title, a.topic, a.written_date"
                      + " FROM Articles a"
                      + " WHERE a.topic LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(artSQL)) {
            ps.setString(1, keyword);
            printResultSet(ps.executeQuery());
        }
    }

    // =========================================================================
    // OPERATION 10 – Find books / articles by date range
    // =========================================================================
    /**
     * Returns books whose publication_date falls within the range, and
     * articles whose written_date falls within the range.
     */
    private static void findByDateRange(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Find Books/Articles by Date Range --");
        System.out.print("Start date (YYYY-MM-DD): "); String start = sc.nextLine().trim();
        System.out.print("End date   (YYYY-MM-DD): "); String end   = sc.nextLine().trim();

        System.out.println("\n[ Books ]");
        String bookSQL = "SELECT p.PubID, p.Title, b.ISBN, b.edition_number, b.publication_date"
                       + " FROM Publications p JOIN Books b ON p.PubID = b.PubID"
                       + " WHERE b.publication_date BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(bookSQL)) {
            ps.setString(1, start);
            ps.setString(2, end);
            printResultSet(ps.executeQuery());
        }

        System.out.println("\n[ Articles ]");
        String artSQL = "SELECT a.IID, a.Title, a.topic, a.written_date"
                      + " FROM Articles a"
                      + " WHERE a.written_date BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(artSQL)) {
            ps.setString(1, start);
            ps.setString(2, end);
            printResultSet(ps.executeQuery());
        }
    }

    // =========================================================================
    // OPERATION 11 – Find books / articles by author name
    // =========================================================================
    /**
     * Joins Workers → Works_on_books / Works_on_articles to find content
     * associated with a given worker name (partial match supported).
     */
    private static void findByAuthor(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Find Books/Articles by Author Name --");
        System.out.print("Author name (partial OK): "); String name = "%" + sc.nextLine().trim() + "%";

        System.out.println("\n[ Books ]");
        String bookSQL = "SELECT w.worker_name, p.PubID, p.Title, b.edition_number, b.publication_date"
                       + " FROM Workers w"
                       + " JOIN Works_on_books wb ON w.EID = wb.EID"
                       + " JOIN Publications p    ON wb.PubID = p.PubID"
                       + " JOIN Books b           ON p.PubID  = b.PubID"
                       + " WHERE w.worker_name LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(bookSQL)) {
            ps.setString(1, name);
            printResultSet(ps.executeQuery());
        }

        System.out.println("\n[ Articles ]");
        String artSQL = "SELECT w.worker_name, a.IID, a.Title, a.topic, a.written_date"
                      + " FROM Workers w"
                      + " JOIN Works_on_articles wa ON w.EID = wa.EID"
                      + " JOIN Articles a           ON wa.IID = a.IID AND wa.Title = a.Title"
                      + " WHERE w.worker_name LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(artSQL)) {
            ps.setString(1, name);
            printResultSet(ps.executeQuery());
        }
    }

    // =========================================================================
    // OPERATION 12 – Enter a payment for author or editor  *** TRANSACTION ***
    // =========================================================================
    /**
     * TRANSACTION:
     *   Step 1 – INSERT into Worker_Payments (pay_claim_date intentionally NULL
     *            since the payment has not yet been claimed).
     *   Step 2 – INSERT into Get_Paid to link the payment to the worker.
     *   COMMIT on success; ROLLBACK if either step fails.
     *   (Example unexpected event: invalid EID causes FK violation on step 2.)
     *
     * Assumption: the EID already exists in Workers.
     */
    private static void enterPayment(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Enter Payment (Transaction) --");
        System.out.print("Payment ID (PID): ");                    int pid    = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Worker EID: ");                          int eid    = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Amount: ");                              double amt = Double.parseDouble(sc.nextLine().trim());
        System.out.print("Payment type (salary/freelance/etc): "); String typ = sc.nextLine().trim();
        System.out.print("Pay issue date (YYYY-MM-DD): ");         String iss = sc.nextLine().trim();

        String paySQL     = "INSERT INTO Worker_Payments (PID, amount, work_payment_type, pay_claim_date, pay_issue_date)"
                          + " VALUES (?, ?, ?, NULL, ?)";
        String getPaidSQL = "INSERT INTO Get_Paid (PID, EID) VALUES (?, ?)";

        conn.setAutoCommit(false);
        try (PreparedStatement psPay     = conn.prepareStatement(paySQL);
             PreparedStatement psGetPaid = conn.prepareStatement(getPaidSQL)) {

            // Step 1: record the payment (claim date is NULL – not yet claimed).
            psPay.setInt(1, pid);
            psPay.setDouble(2, amt);
            psPay.setString(3, typ);
            psPay.setString(4, iss);
            psPay.executeUpdate();

            // Step 2: link payment to the worker.
            psGetPaid.setInt(1, pid);
            psGetPaid.setInt(2, eid);
            psGetPaid.executeUpdate();

            conn.commit();
            System.out.println("Payment recorded and linked to worker EID=" + eid + ".");

        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Payment transaction rolled back: " + e.getMessage());
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // =========================================================================
    // OPERATION 13 – Update when a payment was claimed
    // =========================================================================
    /**
     * Sets pay_claim_date for an existing payment row (identified by PID).
     * This represents the moment the author/editor has picked up their payment.
     */
    private static void updatePaymentClaimed(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Update Payment Claim Date --");
        System.out.print("Payment PID: ");             int pid  = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Claim date (YYYY-MM-DD): "); String date = sc.nextLine().trim();

        String sql = "UPDATE Worker_Payments SET pay_claim_date = ? WHERE PID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ps.setInt(2, pid);
            int rows = ps.executeUpdate();
            System.out.println(rows + " payment row(s) updated with claim date.");
        }
    }

    // =========================================================================
    // OPERATION 14 – List payments issued but not claimed within a date range
    // =========================================================================
    /**
     * Returns Worker_Payments rows where pay_claim_date IS NULL and
     * pay_issue_date falls within the specified window.
     */
    private static void listUnclaimedPayments(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- List Unclaimed Payments (Date Range) --");
        System.out.print("Issue date start (YYYY-MM-DD): "); String start = sc.nextLine().trim();
        System.out.print("Issue date end   (YYYY-MM-DD): "); String end   = sc.nextLine().trim();

        String sql = "SELECT wp.PID, gp.EID, w.worker_name, wp.amount, wp.work_payment_type, wp.pay_issue_date"
                   + " FROM Worker_Payments wp"
                   + " JOIN Get_Paid gp ON wp.PID = gp.PID"
                   + " JOIN Workers  w  ON gp.EID = w.EID"
                   + " WHERE wp.pay_claim_date IS NULL"
                   + "   AND wp.pay_issue_date BETWEEN ? AND ?"
                   + " ORDER BY wp.pay_issue_date";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, start);
            ps.setString(2, end);
            printResultSet(ps.executeQuery());
        }
    }

    // =========================================================================
    // OPERATION 15 – Compare two issues by listing their associated articles
    // =========================================================================
    /**
     * Retrieves all articles for each of two specified issue IDs and displays
     * them side-by-side (sequentially, labelled by IID) in a single output.
     * Assumption: both IIDs exist in the Issues table.
     */
    private static void compareIssues(Connection conn, Scanner sc) throws SQLException {
        System.out.println("\n-- Compare Two Issues --");
        System.out.print("First  IID: "); int iid1 = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Second IID: "); int iid2 = Integer.parseInt(sc.nextLine().trim());

        String sql = "SELECT a.IID, i.subtitle, a.Title, a.topic, a.written_date"
                   + " FROM Articles a"
                   + " JOIN Issues i ON a.IID = i.IID"
                   + " WHERE a.IID IN (?, ?)"
                   + " ORDER BY a.IID, a.Title";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, iid1);
            ps.setInt(2, iid2);
            ResultSet rs = ps.executeQuery();

            int currentIID = -1;
            boolean anyRows = false;
            while (rs.next()) {
                anyRows = true;
                int iid = rs.getInt("IID");
                if (iid != currentIID) {
                    currentIID = iid;
                    System.out.println("\n=== Issue " + iid
                            + " – " + rs.getString("subtitle") + " ===");
                    System.out.printf("%-40s %-20s %-15s%n", "Article Title", "Topic", "Written Date");
                    System.out.println("-".repeat(78));
                }
                System.out.printf("%-40s %-20s %-15s%n",
                        rs.getString("Title"),
                        rs.getString("topic"),
                        rs.getString("written_date"));
            }
            if (!anyRows) System.out.println("No articles found for those issue IDs.");
        }
    }

    // =========================================================================
    // OPERATION 16 – View all tables
    // =========================================================================
    /**
     * Prints the full contents of every table in the schema, with column
     * headers and a row count, so you can verify the current state of the DB.
     */
    private static void viewAllTables(Connection conn) throws SQLException {
        String[] tables = {
            "Publications",
            "Periodicals",
            "Books",
            "Issues",
            "Articles",
            "Orders",
            "Orders_books",
            "Orders_issues",
            "Distributors",
            "Workers",
            "Worker_Payments",
            "Distributor_payments",
            "Works_on_articles",
            "Works_on_books",
            "Places",
            "Make",
            "Made_of",
            "Get_Paid"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String table : tables) {
                System.out.println("\n========== " + table + " ==========");
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();

                // Print column headers.
                for (int i = 1; i <= cols; i++) {
                    System.out.printf("%-25s", meta.getColumnLabel(i));
                }
                System.out.println();
                System.out.println("-".repeat(25 * cols));

                // Print rows.
                int rowCount = 0;
                while (rs.next()) {
                    for (int i = 1; i <= cols; i++) {
                        System.out.printf("%-25s", rs.getString(i));
                    }
                    System.out.println();
                    rowCount++;
                }
                if (rowCount == 0) System.out.println("(empty table)");
            }
        }
    }

    // =========================================================================
    // UTILITY – Print a generic ResultSet to stdout
    // =========================================================================
    private static void printResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();

        // Print header.
        for (int i = 1; i <= cols; i++) {
            System.out.printf("%-20s", meta.getColumnLabel(i));
        }
        System.out.println();
        System.out.println("-".repeat(20 * cols));

        // Print rows.
        int count = 0;
        while (rs.next()) {
            for (int i = 1; i <= cols; i++) {
                System.out.printf("%-20s", rs.getString(i));
            }
            System.out.println();
            count++;
        }
        if (count == 0) System.out.println("(no results)");
    }
}