package servlet;

import dao.EstudiantewebJpaController;
import dao.exceptions.NonexistentEntityException;
import dto.Estudianteweb;

import javax.json.*;
import javax.json.stream.JsonParsingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "EstudianteServlet", urlPatterns = {"/EstudianteServlet"})
public class EstudianteServlet extends HttpServlet {

    private EstudiantewebJpaController dao;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void init() throws ServletException {
        super.init();
        // IMPORTANTE: Reemplaza "TuUnidadDePersistenciaPU" con el nombre de tu unidad de persistencia real
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("com.mycompany_Preg01_war_1.0-SNAPSHOTPU");
        dao = new EstudiantewebJpaController(emf);
    }

    protected void processError(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        JsonObjectBuilder errorBuilder = Json.createObjectBuilder();
        errorBuilder.add("error", message);
        out.print(errorBuilder.build().toString());
        out.flush();
    }
    
    private String dateToString(Date date) {
        if (date == null) return null;
        return sdf.format(date);
    }

    private Date stringToDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) return null;
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            Logger.getLogger(EstudianteServlet.class.getName()).log(Level.WARNING, "Error parsing date: " + dateString, e);
            return null; 
        }
    }
    
    private JsonObject estudianteToJson(Estudianteweb e) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (e.getCodiEstdWeb() != null) {
            builder.add("codiEstdWeb", e.getCodiEstdWeb());
        }
        if (e.getDniEstdWeb() != null) {
            builder.add("dniEstdWeb", e.getDniEstdWeb());
        } else {
            builder.addNull("dniEstdWeb");
        }
        if (e.getAppaEstdWeb() != null) {
            builder.add("appaEstdWeb", e.getAppaEstdWeb());
        } else {
            builder.addNull("appaEstdWeb");
        }
        if (e.getApmaEstdWeb() != null) {
            builder.add("apmaEstdWeb", e.getApmaEstdWeb());
        } else {
            builder.addNull("apmaEstdWeb");
        }
        if (e.getNombEstdWeb() != null) {
            builder.add("nombEstdWeb", e.getNombEstdWeb());
        } else {
            builder.addNull("nombEstdWeb");
        }
        if (e.getFechNaciEstdWeb() != null) {
            builder.add("fechNaciEstdWeb", dateToString(e.getFechNaciEstdWeb()));
        } else {
            builder.addNull("fechNaciEstdWeb");
        }
        if (e.getLogiEstd() != null) {
            builder.add("logiEstd", e.getLogiEstd());
        } else {
            builder.addNull("logiEstd");
        }
        if (e.getPassEstd() != null) {
            builder.add("passEstd", e.getPassEstd()); // Considerar no exponer passwords
        } else {
            builder.addNull("passEstd");
        }
        return builder.build();
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String idParam = request.getParameter("id");

        if (idParam != null && !idParam.isEmpty()) {
            try {
                Integer id = Integer.parseInt(idParam);
                Estudianteweb estudiante = dao.findEstudianteweb(id);
                if (estudiante != null) {
                    out.print(estudianteToJson(estudiante).toString());
                } else {
                    processError(response, "Estudiante no encontrado con ID: " + id, HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            } catch (NumberFormatException e) {
                processError(response, "ID inválido: " + idParam, HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        } else {
            List<Estudianteweb> estudiantes = dao.findEstudiantewebEntities();
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            if (estudiantes != null) { // Siempre verificar si la lista no es nula
                for (Estudianteweb e : estudiantes) {
                    arrayBuilder.add(estudianteToJson(e));
                }
            }
            out.print(arrayBuilder.build().toString());
        }
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            processError(response, "Error al leer el cuerpo de la solicitud: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        JsonObject jsonObject;
        try (JsonReader jsonReader = Json.createReader(new StringReader(sb.toString()))) {
            jsonObject = jsonReader.readObject();
        } catch (JsonParsingException e) {
            processError(response, "JSON inválido: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Estudianteweb estudiante = new Estudianteweb();
        
        // El ID es autogenerado, no se setea aquí.
        if (jsonObject.containsKey("dniEstdWeb") && !jsonObject.isNull("dniEstdWeb")) {
            estudiante.setDniEstdWeb(jsonObject.getString("dniEstdWeb"));
        }
        if (jsonObject.containsKey("appaEstdWeb") && !jsonObject.isNull("appaEstdWeb")) {
            estudiante.setAppaEstdWeb(jsonObject.getString("appaEstdWeb"));
        }
        if (jsonObject.containsKey("apmaEstdWeb") && !jsonObject.isNull("apmaEstdWeb")) {
            estudiante.setApmaEstdWeb(jsonObject.getString("apmaEstdWeb"));
        }
        if (jsonObject.containsKey("nombEstdWeb") && !jsonObject.isNull("nombEstdWeb")) {
            estudiante.setNombEstdWeb(jsonObject.getString("nombEstdWeb"));
        }
        if (jsonObject.containsKey("fechNaciEstdWeb") && !jsonObject.isNull("fechNaciEstdWeb")) {
            estudiante.setFechNaciEstdWeb(stringToDate(jsonObject.getString("fechNaciEstdWeb")));
        }
        if (jsonObject.containsKey("logiEstd") && !jsonObject.isNull("logiEstd")) {
            estudiante.setLogiEstd(jsonObject.getString("logiEstd"));
        }
        if (jsonObject.containsKey("passEstd") && !jsonObject.isNull("passEstd")) {
            estudiante.setPassEstd(jsonObject.getString("passEstd")); // Considerar hashing de contraseña
        }

        try {
            dao.create(estudiante);
            JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
            responseBuilder.add("message", "Estudiante creado exitosamente");
            if (estudiante.getCodiEstdWeb() != null) { // El ID se genera después del create
                 responseBuilder.add("id", estudiante.getCodiEstdWeb());
            }
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(responseBuilder.build().toString());
        } catch (Exception e) {
            Logger.getLogger(EstudianteServlet.class.getName()).log(Level.SEVERE, "Error al crear estudiante", e);
            processError(response, "Error al crear estudiante: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            processError(response, "Error al leer el cuerpo de la solicitud: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        JsonObject jsonObject;
         try (JsonReader jsonReader = Json.createReader(new StringReader(sb.toString()))) {
            jsonObject = jsonReader.readObject();
        } catch (JsonParsingException e) {
            processError(response, "JSON inválido: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!jsonObject.containsKey("codiEstdWeb") || jsonObject.isNull("codiEstdWeb")) {
            processError(response, "El campo 'codiEstdWeb' es requerido para actualizar.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        Integer id = jsonObject.getInt("codiEstdWeb");
        Estudianteweb estudiante = dao.findEstudianteweb(id);

        if (estudiante == null) {
            processError(response, "Estudiante no encontrado con ID: " + id, HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Actualizar campos si están presentes en el JSON
        if (jsonObject.containsKey("dniEstdWeb")) { // Permitir enviar null para blanquear
            estudiante.setDniEstdWeb(jsonObject.isNull("dniEstdWeb") ? null : jsonObject.getString("dniEstdWeb"));
        }
        if (jsonObject.containsKey("appaEstdWeb")) {
            estudiante.setAppaEstdWeb(jsonObject.isNull("appaEstdWeb") ? null : jsonObject.getString("appaEstdWeb"));
        }
        if (jsonObject.containsKey("apmaEstdWeb")) {
            estudiante.setApmaEstdWeb(jsonObject.isNull("apmaEstdWeb") ? null : jsonObject.getString("apmaEstdWeb"));
        }
        if (jsonObject.containsKey("nombEstdWeb")) {
            estudiante.setNombEstdWeb(jsonObject.isNull("nombEstdWeb") ? null : jsonObject.getString("nombEstdWeb"));
        }
        if (jsonObject.containsKey("fechNaciEstdWeb")) {
             estudiante.setFechNaciEstdWeb(jsonObject.isNull("fechNaciEstdWeb") ? null : stringToDate(jsonObject.getString("fechNaciEstdWeb")));
        }
        if (jsonObject.containsKey("logiEstd")) {
            estudiante.setLogiEstd(jsonObject.isNull("logiEstd") ? null : jsonObject.getString("logiEstd"));
        }
        if (jsonObject.containsKey("passEstd")) {
            estudiante.setPassEstd(jsonObject.isNull("passEstd") ? null : jsonObject.getString("passEstd"));
        }


        try {
            dao.edit(estudiante);
            JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
            responseBuilder.add("message", "Estudiante actualizado exitosamente");
            responseBuilder.add("id", id);
            out.print(responseBuilder.build().toString());
        } catch (NonexistentEntityException e) {
            processError(response, "Estudiante no encontrado para actualizar: " + e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) {
            Logger.getLogger(EstudianteServlet.class.getName()).log(Level.SEVERE, "Error al actualizar estudiante", e);
            processError(response, "Error al actualizar estudiante: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        out.flush();
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            processError(response, "Parámetro 'id' es requerido para eliminar.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Integer id = Integer.parseInt(idParam);
            dao.destroy(id);
            JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
            responseBuilder.add("message", "Estudiante eliminado exitosamente");
            responseBuilder.add("id", id);
            out.print(responseBuilder.build().toString());
        } catch (NumberFormatException e) {
            processError(response, "ID inválido: " + idParam, HttpServletResponse.SC_BAD_REQUEST);
        } catch (NonexistentEntityException e) {
            processError(response, "Estudiante no encontrado para eliminar: " + e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (Exception e) { // Captura otras posibles excepciones del DAO
            Logger.getLogger(EstudianteServlet.class.getName()).log(Level.SEVERE, "Error al eliminar estudiante", e);
            processError(response, "Error al eliminar estudiante: " + e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        out.flush();
    }

    @Override
    public String getServletInfo() {
        return "Servlet para CRUD de Estudiantes con javax.json";
    }
}