package com.gutenbergdb.main;

import java.util.Scanner;

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

                            try {
                                switch (distChoice) {

                                    case 1: {
                                        int did = Integer.parseInt(scanner.nextLine());
                                        String name = scanner.nextLine();
                                        String phone = scanner.nextLine();
                                        String category = scanner.nextLine();
                                        float balance = Float.parseFloat(scanner.nextLine());
                                        String addr = scanner.nextLine();
                                        String contact = scanner.nextLine();

                                        distributorDAO.insertNewDistributor(did, name, phone, category, balance, addr, contact);
                                        break;
                                    }

                                    case 5: {
                                        System.out.println("Enter number of orders:");
                                        int num = Integer.parseInt(scanner.nextLine());

                                        for (int j = 0; j < num; j++) {
                                            System.out.println("Order " + (j + 1));

                                            int did = Integer.parseInt(scanner.nextLine());
                                            int pid = Integer.parseInt(scanner.nextLine());
                                            String date = scanner.nextLine();
                                            float fee = Float.parseFloat(scanner.nextLine());
                                            String due = scanner.nextLine();
                                            float price = Float.parseFloat(scanner.nextLine());
                                            int copies = Integer.parseInt(scanner.nextLine());
                                            boolean isBook = Boolean.parseBoolean(scanner.nextLine());

                                            distributorDAO.inputOrder(did, pid, date, fee, due, price, copies, isBook);
                                        }
                                        break;
                                    }

                                    case 6: {
                                        int did = Integer.parseInt(scanner.nextLine());
                                        float amount = Float.parseFloat(scanner.nextLine());
                                        String date = scanner.nextLine();   // FIXED

                                        distributorDAO.billDistributor(did, amount, date);
                                        break;
                                    }

                                    case 7: {
                                        int did = Integer.parseInt(scanner.nextLine());
                                        float amount = Float.parseFloat(scanner.nextLine());
                                        String date = scanner.nextLine();   // FIXED

                                        distributorDAO.changeDistributorBalance(did, amount, date);
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
    }
}