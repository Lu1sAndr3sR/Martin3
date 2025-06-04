/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aldair
 */
public class BusquedasIdsBlockchain implements Serializable {

    public enum IdToBlock {
        USUARIO("select direccionBlockchain_usuario from usuarios where id_usuario=?"),
        EXPEDIENTE("select direccionBlockchain_expediente from expedientes where id_expediente=?"),
        DOCUMENTO("select direccionBlockchain_documento from documentos where id_documento=?");
        private final String id;

        IdToBlock(String ad) {
            this.id = ad;
        }

        public String getQuery() {
            return id;
        }
    }

    public enum BlockToId {
        USUARIO("select id_usuario from usuarios where direccionBlockchain_usuario=?"),
        EXPEDIENTE("select id_expediente from expedientes where direccionBlockchain_expediente=?"),
        DOCUMENTO("select id_documento from documentos where direccionBlockchain_documento=?");
        private final String id;

        BlockToId(String ad) {
            this.id = ad;
        }

        public String getQuery() {
            return id;
        }
    }

    public static String getDireccionBlockchain(String id, IdToBlock tabla) throws SQLException {
        String sql = tabla.getQuery();
        PreparedStatement ps = null;
        try {
            ps = Conexion.getConnection().prepareStatement(sql);
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAOExpediente.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
        return "";
    }

    public static String getId(String direccionBlockchain, BlockToId tabla) throws SQLException {
        String sql = tabla.getQuery();
        PreparedStatement ps = null;
        try {
            ps = Conexion.getConnection().prepareStatement(sql);
            ps.setString(1, direccionBlockchain);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return  rs.getString(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAOExpediente.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
        return "";
    }

}
