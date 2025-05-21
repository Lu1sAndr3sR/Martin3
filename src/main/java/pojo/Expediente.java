/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 *
 * @author aldair
 */
public class Expediente {
    private String id_expediente;
    private String titulo;
    private Date fecha_creacion;
    private String temas;
    private String id_usuario;
    private String id_autor;
    private Usuario visor;
    private Usuario emisor;
    private List<Documento> documentos;
    private String direccion_blockchain;
    private boolean activo;
    private Timestamp fecha_utc;

    public Timestamp getFecha_utc() {
        return fecha_utc;
    }

    public void setFecha_utc(Timestamp fecha_prueba) {
        this.fecha_utc = fecha_prueba;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean estado) {
        this.activo = estado;
    }
    
    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getId_autor() {
        return id_autor;
    }

    public void setId_autor(String id_autor) {
        this.id_autor = id_autor;
    }

    public String getDireccion_blockchain() {
        return direccion_blockchain;
    }

    public void setDireccion_blockchain(String direccion_blockchain) {
        this.direccion_blockchain = direccion_blockchain;
    }

    public List<Documento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<Documento> documentos) {
        this.documentos = documentos;
    }

    public String getId_expediente() {
        return id_expediente;
    }

    public void setId_expediente(String id) {
        this.id_expediente = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Date getFecha_creacion() {
        return fecha_creacion;
    }

    public void setFecha_creacion(Date fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
    }

    public String getTemas() {
        return temas;
    }

    public void setTemas(String temas) {
        this.temas = temas;
    }

    public Usuario getVisor() {
        return visor;
    }

    public void setVisor(Usuario visor) {
        this.visor = visor;
    }

    public Usuario getEmisor() {
        return emisor;
    }

    public void setEmisor(Usuario emisor) {
        this.emisor = emisor;
    }

}
