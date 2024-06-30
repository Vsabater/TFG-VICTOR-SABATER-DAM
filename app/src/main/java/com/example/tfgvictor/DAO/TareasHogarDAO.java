package com.example.tfgvictor.DAO;

import com.example.tfgvictor.Modelos.Producto;
import com.example.tfgvictor.Modelos.Tarea;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class TareasHogarDAO {
    private DatabaseReference databaseReference;

    public TareasHogarDAO() {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://tfg-victor-sabater-final-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = db.getReference("TareasHogar");
    }


    public Task<Void> add(ArrayList<Tarea> listaTareas) { //Añadir productos a la bd
        return databaseReference.setValue(listaTareas);
    }

    public Task<Void> update(String key, HashMap<String, Object> hashMap) {
        return databaseReference.child(key).updateChildren(hashMap);
    }

    public Task<Void> remove(String key) {
        return databaseReference.child(key).removeValue();
    }
}
