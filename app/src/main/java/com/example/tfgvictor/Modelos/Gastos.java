package com.example.tfgvictor.Modelos;

public class Gastos {
    private String id;
    private String mes;
    private String dia;
    private String nombreGasto;
    private float precioGasto;


    public Gastos() {
    }

    public Gastos(String id, String mes, String dia, String nombreGasto, float precioGasto) {
        this.id = id;
        this.mes = mes;
        this.dia = dia;
        this.nombreGasto = nombreGasto;
        this.precioGasto = precioGasto;
    }

    public String getId() {
        return id;
    }


    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getNombreGasto() {
        return nombreGasto;
    }

    public void setNombreGasto(String nombreGasto) {
        this.nombreGasto = nombreGasto;
    }

    public float getPrecioGasto() {

        return precioGasto;
    }

    public void setPrecioGasto(float precioGasto) {

        this.precioGasto = precioGasto;
    }


    @Override
    public String toString() {
        return "Gastos{" +
                "mes='" + mes + '\'' +
                ", dia='" + dia + '\'' +
                ", nombreGasto='" + nombreGasto + '\'' +
                ", precioGasto=" + precioGasto;
    }


}
