import java.io.*;
import java.sql.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/select")
public class SelectServlet extends HttpServlet {

    private String dbUrl = "jdbc:mysql://localhost:3306/clickerdb";
    private String dbUser = "myuser";
    private String dbPassword = "annado";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        String choice = request.getParameter("choice");
        String comment = request.getParameter("comment");
        String qParam = request.getParameter("q");
        String studentName = request.getParameter("name"); // ✅ NEW

        if (!ControlServlet.isActive) {
            out.print("TIMEOUT");
            return;
        }

        if (choice == null || choice.trim().isEmpty()) {
            out.print("INVALID");
            return;
        }

        choice = choice.toLowerCase().trim();
        if (!choice.matches("[abcd]")) {
            out.print("INVALID");
            return;
        }

        int questionNo;
        try {
            questionNo = Integer.parseInt(qParam);
        } catch (Exception e) {
            out.print("INVALID_QUESTION");
            return;
        }

        if (questionNo != ControlServlet.currentQuestionNo) {
            out.print("QUESTION_CHANGED");
            return;
        }

        if (comment == null) comment = "";
        if (studentName == null) studentName = "";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            String sql = "INSERT INTO responses (questionNo, choice, comment, studentName) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, questionNo);
            pstmt.setString(2, choice);
            pstmt.setString(3, comment.trim());
            pstmt.setString(4, studentName.trim()); // ✅ NEW

            pstmt.executeUpdate();
            out.print("RECORDED");

        } catch (Exception e) {
            out.print("ERROR: " + e);
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
            out.close();
        }
    }
}