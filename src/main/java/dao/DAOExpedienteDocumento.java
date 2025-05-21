/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import client.Eth_Expediente;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import pojo.ErrorClass;

/**
 *
 * @author aldair
 */
public class DAOExpedienteDocumento implements Serializable {

    private static DAOExpedienteDocumento daoexpedientedocumento = null;

    private DAOExpedienteDocumento() {

    }

    public static DAOExpedienteDocumento getDaoExpedienteDocumento() {
        if (daoexpedientedocumento == null) {
            daoexpedientedocumento = new DAOExpedienteDocumento();
        }
        return daoexpedientedocumento;
    }

    public void deleteOnError(String direccion_documento) throws SQLException{
        String sql = "delete from exps_docs where direccion_blockchain_documento = ?";
        try(PreparedStatement pr = Conexion.getConnection().prepareStatement(sql)){
            pr.setString(1, direccion_documento);
            pr.execute();
        }
    }
    //Agregar un documento existente a otro expediente
    public boolean agregarDocumento(String id_expediente, String id_documento) throws SQLException, ErrorClass, IOException, JSONException {

        Connection c = Conexion.getConnection();
        try {
            String sql1 = "select nivel_confidencialidad from documentos where id_documento = ?";
            try (PreparedStatement pr2 = Conexion.getConnection().prepareStatement(sql1)) {
                pr2.setString(1, id_documento);
                try (ResultSet rs = pr2.executeQuery()) {
                    int nivel_conf = 0;
                    if (rs.next()) {
                        nivel_conf = rs.getInt(1);
                    } else {
                        ErrorClass error = new ErrorClass();
                        error.setCode("1000");
                        error.setMessage("No se ha encontrado el documento con el id " + id_documento);
                        throw error;
                    }

                    if (nivel_conf == 3 || nivel_conf == 4) {
                        ErrorClass error = new ErrorClass();
                        error.setCode("2000");
                        error.setMessage("No se puede realizar esta acción, ya que no tiene el nivel de confidencialidad apto");
                        throw error;
                    }

                    String sql = "insert into exps_docs values(?,?)";

                    try (PreparedStatement pr3 = Conexion.getConnection().prepareStatement(sql)) {
                        String direccionblockexp = BusquedasIdsBlockchain.getDireccionBlockchain(id_expediente, BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
                        String direccionblockdoc = BusquedasIdsBlockchain.getDireccionBlockchain(id_documento, BusquedasIdsBlockchain.IdToBlock.DOCUMENTO);
                        Eth_Expediente eth = Eth_Expediente.getEth_Expediente();
                        eth.addDoc(direccionblockexp, direccionblockdoc);
                        pr3.setString(1, direccionblockexp);
                        pr3.setString(2, direccionblockdoc);
                        pr3.execute();
                        return true;
                    } catch (SQLException ex) {
                        Logger.getLogger(DAOExpedienteDocumento.class.getName()).log(Level.SEVERE, null, ex);
                        throw ex;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAOExpedienteDocumento.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    //Borra un documento de algún expediente
    public boolean quitarDocumento(String id_expediente, String id_documento) throws SQLException {
        String sql = "delete from exps_docs where direccion_blockchain_expediente =? and direccion_blockchain_documento = ? ";
        try (PreparedStatement pr = Conexion.getConnection().prepareStatement(sql)) {
            pr.setString(1, BusquedasIdsBlockchain.getDireccionBlockchain(id_expediente, BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE));
            pr.setString(2, BusquedasIdsBlockchain.getDireccionBlockchain(id_documento, BusquedasIdsBlockchain.IdToBlock.DOCUMENTO));
            pr.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DAOExpedienteDocumento.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }
}
