package com.example.tfgvictor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.tfgvictor.Modelos.Usuarios;
import com.example.tfgvictor.databinding.ActivityRegisterBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/");


        binding.btnRegistroRegistro.setEnabled(false);


        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                comprobarParametros();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                comprobarParametros();

            }

            @Override
            public void afterTextChanged(Editable s) {
                comprobarParametros();

            }
        };

        binding.txtNombreRegistro.addTextChangedListener(textWatcher);
        binding.txtApellidoRegistro.addTextChangedListener(textWatcher);
        binding.txtCorreoRegistro.addTextChangedListener(textWatcher);
        binding.txtEdadRegistro.addTextChangedListener(textWatcher);
        binding.txtContrasenyaRegistro.addTextChangedListener(textWatcher);

        binding.imgFotoPerfilRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(RegisterActivity.this)
                        .galleryOnly()
                        .start();
            }
        });

        binding.btnRegistroRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.txtCorreoRegistro.getText().toString();
                String password = binding.txtContrasenyaRegistro.getText().toString();
                String nombre = binding.txtNombreRegistro.getText().toString();
                String apellido = binding.txtApellidoRegistro.getText().toString();
                int edad = Integer.parseInt(binding.txtEdadRegistro.getText().toString());

                if (imageUri != null && !nombre.isEmpty() && !password.isEmpty() && !apellido.isEmpty()) {
                    registerUser(email, password, nombre, apellido, edad);
                } else {
                    Toast.makeText(RegisterActivity.this, "Por favor seleccione una foto de perfil y rellene todos los datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void comprobarParametros() {
        String email = binding.txtCorreoRegistro.getText().toString();
        String password = binding.txtContrasenyaRegistro.getText().toString();
        String nombre = binding.txtNombreRegistro.getText().toString();
        String apellido = binding.txtApellidoRegistro.getText().toString();

        if (!email.isEmpty() && !password.isEmpty() && password.length() >= 6 && !nombre.isEmpty() && !apellido.isEmpty() && imageUri != null) {
            binding.btnRegistroRegistro.setEnabled(true);
        } else {
            binding.btnRegistroRegistro.setEnabled(false);
        }
    }

    private void registerUser(String email, String password, String nombre, String apellido, int edad) {

        if (imageUri != null && !nombre.isEmpty() && !password.isEmpty() && !apellido.isEmpty() && !email.isEmpty()) {


            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    uploadImageAndSaveData(user.getUid(), nombre, apellido, edad, email);
                                }
                            } else {

                                Toast.makeText(RegisterActivity.this, "Por favor rellene todos los datos", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void uploadImageAndSaveData(String userId, String nombre, String apellido, int edad, String email) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("imagenes");
        StorageReference imageRef = storageRef.child(userId + "." + getFileExtension(imageUri));
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DatabaseReference usersRef = database.getReference("UsuariosCompletos");
                                String imageUrl = uri.toString();
                                String uid = userId;
                                Usuarios usuario = new Usuarios(nombre, apellido, edad, email, imageUrl, uid); // La URL de la imagen se guarda en Firebase Storage, no en la clase Usuarios
                                usersRef.child(uid).setValue(usuario)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(RegisterActivity.this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegisterActivity.this, LoginPrincipalActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RegisterActivity.this, "Error: Al guardar los datos del usuario", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "Error: Al cargar la imagen", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        Log.d("FILE_EXTENSION", "Extension: " + extension);
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            Log.d("IMAGE_URI", "Image URI: " + imageUri.toString());
            binding.imgFotoPerfilRegistro.setImageURI(imageUri);
            comprobarParametros();
        }
    }
}


