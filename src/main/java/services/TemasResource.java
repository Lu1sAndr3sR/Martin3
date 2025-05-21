/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import dao.DAOTema;
import pojo.ErrorObjectConv;
import pojo.Tema;
import com.google.common.collect.Lists;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author aldair
 */
@Path("temas")
public class TemasResource {

    @Context
    private UriInfo context;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        //TODO return proper representation objec
        DAOTema dao = DAOTema.getDaoTema();
        GenericEntity<List<Tema>> temas;

        try {
            temas = new GenericEntity<List<Tema>>(Lists.newArrayList(dao.getAll())) {
            };
            return Response.ok(temas).build();
        } catch (SQLException ex) {
            Logger.getLogger(TemasResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(Tema tema) {
        DAOTema dao = DAOTema.getDaoTema();
        try {
            dao.post(tema);
            return Response.status(200).build();
        } catch (SQLException ex) {
            Logger.getLogger(TemasResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    @DELETE
    public Response delete(@QueryParam("id_tema") String id_tema) {
        DAOTema dao = DAOTema.getDaoTema();
        try {
            dao.delete(id_tema);
            return Response.status(200).build();
        } catch (SQLException ex) {
            Logger.getLogger(TemasResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }

    }

    @GET
    @Path("/id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTema(@QueryParam("id_tema") String id_tema) {
        DAOTema dao = DAOTema.getDaoTema();
        try {
            return Response.ok(dao.getOne(id_tema)).build();
        } catch (SQLException ex) {
            Logger.getLogger(TemasResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    /**
     * PUT method for updating or creating an instance of GenericResource
     *
     * @param id_tema
     * @param tema
     * @return
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(@QueryParam("id_tema") String id_tema, Tema tema) {
        DAOTema dao = DAOTema.getDaoTema();
        try {
            dao.put(id_tema, tema);
            return Response.status(200).build();
        } catch (SQLException ex) {
            Logger.getLogger(TemasResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }
}
