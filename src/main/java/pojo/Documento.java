/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author aldair
 */
public class Documento {

    private String id_documento;
    private String direccion_blockchain;
    private String nom_documento;
    private Date fecha_creacion;
    private Date fecha_incorporacion;
    private String descripcion;
    private String formato;
    private int tamano;
    private int no_paginas;
    private int nivel_confidencialidad;
    private String archivob64;
    private boolean activo;
    private Timestamp fechautc;

    public Timestamp getFechautc() {
        return fechautc;
    }

    public void setFechautc(Timestamp fechautc) {
        this.fechautc = fechautc;
    }
    

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getArchivob64() {
        return archivob64;
    }

    public void setArchivob64(String archivob64) {
        this.archivob64 = archivob64;
    }

    public int getNivel_confidencialidad() {
        return nivel_confidencialidad;
    }

    public void setNivel_confidencialidad(int nivel_confidencialidad) {
        this.nivel_confidencialidad = nivel_confidencialidad;
    }

    public String getId_documento() {
        return id_documento;
    }

    public void setId_documento(String id_documento) {
        this.id_documento = id_documento;
    }

    public String getNom_documento() {
        return nom_documento;
    }

    public void setNom_documento(String nombre_recurso) {
        this.nom_documento = nombre_recurso;
    }

    public Date getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(Date fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public Date getFecha_incorporacion() {
        return fecha_incorporacion;
    }

    public void setFecha_incorporacion(Date fecha_incorporacion) {
        this.fecha_incorporacion = fecha_incorporacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getDireccion_blockchain() {
        return direccion_blockchain;
    }

    public void setDireccion_blockchain(String direccion_blockchain) {
        this.direccion_blockchain = direccion_blockchain;
    }

    public int getTamano() {
        return tamano;
    }

    public void setTamano(int tamano) {
        this.tamano = tamano;
    }

    public int getNo_paginas() {
        return no_paginas;
    }

    public void setNo_paginas(int no_paginas) {
        this.no_paginas = no_paginas;
    }

    @Override
    public String toString() {
        return "nombre: " + nom_documento + "\nid_documento" + id_documento;
    }

}
