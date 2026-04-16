package com.gutenbergdb.main;

import java.util.Scanner;
import com.gutenbergdb.dao.ReportDAO;

public class Main {
    public static void main(String[] args) {
        ReportDAO reportDAO = new ReportDAO();
        Scanner scanner = new Scanner(System.in);

        try {
            // ---- Existing reports (no user input) ----
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

            // ---- New report: reportPerDistributor(DID) ----
            System.out.print("\nEnter Distributor ID for per-distributor report (e.g. D002): ");
            String did = scanner.nextLine().trim();
            reportDAO.reportPerDistributor(did);

            // ---- New report: reportPerWeek(year, isoWeek) ----
            System.out.print("\nEnter year for weekly publication report (e.g. 2026): ");
            int rwYear = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter ISO week number (e.g. 6): ");
            int rwWeek = Integer.parseInt(scanner.nextLine().trim());
            reportDAO.reportPerWeek(rwYear, rwWeek);

            // ---- New report: reportPerMonth(year, month) ----
            System.out.print("\nEnter year for monthly publication report (e.g. 2026): ");
            int rmYear = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter month number (1-12): ");
            int rmMonth = Integer.parseInt(scanner.nextLine().trim());
            reportDAO.reportPerMonth(rmYear, rmMonth);

            // ---- New report: weeklyRevenueExpenses(year, isoWeek) ----
            System.out.print("\nEnter year for weekly revenue/expenses report (e.g. 2026): ");
            int wreYear = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter ISO week number (e.g. 6): ");
            int wreWeek = Integer.parseInt(scanner.nextLine().trim());
            reportDAO.weeklyRevenueExpenses(wreYear, wreWeek);

            // ---- New report: monthlyRevenueExpenses(year, month) ----
            System.out.print("\nEnter year for monthly revenue/expenses report (e.g. 2026): ");
            int mreYear = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter month number (1-12): ");
            int mreMonth = Integer.parseInt(scanner.nextLine().trim());
            reportDAO.monthlyRevenueExpenses(mreYear, mreMonth);

            // ---- New report: paymentsPerWeek(year, isoWeek) ----
            System.out.print("\nEnter year for weekly worker payments report (e.g. 2026): ");
            int ppwYear = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter ISO week number (e.g. 6): ");
            int ppwWeek = Integer.parseInt(scanner.nextLine().trim());
            reportDAO.paymentsPerWeek(ppwYear, ppwWeek);

            // ---- New report: paymentsPerMonth(year, month) ----
            System.out.print("\nEnter year for monthly worker payments report (e.g. 2026): ");
            int ppmYear = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter month number (1-12): ");
            int ppmMonth = Integer.parseInt(scanner.nextLine().trim());
            reportDAO.paymentsPerMonth(ppmYear, ppmMonth);

            // ---- New report: articlesIssuesPerWeek(year, isoWeek) ----
            System.out.print("\nEnter year for weekly articles/issues report (e.g. 2026): ");
            int aiwYear = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter ISO week number (e.g. 6): ");
            int aiwWeek = Integer.parseInt(scanner.nextLine().trim());
            reportDAO.articlesIssuesPerWeek(aiwYear, aiwWeek);

            // ---- New report: articlesIssuesPerMonth(year, month) ----
            System.out.print("\nEnter year for monthly articles/issues report (e.g. 2026): ");
            int aimYear = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter month number (1-12): ");
            int aimMonth = Integer.parseInt(scanner.nextLine().trim());
            reportDAO.articlesIssuesPerMonth(aimYear, aimMonth);

        } catch (NumberFormatException e) {
            System.err.println("Invalid number entered: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}