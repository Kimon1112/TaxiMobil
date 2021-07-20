package com.example.taximobil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerRegLoginActivity extends AppCompatActivity {

    TextView CustomerStatus, question;
    Button SignInBtn, SignUpBtn;
    EditText emailET, passwordET;

    FirebaseAuth mAuth;
    DatabaseReference CustomerDatabaseRef;
    String OnlineCustomerID;

    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_reg_login);

        CustomerStatus = (TextView) findViewById(R.id.statusCustomer);
        question = (TextView) findViewById(R.id.accountCreateCustomer);
        SignInBtn = (Button) findViewById(R.id.SingInCustomer);
        SignUpBtn = (Button) findViewById(R.id.SignUpCustomer);
        emailET = (EditText) findViewById(R.id.CustomerEmail);
        passwordET = (EditText) findViewById(R.id.CustomerPassword);

        mAuth = FirebaseAuth.getInstance();

        loadingBar = new ProgressDialog(this);

        SignUpBtn.setVisibility(View.INVISIBLE);
        SignUpBtn.setEnabled(true);

        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInBtn.setVisibility(View.INVISIBLE);
                question.setVisibility(View.INVISIBLE);
                SignUpBtn.setVisibility(View.VISIBLE);
                SignUpBtn.setEnabled(true);
                CustomerStatus.setText("Регистрация Клиента");
            }
        });
        SignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                RegisterCustomer(email, password);
            }
        });

        SignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                SignInCustomer(email, password);
            }
        });
    }

    private void RegisterCustomer(String email, String password)
    {
        loadingBar.setTitle("Регистрация клиента");
        loadingBar.setMessage("Пожалуйста, дождитесь загрузки");
        loadingBar.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    OnlineCustomerID = mAuth.getCurrentUser().getUid();
                    CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Customers").child(OnlineCustomerID);
                    CustomerDatabaseRef.setValue(true);

                    Intent customerIntent = new Intent(CustomerRegLoginActivity.this,CustomersMapActivity.class);
                    startActivity(customerIntent);

                    Toast.makeText(CustomerRegLoginActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                } else {
                    Toast.makeText(CustomerRegLoginActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void SignInCustomer(String email, String password) {
        {
            loadingBar.setTitle("Авторизация Клиента");
            loadingBar.setMessage("Пожалуйста, дождитесь загрузки");
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CustomerRegLoginActivity.this, "Вход выполнен", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                        Intent customerIntent = new Intent(CustomerRegLoginActivity.this,CustomersMapActivity.class);
                        startActivity(customerIntent);

                    } else {
                        Toast.makeText(CustomerRegLoginActivity.this, "Неверные данные", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }
}