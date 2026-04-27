import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/display")
public class DisplayServlet extends HttpServlet {

    private String dbUrl = "jdbc:mysql://localhost:3306/clickerdb";
    private String dbUser = "myuser";
    private String dbPassword = "annado";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        int currentQ = ControlServlet.currentQuestionNo;

        String questionText = "";
        String optionA = "", optionB = "", optionC = "", optionD = "";

        Map<String, Integer> counts = new HashMap<>();
        counts.put("a", 0);
        counts.put("b", 0);
        counts.put("c", 0);
        counts.put("d", 0);

        Connection conn = null;
        PreparedStatement qstmt = null;
        PreparedStatement countStmt = null;
        ResultSet qrs = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            // 1. GET QUESTION
            qstmt = conn.prepareStatement(
                "SELECT questionText, optionA, optionB, optionC, optionD FROM questions WHERE questionNo=?"
            );
            qstmt.setInt(1, currentQ);
            qrs = qstmt.executeQuery();
            if (qrs.next()) {
                questionText = qrs.getString("questionText");
                optionA = qrs.getString("optionA");
                optionB = qrs.getString("optionB");
                optionC = qrs.getString("optionC");
                optionD = qrs.getString("optionD");
            }

            // 2. GET VOTE COUNTS
            countStmt = conn.prepareStatement(
                "SELECT choice, COUNT(*) AS count FROM responses WHERE questionNo=? GROUP BY choice"
            );
            countStmt.setInt(1, currentQ);
            rs = countStmt.executeQuery();
            while (rs.next()) {
                String choice = rs.getString("choice").trim().toLowerCase();
                counts.put(choice, rs.getInt("count"));
            }

            int a = counts.get("a");
            int b = counts.get("b");
            int c = counts.get("c");
            int d = counts.get("d");

            // 3. HTML HEAD
            out.println("<!DOCTYPE html>");
            out.println("<html><head>");
            out.println("<title>Clicker System</title>");

            // Google Charts
            out.println("<script src='https://www.gstatic.com/charts/loader.js'></script>");
            out.println("<script>");
            out.println("google.charts.load('current', {packages:['corechart']});");
            out.println("google.charts.setOnLoadCallback(drawChart);");

            out.println("function drawChart(){");
            out.println("  var data = google.visualization.arrayToDataTable([");
            out.println("    ['Option','Votes',{role:'style'},{role:'tooltip'}],");
            out.println("    ['" + escapeJs(optionA) + "'," + a + ",'#2196F3','" + a + " vote(s)'],");
            out.println("    ['" + escapeJs(optionB) + "'," + b + ",'#f44336','" + b + " vote(s)'],");
            out.println("    ['" + escapeJs(optionC) + "'," + c + ",'#333333','" + c + " vote(s)'],");
            out.println("    ['" + escapeJs(optionD) + "'," + d + ",'#FFC107','" + d + " vote(s)']");
            out.println("  ]);");

            out.println("  var options={");
            out.println("    legend:'none',");
            out.println("    backgroundColor:'#ffffff',");
            out.println("    chartArea:{left:50,top:20,width:'80%',height:'70%'},");
            out.println("    hAxis:{textStyle:{fontSize:12},slantedText:false},");
            out.println("    vAxis:{minValue:0,viewWindow:{min:0},format:'0'},");
            out.println("    bar:{groupWidth:'55%'},");
            out.println("    tooltip:{trigger:'focus'}");
            out.println("  };");

            out.println("  var chart = new google.visualization.ColumnChart(document.getElementById('chart'));");
            out.println("  chart.draw(data, options);");

            // Click on bar → go to filter page
            out.println("  var choices = ['a','b','c','d'];");
            out.println("  google.visualization.events.addListener(chart, 'select', function(){");
            out.println("    var sel = chart.getSelection();");
            out.println("    if(sel.length > 0){");
            out.println("      var idx = sel[0].row;");
            out.println("      var choice = choices[idx];");
            out.println("      window.location.href = 'filter?choice=' + choice + '&q=" + currentQ + "';");
            out.println("    }");
            out.println("  });");

            out.println("}");
            out.println("</script>");

            // CSS
            out.println("<style>");
            out.println("body{font-family:Arial;background:#f5f5f5;margin:0;padding:20px;}");
            out.println(".box{width:860px;margin:auto;background:white;padding:24px;border:1px solid #ccc;border-radius:6px;}");
            out.println(".top-buttons{margin-bottom:12px;}");
            out.println(".top-buttons button{padding:8px 16px;margin-right:8px;cursor:pointer;border:1px solid #aaa;border-radius:4px;background:#eeeeee;}");
            out.println(".top-buttons button:hover{background:#ddd;}");
            out.println(".status{font-size:17px;margin:12px 0;}");
            out.println(".ctrl{display:flex;align-items:center;flex-wrap:wrap;gap:8px;margin-bottom:12px;}");
            out.println(".ctrl button{padding:8px 20px;font-weight:bold;border:none;border-radius:4px;cursor:pointer;color:white;}");
            out.println(".start{background:#4CAF50;}");
            out.println(".stop{background:#f44336;}");
            out.println(".reset-q{background:#FF9800;}");
            out.println(".reset-all{background:#9C27B0;}");
            out.println(".ctrl button:hover{opacity:0.85;}");
            out.println(".hint{font-size:12px;color:#888;margin-top:6px;}");
            out.println("</style>");

            out.println("</head><body>");
            out.println("<div class='box'>");

            // Question selector
            out.println("<div class='top-buttons'>");
            out.println("<strong>Select Question:</strong><br><br>");
            out.println("<a href='control?action=setQuestion&q=1'><button>Question 1</button></a>");
            out.println("<a href='control?action=setQuestion&q=2'><button>Question 2</button></a>");
            out.println("<a href='control?action=setQuestion&q=3'><button>Question 3</button></a>");
            out.println("</div><hr>");

            // Question + status
            out.println("<h2>Q" + currentQ + ". " + escapeHtml(questionText) + "</h2>");
            out.println("<div class='status'><b>Status:</b> " +
                (ControlServlet.isActive ? "ACTIVE 🟢" : "STOPPED 🔴") + "</div>");

            // Start / Stop / Reset buttons
            out.println("<div class='ctrl'>");
            out.println("<a href='control?action=start'><button class='start'>START</button></a>");
            out.println("<a href='control?action=stop'><button class='stop'>STOP</button></a>");
            out.println("<a href='reset?scope=current' onclick=\"return confirm('Reset all responses for Q" + currentQ + "?')\"><button class='reset-q'>RESET Q" + currentQ + "</button></a>");
            out.println("<a href='reset?scope=all' onclick=\"return confirm('Reset ALL responses for ALL questions? This cannot be undone.')\"><button class='reset-all'>RESET ALL</button></a>");
            out.println("</div><hr>");

            // Options
            out.println("<p>A. " + escapeHtml(optionA) + "</p>");
            out.println("<p>B. " + escapeHtml(optionB) + "</p>");
            out.println("<p>C. " + escapeHtml(optionC) + "</p>");
            out.println("<p>D. " + escapeHtml(optionD) + "</p>");

            // Chart
            out.println("<div id='chart' style='width:780px;height:320px;cursor:pointer;'></div>");
            out.println("<p class='hint'>💡 Click a bar to see who chose that option</p>");

            out.println("</div></body></html>");

        } catch (Exception e) {
            out.println("<pre>" + e + "</pre>");
        } finally {
            try { if (qrs != null) qrs.close(); } catch (Exception e) {}
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (qstmt != null) qstmt.close(); } catch (Exception e) {}
            try { if (countStmt != null) countStmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
            out.close();
        }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }
}