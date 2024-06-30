package com.example.tfgvictor.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.tfgvictor.Adapters.ListaCompraAdapter;
import com.example.tfgvictor.DAO.ListaCompraDAO;

import com.example.tfgvictor.Modelos.Producto;
import com.example.tfgvictor.Modelos.Usuarios;
import com.example.tfgvictor.R;
import com.example.tfgvictor.databinding.FragmentListaCompraBinding;

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


public class ListaCompraFragment extends Fragment {

    private FragmentListaCompraBinding binding;
    private RecyclerView recyclerView;
    private ArrayList<Producto> listaProductos;
    private ListaCompraAdapter adapter;
    private RecyclerView.LayoutManager lm;
    private Toolbar toolbar;

    private FirebaseUser user;
    private FirebaseAuth auth;
    private DatabaseReference myRef, usuariosCompletos;
    private FirebaseDatabase mDatabase;
    private static String uid;
    private static String imagenPerfilUsuarioNormal;
    private ArrayList<Usuarios> listaUsuarios;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentListaCompraBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        listaProductos = new ArrayList<>();
        listaUsuarios = new ArrayList<>();

        View vista = binding.getRoot();

        myRef = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/").getReference("ListaCompra");
        mDatabase = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/");
        usuariosCompletos = mDatabase.getReference("UsuariosCompletos").child(user.getUid());


        cargarListaCompra(myRef); //Con esto cargamos el recycler de la bd de Firebase

        uid = user.getUid();

        toolbar = vista.findViewById(R.id.my_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Lista De La Compra");


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
                Toast.makeText(activity, "Error: No se puede cargar la imagen del usuario actual", Toast.LENGTH_SHORT).show();
            }
        });


        recyclerView = vista.findViewById(R.id.recyclerListaCompra);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        //Esto se lo pasamos al Adapater y el context en los fragments es getAtivity
        adapter = new ListaCompraAdapter(listaProductos, R.layout.listacompra_view_holder, getActivity());
        recyclerView.setAdapter(adapter);


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearProducto().show();
            }
        });


        return vista;
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
                Toast.makeText(getActivity(), "Error: Al cargar la imagen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarListaCompra(DatabaseReference myRef) {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() { //Para recuperar los datos de firebase y cargarlos en el recycler
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaProductos.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Producto producto = ds.getValue(Producto.class);
                    listaProductos.add(producto);
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Error: Al cargar la lista de la compra", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private AlertDialog crearProducto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("AÑADIR COMPRA");
        builder.setCancelable(true);

        View compraViewModel = LayoutInflater.from(getActivity()).inflate(R.layout.listacompra_view_model, null);
        EditText txtNombre = compraViewModel.findViewById(R.id.txtNombreProductoViewModel);
        EditText txtCantidad = compraViewModel.findViewById(R.id.txtCantidadProductoViewModel);
        builder.setView(compraViewModel);


        builder.setNegativeButton("Cancelar", null);

        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (txtNombre.getText().toString().isEmpty() || txtCantidad.getText().toString().isEmpty() || Integer.parseInt(String.valueOf(txtCantidad.getText())) >= 100) {

                    if (txtNombre.getText().toString().isEmpty() || txtCantidad.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "Error: Faltan campos por rellenar", Toast.LENGTH_SHORT).show();
                    } else {
                        txtCantidad.setText("");
                        Toast.makeText(getActivity(), "Error: la cantidad debe ser menor de 100", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    // ListaCompraDAO dao = new ListaCompraDAO();

                    String productId = myRef.push().getKey();

                    String nombreProducto = txtNombre.getText().toString();
                    String convertidoEnMayus = nombreProducto.toUpperCase().charAt(0) + nombreProducto.substring(1, nombreProducto.length()).toLowerCase();
                    Producto producto = new Producto(productId, convertidoEnMayus, txtCantidad.getText().toString());
                    listaProductos.add(0, producto);


                    myRef.child(productId).setValue(producto).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getActivity(), "Producto Añadido", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    adapter.notifyItemInserted(0);
                }
            }
        });

        return builder.create();

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onResume() {
        super.onResume();
        listaProductos.clear();

    }


}