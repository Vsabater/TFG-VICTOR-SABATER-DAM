package com.example.tfgvictor.Helpers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfgvictor.Adapters.GastosHogarAdapter;
import com.example.tfgvictor.Modelos.Gastos;

public class DeslizarParaBorrarCallback extends ItemTouchHelper.SimpleCallback {
    private GastosHogarAdapter adapter;
    private Context context;

    public DeslizarParaBorrarCallback(GastosHogarAdapter adapter, Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.context = context;
    }


    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        Gastos gastos = adapter.getItem(position);
        confirmarBorrar(gastos, position).show();
    }

    private Dialog confirmarBorrar(Gastos gastos, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¿Seguro que quieres eliminar el gasto?");
        builder.setCancelable(false);

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                adapter.notifyItemChanged(position);
            }
        });

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                adapter.deleteItem(position);
            }
        });

        return builder.create();
    }
}
