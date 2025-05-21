/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

/**
 *
 * @author aldair
 */
public class Usuario {

    private String id_usuario;
    private String nombre;
    private String apellido_paterno;
    private String apellido_materno;
    private String biometria;
    private String pasw;
    private String email;
    private String tel_casa;
    private String tel_movil;
    private String direccion_blockchain;
    private Perfil id_perfil;
    private boolean activo;
    private String sal;

    public String getSal() {
        return sal;
    }

    public void setSal(String sal) {
        this.sal = sal;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    

    public enum Perfil {
        ADMINISTRADOR("ADMINISTRADOR"),
        USUARIO("USUARIO"),
        AUTOR("AUTOR");
        private final String typo;

        Perfil(String ad) {
            this.typo = ad;
        }

        public String getPerfil() {
            return typo;
        }

    }

    public static Perfil toPerfil(String perfil) {
        switch (perfil.trim()) {
            case "ADMINISTRADOR":
                return Perfil.ADMINISTRADOR;
            case "USUARIO":
                return Perfil.USUARIO;
            case "AUTOR":
                return Perfil.AUTOR;
            default:
                return null;
        }
    }

    public Perfil getId_perfil() {
        return id_perfil;
    }

    public void setId_perfil(Perfil id_perfil) {
        this.id_perfil = id_perfil;
    }

    public String getDireccion_blockchain() {
        return direccion_blockchain;
    }

    public void setDireccion_blockchain(String direccion_blockchain) {
        this.direccion_blockchain = direccion_blockchain;
    }

    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getApellido_materno() {
        return apellido_materno;
    }

    public void setApellido_materno(String apelliddo_materno) {
        this.apellido_materno = apelliddo_materno;
    }

    public String getBiometria() {
        return biometria;
    }

    public void setBiometria(String biometria) {
        this.biometria = biometria;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTel_casa() {
        return tel_casa;
    }

    public void setTel_casa(String tel_casa) {
        this.tel_casa = tel_casa;
    }

    public String getTel_movil() {
        return tel_movil;
    }

    public void setTel_movil(String tel_movil) {
        this.tel_movil = tel_movil;
    }

    public String getPasw() {
        return pasw;
    }

    public void setPasw(String password) {
        this.pasw = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido_paterno() {
        return apellido_paterno;
    }

    public void setApellido_paterno(String apellidopaterno) {
        this.apellido_paterno = apellidopaterno;
    }

}
