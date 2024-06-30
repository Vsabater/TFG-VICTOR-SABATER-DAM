package com.example.tfgvictor.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfgvictor.DAO.GastoHogarDAO;
import com.example.tfgvictor.Modelos.Gastos;
import com.example.tfgvictor.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GastosHogarAdapter extends RecyclerView.Adapter<GastosHogarAdapter.GastosVH> {
    private List<Gastos> objects;
    private int resource;
    private Context context;
    private TextView txtPrecioTotal;


    public GastosHogarAdapter(List<Gastos> objects, int resource, Context context, TextView txtPrecioTotal) {
        this.objects = objects;
        this.resource = resource;
        this.context = context;
        this.txtPrecioTotal = txtPrecioTotal;
    }

    @NonNull
    @Override
    public GastosVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View gastosHogarView = LayoutInflater.from(context).inflate(resource, null);
        gastosHogarView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        return new GastosVH(gastosHogarView);
    }

    @Override
    public void onBindViewHolder(@NonNull GastosVH holder, int position) {
        Gastos gastos = objects.get(position);
        holder.lbMesGastos.setText(gastos.getMes());
        holder.lbDiaGastos.setText(gastos.getDia());
        holder.lbNombreGastos.setText(gastos.getNombreGasto());
       //holder.lbPrecioGastos.setText(String.valueOf(gastos.getPrecioGasto()) + "€");

        float precioGasto = gastos.getPrecioGasto();
        String precioFormateado = String.format("%.1f€", precioGasto);
        holder.lbPrecioGastos.setText(precioFormateado);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarModificar(gastos).show();
            }
        });
    }

    private AlertDialog confirmarModificar(Gastos gastos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("EDITAR GASTO");
        builder.setCancelable(false);

        View gastoViewModel = LayoutInflater.from(context).inflate(R.layout.gastohogar_view_model, null);
        EditText txtNombre = gastoViewModel.findViewById(R.id.txtNombreGastoModel);
        EditText txtPrecio = gastoViewModel.findViewById(R.id.txtPrecioGastoModel);
        Spinner spMesGasto = gastoViewModel.findViewById(R.id.spMesGastoModel);
        EditText txtDia = gastoViewModel.findViewById(R.id.txtDiaGastoModel);


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


        ArrayAdapter<String> adapterMeses = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, meses);

        adapterMeses.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);

        spMesGasto.setAdapter(adapterMeses);


        txtNombre.setText(gastos.getNombreGasto());
        txtPrecio.setText(String.valueOf(gastos.getPrecioGasto()));
        spMesGasto.setSelection(meses.indexOf(gastos.getMes()));

        txtDia.setText(gastos.getDia());

        builder.setView(gastoViewModel);

        builder.setNegativeButton("Cancelar", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nuevoNombre = txtNombre.getText().toString();
                String nuevoPrecio = txtPrecio.getText().toString();
                String nuevoDia = txtDia.getText().toString();
                String nuevoMes = (String) spMesGasto.getSelectedItem();
                String nuevoMes2 = "";

                if (nuevoMes.equals("Enero")) {
                    nuevoMes2 = "EN";
                }
                if (nuevoMes.equals("Febrero")) {
                    nuevoMes2 = "FEB";
                }
                if (nuevoMes.equals("Marzo")) {
                    nuevoMes2 = "MAR";
                }
                if (nuevoMes.equals("Abril")) {
                    nuevoMes2 = "ABR";
                }
                if (nuevoMes.equals("Mayo")) {
                    nuevoMes2 = "MAY";
                }
                if (nuevoMes.equals("Junio")) {
                    nuevoMes2 = "JUN";
                }
                if (nuevoMes.equals("Julio")) {
                    nuevoMes2 = "JUL";
                }
                if (nuevoMes.equals("Agosto")) {
                    nuevoMes2 = "AG";
                }
                if (nuevoMes.equals("Septiembre")) {
                    nuevoMes2 = "SEP";
                }
                if (nuevoMes.equals("Octubre")) {
                    nuevoMes2 = "OCT";
                }
                if (nuevoMes.equals("Noviembre")) {
                    nuevoMes2 = "NOV";
                }
                if (nuevoMes.equals("Diciembre")) {
                    nuevoMes2 = "DIC";
                }


                if (txtNombre.getText().toString().isEmpty() || txtPrecio.getText().toString().isEmpty() || txtDia.getText().toString().isEmpty() || nuevoMes.isEmpty()) {
                    Toast.makeText(context, "FALTAN DATOS POR RELLENAR", Toast.LENGTH_SHORT).show();
                } else {
                    gastos.setNombreGasto(nuevoNombre);
                    gastos.setPrecioGasto(Float.parseFloat(nuevoPrecio));
                    gastos.setDia(nuevoDia);
                    gastos.setMes(nuevoMes2);


                    actualizarEnFirebase(gastos);

                    recalcularYActualizarPrecioTotal();

                    notifyItemChanged(objects.indexOf(gastos));
                }
            }
        });
        return builder.create();
    }

    private void actualizarEnFirebase(Gastos gastos) {
        GastoHogarDAO dao = new GastoHogarDAO();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("nombreGasto", gastos.getNombreGasto());
        hashMap.put("precioGasto", gastos.getPrecioGasto());
        hashMap.put("mes", gastos.getMes());
        hashMap.put("dia", gastos.getDia());

        dao.update(gastos.getId(), hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Toast.makeText(context, "Gasto actualizado!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, "Error: Al actualizar el gasto", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private AlertDialog confirmarBorrar(Gastos gastos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¿SEGURO QUE QUIERES ELIMINAR EL GASTO?");
        builder.setCancelable(false);


        builder.setNegativeButton("No", null);

        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int posicion = objects.indexOf(gastos);
                objects.remove(posicion);
                borrarDeFirebase(posicion, gastos);
                notifyItemRemoved(posicion);


            }
        });
        return builder.create();
    }


    private void borrarDeFirebase(int posicion, Gastos gastos) {
        GastoHogarDAO dao = new GastoHogarDAO();

        dao.remove(gastos.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Gasto Eliminado!", Toast.LENGTH_SHORT).show();
                objects.remove(gastos);
                notifyItemRemoved(posicion);


                recalcularYActualizarPrecioTotal();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error: Al eliminar el gasto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recalcularYActualizarPrecioTotal() {
        float precioTotal = 0;
        for (Gastos gastos : objects) {
            precioTotal += gastos.getPrecioGasto();
        }

        txtPrecioTotal.setText(precioTotal + "€");

    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public void deleteItem(int position) {
        Gastos gastos = objects.get(position);
        borrarDeFirebase(position, gastos);
    }

    public Gastos getItem(int position) {
        return objects.get(position);
    }

    public class GastosVH extends RecyclerView.ViewHolder {
        TextView lbMesGastos, lbDiaGastos, lbNombreGastos, lbPrecioGastos;

        public GastosVH(@NonNull View itemView) {
            super(itemView);


            lbMesGastos = itemView.findViewById(R.id.lbMesGastosViewHolder);
            lbDiaGastos = itemView.findViewById(R.id.lbDiaGastosViewHolder);
            lbNombreGastos = itemView.findViewById(R.id.lbNombreGastosViewHolder);
            lbPrecioGastos = itemView.findViewById(R.id.lbDineroGastosViewHolder);


        }
    }
}
