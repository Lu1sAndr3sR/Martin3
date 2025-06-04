/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import client.EthExpediente;
import java.io.IOException;
import pojo.Expediente;
import pojo.ExpedientesAutor;
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

    // Crea un nuevo expediente (Funciona)
    public boolean post(Expediente ex) throws SQLException, IOException, JSONException, ErrorClass {

        Connection con = Conexion.getConnection();
        PreparedStatement pr = null;
        try {
            Instant instan = Instant.now();
            ZonedDateTime there = ZonedDateTime.ofInstant(instan, ZoneId.of("UTC"));
            EthExpediente ethexp = EthExpediente.getEthExpediente();
            Timestamp fecha = Timestamp.from(instan);
            ex.setFechaCreacion(fecha);
            ex.setIdExpediente("exp" + UUID.randomUUID().toString().substring(0, 12));
            ex.setIdUsuario(BusquedasIdsBlockchain.getDireccionBlockchain(ex.getIdUsuario(),
                    BusquedasIdsBlockchain.IdToBlock.USUARIO));
            ex.setIdAutor(BusquedasIdsBlockchain.getDireccionBlockchain(ex.getIdAutor(),
                    BusquedasIdsBlockchain.IdToBlock.USUARIO));
            String dirblock = ethexp.postExp(ex);
            pr = con.prepareStatement("insert into expedientes values (?,?,?,?,?,?,?,?)");
            pr.setString(1, ex.getIdExpediente());
            pr.setString(2, dirblock); // SIMULANDO EL ID DE BLOCKCHAIN
            pr.setString(3, ex.getTitulo());
            pr.setTimestamp(4, Timestamp.from(there.toInstant()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            pr.setString(5, ex.getTemas());
            pr.setString(6, ex.getIdUsuario());
            pr.setString(7, ex.getIdAutor());
            pr.setBoolean(8, true);
            pr.executeUpdate();
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
        return true;
    }

    // Borra un expediente (Funciona)
    public boolean delete(String idExpediente) throws SQLException, ErrorClass {
        PreparedStatement pr = null;
        try {
            String dir = BusquedasIdsBlockchain.getDireccionBlockchain(idExpediente,
                    BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            EthExpediente.getEthExpediente().disableExpediente(dir);
            pr = Conexion.getConnection()
                    .prepareStatement("update expedientes set activo = ? where direccion_blockchain_expediente=?");
            pr.setBoolean(1, false);
            String direccion = BusquedasIdsBlockchain.getDireccionBlockchain(idExpediente,
                    BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            pr.setString(2, direccion);
            pr.execute();
        } catch (IOException ex) {
            Logger.getLogger(DAOExpediente.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
        return true;
    }

    // Modificar un expediente (Funciona)
    public boolean put(String idExpediente, Expediente pojo) throws SQLException {
        try (PreparedStatement pr = Conexion.getConnection().prepareStatement("update expedientes set "
                + "id_expediente=?,"
                + "titulo=?,"
                + "tema=?,"
                + "activo=? "
                + "where id_expediente=? ")) {
            pr.setString(1, pojo.getIdExpediente());
            pr.setString(2, pojo.getTitulo());
            pr.setString(3, pojo.getTemas());
            pr.setBoolean(4, pojo.isActivo());
            pr.setString(5, idExpediente);
            pr.executeUpdate();
            return true;
        }
    }

    // Método que retorna 1 solo expediente (Funciona)
    public Expediente getOne(String idExpediente) throws SQLException {
        String sql = "select id_expediente, direccion_blockchain, titulo, fecha_creacion, temas, visor, emisor, activo from expedientes where id_expediente = ?";
        Expediente expediente = null;

        try (PreparedStatement ps = Conexion.getConnection().prepareStatement(sql)) {
            ps.setString(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    expediente = new Expediente();
                    expediente.setIdExpediente(rs.getString(1));
                    expediente.setDireccionBlockchain(rs.getString(2));
                    expediente.setTitulo(rs.getString(3));
                    expediente.setFechaCreacion(rs.getTimestamp(4));
                    expediente.setTemas(rs.getString(5));
                    expediente.setVisor(DAOUsuario.getDaoUsuario().getOne(
                            BusquedasIdsBlockchain.getId(rs.getString(6), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setEmisor(DAOUsuario.getDaoUsuario().getOne(
                            BusquedasIdsBlockchain.getId(rs.getString(7), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setActivo(rs.getBoolean(8));
                }
                return expediente;
            }
        }
    }

    private List<Documento> getDocumentos(String query, Expediente expediente) throws SQLException {
        List<Documento> documentos = new ArrayList<>();
        try (PreparedStatement ps = Conexion.getConnection().prepareStatement(query)) {
            String dir = BusquedasIdsBlockchain.getDireccionBlockchain(expediente.getIdExpediente(),
                    BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            expediente.setDireccionBlockchain(dir);
            ps.setString(1, dir);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Documento documento = new Documento();
                    documento.setIdDocumento(rs.getString(1));
                    documento.setNombreDocumento(rs.getString(2));
                    documento.setFechaCreacion(rs.getDate(3));
                    documento.setFechaIncorporacion(rs.getTimestamp(4));
                    documento.setDescripcion(rs.getString(5));
                    documento.setFormato(rs.getString(6));
                    documento.setTamano(rs.getInt(7));
                    documento.setNumeroPaginas(rs.getInt(8));
                    documento.setNivelConfidencialidad(rs.getInt(9));
                    documento.setActivo(rs.getBoolean(10));
                    documento.setDireccionBlockchain(rs.getString(11));
                    documentos.add(documento);
                }
            }
        }
        return documentos;
    }

    private String getPerfil(String idUsuario, Expediente expediente) throws SQLException {
        String sql = "select (id_perfil) from usuarios where id_usuario = ?";
        String sqlreturn = "";
        try (PreparedStatement ps = Conexion.getConnection().prepareStatement(sql)) {
            ps.setString(1, idUsuario);
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
                        sqlreturn += "where documentos.nivel_confidencialidad <=2 or documentos.nivel_confidencialidad=5 and documentos.activo";// 1,2,5
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

    public Expediente getOne(String idExpediente, String idUsuario) throws ErrorClass, SQLException {

        Expediente expediente = null;

        try (PreparedStatement ps = Conexion.getConnection().prepareStatement("select id_expediente, titulo, fecha_creacion, temas, visor, emisor, activo from expedientes where id_expediente = ? order by titulo")) {
            ps.setString(1, idExpediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    expediente = new Expediente();
                    expediente.setIdExpediente(rs.getString(1));
                    expediente.setTitulo(rs.getString(3));
                    expediente.setFechaCreacion(rs.getTimestamp(4));
                    expediente.setTemas(rs.getString(5));
                    expediente.setVisor(DAOUsuario.getDaoUsuario().getOne(
                            BusquedasIdsBlockchain.getId(rs.getString(6), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setEmisor(DAOUsuario.getDaoUsuario().getOne(
                            BusquedasIdsBlockchain.getId(rs.getString(7), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setActivo(rs.getBoolean(8));

                    String sql = "select documentos.id_documento, nom_documento,fecha_creacion, fecha_incorporacion, descripcion, formato, tamano, no_paginas, nivel_confidencialidad,documentos.activo,documentos.direccion_blockchain_documento from\n"
                            + "(select * from exps_docs where direccion_blockchain_expediente=?) as tabla\n"
                            + "inner join\n"
                            + "documentos\n"
                            + "on\n"
                            + "tabla.direccion_blockchain_documento = documentos.direccion_blockchain_documento ";

                    String res = getPerfil(idUsuario, expediente);
                    if (res.isEmpty()) {
                        return expediente;
                    }
                    sql += getPerfil(idUsuario, expediente) + " order by (documentos.activo, nom_documento) desc";
                    expediente.setDocumentos(getDocumentos(sql, expediente));
                    return expediente;
                } else {
                    throw new ErrorClass("No se ha encontrado expediente", null, "404");
                }
            }
        }
    }

    // Método que devuelve la lista de expedientes de un usuario(visor) determinado.
    // (Funciona)
    public List<Expediente> getAllByUsuario(String idUsuario) throws SQLException {
        String sql = "select id_expediente, direccion_blockchain, titulo, fecha_creacion, temas, visor, emisor, activo from expedientes where direccion_blockchain_usuario = ? order by (activo,titulo) desc";
        List<Expediente> expedientes = new ArrayList<>();
        try (PreparedStatement ps = Conexion.getConnection().prepareStatement(sql)) {
            ps.setString(1,
                    BusquedasIdsBlockchain.getDireccionBlockchain(idUsuario, BusquedasIdsBlockchain.IdToBlock.USUARIO));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Expediente expediente = new Expediente();
                    expediente.setIdExpediente(rs.getString(1));
                    expediente.setDireccionBlockchain(rs.getString(2));
                    expediente.setTitulo(rs.getString(3));
                    expediente.setFechaCreacion(rs.getTimestamp(4));
                    expediente.setTemas(rs.getString(5));
                    expediente.setVisor(DAOUsuario.getDaoUsuario().getOne(
                            BusquedasIdsBlockchain.getId(rs.getString(6), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setEmisor(DAOUsuario.getDaoUsuario().getOne(
                            BusquedasIdsBlockchain.getId(rs.getString(7), BusquedasIdsBlockchain.BlockToId.USUARIO)));
                    expediente.setActivo(rs.getBoolean(8));
                    expedientes.add(expediente);
                }
            }
            return expedientes;
        }
    }

    // Regresa una liste de expedientes filtrados por el autor y usuario (Funciona)
    public ExpedientesAutor getAllByAutorUsuario(String idAutor, String idUsuario) throws SQLException {
        String sql = "select id_expediente, direccion_blockchain, titulo, fecha_creacion, temas, activo from expedientes where direccion_blockchain_usuario=? and direccion_blockchain_autor=? order by titulo";
        ExpedientesAutor autorExpedientes = new ExpedientesAutor();
        List<Expediente> expedientes = autorExpedientes.getExpedientesEmitidos();
        autorExpedientes.setAutor(DAOUsuario.getDaoUsuario().getOne(idAutor));
        autorExpedientes.setUsuario(DAOUsuario.getDaoUsuario().getOne(idUsuario));
        try (PreparedStatement ps = Conexion.getConnection().prepareStatement(sql)) {
            ps.setString(2, BusquedasIdsBlockchain.getDireccionBlockchain(idAutor,
                    BusquedasIdsBlockchain.IdToBlock.USUARIO));
            ps.setString(1, BusquedasIdsBlockchain.getDireccionBlockchain(idUsuario,
                    BusquedasIdsBlockchain.IdToBlock.USUARIO));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Expediente expediente = new Expediente();
                    expediente.setIdExpediente(rs.getString(1));
                    expediente.setDireccionBlockchain(rs.getString(2));
                    expediente.setTitulo(rs.getString(3));
                    expediente.setFechaCreacion(rs.getTimestamp(4));
                    expediente.setTemas(rs.getString(5));
                    expediente.setActivo(rs.getBoolean(8));
                    expedientes.add(expediente);
                }
            }
        }
        return autorExpedientes;

    }

    // Regresa lista de expedientes de un autor
    public List<Expediente> getAllByAutor(String idAutor, List<Filtro> filtros) throws SQLException {
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
            ps.setString(1,
                    BusquedasIdsBlockchain.getDireccionBlockchain(idAutor, BusquedasIdsBlockchain.IdToBlock.USUARIO));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Expediente expediente = new Expediente();
                    expediente.setIdExpediente(rs.getString(1));
                    expediente.setDireccionBlockchain(rs.getString(2));
                    expediente.setTitulo(rs.getString(3));
                    expediente.setFechaCreacion(rs.getTimestamp(4));
                    expediente.setTemas(rs.getString(5));
                    expediente.setActivo(rs.getBoolean(8));
                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(rs.getString(9));
                    usuario.setNombre(rs.getString(11));
                    usuario.setApellidoPaterno(rs.getString(12));
                    usuario.setApellidoMaterno(rs.getString(13));
                    usuario.setEmail(rs.getString(18));
                    expediente.setVisor(usuario);
                    expedientes.add(expediente);
                }
            }
            return expedientes;

        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public void upExp(String idExpediente) throws SQLException, ErrorClass {
        PreparedStatement pr = null;
        try {
            String dir = BusquedasIdsBlockchain.getDireccionBlockchain(idExpediente,
                    BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            EthExpediente.getEthExpediente().enableExpediente(dir);
            pr = Conexion.getConnection()
                    .prepareStatement("update expedientes set activo = ? where direccion_blockchain_expediente=?");
            pr.setBoolean(1, true);
            String direccion = BusquedasIdsBlockchain.getDireccionBlockchain(idExpediente,
                    BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            pr.setString(2, direccion);
            pr.execute();

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
