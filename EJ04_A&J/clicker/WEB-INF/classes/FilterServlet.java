import java.io.*;
import java.sql.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/filter")
public class FilterServlet extends HttpServlet {

    private String dbUrl = "jdbc:mysql://localhost:3306/clickerdb";
    private String dbUser = "myuser";
    private String dbPassword = "annado";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String choice = request.getParameter("choice");
        String qParam = request.getParameter("q");

        if (choice == null || qParam == null) {
            out.print("<p>Invalid request</p>");
            return;
        }

        int questionNo;
        try {
            questionNo = Integer.parseInt(qParam);
        } catch (Exception e) {
            out.print("<p>Invalid question</p>");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            pstmt = conn.prepareStatement(
                "SELECT studentName, comment FROM responses " +
                "WHERE questionNo=? AND choice=? ORDER BY id ASC"
            );
            pstmt.setInt(1, questionNo);
            pstmt.setString(2, choice.toLowerCase().trim());
            rs = pstmt.executeQuery();

            out.println("<!DOCTYPE html>");
            out.println("<html><head><title>Responses - Option " + choice.toUpperCase() + "</title>");
            out.println("<style>");
            out.println("body{font-family:Arial;background:#f5f5f5;padding:20px;}");
            out.println(".box{width:800px;margin:auto;background:white;padding:24px;border:1px solid #ccc;border-radius:6px;}");
            out.println("table{width:100%;border-collapse:collapse;margin-top:10px;}");
            out.println("th,td{border:1px solid #999;padding:10px;text-align:left;}");
            out.println("th{background:#eeeeee;}");
            out.println(".back{display:inline-block;margin-bottom:16px;padding:8px 16px;background:#2196F3;color:white;text-decoration:none;border-radius:4px;}");
            out.println("h2{margin-top:0;}");
            out.println("</style>");
            out.println("</head><body>");
            out.println("<div class='box'>");
            out.println("<a class='back' href='display'>&#8592; Back to Dashboard</a>");
            out.println("<h2>Option " + choice.toUpperCase() + " &mdash; Q" + questionNo + " Responses</h2>");

            out.println("<table>");
            out.println("<tr><th>Name</th><th>Comment</th></tr>");

            boolean found = false;
            while (rs.next()) {
                found = true;
                String name = rs.getString("studentName");
                String comment = rs.getString("comment");
                if (name == null || name.isEmpty()) name = "(no name)";
                if (comment == null || comment.isEmpty()) comment = "-";
                out.println("<tr>");
                out.println("<td>" + escapeHtml(name) + "</td>");
                out.println("<td>" + escapeHtml(comment) + "</td>");
                out.println("</tr>");
            }

            if (!found) {
                out.println("<tr><td colspan='2'>No responses for option " +
                            choice.toUpperCase() + "</td></tr>");
            }

            out.println("</table>");
            out.println("</div></body></html>");

        } catch (Exception e) {
            out.print("<p>Error: " + e.getMessage() + "</p>");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
            out.close();
        }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}