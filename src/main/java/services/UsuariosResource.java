/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import dao.DAOUsuario;
import dao.Filtro;
import pojo.ErrorObjectConv;
import pojo.Usuario;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import pojo.ErrorClass;

/**
 * REST Web Service
 *
 * @author aldair
 */
@Path("usuarios")
public class UsuariosResource {

    @Context
    private UriInfo context;

    /**
     * Retrieves representation of an instance of Services.UsuariosResource
     *
     * @param usuario
     * @param pasw
     * @return an instance of java.lang.String
     */
    @GET
    @Path("connected")
    @Produces(MediaType.APPLICATION_JSON)
    public Response connected() {
        JSONObject json = new JSONObject();
        try {
            json.put("message", "connected!");
        } catch (JSONException ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.ok(json.toString(), MediaType.APPLICATION_JSON).build();
    }

    @PUT
    @Path("enable")
    @Produces(MediaType.APPLICATION_JSON)
    public Response upUsuario(@QueryParam("id_usuario") String id_usuario) {

        try {
            DAOUsuario dao = DAOUsuario.getDaoUsuario();
            dao.upUser(id_usuario);
            return Response.ok().build();
        } catch (SQLException | IOException ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }

    @GET
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@QueryParam("id_usuario") String usuario,
            @QueryParam("pasw") String pasw) throws JSONException {
        Usuario u;
        try {
            u = DAOUsuario.getDaoUsuario().login(usuario, pasw);
            return Response.ok(u).build();
        } catch (SQLException | IOException | NoSuchAlgorithmException | ErrorClass ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        //TODO return proper representation objec
        DAOUsuario dao = DAOUsuario.getDaoUsuario();
        GenericEntity<List<Usuario>> usuarios;
        try {
            usuarios = new GenericEntity<List<Usuario>>(Lists.newArrayList(dao.getAll())) {
            };
            return Response.ok(usuarios).build();
        } catch (SQLException ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    @GET
    @Path("/tipo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllByType(@QueryParam("tipo") String tipo) {
        //TODO return proper representation objec
        DAOUsuario dao = DAOUsuario.getDaoUsuario();
        GenericEntity<List<Usuario>> usuarios;
        try {
            usuarios = new GenericEntity<List<Usuario>>(Lists.newArrayList(dao.getAllType(DAOUsuario.Perfil.valueOf(tipo)))) {
            };
            return Response.ok(usuarios).build();
        } catch (SQLException ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(Usuario usuario) {
        if(usuario.getId_usuario().isEmpty())
        {
            ErrorClass ex = new ErrorClass();
            ex.setCode("Validación");
            ex.setMessage("El id del usuario no puede ir vacío");
            return Response.status(409).entity(ex).build();
        }
        if(usuario.getNombre().length()>15){
            ErrorClass ex = new ErrorClass();
            ex.setCode("Validación");
            ex.setMessage("El nombre del usuario no puede exceder los 10 caracteres");
            return Response.status(409).entity(ex).build();
        }
        
        
        DAOUsuario dao = DAOUsuario.getDaoUsuario();
        try {
            dao.post(usuario);
            return Response.status(200).build();
        } catch (SQLException | NoSuchAlgorithmException | IOException | JSONException ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response disableUsuario(@QueryParam("id_usuario") String id_usuario) {
        DAOUsuario dao = DAOUsuario.getDaoUsuario();
        try {
            dao.delete(id_usuario);
            return Response.status(200).build();
        } catch (IOException | SQLException ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }

    @GET
    @Path("/id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsuario(@QueryParam("id_usuario") String id_usuario) {
        DAOUsuario dao = DAOUsuario.getDaoUsuario();
        try {
            return Response.ok(dao.get(id_usuario)).build();
        } catch (SQLException | IOException | JSONException | ErrorClass ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    /**
     * PUT method for updating or creating an instance of GenericResource
     *
     * @param id_usuario
     * @param usuario
     * @return
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(@QueryParam("id_usuario") String id_usuario, Usuario usuario) {
        DAOUsuario dao = DAOUsuario.getDaoUsuario();
        try {
            dao.put(id_usuario, usuario);
            return Response.status(200).build();
        } catch (SQLException ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    @PUT
    @Path("filter")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsersAndFilter(String filtros) {

        GenericEntity<List<Usuario>> usuarios;
        DAOUsuario dao = DAOUsuario.getDaoUsuario();
        try {
            List<Filtro> f = Filtro.getFiltrosList(filtros);
            if (!f.isEmpty()) {
                usuarios = new GenericEntity<List<Usuario>>(Lists.newArrayList(dao.getAndFilter(f))) {
                };
            } else {
                usuarios = new GenericEntity<List<Usuario>>(Lists.newArrayList(dao.getAll())) {
                };
            }
            return Response.ok(usuarios).build();
        } catch (SQLException ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }

}
