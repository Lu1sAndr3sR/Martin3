package services;

import dao.DAOSolicitud;
import pojo.ErrorObjectConv;
import pojo.Expediente;
import pojo.Solicitud;
import com.google.common.collect.Lists;
import dao.DAOExpediente;
import dao.Filtro;
import java.sql.SQLException;
import java.util.ArrayList;
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import pojo.ErrorClass;

/**
 * REST Web Service
 *
 * @author aldair
 */
@Path("solicitudes")
public class SolicitudesResource {

    @Context
    private UriInfo context;
    /**
     * Retrieves representation of an instance of Services.SolicitudesResource
     *
     * @param id_solicitud
     * @param pasw
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response Ingresar(@QueryParam("id_solicitud") String id_solicitud,
            @QueryParam("password") String pasw) {
        //TODO return proper representation object
        DAOSolicitud ds = DAOSolicitud.getDaoSolicitud();
        Expediente e;
        try {
            e = ds.login(id_solicitud, pasw);
            if (e == null) {
                return Response.status(404).build();
            }
            return Response.ok(e).build();
        } catch (Exception ex) {
            Logger.getLogger(SolicitudesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }

    }

    //Eliminar una solicitud o cancelar
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSolicitud(@QueryParam("id_solicitud") String id_solicitud) {
        DAOSolicitud ds = DAOSolicitud.getDaoSolicitud();
        try {
            ds.delete(id_solicitud);
            return Response.status(200).build();
        } catch (SQLException ex) {
            Logger.getLogger(SolicitudesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    //Crear solicitud
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSolicitud(@QueryParam("id_expediente") String id_expediente,
            Solicitud solicitud) {
        DAOSolicitud ds = DAOSolicitud.getDaoSolicitud();

        try {

            ds.post(solicitud, id_expediente);

            return Response.status(200).build();
        } catch (SQLException ex) {
            Logger.getLogger(SolicitudesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }

    }

    //Retorna las solicitudes pendientes que tiene un usuario
    @GET
    @Path("usuarios")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSolicitudesDeUsuario(@QueryParam("id_usuario") String id_usuario) {
        DAOSolicitud ds = DAOSolicitud.getDaoSolicitud();

        GenericEntity<List<Solicitud>> solicitudes;
        try {
            solicitudes = new GenericEntity<List<Solicitud>>(Lists.newArrayList(ds.getAllByUsuario(id_usuario))) {
            };
            return Response.ok(solicitudes).build();
        } catch (SQLException ex) {
            Logger.getLogger(SolicitudesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSolicitudes(@QueryParam("id_autor") String id_autor, String filtros) {
        DAOSolicitud dao = DAOSolicitud.getDaoSolicitud();
        List<Filtro> f = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(filtros);
            for (int i = 0; i < array.length(); i++) {
                Filtro filtro = new Filtro();
                filtro.objectify(array.getJSONObject(i));
                f.add(filtro);
            }
            GenericEntity<List<Solicitud>> solicitudes;
            solicitudes = new GenericEntity<List<Solicitud>>(Lists.newArrayList(dao.check(id_autor))) {
            };
            return Response.ok(solicitudes).build();
        } catch (JSONException | SQLException ex) {
            Logger.getLogger(SolicitudesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }

    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(Solicitud solicitud) {
        try {
            DAOSolicitud dao = DAOSolicitud.getDaoSolicitud();
            dao.update(solicitud);
            return Response.ok().build();
        } catch (SQLException ex) {
            Logger.getLogger(SolicitudesResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }
}
