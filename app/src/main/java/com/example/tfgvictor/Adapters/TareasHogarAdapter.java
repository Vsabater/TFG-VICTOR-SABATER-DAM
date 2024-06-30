package com.example.tfgvictor.Adapters;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tfgvictor.DAO.TareasHogarDAO;
import com.example.tfgvictor.Dialogs.DatePickerFragment;
import com.example.tfgvictor.Fragments.TareasHogarFragment;
import com.example.tfgvictor.Modelos.Tarea;
import com.example.tfgvictor.Modelos.Usuarios;
import com.example.tfgvictor.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TareasHogarAdapter extends RecyclerView.Adapter<TareasHogarAdapter.TareaVH> {
    private List<Tarea> objects;
    private int resource;
    private Context context;
    private String imagenUsuarioNormal;
    private List<Usuarios> listaUsuarios;
    private FragmentManager fragmentManager;


    public TareasHogarAdapter(List<Tarea> objects, int resource, Context context, String imagenUsuarioNormal, List<Usuarios> listaUsuarios, FragmentManager fragmentManager) {
        this.objects = objects;
        this.resource = resource;
        this.context = context;
        this.imagenUsuarioNormal = imagenUsuarioNormal;
        this.listaUsuarios = listaUsuarios;
        this.fragmentManager = fragmentManager;


    }

    @NonNull
    @Override
    public TareaVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View tareasHogarView = LayoutInflater.from(context).inflate(resource, null);

        tareasHogarView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return new TareaVH(tareasHogarView);
    }


    @Override
    public void onBindViewHolder(@NonNull TareaVH holder, int position) {
        Tarea tarea = objects.get(position);


        holder.lbNombre.setText(tarea.getNombre());
        holder.lbFecha.setText(tarea.getFecha());
        holder.lbHora.setText(tarea.getHora());
        holder.chTareaCompletada.setOnCheckedChangeListener(null);


        Picasso.get()
                .load(tarea.getImagenUsuario())
                .into(holder.imgFotoPerfil);

        cambiarColorFecha(holder,tarea);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null && tarea.getIdUsuario().equals(currentUser.getUid())) {
                    confirmarModificar("¿Seguro que quieres?", tarea).show();
                } else {
                    Toast.makeText(context, "Solo el propietario de la tarea puede editar esta tarea", Toast.LENGTH_SHORT).show();
                }


            }
        });


        holder.chTareaCompletada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarBorrar(tarea, holder).show();
            }
        });
    }

    private void cambiarColorFecha(TareaVH holder, Tarea tarea) {
        Calendar calendarioActual = Calendar.getInstance();
        long tiempoActual = calendarioActual.getTimeInMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("dd / MM / yyyy HH:mm", Locale.getDefault());
        String fechaHoraTareaString = tarea.getFecha() + " " + tarea.getHora();
        Date fechaHoraTarea = null;

        try {
            fechaHoraTarea = sdf.parse(fechaHoraTareaString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (fechaHoraTarea != null) {
            long tiempoTarea = fechaHoraTarea.getTime();
            if (tiempoTarea < tiempoActual) {
                holder.lbFecha.setTextColor(Color.RED);
                holder.lbHora.setTextColor(Color.RED);
            } else {
                holder.lbFecha.setTextColor(Color.BLACK);
                holder.lbHora.setTextColor(Color.BLACK);
            }
        }
    }

    private AlertDialog confirmarModificar(String mensaje, Tarea tarea) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("EDITAR TAREA");
        builder.setCancelable(false);

        View tareaViewModel = LayoutInflater.from(context).inflate(R.layout.tareashogar_view_model, null);
        EditText txtNombre = tareaViewModel.findViewById(R.id.txtNombreTareaModel);
        EditText txtFecha = tareaViewModel.findViewById(R.id.txtFechaViewModel);
        EditText txtHora = tareaViewModel.findViewById(R.id.txtHoraViewModel);

        txtFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.txtFechaViewModel:
                        showDatePickerDialog(txtFecha);
                        break;
                }
            }
        });

        txtHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hora = c.get(Calendar.HOUR_OF_DAY);
                int minutos = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
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


        txtNombre.setText(tarea.getNombre());
        txtFecha.setText(tarea.getFecha());
        txtHora.setText(tarea.getHora());

        builder.setView(tareaViewModel);

        builder.setNegativeButton("Cancelar", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nuevoNombre = txtNombre.getText().toString();
                String nuevaFecha = txtFecha.getText().toString();
                String nuevaHora = txtHora.getText().toString();


                if (txtNombre.getText().toString().isEmpty() || txtFecha.getText().toString().isEmpty() || txtHora.getText().toString().isEmpty()) {
                    Toast.makeText(context, "FALTAN DATOS POR RELLENAR", Toast.LENGTH_SHORT).show();
                } else {
                    tarea.setNombre(nuevoNombre);
                    tarea.setFecha(nuevaFecha);
                    tarea.setHora(nuevaHora);

                    actualizarEnFirebase(tarea);


                    notifyItemChanged(objects.indexOf(tarea));
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

                    Toast.makeText(context, "No se puede seleccionar una fecha anterior a la de hoy", Toast.LENGTH_SHORT).show();
                } else {
                    String selectedDate = dayOfMonth + " / " + (month + 1) + " / " + year;
                    txtFecha.setText(selectedDate);
                }

            }
        });
        datePickerFragment.show(fragmentManager, "datePicker");
    }

    private boolean mismoDia(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void actualizarEnFirebase(Tarea tarea) {
        TareasHogarDAO dao = new TareasHogarDAO();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("nombre", tarea.getNombre());
        hashMap.put("fecha", tarea.getFecha());
        hashMap.put("hora", tarea.getHora());

        dao.update(tarea.getIdTarea(), hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Tarea actualizada!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error: Al actualizar la tarea", Toast.LENGTH_SHORT).show();
            }
        });


    }


    private AlertDialog confirmarBorrar(Tarea tarea, TareaVH holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¿SEGURO QUE YA HAS COMPLETADO LA TAREA ?");
        builder.setCancelable(false);


        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                holder.chTareaCompletada.setChecked(false);
            }
        });

        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int posicion = objects.indexOf(tarea);
                objects.remove(posicion);
                borrarDeFirebase(posicion, tarea); //Borrar de la bd de Firebase
                notifyItemRemoved(posicion);
                notifyItemRangeChanged(posicion,objects.size());

            }
        });
        return builder.create();
    }

    private void borrarDeFirebase(int posicion, Tarea tarea) {
        TareasHogarDAO dao = new TareasHogarDAO();

        dao.remove(tarea.getIdTarea()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Tarea eliminada!", Toast.LENGTH_SHORT).show();
                notifyItemRemoved(posicion);
                notifyItemRangeChanged(posicion, objects.size());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error: Al borrar la tarea", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }


    public class TareaVH extends RecyclerView.ViewHolder {
        TextView lbNombre, lbFecha, lbHora;
        ImageView imgFotoPerfil;
        CheckBox chTareaCompletada;

        public TareaVH(@NonNull View itemView) {
            super(itemView);


            lbNombre = itemView.findViewById(R.id.lbNombreTareaHogarHolder);
            lbFecha = itemView.findViewById(R.id.lbFechaTareasHogarHolder);
            lbHora = itemView.findViewById(R.id.lbHoraTareasHogarHolder);
            chTareaCompletada = itemView.findViewById(R.id.chCompletadoTaregasHogarHolder);
            imgFotoPerfil = itemView.findViewById(R.id.imgFotoPerfilTareasHogarHolder);
        }
    }

}
