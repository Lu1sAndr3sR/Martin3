/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import client.EthDocumento;
import client.Eth_Expediente;
import pojo.Documento;
import java.io.IOException;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jettison.json.JSONException;
import pojo.ErrorClass;

/**
 *
 * @author aldair
 */
public class DAODocumento implements Serializable {

    private static DAODocumento daodocumento = null;

    private DAODocumento() {
        
    }

    public static DAODocumento getDAODocumento() {
        if (daodocumento == null) {
            daodocumento = new DAODocumento();
        }
        return daodocumento;
    }

    public void deleteOnError(String direccion) throws SQLException{
        deleteCriptoParams(direccion);
        String sql = "delete from documentos where direccion_blockchain_documento = ?";
        try(PreparedStatement pr = Conexion.getConnection().prepareStatement(sql)){
            pr.setString(1, direccion);
            pr.execute();
        }
    }
    
    public void deleteCriptoParams(String direccion) throws SQLException{
        String sql = "delete from criptoparametros where direccion_blockchain_documento = ?";
        try(PreparedStatement pr = Conexion.getConnection().prepareStatement(sql)){
            pr.setString(1, direccion);
            pr.execute();
        }
    }
    //Crea un nuevo documento (Funciona)
    public boolean post(Documento pojo, String id_expediente) throws SQLException, IOException, JSONException, ErrorClass {
        Connection c = Conexion.getConnection();
        Instant instan = Instant.now();
        ZonedDateTime there = ZonedDateTime.ofInstant(instan, ZoneId.of("UTC"));
        Timestamp date = Timestamp.from(instan);
        CallableStatement callst = null;
        try {
            String sql = "{call insert_new_documentos(?,?,?,?,?,?,?,?,?,?,?)}";
            callst = c.prepareCall(sql);
            String id = "doc" + UUID.randomUUID().toString().substring(0, 6);
            byte[] backToBytes = Base64.decodeBase64(pojo.getArchivob64());
            pojo.setFecha_incorporacion(date);
            pojo.setId_documento(id);
            pojo.setTamano(backToBytes.length);
            String dirblock = EthDocumento.getEthDocumento().post(pojo);
            callst.setString(1, pojo.getId_documento());
            callst.setString(2, dirblock);
            callst.setString(3, pojo.getNom_documento());
            callst.setTimestamp(4, Timestamp.from(pojo.getFecha_creacion().toInstant()));
            callst.setTimestamp(5, Timestamp.from(there.toInstant()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            callst.setString(6, pojo.getDescripcion());
            callst.setString(7, pojo.getFormato().toUpperCase());
            callst.setInt(8, pojo.getTamano());
            callst.setInt(9, pojo.getNo_paginas());
            callst.setInt(10, pojo.getNivel_confidencialidad());
            callst.setString(11, id_expediente);
            String direxp = BusquedasIdsBlockchain.getDireccionBlockchain(id_expediente, BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            Eth_Expediente eth = Eth_Expediente.getEth_Expediente();
            eth.addDoc(direxp, dirblock);
            callst.executeUpdate();
            pojo.setDireccion_blockchain(dirblock);
            EthDocumento ethDoc = EthDocumento.getEthDocumento();
            ethDoc.uploadDoc(backToBytes, dirblock);
            return true;
        } catch (SQLException | ErrorClass | IOException ex) {
            throw ex;
        } finally {
            if (callst != null) {
                callst.close();
            }
        }
    }

    //Borra un documento por su id. (Funciona)
    public boolean delete(String id_documento) throws SQLException, ErrorClass, IOException {
        String sql = "update documentos set activo=false where direccion_blockchain_documento = ?";
        Connection c = Conexion.getConnection();
        PreparedStatement ps2 = null;
        try {
            String direccion = BusquedasIdsBlockchain.getDireccionBlockchain(id_documento, BusquedasIdsBlockchain.IdToBlock.DOCUMENTO);
            EthDocumento.getEthDocumento().disableDoc(direccion);
            ps2 = c.prepareStatement(sql);
            ps2.setString(1, BusquedasIdsBlockchain.getDireccionBlockchain(id_documento, BusquedasIdsBlockchain.IdToBlock.DOCUMENTO));
            ps2.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DAODocumento.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (IOException ex) {
            Logger.getLogger(DAODocumento.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            if (ps2 != null) {
                ps2.close();
            }
        }
        return true;
    }

    //Modifica un documento (Funciona)
    public boolean put(String id_documento, Documento pojo) throws SQLException {
        String sql = "update documentos set "
                + "id_documento = ?, "
                + "nom_documento = ?, "
                + "fecha_creacion = ?, "
                + "descripcion=?,"
                + "formato = ?,"
                + "tamano =?,"
                + "no_paginas=?,"
                + "nivel_confidencialidad=?,"
                + "activo=? "
                + "where id_documento = ?";

        Connection c = Conexion.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id_documento);
            ps.setString(2, pojo.getNom_documento());
            ps.setTimestamp(3, Timestamp.from(pojo.getFecha_creacion().toInstant()));
            ps.setString(4, pojo.getDescripcion());
            ps.setString(5, pojo.getFormato().toUpperCase());
            ps.setInt(6, pojo.getTamano());
            ps.setInt(7, pojo.getNo_paginas());
            ps.setInt(8, pojo.getNivel_confidencialidad());
            ps.setString(10, id_documento);
            ps.setBoolean(9, pojo.isActivo());
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            throw ex;
        }
    }

    public Documento getOneEth(String id_documento) throws IOException, ErrorClass, SQLException {
        EthDocumento eth = EthDocumento.getEthDocumento();
        String dir = BusquedasIdsBlockchain.getDireccionBlockchain(id_documento, BusquedasIdsBlockchain.IdToBlock.DOCUMENTO);
        if (dir.isEmpty()) {
            dir = "notfound";
        }
        Documento doc = eth.get(dir);
        doc.setDireccion_blockchain(dir);
        return doc;
    }

    //Retorna un documento ubicandolo por su id
    public Documento getOne(String id_documento) throws SQLException {
        Documento documento = null;
        String sql = "select * from documentos where id_documento=?";
        PreparedStatement ps = null;
        try {
            ps = Conexion.getConnection().prepareStatement(sql);
            ps.setString(1, id_documento);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    documento = new Documento();
                    documento.setId_documento(rs.getString(1));
                    documento.setNom_documento(rs.getString(3));
                    documento.setFecha_creacion(rs.getDate(4));
                    documento.setFecha_incorporacion(rs.getTimestamp(5));
                    documento.setDescripcion(rs.getString(6));
                    documento.setFormato(rs.getString(7));
                    documento.setTamano(rs.getInt(8));
                    documento.setNo_paginas(rs.getInt(9));
                    documento.setNivel_confidencialidad(rs.getInt(10));
                    documento.setActivo(rs.getBoolean(11));
                }
                return documento;
            } catch (SQLException ex) {
                Logger.getLogger(DAODocumento.class.getName()).log(Level.SEVERE, null, ex);
                throw ex;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAODocumento.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    //Retorna la lista de documentos que pertencen a un expediente
    public List<Documento> getDocumentosByExpediente(String id_expediente, String id_usuario) throws SQLException {
        String sql;
        List<Documento> documentos = new ArrayList<>();
        sql = "select (id_perfil) from usuarios where id_usuario = ?";
        try (PreparedStatement praux = Conexion.getConnection().prepareStatement(sql)) {
            praux.setString(1, id_usuario);
            try (ResultSet rs = praux.executeQuery()) {
                String perfil = "";
                if (rs.next()) {
                    perfil = rs.getString(1);
                }

                sql = "select documentos.id_documento, nom_documento,fecha_creacion, fecha_incorporacion, descripcion, formato, tamano, no_paginas, nivel_confidencialidad,documentos.direccion_blockchain_documento from\n"
                        + "(select * from exps_docs where direccion_blockchain_expediente = ?) as tabla\n"
                        + "inner join\n"
                        + "documentos\n"
                        + "on\n"
                        + "tabla.direccion_blockchain_documento = documentos.direccion_blockchain_documento ";
                switch (perfil.trim()) {
                    case "USUARIO":
                        sql += "where documentos.nivel_confidencialidad between 1 and 4";
                        break;
                    case "AUTOR":
                        break;
                    case "ADMINISTRADOR":
                        return documentos;
                    default:
                        sql += "where documentos.nivel_confidencialidad between 1 and 2";
                        break;
                }

                try (PreparedStatement ps2 = Conexion.getConnection().prepareStatement(sql)) {
                    ps2.setString(1, BusquedasIdsBlockchain.getDireccionBlockchain(id_expediente, BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE));
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            Documento documento = new Documento();
                            documento.setId_documento(rs2.getString(1));
                            documento.setNom_documento(rs2.getString(2));
                            documento.setFecha_creacion(rs2.getDate(3));
                            documento.setFecha_incorporacion(rs2.getDate(4));
                            documento.setDescripcion(rs2.getString(5));
                            documento.setFormato(rs2.getString(6));
                            documento.setTamano(rs2.getInt(7));
                            documento.setNo_paginas(rs2.getInt(8));
                            documento.setNivel_confidencialidad(rs2.getInt(9));
                            documento.setDireccion_blockchain(rs2.getString(10));
                            documentos.add(documento);
                        }
                    }
                }

            }

        } catch (SQLException ex) {
            Logger.getLogger(DAODocumento.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        return documentos;
    }

    public void upDoc(String id_documento) throws SQLException, ErrorClass {
        String sql = "update documentos set activo=true where direccion_blockchain_documento = ?";
        Connection c = Conexion.getConnection();
        try (PreparedStatement ps2 = c.prepareStatement(sql)) {
            String dir = BusquedasIdsBlockchain.getDireccionBlockchain(id_documento, BusquedasIdsBlockchain.IdToBlock.DOCUMENTO);
            EthDocumento.getEthDocumento().enableDoc(dir);
            ps2.setString(1, BusquedasIdsBlockchain.getDireccionBlockchain(id_documento, BusquedasIdsBlockchain.IdToBlock.DOCUMENTO));
            ps2.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DAODocumento.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (IOException ex) {
            Logger.getLogger(DAODocumento.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
