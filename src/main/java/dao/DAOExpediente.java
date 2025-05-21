/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import client.Eth_Expediente;
import java.io.IOException;
import pojo.Expediente;
import pojo.Expedientes_Autor;
import pojo.Usuario;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import pojo.Documento;
import pojo.ErrorClass;

/**
 *
 * @author aldair
 */
public class DAOExpediente implements Serializable {

    private static DAOExpediente daoexpediente = null;

    private DAOExpediente() {

    }

    public static DAOExpediente getDAOExpediente() {
        if (daoexpediente == null) {
            daoexpediente = new DAOExpediente();
        }
        return daoexpediente;
    }

    //Crea un nuevo expediente (Funciona)
    public boolean post(Expediente ex) throws SQLException, IOException, JSONException, ErrorClass {

        Connection con = Conexion.getConnection();
        PreparedStatement pr = null;
        try {
            Instant instan = Instant.now();
            ZonedDateTime there = ZonedDateTime.ofInstant(instan, ZoneId.of("UTC"));
            Eth_Expediente ethexp = Eth_Expediente.getEth_Expediente();
            Timestamp fecha = Timestamp.from(instan);
            ex.setFecha_creacion(fecha);
            ex.setId_expediente("exp" + UUID.randomUUID().toString().substring(0, 12));
            ex.setId_usuario(BusquedasIdsBlockchain.getDireccionBlockchain(ex.getId_usuario(), BusquedasIdsBlockchain.IdToBlock.USUARIO));
            ex.setId_autor(BusquedasIdsBlockchain.getDireccionBlockchain(ex.getId_autor(), BusquedasIdsBlockchain.IdToBlock.USUARIO));
            String dirblock = ethexp.postExp(ex);
            pr = con.prepareStatement("insert into expedientes values (?,?,?,?,?,?,?,?)");
            pr.setString(1, ex.getId_expediente());
            pr.setString(2, dirblock); //SIMULANDO EL ID DE BLOCKCHAIN
            pr.setString(3, ex.getTitulo());
            pr.setTimestamp(4, Timestamp.from(there.toInstant()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            pr.setString(5, ex.getTemas());
            pr.setString(6, ex.getId_usuario());
            pr.setString(7, ex.getId_autor());
            pr.setBoolean(8, true);
            pr.executeUpdate();
        } catch (SQLException | IOException | JSONException | ErrorClass exc) {
            Logger.getLogger(DAOExpediente.class.getName()).log(Level.SEVERE, null, exc);
            throw exc;
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
        return true;
    }

    //Borra un expediente (Funciona)
    public boolean delete(String id_expediente) throws SQLException, ErrorClass {
        PreparedStatement pr = null;
        try {
            String dir = BusquedasIdsBlockchain.getDireccionBlockchain(id_expediente, BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            Eth_Expediente.getEth_Expediente().disableExpediente(dir);
            pr = Conexion.getConnection().prepareStatement("update expedientes set activo = ? where direccion_blockchain_expediente=?");
            pr.setBoolean(1, false);
            String direccion = BusquedasIdsBlockchain.getDireccionBlockchain(id_expediente, BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            pr.setString(2, direccion);
            pr.execute();
        } catch (SQLException ex) {
            Logger.getLogger(DAOExpediente.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (IOException ex) {
            Logger.getLogger(DAOExpediente.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
        return true;
    }

    //Modificar un expediente (Funciona)
    public boolean put(String id_expediente, Expediente pojo) throws SQLException {
        try (PreparedStatement pr = Conexion.getConnection().prepareStatement("update expedientes set "
                + "id_expediente=?,"
                + "titulo=?,"
                + "tema=?,"
                + "activo=? "
                + "where id_expediente=? ")) {
            pr.setString(1, pojo.getId_expediente());
            pr.setString(2, pojo.getTitulo());
            pr.setString(3, pojo.getTemas());
            pr.setString(4, id_expediente);
            pr.setBoolean(5, pojo.isActivo());
            pr.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DAOExpediente.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }

    }

    //Método que retorna 1 solo expediente (Funciona)
    public Expediente getOne(String id_expediente) throws SQLException {
        String sql = "select * from expedientes where id_expediente = ?";
        Expediente expediente = null;

        try (PreparedStatement ps = Conexion.getConnection().prepareStatement(sql)) {
            ps.setString(1, id_expediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    expediente = new Expediente();
                    expediente.setId_expediente(rs.getString(1));
                    expediente.setDireccion_blockchain(rs.getString(2));
                    expediente.setTitulo(rs.getString(3));
                    expediente.setFecha_creacion(rs.getTimestamp(4));
                    expediente.setTemas(rs.getString(5));
                    expediente.setVisor(DAOUsuario.getDaoUsuario().getOne(BusquedasIdsBlockchain.getId(rs.getString(6), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setEmisor(DAOUsuario.getDaoUsuario().getOne(BusquedasIdsBlockchain.getId(rs.getString(7), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setActivo(rs.getBoolean(8));
                }
                return expediente;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAOExpediente.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    private List<Documento> getDocumentos(String query, Expediente expediente) throws SQLException {
        List<Documento> documentos = new ArrayList<>();
        try (PreparedStatement ps = Conexion.getConnection().prepareStatement(query)) {
            String dir = BusquedasIdsBlockchain.getDireccionBlockchain(expediente.getId_expediente(), BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            expediente.setDireccion_blockchain(dir);
            ps.setString(1, dir);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Documento documento = new Documento();
                    documento.setId_documento(rs.getString(1));
                    documento.setNom_documento(rs.getString(2));
                    documento.setFecha_creacion(rs.getDate(3));
                    documento.setFecha_incorporacion(rs.getTimestamp(4));
                    documento.setDescripcion(rs.getString(5));
                    documento.setFormato(rs.getString(6));
                    documento.setTamano(rs.getInt(7));
                    documento.setNo_paginas(rs.getInt(8));
                    documento.setNivel_confidencialidad(rs.getInt(9));
                    documento.setActivo(rs.getBoolean(10));
                    documento.setDireccion_blockchain(rs.getString(11));
                    documentos.add(documento);
                }
            }
        }
        return documentos;
    }

    private String getPerfil(String id_usuario, Expediente expediente) throws SQLException {
        String sql = "select (id_perfil) from usuarios where id_usuario = ?";
        String sqlreturn = "";
        try (PreparedStatement ps = Conexion.getConnection().prepareStatement(sql)) {
            ps.setString(1, id_usuario);
            try (ResultSet rs = ps.executeQuery()) {
                String perfil = "";
                if (rs.next()) {
                    perfil = rs.getString(1);
                }
                switch (perfil.trim()) {
                    case "USUARIO":
                        if (expediente.isActivo()) {
                            sqlreturn += "where documentos.nivel_confidencialidad between 1 and 4";
                        } else {
                            sqlreturn += "where documentos.nivel_confidencialidad = 0";
                        }
                        break;
                    case "AUTOR":
                        sqlreturn += "where documentos.nivel_confidencialidad <=2 or documentos.nivel_confidencialidad=5 and documentos.activo";//1,2,5
                        break;
                    case "ADMINISTRADOR":
                        return "";
                    default:
                        sqlreturn += "where documentos.nivel_confidencialidad<=2 and documentos.activo";
                        break;
                }
            }
        }

        return sqlreturn;
    }

    public Expediente getOne(String id_expediente, String id_usuario) throws ErrorClass, SQLException {

        String sql = "select * from "
                + "expedientes "
                + "where id_expediente = ? order by titulo";

        Expediente expediente = null;
        
        
        try (PreparedStatement ps = Conexion.getConnection().prepareStatement(sql)) {
            ps.setString(1, id_expediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    expediente = new Expediente();
                    expediente.setId_expediente(rs.getString(1));
                    expediente.setTitulo(rs.getString(3));
                    expediente.setFecha_creacion(rs.getTimestamp(4));
                    expediente.setTemas(rs.getString(5));
                    expediente.setVisor(DAOUsuario.getDaoUsuario().getOne(BusquedasIdsBlockchain.getId(rs.getString(6), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setEmisor(DAOUsuario.getDaoUsuario().getOne(BusquedasIdsBlockchain.getId(rs.getString(7), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setActivo(rs.getBoolean(8));

                    sql = "select documentos.id_documento, nom_documento,fecha_creacion, fecha_incorporacion, descripcion, formato, tamano, no_paginas, nivel_confidencialidad,documentos.activo,documentos.direccion_blockchain_documento from\n"
                            + "(select * from exps_docs where direccion_blockchain_expediente=?) as tabla\n"
                            + "inner join\n"
                            + "documentos\n"
                            + "on\n"
                            + "tabla.direccion_blockchain_documento = documentos.direccion_blockchain_documento ";

                    String res = getPerfil(id_usuario, expediente);
                    if (res.isEmpty()) {
                        return expediente;
                    }
                    sql += getPerfil(id_usuario, expediente) + " order by (documentos.activo, nom_documento) desc";
                    expediente.setDocumentos(getDocumentos(sql, expediente));
                    return expediente;
                } else {
                    ErrorClass ex = new ErrorClass();
                    ex.setCode("404");
                    ex.setMessage("No se ha encontrado expediente");
                    throw ex;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAOExpediente.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } 
    }

//Método que devuelve la lista de expedientes de un usuario(visor) determinado. (Funciona)
    public List<Expediente> getAllByUsuario(String id_usuario) throws SQLException {
        String sql = "select * from expedientes where direccion_blockchain_usuario = ? order by (activo,titulo) desc";
        List<Expediente> expedientes = new ArrayList<>();
        try (PreparedStatement ps = Conexion.getConnection().prepareStatement(sql)) {
            ps.setString(1, BusquedasIdsBlockchain.getDireccionBlockchain(id_usuario, BusquedasIdsBlockchain.IdToBlock.USUARIO));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Expediente expediente = new Expediente();
                    expediente.setId_expediente(rs.getString(1));
                    expediente.setDireccion_blockchain(rs.getString(2));
                    expediente.setTitulo(rs.getString(3));
                    expediente.setFecha_creacion(rs.getTimestamp(4));
                    expediente.setTemas(rs.getString(5));
                    expediente.setVisor(DAOUsuario.getDaoUsuario().getOne(BusquedasIdsBlockchain.getId(rs.getString(6), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setEmisor(DAOUsuario.getDaoUsuario().getOne(BusquedasIdsBlockchain.getId(rs.getString(7), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setActivo(rs.getBoolean(8));
                    expedientes.add(expediente);
                }
            }
            return expedientes;
        } catch (SQLException ex) {
            Logger.getLogger(DAOExpediente.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    //Regresa una liste de expedientes filtrados por el autor y usuario (Funciona)
    public Expedientes_Autor getAllByAutor_Usuario(String id_autor, String id_usuario) throws SQLException {
        String sql = "select * from expedientes where direccion_blockchain_usuario=? and direccion_blockchain_autor=? order by titulo";
        Expedientes_Autor autor_expedientes = new Expedientes_Autor();
        List<Expediente> expedientes = autor_expedientes.getExpedientes_emitidos();
        try {
            autor_expedientes.setAutor(DAOUsuario.getDaoUsuario().getOne(id_autor));
            autor_expedientes.setUsuario(DAOUsuario.getDaoUsuario().getOne(id_usuario));
            try (PreparedStatement ps = Conexion.getConnection().prepareStatement(sql)) {
                ps.setString(2, BusquedasIdsBlockchain.getDireccionBlockchain(id_autor, BusquedasIdsBlockchain.IdToBlock.USUARIO));
                ps.setString(1, BusquedasIdsBlockchain.getDireccionBlockchain(id_usuario, BusquedasIdsBlockchain.IdToBlock.USUARIO));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Expediente expediente = new Expediente();
                        expediente.setId_expediente(rs.getString(1));
                        expediente.setDireccion_blockchain(rs.getString(2));
                        expediente.setTitulo(rs.getString(3));
                        expediente.setFecha_creacion(rs.getTimestamp(4));
                        expediente.setTemas(rs.getString(5));
                        expediente.setActivo(rs.getBoolean(8));
                        expedientes.add(expediente);
                    }
                }
            }
            return autor_expedientes;
        } catch (SQLException ex) {
            Logger.getLogger(DAOExpediente.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    //Regresa lista de expedientes de un autor
    public List<Expediente> getAllByAutor(String id_autor, List<Filtro> filtros) throws SQLException {
        StringBuilder sql = new StringBuilder("select * from expedientes inner join usuarios "
                + "on expedientes.direccion_blockchain_usuario = usuarios.direccion_blockchain_usuario "
                + "where direccion_blockchain_autor=?");
        PreparedStatement ps = null;
        int i = 2;
        try {
            if (!filtros.isEmpty()) {
                sql.append(" and ");
                for (Filtro f : filtros) {
                    sql.append(f.toString());
                    sql.append("and ");
                }
                sql.delete(sql.length() - 4, sql.length() - 1);
                ps = Conexion.getConnection().prepareStatement(sql.toString());
                for (Filtro f : filtros) {
                    if (f.getCriterio() == Filtro.Criterio.LIKE) {
                        ps.setObject(i, "%" + f.getValue() + "%");
                    } else {
                        ps.setObject(i, f.getValue());
                    }
                    i++;
                }
            } else {
                sql.append(" order by titulo");
                ps = Conexion.getConnection().prepareStatement(sql.toString());
            }
            List<Expediente> expedientes = new ArrayList<>();
            ps.setString(1, BusquedasIdsBlockchain.getDireccionBlockchain(id_autor, BusquedasIdsBlockchain.IdToBlock.USUARIO));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Expediente expediente = new Expediente();
                    expediente.setId_expediente(rs.getString(1));
                    expediente.setDireccion_blockchain(rs.getString(2));
                    expediente.setTitulo(rs.getString(3));
                    expediente.setFecha_creacion(rs.getTimestamp(4));
                    expediente.setTemas(rs.getString(5));
                    expediente.setActivo(rs.getBoolean(8));
                    Usuario usuario = new Usuario();
                    usuario.setId_usuario(rs.getString(9));
                    usuario.setNombre(rs.getString(11));
                    usuario.setApellido_paterno(rs.getString(12));
                    usuario.setApellido_materno(rs.getString(13));
                    usuario.setEmail(rs.getString(18));
                    expediente.setVisor(usuario);
                    expedientes.add(expediente);
                }
            }
            return expedientes;

        } catch (SQLException ex) {
            Logger.getLogger(DAOExpediente.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void upExp(String id_expediente) throws SQLException, ErrorClass {
        PreparedStatement pr = null;
        try {
            String dir = BusquedasIdsBlockchain.getDireccionBlockchain(id_expediente, BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            Eth_Expediente.getEth_Expediente().enableExpediente(dir);
            pr = Conexion.getConnection().prepareStatement("update expedientes set activo = ? where direccion_blockchain_expediente=?");
            pr.setBoolean(1, true);
            String direccion = BusquedasIdsBlockchain.getDireccionBlockchain(id_expediente, BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            pr.setString(2, direccion);
            pr.execute();

        } catch (SQLException ex) {
            Logger.getLogger(DAOExpediente.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw ex;

        } catch (IOException ex) {
            Logger.getLogger(DAOExpediente.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
    }
}
