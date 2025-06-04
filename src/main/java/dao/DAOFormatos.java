/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import pojo.Formato;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aldair
 */
public class DAOFormatos implements DAOGeneral<Formato, String> {

    private static DAOFormatos daoformato = null;

    private DAOFormatos() {

    }

    public static DAOFormatos getDAOFormato() {
        if (daoformato == null) {
            daoformato = new DAOFormatos();
        }
        return daoformato;
    }

    @Override
    public boolean post(Formato pojo) throws SQLException {
        String sql = "insert into formatos(formato) values(?)";
        Connection c = Conexion.getConnection();
        try (PreparedStatement pr = c.prepareStatement(sql)) {
            pr.setString(1, pojo.getFormato().toUpperCase());
            pr.executeUpdate();
            return true;
        } 
    }

    @Override
    public boolean delete(String clave) throws SQLException {
        Connection c = Conexion.getConnection();
        PreparedStatement pr = null;
        try {
            String sql = "delete from formatos where formato=?";
            pr = c.prepareStatement(sql);
            pr.setString(1, clave);
            pr.executeUpdate();

            return true;
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
    }

    @Override
    public boolean put(String clave, Formato pojo) throws SQLException {
        String sql = "update formatos set formato = ? where formato = ?";
        PreparedStatement pr = null;
        Connection c = Conexion.getConnection();
        try {
            pr = c.prepareStatement(sql);
            pr.setString(1, pojo.getFormato().toUpperCase());
            pr.setString(2, clave);
            pr.executeUpdate();

            return true;
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
    }

    @Override
    public Formato getOne(String clave) throws SQLException {
        String sql = "select nombre_formato from formatos where formato = ?";
        PreparedStatement pr = null;
        Formato formato = null;
        try {
            pr = Conexion.getConnection().prepareStatement(sql);
            pr.setString(1, clave);
            try (ResultSet rs = pr.executeQuery()) {
                if (rs.next()) {
                    formato = new Formato();
                    formato.setFormato(rs.getString(1));
                    return formato;
                }
            }
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
        return formato;
    }

    @Override
    public List<Formato> getAll() throws SQLException {
        String sql = "select nombre_formato from formatos";
        PreparedStatement pr = null;
        List<Formato> formatos = new ArrayList<>();

        try {
            pr = Conexion.getConnection().prepareStatement(sql);
            try (ResultSet rs = pr.executeQuery()) {
                while (rs.next()) {
                    Formato formato = new Formato();
                    formato.setFormato(rs.getString(1));
                    formatos.add(formato);
                }
                return formatos;
            }
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
    }

}
