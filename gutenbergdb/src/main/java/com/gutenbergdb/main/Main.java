package com.gutenbergdb.main;

import com.gutenbergdb.dao.ReportDAO;

public class Main {
    public static void main(String[] args) {
        ReportDAO reportDAO = new ReportDAO();
        PublicationDAO dao = new PublicationDAO();
        dao.insertNewPeriodical();
        dao.insertNewBook();
        dao.showBookDetails(12);
        dao.updatePeriodical();
        dao.updateBookEdition();
        try {
            reportDAO.reportPerDistributor();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
