package com.example.tfgvictor.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.tfgvictor.Adapters.GastosHogarAdapter;
import com.example.tfgvictor.Helpers.DeslizarParaBorrarCallback;
import com.example.tfgvictor.Modelos.Gastos;
import com.example.tfgvictor.Modelos.Usuarios;
import com.example.tfgvictor.R;
import com.example.tfgvictor.databinding.FragmentGastosHogarBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class GastosHogarFragment extends Fragment {

    private FragmentGastosHogarBinding binding;
    private RecyclerView recyclerView;
    private ArrayList<Gastos> listaGastos;
    private GastosHogarAdapter adapter;
    private RecyclerView.LayoutManager lm;
    private Toolbar toolbar;
    private static String mesSeleccionado;

    private FirebaseUser user;
    private FirebaseAuth auth;
    private DatabaseReference myRef, usuariosCompletos;
    private FirebaseDatabase mDatabase;
    private static String uid;
    private static String imagenPerfilUsuarioNormal;
    private ArrayList<Usuarios> listaUsuarios;
    private Gastos gastos;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentGastosHogarBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        listaUsuarios = new ArrayList<>();
        uid = user.getUid();

        Toast.makeText(getActivity(), "DESLIZA PARA ELIMINAR, CLICK PARA EDITAR", Toast.LENGTH_SHORT).show();

        listaGastos = new ArrayList<>();


        View vista = binding.getRoot();


        myRef = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/").getReference("GastosHogar");
        mDatabase = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/");
        usuariosCompletos = mDatabase.getReference("UsuariosCompletos").child(user.getUid());

        cargarGastosHogar(myRef); //Lo cargamos de firebase

        toolbar = vista.findViewById(R.id.my_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Gasto Hogar");


        ImageView toolbarLogoImageView = vista.findViewById(R.id.userImage);

        cargarImagenUsuarioActual(toolbarLogoImageView);

        usuariosCompletos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    cargarImagenUsuarioActual(toolbarLogoImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(activity, "Error: Al cargar la imagen del usuario actual", Toast.LENGTH_SHORT).show();
            }
        });


        recyclerView = vista.findViewById(R.id.recyclerGastosHogar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new GastosHogarAdapter(listaGastos, R.layout.gastoshogar_view_holder, getActivity(), binding.txtPrecioTotalFrgament);
        recyclerView.setAdapter(adapter);

        //Con el ItemTouchHelper deslizo para eliminar

        ItemTouchHelper.SimpleCallback deslizarParaBorrarCallback = new DeslizarParaBorrarCallback(adapter, getContext());

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(deslizarParaBorrarCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearGasto().show();
            }
        });

        cargarGastosHogarFirebase(binding);
        return vista;

    }

    private void cargarGastosHogarFirebase(FragmentGastosHogarBinding binding) {
        listaGastos.clear();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaGastos.clear();
                float precioTotal = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Gastos gastos1 = ds.getValue(Gastos.class);
                        listaGastos.add(gastos1);

                        precioTotal += gastos1.getPrecioGasto();
                    }
                    binding.txtPrecioTotalFrgament.setText(String.valueOf(precioTotal) + "€");
                    adapter.notifyDataSetChanged();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                Toast.makeText(getActivity(), "Error: Al cargar la imagen del usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarGastosHogar(DatabaseReference myRef) { //Cargar los gastos de la bd de firebase
        listaGastos.clear();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() { //Para recuperar los datos de firebase y cargarlos en el recycler
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaGastos.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Gastos gastos = ds.getValue(Gastos.class);


                    listaGastos.add(gastos);


                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private AlertDialog crearGasto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("AÑADIR GASTO");
        builder.setCancelable(true);

        View gastoViewModel = LayoutInflater.from(getActivity()).inflate(R.layout.gastohogar_view_model, null);
        EditText txtNombre = gastoViewModel.findViewById(R.id.txtNombreGastoModel);
        EditText txtDia = gastoViewModel.findViewById(R.id.txtDiaGastoModel);
        EditText txtPrecio = gastoViewModel.findViewById(R.id.txtPrecioGastoModel);
        Spinner spinner = gastoViewModel.findViewById(R.id.spMesGastoModel);


        ArrayList<String> meses = new ArrayList<>();
        meses.add("Enero");
        meses.add("Febrero");
        meses.add("Marzo");
        meses.add("Abril");
        meses.add("Mayo");
        meses.add("Junio");
        meses.add("Julio");
        meses.add("Agosto");
        meses.add("Septiembre");
        meses.add("Octubre");
        meses.add("Noviembre");
        meses.add("Diciembre");

        ArrayAdapter<String> adapterMeses = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, meses);
        adapterMeses.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(adapterMeses);


        builder.setView(gastoViewModel);


        builder.setNegativeButton("Cancelar", null);

        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (txtNombre.getText().toString().isEmpty() || spinner.getSelectedItem().toString().equals("") || txtPrecio.getText().toString().isEmpty() || txtDia.getText().toString().isEmpty() || Integer.parseInt(txtDia.getText().toString()) <= 0 || Integer.parseInt(txtDia.getText().toString()) > 31) {

                    if (txtNombre.getText().toString().isEmpty() || spinner.getSelectedItem().toString().equals("") || txtPrecio.getText().toString().isEmpty() || txtDia.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "Error: Faltan datos por rellenar", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Error: El dia debe ser entre 1 y 31", Toast.LENGTH_SHORT).show();
                        txtDia.setText("");
                    }


                } else {

                    if (spinner.getSelectedItem().equals("Enero")) {
                        mesSeleccionado = "EN";
                    }
                    if (spinner.getSelectedItem().equals("Febrero")) {
                        mesSeleccionado = "FEB";
                    }
                    if (spinner.getSelectedItem().equals("Marzo")) {
                        mesSeleccionado = "MAR";
                    }
                    if (spinner.getSelectedItem().equals("Abril")) {
                        mesSeleccionado = "ABR";
                    }
                    if (spinner.getSelectedItem().equals("Mayo")) {
                        mesSeleccionado = "MAY";
                    }
                    if (spinner.getSelectedItem().equals("Junio")) {
                        mesSeleccionado = "JUN";
                    }
                    if (spinner.getSelectedItem().equals("Julio")) {
                        mesSeleccionado = "JUL";
                    }
                    if (spinner.getSelectedItem().equals("Agosto")) {
                        mesSeleccionado = "AG";
                    }
                    if (spinner.getSelectedItem().equals("Septiembre")) {
                        mesSeleccionado = "SEP";
                    }
                    if (spinner.getSelectedItem().equals("Octubre")) {
                        mesSeleccionado = "OCT";
                    }
                    if (spinner.getSelectedItem().equals("Noviembre")) {
                        mesSeleccionado = "NOV";
                    }
                    if (spinner.getSelectedItem().equals("Diciembre")) {
                        mesSeleccionado = "DIC";
                    }
                    String gastoId = myRef.push().getKey();


                    String nombreGastos = txtNombre.getText().toString();
                    String convertidoEnMayus = nombreGastos.toUpperCase().charAt(0) + nombreGastos.substring(1, nombreGastos.length()).toLowerCase();
                    gastos = new Gastos(gastoId, mesSeleccionado, txtDia.getText().toString(), convertidoEnMayus, Float.parseFloat(txtPrecio.getText().toString()));
                    listaGastos.add(0, gastos);


                    myRef.child(gastoId).setValue(gastos).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            actualizarPrecioTotal();
                            adapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Error: Al añadir el gasto", Toast.LENGTH_SHORT).show();
                        }
                    });


                    adapter.notifyItemInserted(0);
                }
            }

        });


        return builder.create();
    }

    private void actualizarPrecioTotal() {
        float precioTotal = 0;
        for (Gastos gasto : listaGastos) {
            precioTotal += gasto.getPrecioGasto();
        }
        binding.txtPrecioTotalFrgament.setText(precioTotal + "€");
    }


}