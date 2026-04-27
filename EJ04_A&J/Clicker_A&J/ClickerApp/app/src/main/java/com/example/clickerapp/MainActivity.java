package com.example.clickerapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button btnRefresh, btnA, btnB, btnC, btnD, btnSubmit;
    TextView txtQuestion, txtResult;
    EditText edtComment;

    String selectedChoice = "";
    int currentQuestionNo = 0;
    String studentName = "";

    String serverBaseUrl = "http://10.0.2.2:9999/clicker/select";
    String questionUrl   = "http://10.0.2.2:9999/clicker/question";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        studentName = getIntent().getStringExtra("studentName");
        if (studentName == null) studentName = "";

        btnRefresh = findViewById(R.id.btnRefresh);
        btnA       = findViewById(R.id.btnA);
        btnB       = findViewById(R.id.btnB);
        btnC       = findViewById(R.id.btnC);
        btnD       = findViewById(R.id.btnD);
        btnSubmit  = findViewById(R.id.btnSubmit);
        txtQuestion = findViewById(R.id.txtQuestion);
        txtResult   = findViewById(R.id.txtResult);
        edtComment  = findViewById(R.id.edtComment);

        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder().permitAll().build()
        );

        // Show name at top so student knows who they are
        txtResult.setText("Hello, " + studentName + "!");

        loadQuestion();

        btnRefresh.setOnClickListener(v -> loadQuestion());

        btnA.setOnClickListener(v -> { selectedChoice = "a"; txtResult.setText("Selected: A"); });
        btnB.setOnClickListener(v -> { selectedChoice = "b"; txtResult.setText("Selected: B"); });
        btnC.setOnClickListener(v -> { selectedChoice = "c"; txtResult.setText("Selected: C"); });
        btnD.setOnClickListener(v -> { selectedChoice = "d"; txtResult.setText("Selected: D"); });

        btnSubmit.setOnClickListener(v -> {
            if (selectedChoice.isEmpty()) {
                txtResult.setText("Please select an option first");
                return;
            }
            sendChoice(selectedChoice);
        });
    }

    private void loadQuestion() {
        try {
            URL url = new URL(questionUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            String qNo   = in.readLine();
            String qText = in.readLine();
            String aText = in.readLine();
            String bText = in.readLine();
            String cText = in.readLine();
            String dText = in.readLine();

            in.close();
            conn.disconnect();

            if (qNo != null && qText != null) {
                currentQuestionNo = Integer.parseInt(qNo.trim());
                txtQuestion.setText("Q" + qNo.trim() + ". " + qText);
                btnA.setText("A. " + aText);
                btnB.setText("B. " + bText);
                btnC.setText("C. " + cText);
                btnD.setText("D. " + dText);
                selectedChoice = ""; // reset selection on new question
            } else {
                txtResult.setText("Failed to load question");
            }

        } catch (Exception e) {
            txtResult.setText("Load question error");
        }
    }

    private void sendChoice(String choice) {
        try {
            String comment = edtComment.getText().toString();

            String encodedComment = java.net.URLEncoder.encode(
                    comment, java.nio.charset.StandardCharsets.UTF_8.toString()
            );

            String encodedName =
                    java.net.URLEncoder.encode(
                            studentName, java.nio.charset.StandardCharsets.UTF_8.toString()
                    );

            String urlStr = serverBaseUrl
                    + "?choice=" + choice
                    + "&comment=" + encodedComment
                    + "&q=" + currentQuestionNo
                    + "&name=" + encodedName;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder responseText = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                responseText.append(inputLine);
            }

            in.close();
            conn.disconnect();

            String serverResponse = responseText.toString().trim();

            if (serverResponse.equals("TIMEOUT")) {
                txtResult.setText("Voting is not active yet");
            } else if (serverResponse.equals("QUESTION_CHANGED")) {
                txtResult.setText("Question changed! Please refresh.");
            } else if (serverResponse.equals("RECORDED")) {
                txtResult.setText("✅ Recorded: " + choice.toUpperCase());
                edtComment.setText("");
                selectedChoice = "";
            } else if (serverResponse.equals("INVALID")) {
                txtResult.setText("Invalid choice");
            } else {
                txtResult.setText("Server: " + serverResponse);
            }

        } catch (Exception e) {
            txtResult.setText("Connection error");
        }
    }
}