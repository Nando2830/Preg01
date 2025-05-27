package servlet;

import dao.EstudiantewebJpaController; // Asumiendo que puedes usar dao.findByLogiEstd()
import dto.Estudianteweb;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "LoginServlet", urlPatterns = {"/Login"})
public class LoginServlet extends HttpServlet {

    private EstudiantewebJpaController estudianteDAO;
    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        super.init();
        // IMPORTANTE: Reemplaza "TuUnidadDePersistenciaPU" con el nombre real de tu unidad de persistencia
        // Ejemplo: this.emf = Persistence.createEntityManagerFactory("com.mycompany_Preg01_war_1.0-SNAPSHOTPU");
        this.emf = Persistence.createEntityManagerFactory("com.mycompany_Preg01_war_1.0-SNAPSHOTPU");
        this.estudianteDAO = new EstudiantewebJpaController(emf);
    }

    private void sendJsonResponse(HttpServletResponse response, int statusCode, JsonObject jsonObject) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonObject.toString());
            out.flush();
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
        JsonObjectBuilder errorBuilder = Json.createObjectBuilder();
        if (message != null) {
            errorBuilder.add("error", message);
        } else {
            errorBuilder.add("error", "Error desconocido");
        }
        sendJsonResponse(response, statusCode, errorBuilder.build());
    }

    // Opción 1: Usar un método dentro del servlet si no puedes/quieres modificar el DAO
    // Asegúrate que la NamedQuery "Estudianteweb.findByLogiEstd" exista en tu entidad Estudianteweb.java
    private Estudianteweb findEstudianteByLoginInterno(String login) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Estudianteweb> query = em.createNamedQuery("Estudianteweb.findByLogiEstd", Estudianteweb.class);
            query.setParameter("logiEstd", login);
            List<Estudianteweb> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            Logger.getLogger(LoginServlet.class.getName()).log(Level.SEVERE, "Error en findEstudianteByLoginInterno para " + login, e);
            return null;
        }
        finally {
            if (em != null) {
                em.close();
            }
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            Logger.getLogger(LoginServlet.class.getName()).log(Level.SEVERE, "Error al leer cuerpo de POST en LoginServlet", e);
            sendErrorResponse(response, "Error al leer datos de la solicitud.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        JsonObject jsonRequest;
        try (JsonReader jsonReader = Json.createReader(new StringReader(sb.toString()))) {
            jsonRequest = jsonReader.readObject();
        } catch (JsonParsingException e) {
            Logger.getLogger(LoginServlet.class.getName()).log(Level.WARNING, "JSON inválido en LoginServlet POST: " + sb.toString(), e);
            sendErrorResponse(response, "Formato JSON inválido: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Verificar que los campos necesarios para login están presentes
        if (!jsonRequest.containsKey("logiEstd") || jsonRequest.isNull("logiEstd") ||
            !jsonRequest.containsKey("passEstd") || jsonRequest.isNull("passEstd")) {
            sendErrorResponse(response, "Usuario y contraseña son requeridos.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String logiEstd = jsonRequest.getString("logiEstd");
        String passEstd = jsonRequest.getString("passEstd");

        Estudianteweb estudiante;
        try {
            // Opción 1: Usar el método interno del servlet
            estudiante = findEstudianteByLoginInterno(logiEstd);

            // Opción 2: Si tienes findByLogiEstd en tu EstudiantewebJpaController
            // Asumiendo que tu DAO tiene un método como el que te proporcioné:
            // estudiante = estudianteDAO.findByLogiEstd(logiEstd); 

        } catch (Exception e) {
            Logger.getLogger(LoginServlet.class.getName()).log(Level.SEVERE, "Error al buscar estudiante " + logiEstd, e);
            sendErrorResponse(response, "Error interno del servidor al verificar credenciales.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        

        if (estudiante != null && estudiante.getPassEstd() != null && estudiante.getPassEstd().equals(passEstd)) {
            // ¡Login exitoso!
            // En una aplicación real, aquí generarías un token de sesión o usarías HttpSession.
            // Por ahora, solo confirmamos el éxito.
            JsonObjectBuilder successResponse = Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", "Login exitoso.")
                    .add("logiEstd", estudiante.getLogiEstd()); // Enviar el login para guardarlo en sessionStorage
            sendJsonResponse(response, HttpServletResponse.SC_OK, successResponse.build());
        } else {
            // Credenciales incorrectas
            sendErrorResponse(response, "Usuario o contraseña incorrectos.", HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Override
    public String getServletInfo() {
        return "Servlet para autenticación de Estudiantes";
    }
}