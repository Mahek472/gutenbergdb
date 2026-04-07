package com.gutenbergdb.dao;

public class DistributorDAO {
    public void enterNewDistributor(int iDID, string iname, string iphone_number, string icategory, float ioutstanding_balance, string iaddr, string icontact) throws SQLException {
        // Implementation for entering a new distributor
        string sql = "INSERT INTO Distributors (DID, name, phone_number, category, outstanding_balance, addr, contact) " +
        "VALUES (iDID, iname, iphone_number, icategory, ioutstanding_balance, iaddr, icontact);"

    }
    public void updateDistributorInfo(int iDID, string iname, string iphone_number, string icategory, float ioutstanding_balance, string iaddr, string icontact) throws SQLException {
        // Implementation for updating distributor information
        string sql = "UPDATE Distributors " +
        "SET phone_number = iphone_number, " +
        "addr = iaddr, " +
        "contact = icontact " +
        "WHERE DID = iDID;";

    }
    public void deleteDistributor(int iDID) throws SQLException {
        // Implementation for deleting a distributor
        string sql = "DELETE FROM Distributors WHERE DID = iDID;";
    }
    public void inputOrder(int iDID, int iPubID, string idate_ordered, float ishipping_fee, string idate_due, float iunit_price, int inumber_of_copies) throws SQLException {
        // Implementation for inputting an order
        string sql = "START TRANSACTION; " +
                     "INSERT INTO Orders (OID, date_ordered, shipping_fee, date_due, is_produced) " +
                     "VALUES (iOID, idate_ordered, ishipping_fee, idate_due, FALSE);" + 
                     "INSERT INTO Places (OID, DID) " +
                     "VALUES (iOID, iDID); " +
                     "INSERT INTO Orders_books (OID, PubID, unit_price, number_of_copies) " +
                     "VALUES (iOID, iPubID, iunit_price, inumber_of_copies); " +
                     "COMMIT;"
    }

    #Needs more work
    public void inputMultipleOrders(int numOfOrders) throws SQLException {
        // Implementation for inputting multiple orders
        for (int i = 0; i < numOfOrders; i++) {
            inputOrder();
        }
    }
    public void billDistributor(int iDID, float ipayment_amount, string ipayment_date) throws SQLException {
        // Implementation for billing a distributor
        string sql = "START TRANSACTION;" + 
                    "INSERT INTO Distributor_payments (DBID, payment_date, payment_amount)" +
                    "VALUES (iDBID, ipayment_date, ipayment_amount);" + 
                    "INSERT INTO Make (DBID, DID)" +
                    "VALUES (iDBID, iDID);" + 
                    "UPDATE Distributors" +
                    "SET outstanding_balance = outstanding_balance + ipayment_amount" +
                    "WHERE DID = iDID;" +
                    "COMMIT;"

    }
    public void changeDistributorBalance(int iDID, float ipayment_amount, string ipayment_date) throws SQLException {
        // Implementation for changing a distributor's balance
        string sql = "START TRANSACTION;" + 
                    "INSERT INTO Distributor_payments (DBID, payment_date, payment_amount)" +
                    "VALUES (iDBID, ipayment_date, ipayment_amount);" + 
                    "INSERT INTO Make (DBID, DID)" +
                    "VALUES (iDBID, iDID);" + 
                    "UPDATE Distributors" +
                    "SET outstanding_balance = outstanding_balance - ipayment_amount" +
                    "WHERE DID = iDID;" +
                    "COMMIT;"
    }
    public String identifyNonMatchingDistributorBalances() throws SQLException {
        // Implementation for identifying non-matching distributor balances
        string sql = "SELECT d.DID, d.name, d.outstanding_balance, COALESCE(SUM(dp.payment_amount), 0) AS total_payments" +
                     "FROM Distributors d " +
                     "LEFT JOIN Make m ON d.DID = m.DID " +
                     "LEFT JOIN Distributor_payments dp ON m.DBID = dp.DBID " +
                     "GROUP BY d.DID, d.name, d.outstanding_balance " +
                     "HAVING d.outstanding_balance != COALESCE(SUM(dp.payment_amount), 0);";

        #Still need to parse output from SQL statement and return it in a readable format

    }
    public String identifyDistributorInLocation(string location, string type) throws SQLException {
        // Implementation for identifying distributors in a specific location
        string sql = "SELECT DID, name, phone_number, category, outstanding_balance, addr, contact FROM Distributors WHERE addr LIKE '%location%' AND category = type;";
        #Still need to parse output from SQL statement and return it in a readable format

    }
}
