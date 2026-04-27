import java.io.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/control")
public class ControlServlet extends HttpServlet {

    public static boolean isActive = false;
    public static int currentQuestionNo = 1;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String action = request.getParameter("action");

        if ("start".equals(action)) {
            isActive = true;

        } else if ("stop".equals(action)) {
            isActive = false;

        } else if ("setQuestion".equals(action)) {
            String q = request.getParameter("q");
            if (q != null) {
                try {
                    currentQuestionNo = Integer.parseInt(q);
                } catch (NumberFormatException e) {
                    // ignore invalid input
                }
            }
        }

        response.sendRedirect("display");
    }
}