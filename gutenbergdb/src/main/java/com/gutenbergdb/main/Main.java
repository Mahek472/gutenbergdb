package com.gutenbergdb.main;

import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.gutenbergdb.dao.PublicationDAO;
import com.gutenbergdb.dao.DistributorDAO;
import com.gutenbergdb.dao.ReportDAO;

public class Main {
    public static void main(String[] args) {

        PublicationDAO publicationDAO = new PublicationDAO();
        DistributorDAO distributorDAO = new DistributorDAO();
        ReportDAO reportDAO = new ReportDAO();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nMain Menu");
            System.out.println("1. Publication Menu");
            System.out.println("2. Distributor Menu");
            System.out.println("3. Reports");
            System.out.println("4. Exit");
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
                            System.out.println("9. Identify Distributors by location/type");
                            System.out.println("10. Back To Main Menu");

                            int distChoice = Integer.parseInt(scanner.nextLine());
                            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                            DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            try {
                                switch (distChoice) {

                                    case 1: {
                                        System.out.println("Enter Distributor ID:");
                                        int did_choice = Integer.parseInt(scanner.nextLine());

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

                                        distributorDAO.insertNewDistributor(
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

                                    case 2: {
                                        System.out.println("Enter Distributor ID:");
                                        int did_choice = Integer.parseInt(scanner.nextLine());

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
                                        int did_choice = Integer.parseInt(scanner.nextLine());

                                        distributorDAO.deleteDistributor(did_choice);
                                        break;
                                    }

                                    case 4: {
                                        System.out.println("Enter Distributor ID:");
                                        int did_choice = Integer.parseInt(scanner.nextLine());

                                        System.out.println("Enter Publication ID:");
                                        int pid_choice = Integer.parseInt(scanner.nextLine());

                                        System.out.println("Enter Date Ordered:");
                                        String date_ordered = scanner.nextLine();

                                        System.out.println("Enter Shipping Fee:");
                                        float shipping_fee = Float.parseFloat(scanner.nextLine());

                                        System.out.println("Enter Date Due:");
                                        String date_due = scanner.nextLine();

                                        System.out.println("Enter Unit Price:");
                                        float unit_price = Float.parseFloat(scanner.nextLine());

                                        System.out.println("Enter Number of Copies:");
                                        int num_copies = Integer.parseInt(scanner.nextLine());

                                        System.out.println("Is the order for a book? (true/false):");
                                        boolean is_book = Boolean.parseBoolean(scanner.nextLine());

                                        distributorDAO.inputOrder(
                                                did_choice,
                                                pid_choice,
                                                date_ordered,
                                                shipping_fee,
                                                date_due,
                                                unit_price,
                                                num_copies,
                                                is_book
                                        );
                                        break;
                                    }

                                    case 5: {
                                        System.out.println("Enter number of orders:");
                                        int num = Integer.parseInt(scanner.nextLine());

                                    for(int j = 0; j < num_orders; j++){
                                        System.out.println("Order " + (j+1) + ":");
                                        System.out.println("Enter Distributor ID:");
                                        int did_choice = Integer.parseInt(scanner.nextLine());

                                        System.out.println("Enter Publication ID:");
                                        int pid_choice = Integer.parseInt(scanner.nextLine());

                                        System.out.println("Enter Date Ordered:");
                                        String date_ordered = scanner.nextLine();

                                        System.out.println("Enter Shipping Fee:");
                                        float shipping_fee = Float.parseFloat(scanner.nextLine());

                                        System.out.println("Enter Date Due:");
                                        String date_due = scanner.nextLine();

                                        System.out.println("Enter Unit Price:");
                                        float unit_price = Float.parseFloat(scanner.nextLine());

                                        System.out.println("Enter Number of Copies:");
                                        int num_copies = Integer.parseInt(scanner.nextLine());

                                        System.out.println("Is the order for a book? (true/false):");
                                        boolean is_book = Boolean.parseBoolean(scanner.nextLine());

                                        distributorDAO.inputOrder(did_choice, pid_choice, date_ordered, shipping_fee, date_due, unit_price, num_copies, is_book); 
                                    }
                                    break;
                                case 6:
                                    System.out.println("Enter Distributor ID:");
                                    int did_choice = Integer.parseInt(scanner.nextLine());

                                        System.out.println("Enter payment amount:");
                                        float payment_amount = Float.parseFloat(scanner.nextLine());

                                        System.out.println("Enter payment date:");
                                        int payment_date = Integer.parseInt(scanner.nextLine());

                                        distributorDAO.billDistributor(did_choice, payment_amount, payment_date);
                                        break;
                                    }

                                    case 7: {
                                        System.out.println("Enter Distributor ID:");
                                        int did_choice = Integer.parseInt(scanner.nextLine());

                                        System.out.println("Enter payment amount:");
                                        float payment_amount = Float.parseFloat(scanner.nextLine());

                                        System.out.println("Enter payment date:");
                                        int payment_date = Integer.parseInt(scanner.nextLine());

                                        distributorDAO.changeDistributorBalance(did_choice, payment_amount, payment_date);
                                        break;
                                    }

                                    case 8: {
                                        distributorDAO.identifyNonMatchingDistributorBalances();
                                        break;
                                    }

                                    case 9: {
                                        System.out.println("Enter location:");
                                        String location = scanner.nextLine();

                                        System.out.println("Enter type:");
                                        String type = scanner.nextLine();

                                        distributorDAO.identifyDistributorInLocation(location, type);
                                        break;
                                    }

                                    case 10:
                                        break;

                                    default:
                                        System.out.println("Invalid choice.");
                                }

                                if (distChoice == 10) break;

                            } catch (Exception e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        }
                        break;
                    }

                    // ================= REPORTS =================
                    case 3: {
                        reportDAO.reportPerDistributor();
                        reportDAO.reportPerWeek();
                        reportDAO.reportPerMonth();
                        break;
                    }

                    // ================= EXIT =================
                    case 4:
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