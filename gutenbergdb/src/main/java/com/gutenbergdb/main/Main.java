package com.gutenbergdb.main;

import com.gutenbergdb.dao.ReportDAO;

public class Main {
    public static void main(String[] args) {
        ReportDAO reportDAO = new ReportDAO();
        try {
            reportDAO.reportPerDistributor();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}