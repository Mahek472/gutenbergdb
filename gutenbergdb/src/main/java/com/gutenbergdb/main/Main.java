package com.gutenbergdb.main;

import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.gutenbergdb.dao.PublicationDAO;
import com.gutenbergdb.dao.DistributorDAO;
import com.gutenbergdb.dao.ProductionDAO;
import com.gutenbergdb.dao.ReportDAO;

public class Main {
    public static void main(String[] args) {
        try {
            PublicationDAO publicationDAO = new PublicationDAO();

            DistributorDAO distributorDAO = new DistributorDAO();

            ReportDAO reportDAO = new ReportDAO();

            ProductionDAO productionDAO = new ProductionDAO();

            Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== Main Menu =====");
            System.out.println("1. Publication Menu");
            System.out.println("2. Distributor Menu");
            System.out.println("3. Reports");
            System.out.println("4. Production Menu");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1: {
                        while (true) {
                            System.out.println("\n===== Publication Menu =====");
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
                            System.out.println("14. Back To Main Menu");
                            System.out.print("Enter your choice: ");

                            try {
                                int pubChoice = Integer.parseInt(scanner.nextLine());

                                switch (pubChoice) {
                                    case 1:
                                        publicationDAO.insertNewPeriodical();
                                        break;
                                    case 2:
                                        publicationDAO.insertNewBook();
                                        break;
                                    case 3:
                                        publicationDAO.showBookDetails();
                                        break;
                                    case 4:
                                        publicationDAO.showPeriodical();
                                        break;
                                    case 5:
                                        publicationDAO.updatePeriodical();
                                        break;
                                    case 6:
                                        publicationDAO.updateBookEdition();
                                        break;
                                    case 7:
                                        publicationDAO.assignWorkerToPublication();
                                        break;
                                    case 8:
                                        publicationDAO.removeWorkerFromPublication();
                                        break;
                                    case 9:
                                        publicationDAO.showWorkersAssignedToPublication();
                                        break;
                                    case 10:
                                        publicationDAO.showPublicationsForWorker();
                                        break;
                                    case 11:
                                        publicationDAO.showTableOfContents();
                                        break;
                                    case 12:
                                        publicationDAO.addArticleToPeriodicPublication();
                                        break;
                                    case 13:
                                        publicationDAO.deleteArticleFromPeriodicPublication();
                                        break;
                                    case 14:
                                        break;
                                    default:
                                        System.out.println("Invalid choice. Please enter a number from 1 to 14.");
                                }

                                if (pubChoice == 14) {
                                    break;
                                }
                            } catch (Exception e) {
                                System.out.println("Operation failed: " + e.getMessage());
                            }
                        }
                        break;
                    }

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

                                        distributorDAO.enterNewDistributor(
                                                did_choice,
                                                dname_choice,
                                                dphone_choice,
                                                dcat_choice,
                                                daddr_choice,
                                                dcontact_choice
                                        );
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

                                        distributorDAO.updateDistributorInfo(
                                                did_choice,
                                                dname_choice,
                                                dphone_choice,
                                                dcat_choice,
                                                dbalance_choice,
                                                daddr_choice,
                                                dcontact_choice
                                        );
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

                                        distributorDAO.inputOrder(
                                                did_choice,
                                                identifier,
                                                date_ordered,
                                                shipping_fee,
                                                unit_price,
                                                num_copies,
                                                is_book
                                        );
                                        break;
                                    }

                                    case 5: {
                                        System.out.println("Enter number of orders:");
                                        int num_orders = Integer.parseInt(scanner.nextLine());

                                    for(int j = 0; j < num_orders; j++){
                                        System.out.println("Order " + (j+1) + ":");
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
                                case 6:
                                    System.out.println("Enter Distributor ID:");
                                    String did_choice = scanner.nextLine();

                                    System.out.println("Enter payment amount:");
                                    float payment_amount = Float.parseFloat(scanner.nextLine());

                                    System.out.println("Enter payment date (MM/DD/YYYY):");
                                    
                                    String payment_date = dbFormatter.format(LocalDate.parse(scanner.nextLine(), inputFormatter));

                                    distributorDAO.billDistributor(did_choice, payment_amount, payment_date);
                                    break;
                                    

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

                                    case 10: {
                                        break;
                                    }

                                    default: {
                                        System.out.println("Invalid choice. Please enter a number from 1 to 10.");
                                    }
                                }

                                if (distChoice == 10) {
                                    break;
                                }
                            } catch (Exception e) {
                                System.err.println("Error: " + e.getMessage());
                            }
                        }
                        break;
                    }

                    case 3: {
                        try {
                            reportDAO.reportPerDistributor();
                            reportDAO.reportPerWeek();
                            reportDAO.reportPerMonth();
                            reportDAO.weeklyRevenueExpenses();
                            reportDAO.monthlyRevenueExpenses();
                            reportDAO.totalDistributors();
                            reportDAO.revenuePerCity();
                            reportDAO.revenuePerDistributor();
                            reportDAO.paymentsPerMonth();
                            reportDAO.paymentsPerWorkType();
                        } catch (Exception e) {
                            System.err.println("Error: " + e.getMessage());
                        }
                        break;
                    }

                    case 4: {
                        while (true) {
                            System.out.println("\n===== Production Menu =====");
                            System.out.println("1. Enter New Book Edition");
                            System.out.println("2. Enter New Issue");
                            System.out.println("3. Update Book Edition");
                            System.out.println("4. Delete Book Edition");
                            System.out.println("5. Delete Issue");
                            System.out.println("6. Find Content By Topic");
                            System.out.println("7. Find Content By Date Range");
                            System.out.println("8. Find Content By Author");
                            System.out.println("9. Enter Worker Payment");
                            System.out.println("10. Update Payment Claimed");
                            System.out.println("11. List Unclaimed Payments");
                            System.out.println("12. Compare Two Issues");
                            System.out.println("13. Back To Main Menu");
                            System.out.print("Enter your choice: ");
                
                            try {
                                int productionChoice = Integer.parseInt(scanner.nextLine());
                
                                switch (productionChoice) {
                                    case 1: {
                                        System.out.print("Enter existing ISBN: ");
                                        String isbn = scanner.nextLine().trim();
                
                                        if (!productionDAO.isbnExists(isbn)) {
                                            System.out.println("ISBN " + isbn + " was not found. Add the original book before creating a new edition.");
                                            break;
                                        }
                
                                        System.out.print("Enter new PubID: ");
                                        int pubID = Integer.parseInt(scanner.nextLine().trim());
                
                                        System.out.print("Enter new edition title: ");
                                        String title = scanner.nextLine().trim();
                
                                        System.out.print("Enter new edition number: ");
                                        int edition = Integer.parseInt(scanner.nextLine().trim());
                
                                        System.out.print("Enter publication date (YYYY-MM-DD, blank allowed): ");
                                        String pubDate = scanner.nextLine().trim();
                
                                        System.out.print("Enter written date (YYYY-MM-DD, blank allowed): ");
                                        String writtenDate = scanner.nextLine().trim();
                
                                        System.out.print("Enter full text: ");
                                        String fullText = scanner.nextLine();
                
                                        productionDAO.enterBookEdition(pubID, isbn, title, edition, pubDate, writtenDate, fullText);
                                        break;
                                    }
                
                                    case 2: {
                                        System.out.print("Enter IID: ");
                                        int iid = Integer.parseInt(scanner.nextLine().trim());
                
                                        System.out.print("Enter PubID: ");
                                        int pubID = Integer.parseInt(scanner.nextLine().trim());
                
                                        System.out.print("Enter subtitle: ");
                                        String subtitle = scanner.nextLine().trim();
                
                                        System.out.print("Enter publication date (YYYY-MM-DD): ");
                                        String pubDate = scanner.nextLine().trim();
                
                                        productionDAO.enterIssue(iid, pubID, subtitle, pubDate);
                                        break;
                                    }
                
                                    case 3: {
                                        System.out.print("Enter PubID to update: ");
                                        int pubID = Integer.parseInt(scanner.nextLine().trim());
                
                                        System.out.print("Enter new ISBN (blank to keep current): ");
                                        String isbn = blankToNull(scanner.nextLine());
                
                                        System.out.print("Enter new edition number (blank to keep current): ");
                                        String editionInput = scanner.nextLine().trim();
                                        Integer edition = editionInput.isEmpty() ? null : Integer.parseInt(editionInput);
                
                                        System.out.print("Enter new publication date (YYYY-MM-DD, blank to keep current): ");
                                        String pubDate = blankToNull(scanner.nextLine());
                
                                        productionDAO.updateBookEdition(pubID, isbn, edition, pubDate);
                                        System.out.println("Book edition updated.");
                                        break;
                                    }
                
                                    case 4: {
                                        System.out.print("Enter PubID to delete: ");
                                        int pubID = Integer.parseInt(scanner.nextLine().trim());
                
                                        productionDAO.deleteBookEdition(pubID);
                                        System.out.println("Book edition deleted.");
                                        break;
                                    }
                
                                    case 5: {
                                        System.out.print("Enter IID to delete: ");
                                        int iid = Integer.parseInt(scanner.nextLine().trim());
                
                                        productionDAO.deleteIssue(iid);
                                        System.out.println("Issue deleted.");
                                        break;
                                    }
                
                                    case 6: {
                                        System.out.print("Enter topic keyword: ");
                                        String topic = scanner.nextLine().trim();
                
                                        productionDAO.findByTopic(topic);
                                        break;
                                    }
                
                                    case 7: {
                                        System.out.print("Enter start date (YYYY-MM-DD): ");
                                        String start = scanner.nextLine().trim();
                
                                        System.out.print("Enter end date (YYYY-MM-DD): ");
                                        String end = scanner.nextLine().trim();
                
                                        productionDAO.findByDateRange(start, end);
                                        break;
                                    }
                
                                    case 8: {
                                        System.out.print("Enter author name: ");
                                        String name = scanner.nextLine().trim();
                
                                        productionDAO.findByAuthor(name);
                                        break;
                                    }
                
                                    case 9: {
                                        System.out.print("Enter payment ID (PID): ");
                                        int pid = Integer.parseInt(scanner.nextLine().trim());
                
                                        System.out.print("Enter employee ID (EID): ");
                                        int eid = Integer.parseInt(scanner.nextLine().trim());
                
                                        System.out.print("Enter payment amount: ");
                                        double amount = Double.parseDouble(scanner.nextLine().trim());
                
                                        System.out.print("Enter payment type: ");
                                        String type = scanner.nextLine().trim();
                
                                        System.out.print("Enter issue date (YYYY-MM-DD): ");
                                        String issueDate = scanner.nextLine().trim();
                
                                        productionDAO.enterWorkerPayment(pid, eid, amount, type, issueDate);
                                        break;
                                    }
                
                                    case 10: {
                                        System.out.print("Enter payment ID (PID): ");
                                        int pid = Integer.parseInt(scanner.nextLine().trim());
                
                                        System.out.print("Enter claim date (YYYY-MM-DD): ");
                                        String claimDate = scanner.nextLine().trim();
                
                                        productionDAO.updatePaymentClaimed(pid, claimDate);
                                        break;
                                    }
                
                                    case 11: {
                                        System.out.print("Enter start date (YYYY-MM-DD): ");
                                        String start = scanner.nextLine().trim();
                
                                        System.out.print("Enter end date (YYYY-MM-DD): ");
                                        String end = scanner.nextLine().trim();
                
                                        productionDAO.listUnclaimedPayments(start, end);
                                        break;
                                    }
                
                                    case 12: {
                                        System.out.print("Enter first IID: ");
                                        int iid1 = Integer.parseInt(scanner.nextLine().trim());
                
                                        System.out.print("Enter second IID: ");
                                        int iid2 = Integer.parseInt(scanner.nextLine().trim());
                
                                        productionDAO.compareIssues(iid1, iid2);
                                        break;
                                    }
                
                                    case 13:
                                        break;
                
                                    default:
                                        System.out.println("Invalid choice. Please enter a number from 1 to 13.");
                                }
                            } catch (Exception e) {
                                System.out.println("Operation failed: " + e.getMessage());
                            }
                        }
                    }

                    case 5: {
                        System.out.println("Exiting program.");
                        scanner.close();
                        return;
                    }

                    default: {
                        System.out.println("Invalid choice. Please enter a number from 1 to 5.");
                    }
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

    private static String blankToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
