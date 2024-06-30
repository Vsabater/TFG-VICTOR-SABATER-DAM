package com.example.tfgvictor.Modelos;

public class Producto {

    private String id;

    private String nombre;
    private String cantidad;
    private Boolean comprado;

    public Producto() {
    }

    public Producto(String id,String nombre, String cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.cantidad = cantidad;

    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public Boolean getComprado() {
        return comprado;
    }

    public void setComprado(Boolean comprado) {
        this.comprado = comprado;
    }


    @Override
    public String toString() {
        return "Producto{" +
                "nombre='" + nombre + '\'' +
                ", cantidad='" + cantidad + '\'' +
                ", comprado=" + comprado +
                '}';
    }
}