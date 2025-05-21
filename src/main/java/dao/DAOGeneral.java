/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author aldair
 */
public interface DAOGeneral <T,C> {
    public boolean post(T pojo) throws SQLException;
    public boolean delete(C clave) throws SQLException;
    public boolean put(C clave, T pojo) throws SQLException;
    public T getOne(C clave) throws SQLException;
    public List<T> getAll() throws SQLException;
}
