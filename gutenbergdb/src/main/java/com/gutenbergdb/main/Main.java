package com.gutenbergdb.main;
import com.gutenbergdb.dao.PublicationDAO;
import com.gutenbergdb.dao.ReportDAO;

public class Main {
    public static void main(String[] args) {
        PublicationDAO dao = new PublicationDAO();
        try{
            dao.insertNewPeriodical();
            dao.insertNewBook();
            dao.showBookDetails(12);
            dao.updatePeriodical();
            dao.updateBookEdition();
        }
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
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