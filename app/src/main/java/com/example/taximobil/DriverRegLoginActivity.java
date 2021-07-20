package com.example.taximobil;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverRegLoginActivity extends AppCompatActivity {

    TextView driverStatus, question;
    Button SignInBtn,SignUpBtn;
    EditText emailET,passwordET;

    FirebaseAuth mAuth;
    DatabaseReference DriverDatabaseRef;
    String OnlineDriverID;

    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_reg_login);

        driverStatus = (TextView) findViewById(R.id.statusDriver);
        question = (TextView) findViewById(R.id.accoutCreate);
        SignInBtn = (Button) findViewById(R.id.SingInDriver);
        SignUpBtn = (Button) findViewById(R.id.SignUpDriver);
        emailET = (EditText) findViewById(R.id.DriverEmail);
        passwordET = (EditText) findViewById(R.id.DriverEmail);

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
                SignInBtn.setEnabled(true);
                driverStatus.setText("Регистрация для водителей");

            }
        });
        SignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                RegisterDriver(email,password);
            }
        });

        SignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                SignInDriver(email,password);
            }
        });

    }

    private void SignInDriver(String email, String password)
    {
        loadingBar.setTitle("Авторизация водителя");
        loadingBar.setMessage("Пожалуйста, дождитесь загрузки");
        loadingBar.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(DriverRegLoginActivity.this, "Вход выполнен", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();

                    Intent driverIntent = new Intent(DriverRegLoginActivity.this,DriversMapActivity.class);
                    startActivity(driverIntent);
                } else {
                    Toast.makeText(DriverRegLoginActivity.this, "Неверные данные", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });

    }

    private void RegisterDriver(String email, String password)
        {
            loadingBar.setTitle("Регистрация водителя");
            loadingBar.setMessage("Пожалуйста, дождитесь загрузки");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        OnlineDriverID = mAuth.getCurrentUser().getUid();
                        DriverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users")
                                .child("Drivers").child(OnlineDriverID);
                        DriverDatabaseRef.setValue(true);

                        Intent driverIntent = new Intent(DriverRegLoginActivity.this,DriversMapActivity.class);
                        startActivity(driverIntent);

                        Toast.makeText(DriverRegLoginActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    } else {
                        Toast.makeText(DriverRegLoginActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }
}