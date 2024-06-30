package com.example.tfgvictor.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tfgvictor.DAO.ListaCompraDAO;
import com.example.tfgvictor.Modelos.Producto;
import com.example.tfgvictor.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class ListaCompraAdapter extends RecyclerView.Adapter<ListaCompraAdapter.CompraVH> {

    private List<Producto> objects;
    private int resource;
    private Context context;

    private FirebaseUser fuser;


    public ListaCompraAdapter(List<Producto> objects, int resource, Context context) {
        this.objects = objects;
        this.resource = resource;
        this.context = context;


    }

    @NonNull
    @Override
    public CompraVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listaCompraView = LayoutInflater.from(context).inflate(resource, null);
        listaCompraView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return new CompraVH(listaCompraView);
    }

    @Override
    public void onBindViewHolder(@NonNull CompraVH holder, int position) {
        holder.chCompradoProducto.setChecked(false);
        Producto producto = objects.get(position);
        holder.lbNombreProducto.setText(producto.getNombre());
        holder.lbCantidadProducto.setText(producto.getCantidad());
        holder.chCompradoProducto.setOnCheckedChangeListener(null);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarModificar("¿Seguro que quieres?", producto).show();
            }
        });

        holder.chCompradoProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                confirmarBorrar(producto, holder).show();


            }
        });
    }

    private AlertDialog confirmarModificar(String mensaje, Producto producto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("EDITAR PRODUCTO");
        builder.setCancelable(false);

        View productoViewModel = LayoutInflater.from(context).inflate(R.layout.listacompra_view_model, null);
        EditText txtNombre = productoViewModel.findViewById(R.id.txtNombreProductoViewModel);
        EditText txtCatidad = productoViewModel.findViewById(R.id.txtCantidadProductoViewModel);

        txtNombre.setText(producto.getNombre());
        txtCatidad.setText(producto.getCantidad());

        builder.setView(productoViewModel);

        builder.setNegativeButton("Cancelar", null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nuevoNombre = txtNombre.getText().toString();
                String nuevaCantidad = txtCatidad.getText().toString();


                if (txtNombre.getText().toString().isEmpty() || txtCatidad.getText().toString().isEmpty()) {
                    Toast.makeText(context, "FALTAN DATOS POR RELLENAR", Toast.LENGTH_SHORT).show();
                } else {
                    producto.setNombre(nuevoNombre);
                    producto.setCantidad(nuevaCantidad);

                    actualizarEnFirebase(producto);


                    notifyItemChanged(objects.indexOf(producto));
                }
            }
        });
        return builder.create();
    }

    private void actualizarEnFirebase(Producto producto) {
        ListaCompraDAO dao = new ListaCompraDAO();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("nombre", producto.getNombre());
        hashMap.put("cantidad", producto.getCantidad());

        dao.update(producto.getId(), hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Toast.makeText(context, "Producto actualizado!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, "Error: Al actualizar el producto", Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private AlertDialog confirmarBorrar(Producto producto, CompraVH holder) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¿SEGURO QUE YA HAS COMPRADO EL PRODUCTO?");
        builder.setCancelable(false);


        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                holder.chCompradoProducto.setChecked(false);

            }
        });

        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int posicion = objects.indexOf(producto);
                objects.remove(posicion);
                borrarDeFirebase(posicion, producto); //Borrar de la bd de firebase
                notifyItemRemoved(posicion);
                notifyItemRangeChanged(posicion,objects.size());

            }
        });


        return builder.create();
    }

    private void borrarDeFirebase(int posicion, Producto producto) {
        ListaCompraDAO dao = new ListaCompraDAO();

        dao.remove(producto.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                notifyItemRemoved(posicion);
                notifyItemRangeChanged(posicion, objects.size());
                Toast.makeText(context, "Producto eliminado!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error: Al borrar el producto", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public class CompraVH extends RecyclerView.ViewHolder {
        TextView lbNombreProducto, lbCantidadProducto;
        CheckBox chCompradoProducto;

        public CompraVH(@NonNull View itemView) {
            super(itemView);

            lbNombreProducto = itemView.findViewById(R.id.lbProductoCompraHolder);
            lbCantidadProducto = itemView.findViewById(R.id.lbCantidadCompraHolder);
            chCompradoProducto = itemView.findViewById(R.id.chCompradoHolder);
        }
    }
}
