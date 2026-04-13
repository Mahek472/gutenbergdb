package com.gutenbergdb.main;
import java.util.Scanner;

import com.gutenbergdb.dao.PublicationDAO;
import com.gutenbergdb.dao.ReportDAO;

public class Main {
    public static void main(String[] args) {
        
         PublicationDAO publicationDAO = new PublicationDAO();
        Scanner scanner = new Scanner(System.in);

        while (true) {
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
    }
}
}