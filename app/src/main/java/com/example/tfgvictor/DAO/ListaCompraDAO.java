package com.example.tfgvictor.DAO;

import com.example.tfgvictor.Modelos.Producto;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;


public class ListaCompraDAO {

    private DatabaseReference databaseReference;

    public ListaCompraDAO() {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = db.getReference("ListaCompra");
    }


    public Task<Void> add(ArrayList<Producto> listaProducto) { //Añadir productos a la bd
        return databaseReference.setValue(listaProducto);
    }

    public Task<Void> update(String productoId, HashMap<String, Object> hashMap) {
        return databaseReference.child(productoId).updateChildren(hashMap);
    }

    public Task<Void> remove(String key) {
        return databaseReference.child(key).removeValue();
    }


}