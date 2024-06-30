package com.example.tfgvictor.DAO;

import com.example.tfgvictor.Modelos.Gastos;
import com.example.tfgvictor.Modelos.Producto;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class GastoHogarDAO {
    private DatabaseReference databaseReference;

    public GastoHogarDAO() {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = db.getReference("GastosHogar");
    }


    public Task<Void> add(ArrayList<Gastos> listaGastos) { //Añadir productos a la bd
        return databaseReference.setValue(listaGastos);
    }

    public Task<Void> update(String key, HashMap<String, Object> hashMap) {
        return databaseReference.child(key).updateChildren(hashMap);
    }

    public Task<Void> remove(String key) {
        return databaseReference.child(key).removeValue();
    }
}
