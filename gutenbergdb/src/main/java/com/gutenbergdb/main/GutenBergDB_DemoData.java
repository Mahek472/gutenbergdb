package com.gutenbergdb.main;
import java.sql.*;

public class GutenBergDB_DemoData {
   private static final String URL  = "jdbc:mariadb://classdb2.csc.ncsu.edu:3306/mrkantha";
   private static final String USER = "mrkantha";
   private static final String PASS = "200666691";
   public static void main(String[] args) {
//       try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
//           System.out.println("Connected to MariaDB.");
//           dropTables(conn);
//           createTables(conn);
//           insertData(conn);
//       } catch (SQLException e) {
//           System.err.println("Connection error: " + e.getMessage());
//       }
	   try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
	        System.out.println("Connected to MariaDB.");
//	        dropTables(conn);
//	        createTables(conn);
//	        insertData(conn);
	        displayAllTables(conn);  // <-- add this
	    } catch (SQLException e) {
	        System.err.println("Connection error: " + e.getMessage());
	    }
   }
   // -------------------------------------------------------------------------
   // DROP TABLES
   // -------------------------------------------------------------------------
   private static void dropTables(Connection conn) throws SQLException {
	   
       String[] drops = {
           "DROP TABLE IF EXISTS Get_Paid",
           "DROP TABLE IF EXISTS Made_of",
           "DROP TABLE IF EXISTS Make",
           "DROP TABLE IF EXISTS Places",
           "DROP TABLE IF EXISTS Works_on_articles",
           "DROP TABLE IF EXISTS Works_on_books",
           "DROP TABLE IF EXISTS Orders_books",
           "DROP TABLE IF EXISTS Orders_issues",
           "DROP TABLE IF EXISTS Worker_Payments",
           "DROP TABLE IF EXISTS Distributor_payments",
           "DROP TABLE IF EXISTS Articles",
           "DROP TABLE IF EXISTS Issues",
           "DROP TABLE IF EXISTS Orders",
           "DROP TABLE IF EXISTS Workers",
           "DROP TABLE IF EXISTS Distributors",
           "DROP TABLE IF EXISTS Periodicals",
           "DROP TABLE IF EXISTS Books",
           "DROP TABLE IF EXISTS Publications"
       };
       try (Statement stmt = conn.createStatement()) {
    	   stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
           for (String sql : drops) {
               stmt.executeUpdate(sql);
           }
           stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
           System.out.println("All tables dropped successfully.");
       } catch (SQLException e) {
           System.err.println("Error dropping tables: " + e.getMessage());
           throw e;
       }
   }
   
   private static void displayAllTables(Connection conn) throws SQLException {
	    String[] tables = {
	        "Publications", "Periodicals", "Books", "Issues", "Articles",
	        "Orders", "Orders_books", "Orders_issues", "Distributors", "Workers",
	        "Worker_Payments", "Distributor_payments", "Works_on_articles",
	        "Works_on_books", "Places", "Make", "Made_of", "Get_Paid"
	    };

	    try (Statement stmt = conn.createStatement()) {
	        for (String table : tables) {
	            System.out.println("\n========== " + table + " ==========");
	            ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);
	            ResultSetMetaData meta = rs.getMetaData();
	            int colCount = meta.getColumnCount();

	            // Print column headers
	            for (int i = 1; i <= colCount; i++) {
	                System.out.printf("%-25s", meta.getColumnName(i));
	            }
	            System.out.println();
	            System.out.println("-".repeat(25 * colCount));

	            // Print rows
	            while (rs.next()) {
	                for (int i = 1; i <= colCount; i++) {
	                    System.out.printf("%-25s", rs.getString(i));
	                }
	                System.out.println();
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Error displaying tables: " + e.getMessage());
	        throw e;
	    }
	}
   // -------------------------------------------------------------------------
   // CREATE TABLES
   // -------------------------------------------------------------------------
   private static void createTables(Connection conn) throws SQLException {
       String[] creates = {
           // Publications
    		   "CREATE TABLE Publications (PubID INT, Title VARCHAR(255), periodicity VARCHAR(50), topic VARCHAR(100), PRIMARY KEY (PubID))",

    		   "CREATE TABLE Periodicals (PubID INT, type VARCHAR(50) NOT NULL, PRIMARY KEY (PubID), FOREIGN KEY (PubID) REFERENCES Publications(PubID))",

    		   "CREATE TABLE Books (PubID INT, ISBN VARCHAR(20) NOT NULL UNIQUE, full_text TEXT NOT NULL, edition_number INT NOT NULL, publication_date DATE, written_date DATE, PRIMARY KEY (PubID), FOREIGN KEY (PubID) REFERENCES Publications(PubID))",

    		   "CREATE TABLE Issues (IID INT, subtitle VARCHAR(255), Publication_Date DATE, PRIMARY KEY (IID))",

    		   "CREATE TABLE Articles (IID INT, Title VARCHAR(255), full_text TEXT NOT NULL UNIQUE, written_date DATE, topic VARCHAR(100), PRIMARY KEY (Title, IID), FOREIGN KEY (IID) REFERENCES Issues(IID))",

    		   "CREATE TABLE Orders (OID INT, date_ordered DATE NOT NULL, shipping_fee DECIMAL(10,2) NOT NULL, date_due DATE NOT NULL, is_produced BOOLEAN NOT NULL, PRIMARY KEY (OID))",

    		   "CREATE TABLE Orders_books (OID INT, PubID INT, unit_price DECIMAL(10,2) NOT NULL, number_of_copies INT NOT NULL, PRIMARY KEY (OID, PubID), FOREIGN KEY (OID) REFERENCES Orders(OID), FOREIGN KEY (PubID) REFERENCES Publications(PubID))",

    		   "CREATE TABLE Orders_issues (OID INT, IID INT, unit_price DECIMAL(10,2) NOT NULL, number_of_copies INT NOT NULL, PRIMARY KEY (OID, IID), FOREIGN KEY (OID) REFERENCES Orders(OID), FOREIGN KEY (IID) REFERENCES Issues(IID))",

    		   "CREATE TABLE Distributors (DID INT, name VARCHAR(100) NOT NULL, phone_number VARCHAR(20) NOT NULL UNIQUE, category VARCHAR(50) NOT NULL, outstanding_balance DECIMAL(10,2) NOT NULL DEFAULT 0, addr VARCHAR(255) NOT NULL UNIQUE, contact VARCHAR(100) NOT NULL, PRIMARY KEY (DID))",

    		   "CREATE TABLE Workers (EID INT, is_employee BOOLEAN NOT NULL, worker_type VARCHAR(50) NOT NULL, salary DECIMAL(10,2) NOT NULL, worker_name VARCHAR(100) NOT NULL, PRIMARY KEY (EID))",

    		   "CREATE TABLE Worker_Payments (PID INT, amount DECIMAL(10,2) NOT NULL, work_payment_type VARCHAR(50) NOT NULL, pay_claim_date DATE, pay_issue_date DATE NOT NULL, PRIMARY KEY (PID))",

    		   "CREATE TABLE Distributor_payments (DBID INT, payment_date DATE NOT NULL, payment_amount DECIMAL(10,2) NOT NULL, PRIMARY KEY (DBID))",

    		   "CREATE TABLE Works_on_articles (EID INT, IID INT, Title VARCHAR(255), PRIMARY KEY (EID, IID, Title), FOREIGN KEY (EID) REFERENCES Workers(EID), FOREIGN KEY (Title, IID) REFERENCES Articles(Title, IID))",

    		   "CREATE TABLE Works_on_books (EID INT, PubID INT, PRIMARY KEY (EID, PubID), FOREIGN KEY (EID) REFERENCES Workers(EID), FOREIGN KEY (PubID) REFERENCES Publications(PubID))",

    		   "CREATE TABLE Places (OID INT, DID INT NOT NULL, PRIMARY KEY (OID), FOREIGN KEY (OID) REFERENCES Orders(OID), FOREIGN KEY (DID) REFERENCES Distributors(DID))",

    		   "CREATE TABLE Make (DBID INT, DID INT NOT NULL, PRIMARY KEY (DBID), FOREIGN KEY (DBID) REFERENCES Distributor_payments(DBID), FOREIGN KEY (DID) REFERENCES Distributors(DID))",

    		   "CREATE TABLE Made_of (PubID INT NOT NULL, IID INT, PRIMARY KEY (IID), FOREIGN KEY (PubID) REFERENCES Publications(PubID), FOREIGN KEY (IID) REFERENCES Issues(IID))",

    		   "CREATE TABLE Get_Paid (PID INT, EID INT NOT NULL, PRIMARY KEY (PID), FOREIGN KEY (PID) REFERENCES Worker_Payments(PID), FOREIGN KEY (EID) REFERENCES Workers(EID))"   };
       try (Statement stmt = conn.createStatement()) {
           for (String sql : creates) {
               stmt.executeUpdate(sql);
           }
           System.out.println("All tables created successfully.");
       } catch (SQLException e) {
           System.err.println("Error creating tables: " + e.getMessage());
           throw e;
       }
   }
   // -------------------------------------------------------------------------
   // INSERT DATA
   // -------------------------------------------------------------------------
   private static void insertData(Connection conn) throws SQLException {
       insertPublications(conn);
       insertPeriodicals(conn);
       insertBooks(conn);
       insertIssues(conn);
       insertArticles(conn);
       insertOrders(conn);
       insertOrdersBooks(conn);
       insertOrdersIssues(conn);
       insertDistributors(conn);
       insertWorkers(conn);
       insertWorkerPayments(conn);
       insertDistributorPayments(conn);
       insertWorksOnArticles(conn);
       insertWorksOnBooks(conn);
       insertPlaces(conn);
       insertMake(conn);
       insertMadeOf(conn);
       insertGetPaid(conn);
   }
   private static void insert(Connection conn, String table, String sql, Object[][] rows) throws SQLException {
       try (PreparedStatement ps = conn.prepareStatement(sql)) {
           for (Object[] row : rows) {
               for (int i = 0; i < row.length; i++) {
                   ps.setObject(i + 1, row[i]);
               }
               ps.executeUpdate();
               System.out.println("Inserted row into " + table + ": " + java.util.Arrays.toString(row));
           }
       } catch (SQLException e) {
           System.err.println("Error inserting into " + table + ": " + e.getMessage());
           throw e;
       }
   }
   // -------------------------------------------------------------------------
   // Publications
   // PubID 1 = Book: Foundations of Distributed Databases
   // PubID 2 = Magazine: Tech Weekly
   // PubID 3 = Journal: Global Affairs Review
   // -------------------------------------------------------------------------
   private static void insertPublications(Connection conn) throws SQLException {
       String sql = "INSERT INTO Publications (PubID, Title, periodicity, topic) VALUES (?, ?, ?, ?)";
       Object[][] rows = {
           {1, "Foundations of Distributed Databases", null,      "Databases"},
           {2, "Tech Weekly",                          "Weekly",  "Technology"},
           {3, "Global Affairs Review",                "Monthly", "News"}
       };
       insert(conn, "Publications", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Periodicals — Tech Weekly (Magazine) and Global Affairs Review (Journal)
   // -------------------------------------------------------------------------
   private static void insertPeriodicals(Connection conn) throws SQLException {
       String sql = "INSERT INTO Periodicals (PubID, type) VALUES (?, ?)";
       Object[][] rows = {
           {2, "Magazine"},
           {3, "Journal"}
       };
       insert(conn, "Periodicals", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Books
   // Edition 1: PubID=1, ISBN=9781458300001, published 2025-03-15, written 2025-03-15
   // Edition 2: PubID=1 is the parent pub; we need a second PubID for edition 2.
   // The schema has one row per PubID in Books, so Edition 2 gets PubID=4 (new pub entry).
   // -------------------------------------------------------------------------
   private static void insertBooks(Connection conn) throws SQLException {
       // Insert a stub publication for Edition 2 (same title, different PubID to hold the second ISBN)
       String pubSql = "INSERT INTO Publications (PubID, Title, periodicity, topic) VALUES (?, ?, ?, ?)";
       Object[][] pubRows = {
           {4, "Foundations of Distributed Databases - Edition 2", null, "Databases"}
       };
       insert(conn, "Publications (Edition 2 stub)", pubSql, pubRows);

       String sql = "INSERT INTO Books (PubID, ISBN, full_text, edition_number, publication_date, written_date) VALUES (?, ?, ?, ?, ?, ?)";
       Object[][] rows = {
           // Edition 1 — Ch1 Distributed Transactions full text used as book full_text placeholder
           {1, "9781458300001",
            "Modern distributed database systems coordinate data across multiple sites, often separated by large geographic distances. Ensuring that a transaction commits consistently at every participating node is one of the central challenges of distributed computing.",
            1, "2025-03-15", "2025-03-15"},
           // Edition 2
           {4, "9781458300002",
            "As distributed infrastructures scale to cloud environments, transaction coordination becomes increasingly complex. Newer consensus-based approaches build upon traditional commit protocols to improve fault tolerance and availability.",
            2, "2026-02-01", "2026-02-01"}
       };
       insert(conn, "Books", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Issues
   // IID 1 = Tech Weekly 2026-02-01  (subtitle: Tech Weekly - February 1, 2026)
   // IID 2 = Tech Weekly 2026-02-08
   // IID 3 = Tech Weekly 2026-02-15
   // IID 4 = Global Affairs Review 2026-01-01
   // IID 5 = Global Affairs Review 2026-02-01
   // -------------------------------------------------------------------------
   private static void insertIssues(Connection conn) throws SQLException {
       String sql = "INSERT INTO Issues (IID, subtitle, Publication_Date) VALUES (?, ?, ?)";
       Object[][] rows = {
           {1, "Tech Weekly - February 1, 2026",          "2026-02-01"},
           {2, "Tech Weekly - February 8, 2026",          "2026-02-08"},
           {3, "Tech Weekly - February 15, 2026",         "2026-02-15"},
           {4, "Global Affairs Review - January 2026",    "2026-01-01"},
           {5, "Global Affairs Review - February 2026",   "2026-02-01"}
       };
       insert(conn, "Issues", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Articles
   // -------------------------------------------------------------------------
   private static void insertArticles(Connection conn) throws SQLException {
       String sql = "INSERT INTO Articles (IID, Title, full_text, written_date, topic) VALUES (?, ?, ?, ?, ?)";
       Object[][] rows = {
           // Issue 1: AI in 2026
           {1, "AI in 2026",
            "Artificial intelligence systems in 2026 operate at unprecedented scale, integrating multimodal inputs and real-time decision making. Rapid advances in model efficiency have made AI deployment feasible across industries.",
            "2026-01-20", "Artificial Intelligence"},
           // Issue 2: Quantum Computing Basics
           {2, "Quantum Computing Basics",
            "Quantum computing departs fundamentally from classical computation by leveraging superposition and entanglement. Even small quantum systems demonstrate behaviors that have no classical equivalent.",
            "2026-01-25", "Quantum Computing"},
           // Issue 2: Data Privacy in Practice
           {2, "Data Privacy in Practice",
            "Organizations increasingly face the challenge of protecting user data while maintaining usability and performance. Regulatory frameworks now require concrete safeguards and transparent data-handling practices.",
            "2026-01-20", "Cybersecurity"},
           // Issue 3: Edge AI Applications
           {3, "Edge AI Applications",
            "Deploying AI models on edge devices reduces latency and enhances privacy by processing data locally. Advances in hardware acceleration have made on-device inference more practical than ever before.",
            "2026-02-01", "Artificial Intelligence"},
           // Issue 4: Election Trends Worldwide
           {4, "Election Trends Worldwide",
            "Recent elections across multiple regions reveal shifting voter priorities and evolving campaign strategies. Digital platforms now play a decisive role in shaping public discourse.",
            "2025-12-15", "International Politics"},
           // Issue 5: Energy Policy Updates
           {5, "Energy Policy Updates",
            "Global energy markets continue to respond to geopolitical pressures and climate commitments. Policymakers are balancing economic growth with long-term sustainability goals.",
            "2026-01-05", "Public Policy"}
       };
       insert(conn, "Articles", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Orders
   // OID 1 = Capitol City Books / Global Affairs Review Jan 2026    (date 2026-01-03)
   // OID 2 = Capitol City Books / Global Affairs Review Feb 2026    (date 2026-02-03)
   // OID 3 = Eastern Academic / Tech Weekly Feb 1                   (date 2026-02-03)
   // OID 4 = Eastern Academic / Tech Weekly Feb 8                   (date 2026-02-11)
   // OID 5 = Eastern Academic / Tech Weekly Feb 15                  (date 2026-02-17)
   // OID 6 = Triangle Books / Foundations Ed2                       (date 2026-02-10)
   // OID 7 = Wake County Library / Foundations Ed2                  (date 2026-02-12)
   // date_due is set 14 days after date_ordered as a reasonable default.
   // -------------------------------------------------------------------------
   private static void insertOrders(Connection conn) throws SQLException {
       String sql = "INSERT INTO Orders (OID, date_ordered, shipping_fee, date_due, is_produced) VALUES (?, ?, ?, ?, ?)";
       Object[][] rows = {
           {1, "2026-01-03", 160.00, "2026-01-17", true},
           {2, "2026-02-03", 140.00, "2026-02-17", true},
           {3, "2026-02-03", 750.00, "2026-02-17", true},
           {4, "2026-02-11", 750.00, "2026-02-25", true},
           {5, "2026-02-17", 600.00, "2026-03-03", true},
           {6, "2026-02-10", 350.00, "2026-02-24", true},
           {7, "2026-02-12", 120.00, "2026-02-26", true}
       };
       insert(conn, "Orders", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Orders_books — book orders (OID 6 and 7 are for Foundations Ed2, PubID=4)
   // -------------------------------------------------------------------------
   private static void insertOrdersBooks(Connection conn) throws SQLException {
       String sql = "INSERT INTO Orders_books (OID, PubID, unit_price, number_of_copies) VALUES (?, ?, ?, ?)";
       Object[][] rows = {
           {6, 4, 85.00, 120},  // Triangle Books — Foundations Ed2
           {7, 4, 85.00,  40}   // Wake County Library — Foundations Ed2
       };
       insert(conn, "Orders_books", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Orders_issues — issue orders
   // -------------------------------------------------------------------------
   private static void insertOrdersIssues(Connection conn) throws SQLException {
       String sql = "INSERT INTO Orders_issues (OID, IID, unit_price, number_of_copies) VALUES (?, ?, ?, ?)";
       Object[][] rows = {
           {1, 4, 15.00,  90},  // Capitol City Books — Global Affairs Review Jan 2026
           {2, 5, 15.00,  75},  // Capitol City Books — Global Affairs Review Feb 2026
           {3, 1, 12.00, 650},  // Eastern Academic — Tech Weekly Feb 1
           {4, 2, 12.00, 650},  // Eastern Academic — Tech Weekly Feb 8
           {5, 3, 12.00, 500}   // Eastern Academic — Tech Weekly Feb 15
       };
       insert(conn, "Orders_issues", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Distributors
   // DID 1 = Triangle Books
   // DID 2 = Eastern Academic Wholesale
   // DID 3 = Wake County Public Library
   // DID 4 = Capitol City Books
   // -------------------------------------------------------------------------
   private static void insertDistributors(Connection conn) throws SQLException {
       String sql = "INSERT INTO Distributors (DID, name, phone_number, category, outstanding_balance, addr, contact) VALUES (?, ?, ?, ?, ?, ?, ?)";
       Object[][] rows = {
           {1, "Triangle Books",            "(919) 555-2101", "Bookstore",             4850.00,   "142 Hillsborough Street, Raleigh, NC 27603",  "Laura Thompson"},
           {2, "Eastern Academic Wholesale","(704) 555-7782", "Wholesale Distributor", 12300.00,  "850 Logistics Parkway, Charlotte, NC 28208",  "Mark Reynolds"},
           {3, "Wake County Public Library","(919) 555-4433", "Library",                  0.00,   "336 Fayetteville Street, Raleigh, NC 27601",  "Emily Carter"},
           {4, "Capitol City Books",        "(919) 555-6610", "Bookstore",             2175.50,   "91 Market Square, Durham, NC 27701",          "Daniel Wright"}
       };
       insert(conn, "Distributors", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Workers
   // Authors (worker_type = "author"):
   //   EID 1 = Alice Morgan
   //   EID 2 = Sarah Lee
   //   EID 3 = Michael Brown
   //   EID 4 = Emily Zhang
   //   EID 5 = Daniel Kim
   //   EID 6 = Laura Martinez
   //   EID 7 = Robert Singh
   // Editors (worker_type = "editor"):
   //   EID 8  = Olivia Bennett
   //   EID 9  = Helena Strauss
   //   EID 10 = Daniel Whitmore
   // salary set to 0 for freelance/per-work contributors (non-salaried)
   // -------------------------------------------------------------------------
   private static void insertWorkers(Connection conn) throws SQLException {
       String sql = "INSERT INTO Workers (EID, is_employee, worker_type, salary, worker_name) VALUES (?, ?, ?, ?, ?)";
       Object[][] rows = {
           {1,  false, "author", 0.00, "Alice Morgan"},
           {2,  false, "author", 0.00, "Sarah Lee"},
           {3,  false, "author", 0.00, "Michael Brown"},
           {4,  false, "author", 0.00, "Emily Zhang"},
           {5,  false, "author", 0.00, "Daniel Kim"},
           {6,  false, "author", 0.00, "Laura Martinez"},
           {7,  false, "author", 0.00, "Robert Singh"},
           {8,  true,  "editor", 0.00, "Olivia Bennett"},
           {9,  true,  "editor", 0.00, "Helena Strauss"},
           {10, true,  "editor", 0.00, "Daniel Whitmore"}
       };
       insert(conn, "Workers", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Worker_Payments (15 payments from the demo data)
   // work_payment_type: "author" or "editor" based on payee_role
   // -------------------------------------------------------------------------
   private static void insertWorkerPayments(Connection conn) throws SQLException {
       String sql = "INSERT INTO Worker_Payments (PID, amount, work_payment_type, pay_claim_date, pay_issue_date) VALUES (?, ?, ?, ?, ?)";
       Object[][] rows = {
           {1,  2500.00, "author", "2025-04-05", "2025-03-20"},  // Alice Morgan — Ch1 Distributed Transactions
           {2,  1000.00, "author", null,          "2026-02-05"},  // Alice Morgan — Ch1 Distributed Transactions (Ed2)
           {3,  2000.00, "author", null,          "2026-02-10"},  // Alice Morgan — Ch2 Data Replication
           {4,  1200.00, "author", "2026-02-03",  "2026-01-28"},  // Sarah Lee — AI in 2026
           {5,  1100.00, "author", null,          "2026-01-30"},  // Michael Brown — Quantum Computing Basics
           {6,  1150.00, "author", "2026-02-02",  "2026-01-25"},  // Emily Zhang — Data Privacy in Practice
           {7,  1300.00, "author", null,          "2026-02-05"},  // Daniel Kim — Edge AI Applications
           {8,  1250.00, "author", "2026-01-05",  "2025-12-20"},  // Laura Martinez — Election Trends Worldwide
           {9,  1180.00, "author", null,          "2026-01-10"},  // Robert Singh — Energy Policy Updates
           {10, 4000.00, "editor", "2025-04-10",  "2025-03-15"},  // Olivia Bennett — Foundations Ed1
           {11, 4000.00, "editor", "2026-02-10",  "2026-02-01"},  // Olivia Bennett — Foundations Ed2
           {12, 2500.00, "editor", null,          "2026-02-28"},  // Olivia Bennett — Tech Weekly (3 issues)
           {13, 2000.00, "editor", "2026-03-05",  "2026-02-28"},  // Helena Strauss — Tech Weekly (3 issues)
           {14, 3000.00, "editor", "2026-02-07",  "2026-01-31"},  // Daniel Whitmore — Global Affairs Review Jan
           {15, 3000.00, "editor", null,          "2026-02-28"}   // Daniel Whitmore — Global Affairs Review Feb
       };
       insert(conn, "Worker_Payments", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Distributor_payments (6 payments from the demo data)
   // -------------------------------------------------------------------------
   private static void insertDistributorPayments(Connection conn) throws SQLException {
       String sql = "INSERT INTO Distributor_payments (DBID, payment_date, payment_amount) VALUES (?, ?, ?)";
       Object[][] rows = {
           {1, "2026-02-20", 15000.00},  // Triangle Books
           {2, "2026-02-15", 26000.00},  // Eastern Academic Wholesale
           {3, "2026-02-20",  5100.00},  // Eastern Academic Wholesale
           {4, "2026-02-18",  3520.00},  // Wake County Public Library
           {5, "2026-01-20",  3600.00},  // Capitol City Books
           {6, "2026-02-10",  1299.50}   // Capitol City Books
       };
       insert(conn, "Distributor_payments", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Works_on_articles
   // Maps worker EID to (IID, article title)
   // -------------------------------------------------------------------------
   private static void insertWorksOnArticles(Connection conn) throws SQLException {
       String sql = "INSERT INTO Works_on_articles (EID, IID, Title) VALUES (?, ?, ?)";
       Object[][] rows = {
           {2, 1, "AI in 2026"},                  // Sarah Lee
           {3, 2, "Quantum Computing Basics"},    // Michael Brown
           {4, 2, "Data Privacy in Practice"},    // Emily Zhang
           {5, 3, "Edge AI Applications"},        // Daniel Kim
           {6, 4, "Election Trends Worldwide"},   // Laura Martinez
           {7, 5, "Energy Policy Updates"}        // Robert Singh
       };
       insert(conn, "Works_on_articles", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Works_on_books
   // Alice Morgan (EID=1) authored both book editions (PubID 1 and 4)
   // Olivia Bennett (EID=8) edited both (also cover Works_on_books for editors)
   // -------------------------------------------------------------------------
   private static void insertWorksOnBooks(Connection conn) throws SQLException {
       String sql = "INSERT INTO Works_on_books (EID, PubID) VALUES (?, ?)";
       Object[][] rows = {
           {1,  1},   // Alice Morgan — Foundations Ed1
           {1,  4},   // Alice Morgan — Foundations Ed2
           {8,  1},   // Olivia Bennett (editor) — Foundations Ed1
           {8,  4}    // Olivia Bennett (editor) — Foundations Ed2
       };
       insert(conn, "Works_on_books", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Places — links Orders to Distributors
   // -------------------------------------------------------------------------
   private static void insertPlaces(Connection conn) throws SQLException {
       String sql = "INSERT INTO Places (OID, DID) VALUES (?, ?)";
       Object[][] rows = {
           {1, 4},   // OID 1 — Capitol City Books (DID=4)
           {2, 4},   // OID 2 — Capitol City Books (DID=4)
           {3, 2},   // OID 3 — Eastern Academic Wholesale (DID=2)
           {4, 2},   // OID 4 — Eastern Academic Wholesale (DID=2)
           {5, 2},   // OID 5 — Eastern Academic Wholesale (DID=2)
           {6, 1},   // OID 6 — Triangle Books (DID=1)
           {7, 3}    // OID 7 — Wake County Public Library (DID=3)
       };
       insert(conn, "Places", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Make — links Distributor_payments to Distributors
   // -------------------------------------------------------------------------
   private static void insertMake(Connection conn) throws SQLException {
       String sql = "INSERT INTO Make (DBID, DID) VALUES (?, ?)";
       Object[][] rows = {
           {1, 1},   // DBID 1 — Triangle Books (DID=1)
           {2, 2},   // DBID 2 — Eastern Academic Wholesale (DID=2)
           {3, 2},   // DBID 3 — Eastern Academic Wholesale (DID=2)
           {4, 3},   // DBID 4 — Wake County Public Library (DID=3)
           {5, 4},   // DBID 5 — Capitol City Books (DID=4)
           {6, 4}    // DBID 6 — Capitol City Books (DID=4)
       };
       insert(conn, "Make", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Made_of — links Publications (periodicals) to Issues
   // -------------------------------------------------------------------------
   private static void insertMadeOf(Connection conn) throws SQLException {
       String sql = "INSERT INTO Made_of (PubID, IID) VALUES (?, ?)";
       Object[][] rows = {
           {2, 1},   // Tech Weekly — Issue 1 (Feb 1)
           {2, 2},   // Tech Weekly — Issue 2 (Feb 8)
           {2, 3},   // Tech Weekly — Issue 3 (Feb 15)
           {3, 4},   // Global Affairs Review — Issue 4 (Jan)
           {3, 5}    // Global Affairs Review — Issue 5 (Feb)
       };
       insert(conn, "Made_of", sql, rows);
   }
   // -------------------------------------------------------------------------
   // Get_Paid — links Worker_Payments to Workers
   // -------------------------------------------------------------------------
   private static void insertGetPaid(Connection conn) throws SQLException {
       String sql = "INSERT INTO Get_Paid (PID, EID) VALUES (?, ?)";
       Object[][] rows = {
           {1,  1},   // PID 1  — Alice Morgan (EID=1)
           {2,  1},   // PID 2  — Alice Morgan (EID=1)
           {3,  1},   // PID 3  — Alice Morgan (EID=1)
           {4,  2},   // PID 4  — Sarah Lee (EID=2)
           {5,  3},   // PID 5  — Michael Brown (EID=3)
           {6,  4},   // PID 6  — Emily Zhang (EID=4)
           {7,  5},   // PID 7  — Daniel Kim (EID=5)
           {8,  6},   // PID 8  — Laura Martinez (EID=6)
           {9,  7},   // PID 9  — Robert Singh (EID=7)
           {10, 8},   // PID 10 — Olivia Bennett (EID=8)
           {11, 8},   // PID 11 — Olivia Bennett (EID=8)
           {12, 8},   // PID 12 — Olivia Bennett (EID=8)
           {13, 9},   // PID 13 — Helena Strauss (EID=9)
           {14, 10},  // PID 14 — Daniel Whitmore (EID=10)
           {15, 10}   // PID 15 — Daniel Whitmore (EID=10)
       };
       insert(conn, "Get_Paid", sql, rows);
   }
}

