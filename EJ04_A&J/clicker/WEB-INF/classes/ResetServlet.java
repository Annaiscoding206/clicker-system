import java.io.*;
import java.sql.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/reset")
public class ResetServlet extends HttpServlet {

    private String dbUrl = "jdbc:mysql://localhost:3306/clickerdb";
    private String dbUser = "myuser";
    private String dbPassword = "annado";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String scope = request.getParameter("scope"); // "current" or "all"

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            if ("all".equals(scope)) {
                // Delete ALL responses across all questions
                pstmt = conn.prepareStatement("DELETE FROM responses");
            } else {
                // Default: delete only current question's responses
                pstmt = conn.prepareStatement("DELETE FROM responses WHERE questionNo=?");
                pstmt.setInt(1, ControlServlet.currentQuestionNo);
            }

            pstmt.executeUpdate();

        } catch (Exception e) {
            // Silently fail — display page will show empty chart
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }

        response.sendRedirect("display");
    }
}