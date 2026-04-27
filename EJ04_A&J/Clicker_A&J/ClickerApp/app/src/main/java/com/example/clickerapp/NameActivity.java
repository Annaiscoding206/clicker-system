package com.example.clickerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class NameActivity extends AppCompatActivity {

    EditText edtName;
    Button btnContinue;
    TextView txtNameError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        edtName = findViewById(R.id.edtName);
        btnContinue = findViewById(R.id.btnContinue);
        txtNameError = findViewById(R.id.txtNameError);

        btnContinue.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();

            if (name.isEmpty()) {
                txtNameError.setText("Please enter your name");
                return;
            }

            // Pass name to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("studentName", name);
            startActivity(intent);
            finish(); // prevent going back to name screen
        });
    }
}