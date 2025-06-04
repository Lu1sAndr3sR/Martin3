/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import client.EthExpediente;
import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public void deleteOnError(String direccionDocumento) throws SQLException {
        String sql = "delete from exps_docs where direccion_blockchain_documento = ?";
        try (PreparedStatement pr = Conexion.getConnection().prepareStatement(sql)) {
            pr.setString(1, direccionDocumento);
            pr.execute();
        }
    }

    // Agregar un documento existente a otro expediente
    public boolean agregarDocumento(String idExpediente, String idDocumento)
            throws SQLException, JSONException, ErrorClass, IOException {

        String sql1 = "select nivel_confidencialidad from documentos where id_documento = ?";
        try (PreparedStatement pr2 = Conexion.getConnection().prepareStatement(sql1)) {
            pr2.setString(1, idDocumento);
            return obtenerNivelConfidencialidad(pr2, idExpediente, idDocumento);
        }

    }

    private boolean anadirDatosEth(String idExpediente, String idDocumento)
            throws SQLException, IOException, JSONException, ErrorClass {
        String sql = "insert into exps_docs values(?,?)";
        try (PreparedStatement pr3 = Conexion.getConnection().prepareStatement(sql)) {
            String direccionblockexp = BusquedasIdsBlockchain.getDireccionBlockchain(idExpediente,
                    BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE);
            String direccionblockdoc = BusquedasIdsBlockchain.getDireccionBlockchain(idDocumento,
                    BusquedasIdsBlockchain.IdToBlock.DOCUMENTO);
            EthExpediente eth = EthExpediente.getEthExpediente();
            eth.addDoc(direccionblockexp, direccionblockdoc);
            pr3.setString(1, direccionblockexp);
            pr3.setString(2, direccionblockdoc);
            pr3.execute();
            return true;
        }
    }

    private boolean obtenerNivelConfidencialidad(PreparedStatement pr2, String idExpediente, String idDocumento) throws SQLException, ErrorClass, IOException, JSONException{
        try (ResultSet rs = pr2.executeQuery()) {
            int nivelConf = 0;
            if (rs.next()) {
                nivelConf = rs.getInt(1);
            } else {
                throw new ErrorClass("No se ha encontrado el documento con el id " + idDocumento, null,
                        "1000");
            }

            if (nivelConf == 3 || nivelConf == 4) {
                throw new ErrorClass(
                        "No se puede realizar esta acción, ya que no tiene el nivel de confidencialidad apto", null,
                        "2000");
            }

            return anadirDatosEth(idExpediente, idDocumento);
        }
    }

    // Borra un documento de algún expediente
    public boolean quitarDocumento(String idExpediente, String idDocumento) throws SQLException {
        String sql = "delete from exps_docs where direccion_blockchain_expediente =? and direccion_blockchain_documento = ? ";
        try (PreparedStatement pr = Conexion.getConnection().prepareStatement(sql)) {
            pr.setString(1, BusquedasIdsBlockchain.getDireccionBlockchain(idExpediente,
                    BusquedasIdsBlockchain.IdToBlock.EXPEDIENTE));
            pr.setString(2, BusquedasIdsBlockchain.getDireccionBlockchain(idDocumento,
                    BusquedasIdsBlockchain.IdToBlock.DOCUMENTO));
            pr.execute();
            return true;
        }
    }
}
