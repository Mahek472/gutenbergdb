package com.gutenbergdb.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GutenBergDB_DemoData {

    private static final String URL  = "jdbc:mariadb://classdb2.csc.ncsu.edu:3306/mrkantha";
    private static final String USER = "mrkantha";
    private static final String PASS = "200666691";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            conn.setAutoCommit(false);

            System.out.println("Connected to MariaDB.");

            dropAllTables(conn);     // drops every table in current DB
            createTables(conn);      // creates fresh schema
            insertData(conn);        // inserts demo data

            conn.commit();
            System.out.println("All tables recreated and demo data inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // DROP ALL TABLES IN CURRENT DATABASE
    // -------------------------------------------------------------------------
    private static void dropAllTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            String currentDb = null;
            try (ResultSet rs = stmt.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) {
                    currentDb = rs.getString(1);
                }
            }

            if (currentDb == null || currentDb.isBlank()) {
                throw new SQLException("Could not determine current database.");
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT table_name FROM information_schema.tables " +
                    "WHERE table_schema = ?")) {
                ps.setString(1, currentDb);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String tableName = rs.getString("table_name");
                        String dropSql = "DROP TABLE IF EXISTS `" + tableName + "`";
                        stmt.executeUpdate(dropSql);
                        System.out.println("Dropped table: " + tableName);
                    }
                }
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            System.out.println("All existing tables dropped.");
        }
    }

    // -------------------------------------------------------------------------
    // CREATE TABLES
    // -------------------------------------------------------------------------
    private static void createTables(Connection conn) throws SQLException {
        String[] creates = {

            "CREATE TABLE Workers (" +
                "EID VARCHAR(10) NOT NULL, " +
                "worker_name VARCHAR(100) NOT NULL, " +
                "is_employee BOOLEAN NOT NULL, " +
                "salary DECIMAL(10,2), " +
                "worker_type VARCHAR(50) NOT NULL, " +
                "PRIMARY KEY (EID)" +
            ") ENGINE=InnoDB",

            "CREATE TABLE Publications (" +
                "PubID VARCHAR(10) NOT NULL, " +
                "title VARCHAR(255) NOT NULL, " +
                "topic VARCHAR(100) NOT NULL, " +
                "periodicity VARCHAR(50), " +
                "PRIMARY KEY (PubID)" +
            ") ENGINE=InnoDB",

            "CREATE TABLE Periodicals (" +
                "PubID VARCHAR(10) NOT NULL, " +
                "type VARCHAR(50) NOT NULL, " +
                "PRIMARY KEY (PubID), " +
                "FOREIGN KEY (PubID) REFERENCES Publications(PubID)" +
            ") ENGINE=InnoDB",

            // CHANGED: removed full_text column
            "CREATE TABLE Books (" +
                "ISBN VARCHAR(20) NOT NULL, " +
                "title VARCHAR(255) NOT NULL, " +
                "edition_number INT NOT NULL, " +
                "publication_date DATE NOT NULL, " +
                "PubID VARCHAR(10) NOT NULL, " +
                "PRIMARY KEY (ISBN), " +
                "FOREIGN KEY (PubID) REFERENCES Publications(PubID)" +
            ") ENGINE=InnoDB",

            // CHANGED: added CID as primary key, ISBN becomes a regular FK
            "CREATE TABLE Chapters (" +
                "CID VARCHAR(10) NOT NULL, " +
                "ISBN VARCHAR(20) NOT NULL, " +
                "title VARCHAR(255) NOT NULL, " +
                "topic VARCHAR(100) NOT NULL, " +
                "written_date DATE NOT NULL, " +
                "full_text TEXT, " +
                "PRIMARY KEY (CID), " +
                "FOREIGN KEY (ISBN) REFERENCES Books(ISBN)" +
            ") ENGINE=InnoDB",

            "CREATE TABLE Edits (" +
                "EID VARCHAR(10) NOT NULL, " +
                "PubID VARCHAR(10) NOT NULL, " +
                "PRIMARY KEY (EID, PubID), " +
                "FOREIGN KEY (EID) REFERENCES Workers(EID), " +
                "FOREIGN KEY (PubID) REFERENCES Publications(PubID)" +
            ") ENGINE=InnoDB",

            // CHANGED: Works_on_chapters now references CID (strong entity PK)
            "CREATE TABLE Works_on_chapters (" +
                "EID VARCHAR(10) NOT NULL, " +
                "CID VARCHAR(10) NOT NULL, " +
                "PRIMARY KEY (EID, CID), " +
                "FOREIGN KEY (EID) REFERENCES Workers(EID), " +
                "FOREIGN KEY (CID) REFERENCES Chapters(CID)" +
            ") ENGINE=InnoDB",

            "CREATE TABLE Issues (" +
                "IID VARCHAR(10) NOT NULL, " +
                "publication_date DATE NOT NULL, " +
                "Subtitle VARCHAR(255) NOT NULL, " +
                "PubID VARCHAR(10) NOT NULL, " +
                "PRIMARY KEY (IID), " +
                "FOREIGN KEY (PubID) REFERENCES Periodicals(PubID)" +
            ") ENGINE=InnoDB",

            // CHANGED: added IID as FK (many Articles -> one Issue), removed full_text
            "CREATE TABLE Articles (" +
                "AID VARCHAR(10) NOT NULL, " +
                "title VARCHAR(255) NOT NULL, " +
                "topic VARCHAR(100) NOT NULL, " +
                "written_date DATE NOT NULL, " +
                "full_text TEXT, " +
                "IID VARCHAR(10) NOT NULL, " +
                "PRIMARY KEY (AID), " +
                "FOREIGN KEY (IID) REFERENCES Issues(IID)" +
            ") ENGINE=InnoDB",

            "CREATE TABLE Works_on_articles (" +
                "EID VARCHAR(10) NOT NULL, " +
                "AID VARCHAR(10) NOT NULL, " +
                "PRIMARY KEY (EID, AID), " +
                "FOREIGN KEY (EID) REFERENCES Workers(EID), " +
                "FOREIGN KEY (AID) REFERENCES Articles(AID)" +
            ") ENGINE=InnoDB",

            // REMOVED: Contains_articles — relationship now captured by IID FK in Articles

            "CREATE TABLE Distributors (" +
                "DID VARCHAR(10) NOT NULL, " +
                "name VARCHAR(150) NOT NULL, " +
                "category VARCHAR(100) NOT NULL, " +
                "addr VARCHAR(255) NOT NULL, " +
                "phone_number VARCHAR(25) NOT NULL, " +
                "contact VARCHAR(100) NOT NULL, " +
                "outstanding_balance DECIMAL(10,2) NOT NULL, " +
                "balance_as_of DATE NOT NULL, " +
                "PRIMARY KEY (DID)" +
            ") ENGINE=InnoDB",

            "CREATE TABLE Orders (" +
                "OID VARCHAR(10) NOT NULL, " +
                "date_ordered DATE NOT NULL, " +
                "shipping_fee DECIMAL(10,2) NOT NULL, " +
                "DID VARCHAR(10) NOT NULL, " +
                "PRIMARY KEY (OID), " +
                "FOREIGN KEY (DID) REFERENCES Distributors(DID)" +
            ") ENGINE=InnoDB",

            "CREATE TABLE Orders_issues (" +
                "OID VARCHAR(10) NOT NULL, " +
                "IID VARCHAR(10) NOT NULL, " +
                "number_of_copies INT NOT NULL, " +
                "unit_price DECIMAL(10,2) NOT NULL, " +
                "PRIMARY KEY (OID, IID), " +
                "FOREIGN KEY (OID) REFERENCES Orders(OID), " +
                "FOREIGN KEY (IID) REFERENCES Issues(IID)" +
            ") ENGINE=InnoDB",

            "CREATE TABLE Orders_books (" +
                "OID VARCHAR(10) NOT NULL, " +
                "ISBN VARCHAR(20) NOT NULL, " +
                "number_of_copies INT NOT NULL, " +
                "unit_price DECIMAL(10,2) NOT NULL, " +
                "PRIMARY KEY (OID, ISBN), " +
                "FOREIGN KEY (OID) REFERENCES Orders(OID), " +
                "FOREIGN KEY (ISBN) REFERENCES Books(ISBN)" +
            ") ENGINE=InnoDB",

            "CREATE TABLE Worker_Payments (" +
                "PID VARCHAR(10) NOT NULL, " +
                "amount DECIMAL(10,2) NOT NULL, " +
                "payment_type VARCHAR(255) NOT NULL, " +
                "pay_issue_date DATE NOT NULL, " +
                "pay_claim_date DATE NULL, " +
                "EID VARCHAR(10) NOT NULL, " +
                "PRIMARY KEY (PID), " +
                "FOREIGN KEY (EID) REFERENCES Workers(EID)" +
            ") ENGINE=InnoDB",

            "CREATE TABLE Distributor_payments (" +
                "DPID VARCHAR(10) NOT NULL, " +
                "payment_amount DECIMAL(10,2) NOT NULL, " +
                "payment_date DATE NOT NULL, " +
                "DID VARCHAR(10) NOT NULL, " +
                "PRIMARY KEY (DPID), " +
                "FOREIGN KEY (DID) REFERENCES Distributors(DID)" +
            ") ENGINE=InnoDB"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : creates) {
                stmt.executeUpdate(sql);
            }
            System.out.println("All tables created successfully.");
        }
    }

    // -------------------------------------------------------------------------
    // INSERT DATA
    // -------------------------------------------------------------------------
    private static void insertData(Connection conn) throws SQLException {
        insertWorkers(conn);
        insertPublications(conn);
        insertEdits(conn);
        insertPeriodicals(conn);
        insertIssues(conn);        // Issues must exist before Articles (FK: IID)
        insertBooks(conn);
        insertChapters(conn);
        insertWorksOnChapters(conn);
        insertArticles(conn);
        insertWorksOnArticles(conn);
        // insertContainsArticles removed — IID is now a FK on Articles
        insertDistributors(conn);
        insertOrders(conn);
        insertOrdersIssues(conn);
        insertOrdersBooks(conn);
        insertWorkerPayments(conn);
        insertDistributorPayments(conn);
    }

    private static void insertWorkers(Connection conn) throws SQLException {
        String sql = "INSERT INTO Workers (EID, worker_name, is_employee, salary, worker_type) VALUES (?, ?, ?, ?, ?)";
        Object[][] rows = {
            {"E001", "Alice Morgan", true, 72000.00, "Author"},
            {"E002", "Sarah Lee", true, 70000.00, "Author"},
            {"E003", "Michael Brown", true, 70500.00, "Author"},
            {"E004", "Emily Zhang", true, 71000.00, "Author"},
            {"E005", "Daniel Kim", true, 69500.00, "Author"},
            {"E006", "Laura Martinez", true, 71500.00, "Author"},
            {"E007", "Robert Singh", true, 72500.00, "Author"},
            {"E008", "Olivia Bennett", true, 82000.00, "Editor"},
            {"E009", "Helena Strauss", true, 80000.00, "Editor"},
            {"E010", "Daniel Whitmore", true, 83000.00, "Editor"}
        };
        batchInsert(conn, sql, rows, "Workers");
    }

    private static void insertPublications(Connection conn) throws SQLException {
        String sql = "INSERT INTO Publications (PubID, title, topic, periodicity) VALUES (?, ?, ?, ?)";
        Object[][] rows = {
            {"PUB001", "Foundations of Distributed Databases", "Databases", null},
            {"PUB002", "Tech Weekly", "Technology", "Weekly"},
            {"PUB003", "Global Affairs Review", "News", "Monthly"}
        };
        batchInsert(conn, sql, rows, "Publications");
    }

    private static void insertEdits(Connection conn) throws SQLException {
        String sql = "INSERT INTO Edits (EID, PubID) VALUES (?, ?)";
        Object[][] rows = {
            {"E008", "PUB001"},
            {"E008", "PUB002"},
            {"E009", "PUB002"},
            {"E010", "PUB003"}
        };
        batchInsert(conn, sql, rows, "Edits");
    }

    private static void insertBooks(Connection conn) throws SQLException {
        // CHANGED: removed full_text from insert
        String sql = "INSERT INTO Books (ISBN, title, edition_number, publication_date, PubID) VALUES (?, ?, ?, ?, ?)";
        Object[][] rows = {
            {"9781458300001", "Foundations of Distributed Databases - Edition 1", 1, "2025-03-15", "PUB001"},
            {"9781458300002", "Foundations of Distributed Databases - Edition 2", 2, "2026-02-01", "PUB001"}
        };
        batchInsert(conn, sql, rows, "Books");
    }

    private static void insertChapters(Connection conn) throws SQLException {
        // CHANGED: added CID as first column
        String sql = "INSERT INTO Chapters (CID, ISBN, title, topic, written_date, full_text) VALUES (?, ?, ?, ?, ?, ?)";
        Object[][] rows = {
            {"C001", "9781458300001", "Ch1 Distributed Transactions", "Distributed Systems", "2025-03-15",
                "Modern distributed database systems coordinate data across multiple sites, often separated by large geographic distances. Ensuring that a transaction commits consistently at every participating node is one of the central challenges of distributed computing."},
            {"C002", "9781458300002", "Ch1 Distributed Transactions (Edition 2)", "Distributed Systems", "2026-02-01",
                "As distributed infrastructures scale to cloud environments, transaction coordination becomes increasingly complex. Newer consensus-based approaches build upon traditional commit protocols to improve fault tolerance and availability."},
            {"C003", "9781458300002", "Ch2 Data Replication", "Data Management", "2026-02-01",
                "Data replication allows distributed systems to improve reliability and performance by maintaining multiple copies of data. Different replication strategies, however, introduce trade-offs between consistency, latency, and availability."}
        };
        batchInsert(conn, sql, rows, "Chapters");
    }

    private static void insertWorksOnChapters(Connection conn) throws SQLException {
        // CHANGED: references CID instead of (ISBN, chapter_title)
        String sql = "INSERT INTO Works_on_chapters (EID, CID) VALUES (?, ?)";
        Object[][] rows = {
            {"E001", "C001"},
            {"E001", "C002"},
            {"E001", "C003"}
        };
        batchInsert(conn, sql, rows, "Works_on_chapters");
    }

    private static void insertPeriodicals(Connection conn) throws SQLException {
        String sql = "INSERT INTO Periodicals (PubID, type) VALUES (?, ?)";
        Object[][] rows = {
            {"PUB002", "Magazine"},
            {"PUB003", "Journal"}
        };
        batchInsert(conn, sql, rows, "Periodicals");
    }

    private static void insertIssues(Connection conn) throws SQLException {
        String sql = "INSERT INTO Issues (IID, publication_date, Subtitle, PubID) VALUES (?, ?, ?, ?)";
        Object[][] rows = {
            {"I001", "2026-02-01", "Tech Weekly - February 1, 2026", "PUB002"},
            {"I002", "2026-02-08", "Tech Weekly - February 8, 2026", "PUB002"},
            {"I003", "2026-02-15", "Tech Weekly - February 15, 2026", "PUB002"},
            {"I004", "2026-01-01", "Global Affairs Review - January 2026", "PUB003"},
            {"I005", "2026-02-01", "Global Affairs Review - February 2026", "PUB003"}
        };
        batchInsert(conn, sql, rows, "Issues");
    }

    private static void insertArticles(Connection conn) throws SQLException {
        // CHANGED: added IID as FK column (many-to-one with Issues)
        String sql = "INSERT INTO Articles (AID, title, topic, written_date, full_text, IID) VALUES (?, ?, ?, ?, ?, ?)";
        Object[][] rows = {
            {"A001", "AI in 2026", "Artificial Intelligence", "2026-01-20",
                "Artificial intelligence systems in 2026 operate at unprecedented scale, integrating multimodal inputs and real-time decision making. Rapid advances in model efficiency have made AI deployment feasible across industries.",
                "I001"},
            {"A002", "Quantum Computing Basics", "Quantum Computing", "2026-01-25",
                "Quantum computing departs fundamentally from classical computation by leveraging superposition and entanglement. Even small quantum systems demonstrate behaviors that have no classical equivalent.",
                "I002"},
            {"A003", "Data Privacy in Practice", "Cybersecurity", "2026-01-20",
                "Organizations increasingly face the challenge of protecting user data while maintaining usability and performance. Regulatory frameworks now require concrete safeguards and transparent data-handling practices.",
                "I002"},
            {"A004", "Edge AI Applications", "Artificial Intelligence", "2026-02-01",
                "Deploying AI models on edge devices reduces latency and enhances privacy by processing data locally. Advances in hardware acceleration have made on-device inference more practical than ever before.",
                "I003"},
            {"A005", "Election Trends Worldwide", "International Politics", "2025-12-15",
                "Recent elections across multiple regions reveal shifting voter priorities and evolving campaign strategies. Digital platforms now play a decisive role in shaping public discourse.",
                "I004"},
            {"A006", "Energy Policy Updates", "Public Policy", "2026-01-05",
                "Global energy markets continue to respond to geopolitical pressures and climate commitments. Policymakers are balancing economic growth with long-term sustainability goals.",
                "I005"}
        };
        batchInsert(conn, sql, rows, "Articles");
    }

    private static void insertWorksOnArticles(Connection conn) throws SQLException {
        String sql = "INSERT INTO Works_on_articles (EID, AID) VALUES (?, ?)";
        Object[][] rows = {
            {"E002", "A001"},
            {"E003", "A002"},
            {"E004", "A003"},
            {"E005", "A004"},
            {"E006", "A005"},
            {"E007", "A006"}
        };
        batchInsert(conn, sql, rows, "Works_on_articles");
    }

    // insertContainsArticles REMOVED — IID FK on Articles replaces this join table

    private static void insertDistributors(Connection conn) throws SQLException {
        String sql = "INSERT INTO Distributors (DID, name, category, addr, phone_number, contact, outstanding_balance, balance_as_of) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Object[][] rows = {
            {"D001", "Triangle Books", "Bookstore", "142 Hillsborough St, Raleigh NC 27603", "(919) 555-2101", "Laura Thompson", 4850.00, "2025-12-31"},
            {"D002", "Eastern Academic Wholesale", "Wholesale Distributor", "850 Logistics Pkwy, Charlotte NC 28208", "(704) 555-7782", "Mark Reynolds", 12300.00, "2025-12-31"},
            {"D003", "Wake County Public Library", "Library", "336 Fayetteville St, Raleigh NC 27601", "(919) 555-4433", "Emily Carter", 0.00, "2025-12-31"},
            {"D004", "Capitol City Books", "Bookstore", "91 Market Square, Durham NC 27701", "(919) 555-6610", "Daniel Wright", 2175.50, "2025-12-31"}
        };
        batchInsert(conn, sql, rows, "Distributors");
    }

    private static void insertOrders(Connection conn) throws SQLException {
        String sql = "INSERT INTO Orders (OID, date_ordered, shipping_fee, DID) VALUES (?, ?, ?, ?)";
        Object[][] rows = {
            {"O001", "2026-01-03", 160.00, "D004"},
            {"O002", "2026-02-03", 140.00, "D004"},
            {"O003", "2026-02-03", 750.00, "D002"},
            {"O004", "2026-02-11", 750.00, "D002"},
            {"O005", "2026-02-17", 600.00, "D002"},
            {"O006", "2026-02-10", 350.00, "D001"},
            {"O007", "2026-02-12", 120.00, "D003"}
        };
        batchInsert(conn, sql, rows, "Orders");
    }

    private static void insertOrdersIssues(Connection conn) throws SQLException {
        String sql = "INSERT INTO Orders_issues (OID, IID, number_of_copies, unit_price) VALUES (?, ?, ?, ?)";
        Object[][] rows = {
            {"O001", "I004", 90, 15.00},
            {"O002", "I005", 75, 15.00},
            {"O003", "I001", 650, 12.00},
            {"O004", "I002", 650, 12.00},
            {"O005", "I003", 500, 12.00}
        };
        batchInsert(conn, sql, rows, "Orders_issues");
    }

    private static void insertOrdersBooks(Connection conn) throws SQLException {
        String sql = "INSERT INTO Orders_books (OID, ISBN, number_of_copies, unit_price) VALUES (?, ?, ?, ?)";
        Object[][] rows = {
            {"O006", "9781458300002", 120, 85.00},
            {"O007", "9781458300002", 40, 85.00}
        };
        batchInsert(conn, sql, rows, "Orders_books");
    }

    private static void insertWorkerPayments(Connection conn) throws SQLException {
        String sql = "INSERT INTO Worker_Payments (PID, amount, payment_type, pay_issue_date, pay_claim_date, EID) VALUES (?, ?, ?, ?, ?, ?)";
        Object[][] rows = {
            {"P001", 2500.00, "Ch1 Distributed Transactions", "2025-03-20", "2025-04-05", "E001"},
            {"P002", 1000.00, "Ch1 Distributed Transactions (Edition 2)", "2026-02-05", null, "E001"},
            {"P003", 2000.00, "Ch2 Data Replication", "2026-02-10", null, "E001"},
            {"P004", 1200.00, "AI in 2026", "2026-01-28", "2026-02-03", "E002"},
            {"P005", 1100.00, "Quantum Computing Basics", "2026-01-30", null, "E003"},
            {"P006", 1150.00, "Data Privacy in Practice", "2026-01-25", "2026-02-02", "E004"},
            {"P007", 1300.00, "Edge AI Applications", "2026-02-05", null, "E005"},
            {"P008", 1250.00, "Election Trends Worldwide", "2025-12-20", "2026-01-05", "E006"},
            {"P009", 1180.00, "Energy Policy Updates", "2026-01-10", null, "E007"},
            {"P010", 4000.00, "Foundations of Distributed Databases - Edition 1", "2025-03-15", "2025-04-10", "E008"},
            {"P011", 4000.00, "Foundations of Distributed Databases - Edition 2", "2026-02-01", "2026-02-10", "E008"},
            {"P012", 2500.00, "Tech Weekly - Feb 1, 2026", "2026-02-28", null, "E008"},
            {"P013", 2000.00, "Tech Weekly - Feb 1, 2026", "2026-02-28", "2026-03-05", "E009"},
            {"P014", 3000.00, "Global Affairs Review - January 2026", "2026-01-31", "2026-02-07", "E010"},
            {"P015", 3000.00, "Global Affairs Review - February 2026", "2026-02-28", null, "E010"}
        };
        batchInsert(conn, sql, rows, "Worker_Payments");
    }

    private static void insertDistributorPayments(Connection conn) throws SQLException {
        String sql = "INSERT INTO Distributor_payments (DPID, payment_amount, payment_date, DID) VALUES (?, ?, ?, ?)";
        Object[][] rows = {
            {"DP001", 15000.00, "2026-02-20", "D001"},
            {"DP002", 26000.00, "2026-02-15", "D002"},
            {"DP003", 5100.00, "2026-02-20", "D002"},
            {"DP004", 3520.00, "2026-02-18", "D003"},
            {"DP005", 3600.00, "2026-01-20", "D004"},
            {"DP006", 1299.50, "2026-02-10", "D004"}
        };
        batchInsert(conn, sql, rows, "Distributor_payments");
    }

    // -------------------------------------------------------------------------
    // HELPER
    // -------------------------------------------------------------------------
    private static void batchInsert(Connection conn, String sql, Object[][] rows, String tableName) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    ps.setObject(i + 1, row[i]);
                }
                ps.executeUpdate();
            }
            System.out.println("Inserted into " + tableName);
        }
    }
}
