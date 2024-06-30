package com.example.tfgvictor.Modelos;

import java.time.LocalDateTime;
import java.util.Date;

public class Tarea {

    private String idTarea;
    private String idUsuario;
    private String imagenUsuario;
    private String nombre;
    private String fecha;
    private String hora;

    public Tarea() {
    }


    public Tarea(String idTarea,String nombre, String fecha, String hora,String idUsuario, String imagenUsuario) {
        this.idTarea = idTarea;
        this.nombre = nombre;
        this.fecha = fecha;
        this.hora = hora;
        this.idUsuario = idUsuario;
        this.imagenUsuario = imagenUsuario;
    }

    public String getIdTarea() {
        return idTarea;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getImagenUsuario() {
        return imagenUsuario;
    }

    public void setImagenUsuario(String imagenUsuario) {
        this.imagenUsuario = imagenUsuario;
    }

    @Override
    public String toString() {
        return "Tarea{" +
                "nombre='" + nombre + '\'' +
                ", fecha=" + fecha +
                ", hora=" + hora +
                '}';
    }
}



