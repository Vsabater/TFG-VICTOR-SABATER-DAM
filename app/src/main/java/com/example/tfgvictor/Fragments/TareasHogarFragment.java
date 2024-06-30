package com.example.tfgvictor.Fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tfgvictor.Adapters.TareasHogarAdapter;
import com.example.tfgvictor.DAO.TareasHogarDAO;
import com.example.tfgvictor.Dialogs.DatePickerFragment;
import com.example.tfgvictor.Modelos.Tarea;
import com.example.tfgvictor.Modelos.Usuarios;
import com.example.tfgvictor.R;
import com.example.tfgvictor.databinding.FragmentTareasHogarBinding;
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
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class TareasHogarFragment extends Fragment {
    private FragmentTareasHogarBinding binding;
    private RecyclerView recyclerView;
    private ArrayList<Tarea> listaTareas;
    private TareasHogarAdapter adapter;
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
        binding = FragmentTareasHogarBinding.inflate(inflater, container, false);
        View vista = binding.getRoot();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        listaTareas = new ArrayList<>();
        listaUsuarios = new ArrayList<>();


        myRef = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/").getReference("TareasHogar");
        mDatabase = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/");
        usuariosCompletos = mDatabase.getReference("UsuariosCompletos").child(user.getUid());

        uid = user.getUid();
        cargarTareasHogar(myRef, usuariosCompletos);


        //Aqui controlo el toolbar individual de cada uno
        toolbar = vista.findViewById(R.id.my_toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Tareas Hogar");

        ImageView toolbarLogoImageView = vista.findViewById(R.id.userImage);


        cargarImagenUsuarioActual(toolbarLogoImageView);


        usuariosCompletos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cargarImagenUsuarioActual(toolbarLogoImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        recyclerView = vista.findViewById(R.id.recyclerTareasHogar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TareasHogarAdapter(listaTareas, R.layout.tareashogar_view_holder, getActivity(), imagenPerfilUsuarioNormal, listaUsuarios, getActivity().getSupportFragmentManager());
        recyclerView.setAdapter(adapter);


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearTarea().show();
            }
        });


        return vista;
    }


    private void cargarImagenUsuarioActual(ImageView imageView) {
        DatabaseReference userRef = mDatabase.getReference("UsuariosCompletos").child(user.getUid());

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if (snapshot.exists()) {
                    Usuarios usuarios = snapshot.getValue(Usuarios.class);
                    if (usuarios != null && usuarios.getImagen() != null) {
                        Usuarios usuarios1 = new Usuarios(usuarios.getNombre().toString(), usuarios.getApellido().toString(), usuarios.getEdad(), usuarios.getEmail().toString(), usuarios.getImagen().toString(), usuarios.getUid().toString());
                        imagenPerfilUsuarioNormal = usuarios1.getImagen();
                        Picasso.get().load(usuarios1.getImagen()).into(imageView);
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


    private void cargarTareasHogar(DatabaseReference myRef, DatabaseReference usuariosCompletos) {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() { //Para recuperar los datos de firebase y cargarlos en el recycler
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaTareas.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Tarea tarea = ds.getValue(Tarea.class);
                    listaTareas.add(tarea);
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private AlertDialog crearTarea() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("AÑADIR TAREA");
        builder.setCancelable(true);

        View tareaViewModel = LayoutInflater.from(getActivity()).inflate(R.layout.tareashogar_view_model, null);
        EditText txtNombre = tareaViewModel.findViewById(R.id.txtNombreTareaModel);
        EditText txtFecha = tareaViewModel.findViewById(R.id.txtFechaViewModel);
        EditText txtHora = tareaViewModel.findViewById(R.id.txtHoraViewModel);


        txtFecha.setOnClickListener(new View.OnClickListener() { //Selector de fecha
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.txtFechaViewModel:
                        showDatePickerDialog(txtFecha);
                        break;
                }
            }
        });

        txtHora.setOnClickListener(new View.OnClickListener() { //Selector de Hora
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hora = c.get(Calendar.HOUR_OF_DAY);
                int minutos = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String minutosFormateados = (minute < 10) ? "0" + String.valueOf(minute) : String.valueOf(minute);

                        String horaFormateada;
                        if (hourOfDay >= 1 && hourOfDay <= 9) {

                            horaFormateada = "0" + hourOfDay + ":" + minutosFormateados;
                        } else {
                            horaFormateada = hourOfDay + ":" + minutosFormateados;
                        }

                        txtHora.setText(horaFormateada);
                    }
                }, hora, minutos, false);
                timePickerDialog.show();
            }
        });


        builder.setView(tareaViewModel);


        builder.setNegativeButton("Cancelar", null);

        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fecha = txtFecha.getText().toString();


                if (txtNombre.getText().toString().isEmpty() || txtFecha.getText().toString().isEmpty() || txtHora.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Error: Faltan datos en la tarea", Toast.LENGTH_SHORT).show();

                } else {


                    String tareaId = myRef.push().getKey();
                    String nombreTarea = txtNombre.getText().toString();
                    String convertidoEnMayus = nombreTarea.toUpperCase().charAt(0) + nombreTarea.substring(1, nombreTarea.length()).toLowerCase();
                    Tarea tarea = new Tarea(tareaId, convertidoEnMayus, fecha, txtHora.getText().toString(), uid.toString(), imagenPerfilUsuarioNormal);
                    listaTareas.add(0, tarea);


                    myRef.child(tareaId).setValue(tarea).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getActivity(), "Tarea Añadida", Toast.LENGTH_SHORT).show();
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

    private void showDatePickerDialog(EditText txtFecha) {
        DatePickerFragment datePickerFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);

                Calendar currentCalendar = Calendar.getInstance();

                if (selectedCalendar.before(currentCalendar) && !mismoDia(selectedCalendar, currentCalendar)) {

                    Toast.makeText(getActivity(), "No se puede seleccionar una fecha anterior a la de hoy", Toast.LENGTH_SHORT).show();
                } else {
                    String selectedDate = dayOfMonth + " / " + (month + 1) + " / " + year;
                    txtFecha.setText(selectedDate);
                }
            }
        });
        datePickerFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    private boolean mismoDia(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }


}