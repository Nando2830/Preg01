
package servlet; // Asegúrate que el paquete sea el correcto

import dao.EstudiantewebJpaController; // Cambiado de ClienteJpaController
import dao.exceptions.NonexistentEntityException;
import dto.Estudianteweb; // Cambiado de Cliente

import java.io.BufferedReader; // Para leer JSON del request body
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader; // Para leer JSON del request body
// import java.io.StringWriter; // No es necesario si escribes directamente al PrintWriter de response

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader; // Para leer JSON del request body
import javax.json.JsonWriter;
import javax.json.stream.JsonParsingException; // Para errores de parseo JSON
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
// Eliminado import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/PasswordServlet"}) // Nombre y URL actualizados
public class ChangePasswordServlet extends HttpServlet {

    private EntityManagerFactory emf;
    private EstudiantewebJpaController estudianteController; // Cambiado a EstudiantewebJpaController

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            // IMPORTANTE: Reemplaza "TuUnidadDePersistenciaPU" con el nombre real de tu unidad de persistencia
            // Ejemplo: emf = Persistence.createEntityManagerFactory("com.mycompany_Preg01_war_1.0-SNAPSHOTPU");
            emf = Persistence.createEntityManagerFactory("com.mycompany_Preg01_war_1.0-SNAPSHOTPU");
            estudianteController = new EstudiantewebJpaController(emf);
        } catch (Exception e) {
            Logger.getLogger(ChangePasswordServlet.class.getName()).log(Level.SEVERE, "Error inicializando EntityManagerFactory", e);
            throw new ServletException("Error inicializando EntityManagerFactory en ChangePasswordEstudianteServlet", e);
        }
    }

    private void sendJsonResponse(HttpServletResponse response, int statusCode, JsonObject jsonObject) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        try (PrintWriter out = response.getWriter();
             JsonWriter jsonWriter = Json.createWriter(out)) { // Escribir directamente al PrintWriter de la respuesta
            jsonWriter.writeObject(jsonObject);
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

    // Método para buscar estudiante por login (similar al que teníamos en LoginServlet)
    private Estudianteweb findEstudianteByLogin(String login) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Estudianteweb> query = em.createNamedQuery("Estudianteweb.findByLogiEstd", Estudianteweb.class);
            query.setParameter("logiEstd", login);
            List<Estudianteweb> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            Logger.getLogger(ChangePasswordServlet.class.getName()).log(Level.SEVERE, "Error en findEstudianteByLogin para " + login, e);
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
        JsonObjectBuilder jsonResponseBuilder = Json.createObjectBuilder();

        // Leer el cuerpo JSON de la solicitud
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            Logger.getLogger(ChangePasswordServlet.class.getName()).log(Level.SEVERE, "Error al leer cuerpo de POST", e);
            sendErrorResponse(response, "Error al leer datos de la solicitud.", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        JsonObject jsonRequest;
        try (JsonReader jsonReader = Json.createReader(new StringReader(sb.toString()))) {
            jsonRequest = jsonReader.readObject();
        } catch (JsonParsingException e) {
            Logger.getLogger(ChangePasswordServlet.class.getName()).log(Level.WARNING, "JSON inválido en POST: " + sb.toString(), e);
            sendErrorResponse(response, "Formato JSON inválido: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Obtener datos del JSON request
        String logiEstd = null;
        if (jsonRequest.containsKey("logiEstd") && !jsonRequest.isNull("logiEstd")) {
            logiEstd = jsonRequest.getString("logiEstd");
        }
        
        String currentPassword = null;
        if (jsonRequest.containsKey("currentPassword") && !jsonRequest.isNull("currentPassword")) {
            currentPassword = jsonRequest.getString("currentPassword");
        }

        String newPassword = null;
        if (jsonRequest.containsKey("newPassword") && !jsonRequest.isNull("newPassword")) {
            newPassword = jsonRequest.getString("newPassword");
        }
        
        // No necesitamos 'confirmNewPassword' en el servidor si ya se validó en el cliente,
        // pero si el JSON lo envía, podríamos leerlo. Por ahora, asumimos que el cliente lo valida.

        // Validaciones del lado del servidor
        if (logiEstd == null || logiEstd.trim().isEmpty() ||
            currentPassword == null || currentPassword.trim().isEmpty() ||
            newPassword == null || newPassword.trim().isEmpty()) {
            jsonResponseBuilder.add("success", false)
                               .add("message", "Usuario, contraseña actual y nueva contraseña son obligatorios.");
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, jsonResponseBuilder.build());
            return;
        }
        
        // Buscar al estudiante por su login
        Estudianteweb estudianteLogueado = findEstudianteByLogin(logiEstd);

        if (estudianteLogueado == null) {
            jsonResponseBuilder.add("success", false)
                               .add("message", "Usuario no encontrado. No se puede cambiar la contraseña.");
            sendJsonResponse(response, HttpServletResponse.SC_NOT_FOUND, jsonResponseBuilder.build());
            return;
        }

        // Continuar con las validaciones de contraseña
        if (newPassword.length() < 6) { // Política de longitud (ejemplo)
             jsonResponseBuilder.add("success", false)
                                .add("message", "La nueva contraseña debe tener al menos 6 caracteres.");
        } else if (estudianteLogueado.getPassEstd() == null || !estudianteLogueado.getPassEstd().equals(currentPassword)) {
            // ¡IMPORTANTE! Esto compara contraseñas en texto plano.
            // En producción, deberías comparar hashes de contraseñas.
            jsonResponseBuilder.add("success", false)
                               .add("message", "La contraseña actual es incorrecta.");
        } else if (estudianteLogueado.getPassEstd().equals(newPassword)) {
            jsonResponseBuilder.add("success", false)
                               .add("message", "La nueva contraseña no puede ser igual a la contraseña actual.");
        }
        else { // Todas las validaciones pasaron, proceder a cambiar la contraseña
            try {
                // **ADVERTENCIA DE SEGURIDAD:**
                // Estás guardando la contraseña en texto plano. ¡Esto es muy inseguro!
                // Deberías hashear la nueva contraseña antes de guardarla.
                // Ejemplo (necesitarías una librería como BCrypt y añadirla a tu proyecto):
                // String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12)); // 12 es el costo
                // estudianteLogueado.setPassEstd(hashedPassword);

                estudianteLogueado.setPassEstd(newPassword); // Guardando en texto plano (NO RECOMENDADO)
                
                estudianteController.edit(estudianteLogueado); // Guardar los cambios en la BD

                jsonResponseBuilder.add("success", true)
                                   .add("message", "Contraseña cambiada exitosamente.");

            } catch (NonexistentEntityException ne) { // Aunque ya buscamos el usuario, el edit podría fallar
                 jsonResponseBuilder.add("success", false)
                                   .add("message", "Error: El usuario no fue encontrado durante la actualización.");
                 Logger.getLogger(ChangePasswordServlet.class.getName()).log(Level.WARNING, "Usuario no encontrado durante edit en cambio de pass", ne);
            } 
            catch (Exception e) {
                Logger.getLogger(ChangePasswordServlet.class.getName()).log(Level.SEVERE, "Error en servidor al cambiar contraseña", e);
                jsonResponseBuilder.add("success", false)
                                   .add("message", "Error en el servidor al cambiar la contraseña: " + e.getMessage());
            }
        }
        
        int statusCode = jsonResponseBuilder.build().getBoolean("success", false) ? 
                         HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST;
        if (jsonResponseBuilder.build().containsKey("message") && 
            jsonResponseBuilder.build().getString("message").contains("Usuario no encontrado")) {
            statusCode = HttpServletResponse.SC_NOT_FOUND;
        }
        
        sendJsonResponse(response, statusCode, jsonResponseBuilder.build());
    }


    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        super.destroy();
    }

    @Override
    public String getServletInfo() {
        return "Servlet para cambiar la contraseña del estudiante autenticado (basado en login)";
    }
}