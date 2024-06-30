package com.example.tfgvictor;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.tfgvictor.databinding.ActivityLoginPrincipalBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class LoginPrincipalActivity extends AppCompatActivity {
    private ActivityLoginPrincipalBinding binding;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/");


        //Metodo para registrar al usuario con Firebase

        binding.lnRegistrarseLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = auth.getCurrentUser();
                registro(user);
            }
        });


        //Metodo para loguearse si el usuario existe
        binding.btnIniciarSesionLoginPrincipal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.txtCorreoLoginPrincipal.getText().toString();
                String password = binding.txtPasswordLoginPrincipal.getText().toString();

                if (!email.isEmpty() && !password.isEmpty()) {
                    acceder(email, password);
                } else {
                    if (email.isEmpty()) {
                        binding.txtCorreoLoginPrincipal.setError("Campo requerido");
                        binding.txtCorreoLoginPrincipal.requestFocus();
                    }
                    if (password.isEmpty()) {
                        binding.txtPasswordLoginPrincipal.setError("Campo requerido");
                        binding.txtPasswordLoginPrincipal.requestFocus();
                    }

                }
            }
        });

        binding.lnRestaurar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.txtCorreoLoginPrincipal.getText().toString();
                if (!email.isEmpty()) {
                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginPrincipalActivity.this, "Se le ha envíado un correo electrónico para restablecer su contraséña", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginPrincipalActivity.this, "Error: No se pudo enviar un correo electrónico para restablecer su contraséña", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(LoginPrincipalActivity.this, "Rellena el email para poder recuperar la contraséña.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void registro(FirebaseUser user) {
        user = auth.getCurrentUser();


        Intent intent = new Intent(LoginPrincipalActivity.this, RegisterActivity.class);
        startActivity(intent);


    }


    private void acceder(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginPrincipalActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = auth.getCurrentUser();
                            pasarPantalla(user);
                        }
                    }
                }).addOnFailureListener(LoginPrincipalActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginPrincipalActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void pasarPantalla(FirebaseUser user) {
        if (user != null) {
            String uid = user.getUid();
            Intent intent = new Intent(LoginPrincipalActivity.this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("UID", uid);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        pasarPantalla(auth.getCurrentUser());
    }

}