package com.gutenbergdb.main;
import java.util.Scanner;

import com.gutenbergdb.dao.PublicationDAO;
import com.gutenbergdb.dao.ReportDAO;

public class Main {
    public static void main(String[] args) {
        
         PublicationDAO publicationDAO = new PublicationDAO();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Do you want to go to 1. Publication, 2. Production, 3. Distribution, 4. Reports\n");
            int big_choice = Integer.parseInt(scanner.nextLine());
            try{
                switch(big_choice){
                    case 1:
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
                        System.out.println("14. Exit");
                        System.out.print("Enter your choice: ");

                        int choice = Integer.parseInt(scanner.nextLine());

                        try {
                            switch (choice) {
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
                                    System.out.println("Exiting program.");
                                    return;
                                default:
                                    System.out.println("Invalid choice. Please enter a number from 1 to 14.");
                            }
                        } catch (Exception e) {
                            System.out.println("Operation failed: " + e.getMessage());
                        }
                    case 2:
                    
                    case 3:
                        DistributorDAO distributorDAO = new DistributorDAO();

                        System.out.println("1. Insert New Distributor");
                        System.out.println("2. Update Distributor");
                        System.out.println("3. Delete Distributor");
                        System.out.println("4. Input Order");
                        System.out.println("5. Input Multiple Orders");
                        System.out.println("6. Bill Distributor");
                        System.out.println("7. Change Distributor Balance");
                        System.out.println("8. Identify Non-Matching Distributor Balances");
                        System.out.println("9. Identify Distributors of certain type or location");
                        System.out.print("Enter your choice: ");

                        int distChoice = Integer.parseInt(scanner.nextLine());
                        try{
                            switch (distChoice) {
                                case 1:
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

                                    distributorDAO.insertNewDistributor(did_choice, dname_choice, dphone_choice, dcat_choice, dbalance_choice, daddr_choice, dcontact_choice);
                                    break;
                                case 2:
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

                                    distributorDAO.updateDistributorInfo(did_choice, dname_choice, dphone_choice, dcat_choice, dbalance_choice, daddr_choice, dcontact_choice);
                                    break;
                                case 3:
                                    System.out.println("Enter Distributor ID:");
                                    int did_choice = Integer.parseInt(scanner.nextLine());

                                    distributorDAO.deleteDistributor(did_choice); 
                                    break;
                                case 4:
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
                                    break;
                                case 5:
                                    System.out.println("Enter number of orders:");
                                    int num_orders = Integer.parseInt(scanner.nextLine());

                                    distributorDAO.inputMultipleOrders(num_orders); 
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
                                case 7:
                                    System.out.println("Enter Distributor ID:");
                                    int did_choice = Integer.parseInt(scanner.nextLine());

                                    System.out.println("Enter payment amount:");
                                    float payment_amount = Float.parseFloat(scanner.nextLine());

                                    System.out.println("Enter payment date:");
                                    int payment_date = Integer.parseInt(scanner.nextLine());


                                    distributorDAO.changeDistributorBalance(did_choice, payment_amount, payment_date); 
                                    break;
                                case 8:
                                    distributorDAO.identifyNonMatchingDistributorBalances(); 
                                    break;
                                case 9:
                                    System.out.println("Enter location:");
                                    String loc = scanner.nextLine();

                                    System.out.println("Enter type:");
                                    String type = scanner.nextLine();

                                    distributorDAO.identifyDistributorInLocation(location, type); 
                                    break;
                            }
                            catch (Exception e) {
                                System.err.println("Error: " + e.getMessage());
                            }
                        
                    case 4:
                        ReportDAO reportDAO = new ReportDAO();
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
                    default:
                        System.out.println("Invalid choice. Please enter a number from 1 to 4.");
                }
                        }
            catch (Exception e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
            

        

        
    }
}
}