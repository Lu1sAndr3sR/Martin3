package services;

import dao.DAOExpediente;
import pojo.ErrorObjectConv;
import pojo.Expediente;
import pojo.Expedientes_Autor;
import com.google.common.collect.Lists;
import dao.Filtro;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
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
import org.codehaus.jettison.json.JSONException;
import pojo.ErrorClass;

/**
 * REST Web Service
 *
 * @author aldair
 *
 */
@Path("expedientes")
public class ExpedientesResource {

    @Context
    private UriInfo context;
    
    private final String validacionMessage = "Validación";
    /*
    *
     * Creates a new instance of GenericResource
     */
    /**
     * Retrieves representation of an instance of Services.GenericResource
     *
     * @param id_usuario
     * @return
     */
    
    @PUT
    @Path("enable")
    @Produces(MediaType.APPLICATION_JSON)
    public Response upExpediente(@QueryParam("id_expediente") String id_expediente){
        try {           
            DAOExpediente dao = DAOExpediente.getDAOExpediente();
            dao.upExp(id_expediente);
            return Response.ok().build();
        } catch (SQLException ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        } 
    }
    @GET
    @Path("usuario")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExpedientesByUsuario(@QueryParam("id_usuario") String id_usuario) {
        //TODO return proper representation object
        
        DAOExpediente dao = DAOExpediente.getDAOExpediente();
        GenericEntity<List<Expediente>> expedientes;
        try {
            expedientes = new GenericEntity<List<Expediente>>(Lists.newArrayList(dao.getAllByUsuario(id_usuario))) {
            };
            return Response.ok(expedientes).build();
        } catch (SQLException ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }

    }

    @GET
    @Path("autor_usuario")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllByAuthUser(@QueryParam("id_autor") String id_autor, @QueryParam("id_usuario") String id_usuario) {
        //TODO return proper representation object
        DAOExpediente dao = DAOExpediente.getDAOExpediente();
        try {
            Expedientes_Autor exps_auth = dao.getAllByAutor_Usuario(id_autor, id_usuario);
            return Response.ok(exps_auth).build();
        } catch (SQLException ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(Expediente expediente) {
        if(expediente.getTitulo().isEmpty()){
            ErrorClass ex = new ErrorClass();
            ex.setCode(validacionMessage);
            ex.setMessage("El titulo no puede ir vacío");
            return Response.status(409).entity(ex).build();
        }
        if(expediente.getTitulo().length()>50){
           ErrorClass ex = new ErrorClass();
            ex.setCode(validacionMessage);
            ex.setMessage("El titulo no puede exceder de 50 caracteres");
            return Response.status(409).entity(ex).build(); 
        }
        if(expediente.getTemas().isEmpty()){
            ErrorClass ex = new ErrorClass();
            ex.setCode(validacionMessage);
            ex.setMessage("Agregue al menos un tema");
            return Response.status(409).entity(ex).build();
        }
        
        if(expediente.getTemas().length()>32){
            ErrorClass ex = new ErrorClass();
            ex.setCode(validacionMessage);
            ex.setMessage("Agregue al menos un tema");
            return Response.status(409).entity(ex).build();
        }
        
        DAOExpediente dao = DAOExpediente.getDAOExpediente();
        try {
            dao.post(expediente);
            return Response.status(200).build();
        } catch (IOException | JSONException|SQLException ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch ( ErrorClass ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@QueryParam("id_expediente") String id_expediente) {
        try {
            DAOExpediente dao = DAOExpediente.getDAOExpediente();
            dao.delete(id_expediente);
            return Response.status(200).build();
        } catch (SQLException ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }

    //Retorna un sólo expediente
    @GET
    @Path("/id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getExpediente(@QueryParam("id_expediente") String id_expediente,
            @QueryParam("id_usuario") String id_usuario) throws ParseException, IOException, JSONException {
        try {
            DAOExpediente dao = DAOExpediente.getDAOExpediente();
            return Response.ok(dao.getOne(id_expediente, id_usuario)).build();
        } catch (SQLException ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        } 
    }
    
    @PUT
    @Path("autor")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    //Expedientes de creador
    public Response getExpedientesByAutor(@QueryParam("id_autor") String id_autor, String filtros) {
        GenericEntity<List<Expediente>> expedientes;
        DAOExpediente dao = DAOExpediente.getDAOExpediente();
        try {
            List<Filtro> filtrosa = Filtro.getFiltrosList(filtros);
            expedientes = new GenericEntity<List<Expediente>>(Lists.newArrayList(dao.getAllByAutor(id_autor, filtrosa))) {
            };
            return Response.ok(expedientes).build();
        } catch (SQLException ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }
    
    

    /**
     * PUT method for updating or creating an instance of GenericResource
     *
     * @param id_expediente
     * @param expediente
     * @return
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(@QueryParam("id_expediente") String id_expediente, Expediente expediente) {
        DAOExpediente dao = DAOExpediente.getDAOExpediente();
        try {
            dao.put(id_expediente, expediente);
            return Response.ok().build();
        } catch (SQLException ex) {
            Logger.getLogger(ExpedientesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).build();
        }
    }

}
