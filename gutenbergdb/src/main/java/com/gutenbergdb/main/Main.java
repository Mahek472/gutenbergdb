package com.gutenbergdb.main;

import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.gutenbergdb.dao.PublicationDAO;
import com.gutenbergdb.dao.DistributorDAO;
import com.gutenbergdb.dao.ReportDAO;
import com.gutenbergdb.dao.ProductionDAO;

public class Main {
    public static void main(String[] args) {
        try {
            PublicationDAO publicationDAO = new PublicationDAO();
            DistributorDAO distributorDAO = new DistributorDAO();
            ReportDAO reportDAO = new ReportDAO();
            ProductionDAO productionDAO = new ProductionDAO();

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\nMain Menu");
                System.out.println("1. Publication Menu");
                System.out.println("2. Distributor Menu");
                System.out.println("3. Reports");
                System.out.println("4. Production Menu");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");

                try {
                    int choice = Integer.parseInt(scanner.nextLine());

                    switch (choice) {

                        // ================= PUBLICATION MENU =================
                        case 1: {
                            while (true) {
                                System.out.println("\nPublication Menu");
                                System.out.println("1. Insert New Periodical");
                                System.out.println("2. Insert New Book");
                                System.out.println("3. Show Book Details");
                                System.out.println("4. Show Periodical Details");
                                System.out.println("5. Update Periodical");
                                System.out.println("6. Update Book Edition");
                                System.out.println("7. Assign Worker To Publication");
                                System.out.println("8. Remove Worker From Publication");
                                System.out.println("9. Show Workers Assigned To Publication");
                                System.out.println("10. Show Publications For Worker");
                                System.out.println("11. Show Table Of Contents");
                                System.out.println("12. Add Article To Periodic Publication");
                                System.out.println("13. Delete Article From Periodic Publication");
                                System.out.println("14. Add Chapter To Book");
                                System.out.println("15. Delete Chapter From Book");
                                System.out.println("16. Back To Main Menu");

                                try {
                                    int pubChoice = Integer.parseInt(scanner.nextLine());

                                    switch (pubChoice) {
                                        case 1: publicationDAO.insertNewPeriodical(); break;
                                        case 2: publicationDAO.insertNewBook(); break;
                                        case 3: publicationDAO.showBookDetails(); break;
                                        case 4: publicationDAO.showPeriodical(); break;
                                        case 5: publicationDAO.updatePeriodical(); break;
                                        case 6: publicationDAO.updateBookEdition(); break;
                                        case 7: publicationDAO.assignWorkerToPublication(); break;
                                        case 8: publicationDAO.removeWorkerFromPublication(); break;
                                        case 9: publicationDAO.showWorkersAssignedToPublication(); break;
                                        case 10: publicationDAO.showPublicationsForWorker(); break;
                                        case 11: publicationDAO.showTableOfContents(); break;
                                        case 12: publicationDAO.addArticleToPeriodicPublication(); break;
                                        case 13: publicationDAO.deleteArticleFromPeriodicPublication(); break;
                                        case 14: publicationDAO.addChapterToBook(); break;
                                        case 15: publicationDAO.deleteChapterFromBook(); break;
                                        case 16: break;
                                        default:
                                            System.out.println("Invalid choice. Enter 1–16.");
                                    }

                                    if (pubChoice == 16) break;

                                } catch (Exception e) {
                                    System.out.println("Operation failed: " + e.getMessage());
                                }
                            }
                            break;
                        }

                        // ================= DISTRIBUTOR MENU =================
                        case 2: {
                            while (true) {
                                System.out.println("\n===== Distributor Menu =====");
                                System.out.println("1. Insert New Distributor");
                                System.out.println("2. Update Distributor");
                                System.out.println("3. Delete Distributor");
                                System.out.println("4. Input Order");
                                System.out.println("5. Input Multiple Orders");
                                System.out.println("6. Bill Distributor");
                                System.out.println("7. Change Distributor Balance");
                                System.out.println("8. Identify Non-Matching Distributor Balances");
                                System.out.println("9. Identify Distributors of certain type or location");
                                System.out.println("10. Back To Main Menu");
                                System.out.print("Enter your choice: ");

                                int distChoice = Integer.parseInt(scanner.nextLine());
                                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                                DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                try {
                                    switch (distChoice) {
                                        case 1: {
                                            System.out.println("Enter Distributor ID:");
                                            String did_choice = scanner.nextLine();
                                            System.out.println("Enter Distributor Name:");
                                            String dname_choice = scanner.nextLine();
                                            System.out.println("Enter Distributor Phone #:");
                                            String dphone_choice = scanner.nextLine();
                                            System.out.println("Enter Distributor Category:");
                                            String dcat_choice = scanner.nextLine();
                                            System.out.println("Enter Distributor Address:");
                                            String daddr_choice = scanner.nextLine();
                                            System.out.println("Enter Distributor Contact:");
                                            String dcontact_choice = scanner.nextLine();
                                            distributorDAO.enterNewDistributor(did_choice, dname_choice, dphone_choice, dcat_choice, daddr_choice, dcontact_choice);
                                            break;
                                        }

                                        case 2: {
                                            System.out.println("Enter Distributor ID:");
                                            String did_choice = scanner.nextLine();
                                            System.out.println("Enter Distributor Name:");
                                            String dname_choice = scanner.nextLine();
                                            System.out.println("Enter Distributor Phone #:");
                                            String dphone_choice = scanner.nextLine();
                                            System.out.println("Enter Distributor Category:");
                                            String dcat_choice = scanner.nextLine();
                                            System.out.println("Enter Distributor Outstanding Balance:");
                                            float dbalance_choice = Float.parseFloat(scanner.nextLine());
                                            System.out.println("Enter Distributor Address:");
                                            String daddr_choice = scanner.nextLine();
                                            System.out.println("Enter Distributor Contact:");
                                            String dcontact_choice = scanner.nextLine();
                                            distributorDAO.updateDistributorInfo(did_choice, dname_choice, dphone_choice, dcat_choice, dbalance_choice, daddr_choice, dcontact_choice);
                                            break;
                                        }

                                        case 3: {
                                            System.out.println("Enter Distributor ID:");
                                            String did_choice = scanner.nextLine();
                                            distributorDAO.deleteDistributor(did_choice);
                                            break;
                                        }

                                        case 4: {
                                            System.out.println("Enter Distributor ID:");
                                            String did_choice = scanner.nextLine();
                                            System.out.println("Is the order for a book? (true/false):");
                                            boolean is_book = Boolean.parseBoolean(scanner.nextLine());
                                            String identifier;
                                            if (is_book) {
                                                System.out.println("Enter ISBN:");
                                                identifier = scanner.nextLine();
                                            } else {
                                                System.out.println("Enter Issue ID (IID):");
                                                identifier = scanner.nextLine();
                                            }
                                            System.out.println("Enter Date Ordered (MM/DD/YYYY):");
                                            String date_ordered = dbFormatter.format(LocalDate.parse(scanner.nextLine(), inputFormatter));
                                            System.out.println("Enter Shipping Fee:");
                                            float shipping_fee = Float.parseFloat(scanner.nextLine());
                                            System.out.println("Enter Unit Price:");
                                            float unit_price = Float.parseFloat(scanner.nextLine());
                                            System.out.println("Enter Number of Copies:");
                                            int num_copies = Integer.parseInt(scanner.nextLine());
                                            distributorDAO.inputOrder(did_choice, identifier, date_ordered, shipping_fee, unit_price, num_copies, is_book);
                                            break;
                                        }

                                        case 5: {
                                            System.out.println("Enter number of orders:");
                                            int num_orders = Integer.parseInt(scanner.nextLine());
                                            for (int j = 0; j < num_orders; j++) {
                                                System.out.println("Order " + (j + 1) + ":");
                                                System.out.println("Enter Distributor ID:");
                                                String did_choice = scanner.nextLine();
                                                System.out.println("Is the order for a book? (true/false):");
                                                boolean is_book = Boolean.parseBoolean(scanner.nextLine());
                                                String identifier;
                                                if (is_book) {
                                                    System.out.println("Enter ISBN:");
                                                    identifier = scanner.nextLine();
                                                } else {
                                                    System.out.println("Enter Issue ID (IID):");
                                                    identifier = scanner.nextLine();
                                                }
                                                System.out.println("Enter Date Ordered (MM/DD/YYYY):");
                                                String date_ordered = dbFormatter.format(LocalDate.parse(scanner.nextLine(), inputFormatter));
                                                System.out.println("Enter Shipping Fee:");
                                                float shipping_fee = Float.parseFloat(scanner.nextLine());
                                                System.out.println("Enter Unit Price:");
                                                float unit_price = Float.parseFloat(scanner.nextLine());
                                                System.out.println("Enter Number of Copies:");
                                                int num_copies = Integer.parseInt(scanner.nextLine());
                                                distributorDAO.inputOrder(did_choice, identifier, date_ordered, shipping_fee, unit_price, num_copies, is_book);
                                            }
                                            break;
                                        }

                                        case 6: {
                                            System.out.println("Enter Distributor ID:");
                                            String did_choice = scanner.nextLine();
                                            System.out.println("Enter payment amount:");
                                            float payment_amount = Float.parseFloat(scanner.nextLine());
                                            System.out.println("Enter payment date (MM/DD/YYYY):");
                                            String payment_date = dbFormatter.format(LocalDate.parse(scanner.nextLine(), inputFormatter));
                                            distributorDAO.billDistributor(did_choice, payment_amount, payment_date);
                                            break;
                                        }

                                        case 7: {
                                            System.out.println("Enter Distributor ID:");
                                            String idid_choice = scanner.nextLine();
                                            System.out.println("Enter payment amount:");
                                            float ipayment_amount = Float.parseFloat(scanner.nextLine());
                                            System.out.println("Enter payment date (MM/DD/YYYY):");
                                            String ipayment_date = dbFormatter.format(LocalDate.parse(scanner.nextLine(), inputFormatter));
                                            distributorDAO.changeDistributorBalance(idid_choice, ipayment_amount, ipayment_date);
                                            break;
                                        }

                                        case 8: {
                                            System.out.println(distributorDAO.identifyNonMatchingDistributorBalances());
                                            break;
                                        }

                                        case 9: {
                                            System.out.println("Enter location:");
                                            String location = scanner.nextLine();
                                            System.out.println("Enter type:");
                                            String type = scanner.nextLine();
                                            System.out.println(distributorDAO.identifyDistributorInLocation(location, type));
                                            break;
                                        }

                                        case 10: break;

                                        default:
                                            System.out.println("Invalid choice. Please enter a number from 1 to 10.");
                                    }

                                    if (distChoice == 10) break;

                                } catch (Exception e) {
                                    System.err.println("Error: " + e.getMessage());
                                }
                            }
                            break;
                        }

                        // ================= REPORTS MENU =================
                        case 3: {
                            while (true) {
                                System.out.println("\n===== Reports Menu =====");
                                System.out.println("--- All-Data Reports ---");
                                System.out.println("1.  Publications Per Distributor (all)");
                                System.out.println("2.  Publications Sold Per Week (all)");
                                System.out.println("3.  Publications Sold Per Month (all)");
                                System.out.println("4.  Weekly Revenue & Expenses (all)");
                                System.out.println("5.  Monthly Revenue & Expenses (all)");
                                System.out.println("6.  Total Distributors");
                                System.out.println("7.  Revenue Per City");
                                System.out.println("8.  Revenue Per Distributor");
                                System.out.println("9.  Worker Payments Per Month (all)");
                                System.out.println("10. Worker Payments Per Work Type");
                                System.out.println("--- User-Input Reports ---");
                                System.out.println("11. Publications for a Specific Distributor");
                                System.out.println("12. Publications Sold for a Specific Week");
                                System.out.println("13. Publications Sold for a Specific Month");
                                System.out.println("14. Revenue & Expenses for a Specific Week");
                                System.out.println("15. Revenue & Expenses for a Specific Month");
                                System.out.println("16. Worker Payments for a Specific Week");
                                System.out.println("17. Worker Payments for a Specific Month");
                                System.out.println("18. Articles & Issues for a Specific Week");
                                System.out.println("19. Articles & Issues for a Specific Month");
                                System.out.println("20. Back To Main Menu");
                                System.out.print("Enter your choice: ");

                                try {
                                    int repChoice = Integer.parseInt(scanner.nextLine());

                                    switch (repChoice) {
                                        case 1:  reportDAO.reportPerDistributor(); break;
                                        case 2:  reportDAO.reportPerWeek(); break;
                                        case 3:  reportDAO.reportPerMonth(); break;
                                        case 4:  reportDAO.weeklyRevenueExpenses(); break;
                                        case 5:  reportDAO.monthlyRevenueExpenses(); break;
                                        case 6:  reportDAO.totalDistributors(); break;
                                        case 7:  reportDAO.revenuePerCity(); break;
                                        case 8:  reportDAO.revenuePerDistributor(); break;
                                        case 9:  reportDAO.paymentsPerMonth(); break;
                                        case 10: reportDAO.paymentsPerWorkType(); break;
                                        case 11: reportDAO.reportPerDistributorByInput(); break;
                                        case 12: reportDAO.reportPerWeekByInput(); break;
                                        case 13: reportDAO.reportPerMonthByInput(); break;
                                        case 14: reportDAO.weeklyRevenueExpensesByInput(); break;
                                        case 15: reportDAO.monthlyRevenueExpensesByInput(); break;
                                        case 16: reportDAO.paymentsPerWeekByInput(); break;
                                        case 17: reportDAO.paymentsPerMonthByInput(); break;
                                        case 18: reportDAO.articlesIssuesPerWeekByInput(); break;
                                        case 19: reportDAO.articlesIssuesPerMonthByInput(); break;
                                        case 20: break;
                                        default:
                                            System.out.println("Invalid choice. Enter 1–20.");
                                    }

                                    if (repChoice == 20) break;

                                } catch (Exception e) {
                                    System.out.println("Operation failed: " + e.getMessage());
                                }
                            }
                            break;
                        }

                        // ================= PRODUCTION MENU =================
                        case 4: {
                            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                            DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                            while (true) {
                                System.out.println("\n===== Production Menu =====");
                                System.out.println("--- Book & Issue Management ---");
                                System.out.println("1.  Enter New Book Edition");
                                System.out.println("2.  Enter New Issue");
                                System.out.println("3.  Update Book Edition");
                                System.out.println("4.  Delete Book Edition");
                                System.out.println("5.  Delete Issue");
                                System.out.println("--- Search & Browse ---");
                                System.out.println("6.  Find Books & Articles by Topic");
                                System.out.println("7.  Find Books & Articles by Date Range");
                                System.out.println("8.  Find Books & Articles by Author");
                                System.out.println("9.  Compare Two Issues");
                                System.out.println("--- Worker Payments ---");
                                System.out.println("10. Enter Worker Payment");
                                System.out.println("11. Mark Payment as Claimed");
                                System.out.println("12. List Unclaimed Payments in Date Range");
                                System.out.println("---");
                                System.out.println("13. Back To Main Menu");
                                System.out.print("Enter your choice: ");

                                try {
                                    int prodChoice = Integer.parseInt(scanner.nextLine());

                                    switch (prodChoice) {

                                        case 1: {
                                            // Enter New Book Edition — PubID-first workflow
                                            productionDAO.listAllPublications();
                                            System.out.print("Enter PubID of the existing publication: ");
                                            String pubID = scanner.nextLine().trim();

                                            // Fetch and display the existing publication so user can confirm
                                            String[] pubInfo = productionDAO.getPublicationInfo(pubID);
                                            if (pubInfo == null) {
                                                System.out.println("No publication found for PubID: " + pubID);
                                                break;
                                            }
                                            System.out.println("\nFound Publication:");
                                            System.out.printf("  PubID: %s | Title: %s | Topic: %s%n",
                                                    pubInfo[0], pubInfo[1], pubInfo[2]);

                                            // Show existing editions for this PubID
                                            productionDAO.listEditionsForPub(pubID);

                                            // Collect new edition details only
                                            System.out.print("\nEnter new ISBN for this edition: ");
                                            String newIsbn = scanner.nextLine().trim();
                                            System.out.print("Enter new edition title: ");
                                            String newTitle = scanner.nextLine();
                                            System.out.print("Enter edition number: ");
                                            int edition = Integer.parseInt(scanner.nextLine().trim());
                                            System.out.print("Enter publication date (MM/DD/YYYY): ");
                                            String pubDate = dbFormatter.format(LocalDate.parse(scanner.nextLine().trim(), inputFormatter));

                                            productionDAO.addBookEdition(pubID, newIsbn, newTitle, edition, pubDate);
                                            break;
                                        }

                                        case 2: {
                                            // Enter New Issue
                                            System.out.print("Enter Issue ID (IID, e.g. I006): ");
                                            String iid = scanner.nextLine().trim();
                                            System.out.print("Enter PubID of the periodical (e.g. PUB002): ");
                                            String pubID = scanner.nextLine().trim();
                                            System.out.print("Enter subtitle: ");
                                            String subtitle = scanner.nextLine();
                                            System.out.print("Enter publication date (MM/DD/YYYY): ");
                                            String pubDate = dbFormatter.format(LocalDate.parse(scanner.nextLine().trim(), inputFormatter));
                                            productionDAO.enterIssue(iid, pubID, subtitle, pubDate);
                                            break;
                                        }

                                        case 3: {
                                            // Update Book Edition
                                            System.out.print("Enter ISBN of the book to update: ");
                                            String isbn = scanner.nextLine().trim();
                                            System.out.print("Enter new edition number (or leave blank to keep existing): ");
                                            String edInput = scanner.nextLine().trim();
                                            Integer edition = edInput.isEmpty() ? null : Integer.parseInt(edInput);
                                            System.out.print("Enter new publication date (MM/DD/YYYY, or leave blank): ");
                                            String rawDate = scanner.nextLine().trim();
                                            String pubDate = rawDate.isEmpty() ? null : dbFormatter.format(LocalDate.parse(rawDate, inputFormatter));
                                            productionDAO.updateBookEdition(isbn, edition, pubDate);
                                            break;
                                        }

                                        case 4: {
                                            // Delete Book Edition
                                            System.out.print("Enter ISBN of the book edition to delete: ");
                                            String isbn = scanner.nextLine().trim();
                                            System.out.print("WARNING: This will permanently delete the book edition and all associated data. Confirm? (yes/no): ");
                                            String confirm = scanner.nextLine().trim();
                                            if (confirm.equalsIgnoreCase("yes")) {
                                                productionDAO.deleteBookEdition(isbn);
                                            } else {
                                                System.out.println("Deletion cancelled.");
                                            }
                                            break;
                                        }

                                        case 5: {
                                            // Delete Issue
                                            System.out.print("Enter IID of the issue to delete (e.g. I001): ");
                                            String iid = scanner.nextLine().trim();
                                            System.out.print("WARNING: This will permanently delete the issue and all associated articles. Confirm? (yes/no): ");
                                            String confirm = scanner.nextLine().trim();
                                            if (confirm.equalsIgnoreCase("yes")) {
                                                productionDAO.deleteIssue(iid);
                                            } else {
                                                System.out.println("Deletion cancelled.");
                                            }
                                            break;
                                        }

                                        case 6: {
                                            // Find by Topic
                                            System.out.print("Enter topic keyword: ");
                                            String topic = scanner.nextLine();
                                            productionDAO.findByTopic(topic);
                                            break;
                                        }

                                        case 7: {
                                            // Find by Date Range
                                            System.out.print("Enter start date (MM/DD/YYYY): ");
                                            String start = dbFormatter.format(LocalDate.parse(scanner.nextLine().trim(), inputFormatter));
                                            System.out.print("Enter end date (MM/DD/YYYY): ");
                                            String end = dbFormatter.format(LocalDate.parse(scanner.nextLine().trim(), inputFormatter));
                                            productionDAO.findByDateRange(start, end);
                                            break;
                                        }

                                        case 8: {
                                            // Find by Author
                                            System.out.print("Enter author name (or partial name): ");
                                            String name = scanner.nextLine();
                                            productionDAO.findByAuthor(name);
                                            break;
                                        }

                                        case 9: {
                                            // Compare Two Issues
                                            System.out.print("Enter first Issue ID (IID, e.g. I001): ");
                                            String iid1 = scanner.nextLine().trim();
                                            System.out.print("Enter second Issue ID (IID, e.g. I002): ");
                                            String iid2 = scanner.nextLine().trim();
                                            productionDAO.compareIssues(iid1, iid2);
                                            break;
                                        }

                                        case 10: {
                                            // Enter Worker Payment
                                            System.out.print("Enter Payment ID (PID, e.g. P016): ");
                                            String pid = scanner.nextLine().trim();
                                            System.out.print("Enter Worker ID (EID, e.g. E001): ");
                                            String eid = scanner.nextLine().trim();
                                            System.out.print("Enter payment amount: ");
                                            double amount = Double.parseDouble(scanner.nextLine().trim());
                                            System.out.print("Enter payment type/description: ");
                                            String type = scanner.nextLine();
                                            System.out.print("Enter issue date (MM/DD/YYYY): ");
                                            String issueDate = dbFormatter.format(LocalDate.parse(scanner.nextLine().trim(), inputFormatter));
                                            productionDAO.enterWorkerPayment(pid, eid, amount, type, issueDate);
                                            break;
                                        }

                                        case 11: {
                                            // Mark Payment as Claimed
                                            System.out.print("Enter Payment ID (PID, e.g. P001): ");
                                            String pid = scanner.nextLine().trim();
                                            System.out.print("Enter claim date (MM/DD/YYYY): ");
                                            String claimDate = dbFormatter.format(LocalDate.parse(scanner.nextLine().trim(), inputFormatter));
                                            productionDAO.updatePaymentClaimed(pid, claimDate);
                                            break;
                                        }

                                        case 12: {
                                            // List Unclaimed Payments in Date Range
                                            System.out.print("Enter start date (MM/DD/YYYY): ");
                                            String start = dbFormatter.format(LocalDate.parse(scanner.nextLine().trim(), inputFormatter));
                                            System.out.print("Enter end date (MM/DD/YYYY): ");
                                            String end = dbFormatter.format(LocalDate.parse(scanner.nextLine().trim(), inputFormatter));
                                            productionDAO.listUnclaimedPayments(start, end);
                                            break;
                                        }

                                        case 13: break;

                                        default:
                                            System.out.println("Invalid choice. Enter 1–13.");
                                    }

                                    if (prodChoice == 13) break;

                                } catch (Exception e) {
                                    System.out.println("Operation failed: " + e.getMessage());
                                }
                            }
                            break;
                        }

                        // ================= EXIT =================
                        case 5:
                            System.out.println("Exiting...");
                            scanner.close();
                            return;

                        default:
                            System.out.println("Invalid choice.");
                    }

                } catch (Exception e) {
                    System.out.println("Operation failed: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("FATAL ERROR during initialization:");
            e.printStackTrace();
            System.out.flush();
        }
    }
}
