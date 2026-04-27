import java.io.*;
import java.sql.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/question")
public class QuestionServlet extends HttpServlet {

    private String dbUrl = "jdbc:mysql://localhost:3306/clickerdb";
    private String dbUser = "myuser";
    private String dbPassword = "annado";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        int currentQ = ControlServlet.currentQuestionNo;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            String sql = "SELECT questionText, optionA, optionB, optionC, optionD FROM questions WHERE questionNo=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentQ);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String questionText = rs.getString("questionText");
                String optionA = rs.getString("optionA");
                String optionB = rs.getString("optionB");
                String optionC = rs.getString("optionC");
                String optionD = rs.getString("optionD");

                out.println(currentQ);
                out.println(questionText);
                out.println(optionA);
                out.println(optionB);
                out.println(optionC);
                out.println(optionD);
            } else {
                out.println("0");
                out.println("No question");
                out.println("A");
                out.println("B");
                out.println("C");
                out.println("D");
            }

        } catch (Exception e) {
            out.println("ERROR");
            out.println(e.toString());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
            out.close();
        }
    }
}