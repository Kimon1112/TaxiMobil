package com.example.taximobil;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    Button driverBtn,customerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        driverBtn =(Button)findViewById(R.id.driverBtn);
        customerBtn =(Button)findViewById(R.id.customerBtn);

       driverBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent driver_intent = new Intent(WelcomeActivity.this,DriverRegLoginActivity.class);
               startActivity(driver_intent);
           }
       });

       customerBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent customer_intent = new Intent(WelcomeActivity.this,CustomerRegLoginActivity.class);
               startActivity(customer_intent);
           }
       });
    }
}