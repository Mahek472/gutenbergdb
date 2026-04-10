package com.gutenbergdb.dao;

import com.gutenbergdb.util.DBConnection;
import java.sql.*;

public class ArticleDAO {

    public void insertArticle(int iid, String title, String text, String authorDate, String topic) throws SQLException {
        String sql = "INSERT INTO Articles (IID, Title, full_text, written_date, topic) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, iid);
            pstmt.setString(2, title);
            pstmt.setString(3, text);
            pstmt.setString(4, authorDate);
            pstmt.setString(5, topic);
            pstmt.executeUpdate();
        }
    }

    public void updateArticleText(int iid, String title, String newText) throws SQLException {
        String sql = "UPDATE Articles SET full_text = ? WHERE IID = ? AND Title = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newText);
            pstmt.setInt(2, iid);
            pstmt.setString(3, title);
            pstmt.executeUpdate();
        }
    }

    public void deleteArticle(int iid, String title) throws SQLException {
        String sql = "DELETE FROM Articles WHERE IID = ? AND Title = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, iid);
            pstmt.setString(2, title);
            pstmt.executeUpdate();
        }
    }

    public void findArticlesByAuthor(String authorName) throws SQLException {
        String sql = "SELECT a.* FROM Articles a " +
                     "JOIN Works_on_articles wa ON a.IID = wa.IID AND a.Title = wa.Title " +
                     "JOIN Workers w ON wa.EID = w.EID " +
                     "WHERE w.worker_name LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + authorName + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- Articles by Author: " + authorName + " ---");
                while (rs.next()) {
                    System.out.printf("Title: %s | Topic: %s | Date: %s%n",
                        rs.getString("Title"),
                        rs.getString("topic"),
                        rs.getString("written_date"));
                }
            }
        }
    }
}
