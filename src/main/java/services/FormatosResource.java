/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import dao.DAOFormatos;
import pojo.ErrorObjectConv;
import pojo.Formato;
import com.google.common.collect.Lists;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response;


/**
 * REST Web Service
 *
 * @author aldair
 */
@Path("formatos")
public class FormatosResource {

    @Context
    private UriInfo context;

    /**
     * Retrieves representation of an instance of Services.FormatosResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        //TODO return proper representation objec
        DAOFormatos dao = DAOFormatos.getDAOFormato();
        try {
             GenericEntity<List <Formato>> formatos;
             formatos = new GenericEntity<List<Formato>>(Lists.newArrayList(dao.getAll())){};
             return Response.ok(formatos).build();
        } catch (SQLException ex) {
            Logger.getLogger(FormatosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(Formato formato) {
        try {
            DAOFormatos dao = DAOFormatos.getDAOFormato();
            if (dao.post(formato)) {
               return Response.status(200).build();
            } 
        } catch (SQLException ex) {
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
        return Response.status(409).build();
    }

    @DELETE
    public Response delete(@QueryParam("id_formato") String id_formato) {
        try {
            
            if (DAOFormatos.getDAOFormato().delete(id_formato.toUpperCase())) {
                return Response.status(200).build();
            } 
        } catch (SQLException ex) {
            Logger.getLogger(FormatosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
        
        return Response.status(409).build();
    }

    @GET
    @Path("/id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOne(@QueryParam("id_formato") String id_formato) {
        DAOFormatos dao = DAOFormatos.getDAOFormato();
        try {
            return Response.ok(dao.getOne(id_formato.toUpperCase())).build();
        } catch (SQLException ex) {
            Logger.getLogger(FormatosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(@QueryParam("id_formato") String id_formato, Formato formato) {
        DAOFormatos dao = DAOFormatos.getDAOFormato();
        try {
            if (dao.put(id_formato.toUpperCase(), formato)) {
                return Response.status(200).build();
            } 
        } catch (SQLException ex) {
            Logger.getLogger(FormatosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
        
        return Response.status(409).build();
    }
}
