/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import pojo.Tema;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aldair
 */
public class DAOTema implements DAOGeneral<Tema, String>, Serializable {

    private static DAOTema daotema = null;

    private DAOTema() {

    }

    public static DAOTema getDaoTema() {
        if (daotema == null) {
            daotema = new DAOTema();
        }
        return daotema;
    }

    @Override
    public boolean post(Tema pojo) throws SQLException {
        String sql = "insert into temas values(?)";
        Connection c = Conexion.getConnection();
        try (PreparedStatement pr = c.prepareStatement(sql)) {
            pr.setString(1, pojo.getTema().toUpperCase());
            pr.executeUpdate();
            return true;
        } 
    }

    @Override
    public boolean delete(String clave) throws SQLException {
        Connection c = Conexion.getConnection();
        PreparedStatement pr = null;
        try {
            String sql = "delete from temas where tema = ?";
            pr = c.prepareStatement(sql);
            pr.setString(1, clave.toUpperCase());
            pr.executeUpdate();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DAOTema.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
    }

    @Override
    public boolean put(String clave, Tema pojo) throws SQLException {
        String sql = "update temas set tema = ? where tema = ?";
        PreparedStatement pr = null;
        Connection c = Conexion.getConnection();
        try {
            pr = c.prepareStatement(sql);
            pr.setString(1, pojo.getTema().toUpperCase());
            pr.setString(2, clave.toUpperCase());
            pr.executeUpdate();

            return true;
        } catch (SQLException ex) {
            Logger.getLogger(DAOTema.class.getName()).log(Level.SEVERE, null, ex);

            throw ex;
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
    }

    @Override
    public Tema getOne(String clave) throws SQLException {
        String sql = "select * from temas where tema = ?";
        PreparedStatement pr = null;
        Tema t = null;
        try {
            pr = Conexion.getConnection().prepareStatement(sql);
            pr.setString(1, clave.toUpperCase());
            try (ResultSet rs = pr.executeQuery()) {
                if (rs.next()) {
                    t = new Tema();
                    t.setTema(rs.getString(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DAOTema.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
        return t;
    }

    @Override
    public List<Tema> getAll() throws SQLException {
        String sql = "select * from temas";
        PreparedStatement pr = null;
        List<Tema> temas = new ArrayList<>();

        try {
            pr = Conexion.getConnection().prepareStatement(sql);
            try (ResultSet rs = pr.executeQuery()) {
                while (rs.next()) {
                    Tema t = new Tema();
                    t.setTema(rs.getString(1));
                    temas.add(t);
                }
            }
            return temas;
        } catch (SQLException ex) {
            Logger.getLogger(DAOTema.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
    }
}
