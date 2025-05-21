/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aldair
 */
public class Expedientes_Autor {
    private Usuario autor;
    private Usuario usuario;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    private List<Expediente> expedientes_emitidos;

    public Expedientes_Autor() {
        expedientes_emitidos = new ArrayList<>();
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public List<Expediente> getExpedientes_emitidos() {
        return expedientes_emitidos;
    }

    public void setExpedientes_emitidos(List<Expediente> expedientes_emitidos) {
        this.expedientes_emitidos = expedientes_emitidos;
    }
}
