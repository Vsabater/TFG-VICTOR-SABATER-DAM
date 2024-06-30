package com.example.tfgvictor.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.tfgvictor.Modelos.Gastos;
import com.example.tfgvictor.Modelos.Usuarios;
import com.example.tfgvictor.R;
import com.example.tfgvictor.databinding.FragmentPrincipalBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;


public class PrincipalFragment extends Fragment {

    private FragmentPrincipalBinding binding;
    private Toolbar toolbar;

    private FirebaseUser user;
    private FirebaseAuth auth;
    private DatabaseReference myRef, usuariosCompletos, nombreUsuario, nombreTarea, fechaTarea, horaTarea, nombreTarea2, fechaTarea2, horaTarea2, apellidoUsuario;
    private DatabaseReference precioTotalGastos, tareasRef, gastosRef, listaCrompraRef, nombreListaCompra, cantidadListaCompra, nombreListaCompra2, cantidadListaCompra2;
    private FirebaseDatabase mDatabase;
    private static String uid;
    private static String imagenPerfilUsuarioNormal;
    private ArrayList<Usuarios> listaUsuarios;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPrincipalBinding.inflate(inflater, container, false);

        View vista = binding.getRoot();

        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();

        listaUsuarios = new ArrayList<>();


        myRef = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/").getReference("TareasHogar");
        mDatabase = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/");
        usuariosCompletos = mDatabase.getReference("UsuariosCompletos").child(user.getUid());
        nombreUsuario = mDatabase.getReference("UsuariosCompletos").child(user.getUid()).child("nombre");
        apellidoUsuario = mDatabase.getReference("UsuariosCompletos").child(user.getUid()).child("apellido");


        precioTotalGastos = mDatabase.getReference("GastosHogar").child("0").child("precioFinal");

        uid = user.getUid();

        toolbar = vista.findViewById(R.id.my_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setSubtitle(user.getEmail());


        ImageView toolbarLogoImageView = vista.findViewById(R.id.userImage);
        cargarBasesDeDatosTareas(binding);
        cargarBasesdDeDatosGastos(binding);
        cargarBasesDeDatosListaCompra(binding);
        cargarImagenUsuarioActual(toolbarLogoImageView);
        cargarInformacionGastos(precioTotalGastos, binding);


        usuariosCompletos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cargarImagenUsuarioActual(toolbarLogoImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity, "Error: Al cargar la imagen del usuario", Toast.LENGTH_SHORT).show();
            }
        });


        nombreUsuario.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nombre = snapshot.getValue(String.class);

                    if (nombre != null) {
                        String nombreMayus = nombre.substring(0, 1).toUpperCase() + nombre.substring(1).toLowerCase();

                        DatabaseReference apellidoRef = mDatabase.getReference("UsuariosCompletos").child(user.getUid()).child("apellido");

                        apellidoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String apellido = snapshot.getValue(String.class);

                                    if (apellido != null) {
                                        String apellidoMayus = apellido.substring(0, 1).toUpperCase() + apellido.substring(1).toLowerCase();

                                        String nombreCompleto = nombreMayus + " " + apellidoMayus;
                                        activity.getSupportActionBar().setTitle(nombreCompleto);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity, "Error: Al cargar el nombre del usuario", Toast.LENGTH_SHORT).show();
            }
        });


        return vista;
    }

    private void cargarBasesDeDatosListaCompra(FragmentPrincipalBinding binding) {
        listaCrompraRef = mDatabase.getReference("ListaCompra");

        listaCrompraRef.orderByKey().limitToFirst(2).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                        if (snapshot1.exists()) {
                            switch (i) {
                                case 0:
                                    nombreListaCompra = snapshot1.child("nombre").getRef();
                                    cantidadListaCompra = snapshot1.child("cantidad").getRef();
                                    break;
                                case 1:
                                    nombreListaCompra2 = snapshot1.child("nombre").getRef();
                                    cantidadListaCompra2 = snapshot1.child("cantidad").getRef(); // Aquí estaba el error

                                    break;
                            }
                        }
                        i++;
                    }
                    cargarInformacionListaCompra(nombreListaCompra, cantidadListaCompra, binding, nombreListaCompra2, cantidadListaCompra2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void cargarInformacionListaCompra(DatabaseReference nombreListaCompra, DatabaseReference cantidadListaCompra, FragmentPrincipalBinding binding, DatabaseReference nombreTarea2, DatabaseReference cantidadListaCompra2) {

        if (nombreListaCompra != null && cantidadListaCompra != null) {
            nombreListaCompra.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombreLista = snapshot.getValue(String.class);
                        binding.lbNombreListaCompra1.setText(nombreLista);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Error: Al cargar el nombre del Producto", Toast.LENGTH_SHORT).show();
                }
            });

            cantidadListaCompra.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String cantidadLista = snapshot.getValue(String.class);
                        binding.lbCantidadListaCompra1.setText(cantidadLista);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Error: Al cargar la cantidad del Producto", Toast.LENGTH_SHORT).show();
                }
            });
        }


        if (nombreListaCompra2 != null && cantidadListaCompra2 != null) {
            nombreListaCompra2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombreLista = snapshot.getValue(String.class);
                        binding.lbNombreListaCompra2.setText(nombreLista);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Error: Al cargar el nombre del producto", Toast.LENGTH_SHORT).show();
                }
            });

            cantidadListaCompra2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String cantidadLista = snapshot.getValue(String.class);
                        binding.lbCantidadListaCompra2.setText(cantidadLista);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Error: al cargar la cantidad del Producto", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    private void cargarBasesdDeDatosGastos(FragmentPrincipalBinding binding) {
        gastosRef = mDatabase.getReference("GastosHogar");
        gastosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float precioTotal = 0;


                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        if (snapshot1.exists()) {
                            Gastos gastos = snapshot1.getValue(Gastos.class);
                            precioTotal += gastos.getPrecioGasto();
                        }
                    }

                    binding.lbPrecioTotalGasto.setText(precioTotal + "€");
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error: Al cargar los gastos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarInformacionGastos(DatabaseReference precioTotalGastos, FragmentPrincipalBinding binding) {


        precioTotalGastos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Float gastoTotal = snapshot.getValue(Float.class);
                    binding.lbPrecioTotalGasto.setText(gastoTotal + "€");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void cargarBasesDeDatosTareas(FragmentPrincipalBinding binding) {
        tareasRef = mDatabase.getReference("TareasHogar");


        tareasRef.orderByKey().limitToFirst(2).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;

                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                        if (snapshot1.exists()) {
                            switch (i) {
                                case 0:
                                    nombreTarea = snapshot1.child("nombre").getRef();
                                    fechaTarea = snapshot1.child("fecha").getRef();
                                    horaTarea = snapshot1.child("hora").getRef();
                                    break;
                                case 1:
                                    nombreTarea2 = snapshot1.child("nombre").getRef();
                                    fechaTarea2 = snapshot1.child("fecha").getRef();
                                    horaTarea2 = snapshot1.child("hora").getRef();
                                    break;
                            }
                        }
                        i++;
                    }
                    cargarInformacionTareas(nombreTarea, fechaTarea, horaTarea, binding, nombreTarea2, fechaTarea2, horaTarea2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void cargarInformacionTareas(DatabaseReference nombreTarea, DatabaseReference fechaTarea, DatabaseReference horaTarea, FragmentPrincipalBinding binding, DatabaseReference nombreTarea2, DatabaseReference fechaTarea2, DatabaseReference horaTarea2) {

        if (nombreTarea != null && fechaTarea != null && horaTarea != null) {
            nombreTarea.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombreTarea = snapshot.getValue(String.class);
                        binding.lbNombreTarea1.setText(nombreTarea);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Error: Al cargar el nombre de la Tarea", Toast.LENGTH_SHORT).show();
                }
            });


            fechaTarea.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {
                        String fechaTarea = snapshot.getValue(String.class);
                        binding.lbFechaTarea1.setText(fechaTarea);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Error: Al cargar la fecha de la Tarea", Toast.LENGTH_SHORT).show();
                }
            });


            horaTarea.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {
                        String horaTarea = snapshot.getValue(String.class);
                        binding.lbHoraTarea1.setText(horaTarea);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Error: Al cargar la hora de la tarea", Toast.LENGTH_SHORT).show();
                }
            });
        }


        if (nombreTarea2 != null && fechaTarea2 != null && horaTarea2 != null) {

            nombreTarea2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombreTarea2 = snapshot.getValue(String.class);
                        binding.lbNombreTarea2.setText(nombreTarea2);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Error: Al cargar el nombre de la Tarea", Toast.LENGTH_SHORT).show();
                }
            });


            fechaTarea2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {
                        String fechaTarea2 = snapshot.getValue(String.class);
                        binding.lbFechaTarea2.setText(fechaTarea2);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Error: Al cargar la fecha de la Tarea", Toast.LENGTH_SHORT).show();
                }
            });


            horaTarea2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()) {
                        String horaTarea2 = snapshot.getValue(String.class);
                        binding.lbHoraTarea2.setText(horaTarea2);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Error: Al cargar la hora de la tarea", Toast.LENGTH_SHORT).show();
                }
            });

        }


    }

    private void cargarImagenUsuarioActual(ImageView toolbarLogoImageView) {
        DatabaseReference userRef = mDatabase.getReference("UsuariosCompletos").child(user.getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Usuarios usuarios = snapshot.getValue(Usuarios.class);
                    if (usuarios != null && usuarios.getImagen() != null) {
                        Usuarios usuarios1 = new Usuarios(usuarios.getNombre().toString(), usuarios.getApellido().toString(), usuarios.getEdad(), usuarios.getEmail().toString(), usuarios.getImagen().toString(), usuarios.getUid().toString());
                        imagenPerfilUsuarioNormal = usuarios1.getImagen();
                        Picasso.get().load(usuarios1.getImagen()).into(toolbarLogoImageView);
                        listaUsuarios.add(usuarios);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error: Al cargar la imagen del usuario actual", Toast.LENGTH_SHORT).show();
            }
        });



    }
    
}