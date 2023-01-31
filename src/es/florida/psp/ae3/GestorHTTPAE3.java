package es.florida.psp.ae3;

import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Properties;
import java.util.Scanner;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;

//autors -  Borja Zafra i Pablo Rozalén
public class GestorHTTPAE3 implements HttpHandler {

	// Atributs de classe per a gestionar les conexions a la base de dades MongoDB.
	static MongoClient mongoClient = null;
	static MongoDatabase database = null;
	static MongoCollection<Document> coleccion = null;

	// HANDLE //

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		obrirConexio();
		String requestParamValue = null;
		if ("GET".equals(httpExchange.getRequestMethod())) {
			requestParamValue = handleGetRequest(httpExchange);
			handleGETResponse(httpExchange, requestParamValue);
		} else if ("POST".equals(httpExchange.getRequestMethod())) {
			try {
				requestParamValue = handlePostRequest(httpExchange);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			handlePOSTResponse(httpExchange, requestParamValue);
		}
		tancarConexio();
	}

	// Metodes de Conexions //

	public static void obrirConexio() {
		try {
			mongoClient = new MongoClient("localhost", 27017);
			database = mongoClient.getDatabase("policia");
			coleccion = database.getCollection("delincuentes");
			Thread.sleep(1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void tancarConexio() {
		try {
			mongoClient.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Metodes generals //

	/**
	 * Visualitzar una pagina web amb missatge d'error.
	 * 
	 * @return String amb la pagina web d'error.
	 */
	private String web404() {
		String web = "";
		web = "<html><body> Error de Busqueda, intentalo de nuevo con otra direccion. </body></html>";
		return web;
	}

	/**
	 * Monta la pagina web amb tots el alias de delincuentes a la coleccio
	 * "delincuentes" a la base de dades "policia";
	 * 
	 * @param datos llista de alies de delincuents.
	 * @return String amb la pagina web amb els alies.
	 */
	private String webTodos(ArrayList<String> datos) {

		String web = "";
		web = "<html><body>  <h1>Lista de delincuentes:</h1>";
		for (int i = 0; i < datos.size(); i++) {
			web += datos.get(i) + "<br>";
		}
		web += "<p>localhost:7777/servidor/mostrarTodos para mostrar la lista de delincuentes.<br>";
		web += "localhost:7777/servidor/mostrarUno?alias=(Introducir Alias) para mostrar información del delincuente por su alias.</p>";
		web += "</body></html>";

		return web;
	}

	/**
	 * Monta una pagina web amb totes les dades d'un delincuent.
	 * 
	 * @param datos Llista amb els fields del document mongoDb de la base de dades.
	 * @return string ambla pagina web.
	 */
	private String webUno(ArrayList<String> datos) {

		String web = "";
		web = "<html><body><h1>Datos del Delincuente:</h1>";
		web += "<b>Alias</b>: " + datos.get(0) + "<br>";
		web += "<b>Nombre Completo</b>: " + datos.get(1) + "<br>";
		web += "<b>Fecha Nacimiento</b>: " + datos.get(2) + "<br>";
		web += "<b>Nacionalidad</b>: " + datos.get(3) + "<br>";
		web += "<b>Foto</b>: <img width=\"300\" height=\"300\" src='" + datos.get(4) + "'><br>";
		web += "<p>localhost:7777/servidor/mostrarTodos para mostrar la lista de delincuentes.<br>";
		web += "localhost:7777/servidor/mostrarUno?alias=(Introducir Alias) para mostrar información del delincuente por su alias.</p>";
		web += "</body></html>";

		return web;
	}

	/**
	 * Convirteix el codi en base64 a jpg y guarda la ruta absoluta per a enviar al
	 * mail.
	 * 
	 * @param foto64 codi Base 64 extrair de la base de dades.
	 * @param alias  nom per cridar a la fotografia del criminal.
	 * @return String ruta absoluta de la imatge per a enviarla com a anex en el
	 *         mail.
	 * @throws IOException
	 */
	private static String convertirFoto(String foto64, String alias) throws IOException {
		String codigoFoto = foto64.split(",")[1];
		byte[] data = Base64.getDecoder().decode(codigoFoto);
		// crear el arxiu d'imatge a la carpeta "fotos" del projecte.
		File archivo = new File("fotos\\" + alias + ".jpg");
		FileOutputStream fos = new FileOutputStream(archivo);
		fos.write(data);
		fos.close();
		// Torna la ruta absoluta a partir de la ubicacion de la imatge per enviarla al mail.
		return archivo.getAbsolutePath();
	}

	/**
	 * Borra la foto del proyecte una vegada s'ha enviat per correo electronic.
	 * 
	 * @param String ruta absoluta
	 */
	private static void borrarFoto(String ruta) {
		File archivoBorrar = new File(ruta);
		archivoBorrar.delete();
	}

	//  GET //
	// Metodes de les peticions GET //

	/**
	 * Peticio GET
	 * 
	 * @param httpExchange url peticio. 
	 * @return String/pagina web amb la peticio. 
	 */
	private String handleGetRequest(HttpExchange httpExchange) {
		String web = "";
		String tipo = httpExchange.getRequestURI().toString();
		ArrayList<String> aliasList = new ArrayList<String>();
		if (tipo.contains("mostrarTodos")) {
			MongoCursor<Document> cursor = coleccion.find().iterator();
			while (cursor.hasNext()) {
				JSONObject obj = new JSONObject(cursor.next().toJson());
				aliasList.add(obj.getString("alias"));
			}
			web = webTodos(aliasList);
			System.out.println("Enviant Peticio MostratTodos");
			return web;
		} else if (tipo.contains("mostrarUno")) {
			if (httpExchange.getRequestURI().toString().split("\\?")[1].contains("alias")) {
				ArrayList<String> datos = new ArrayList<String>();
				String alias = httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];
				Bson query = eq("alias", alias);
				MongoCursor<Document> cursorMostrarUno = coleccion.find(query).iterator();
				// Construim el JSON amb les dades del delincuente basat en l'alias.
				while (cursorMostrarUno.hasNext()) {
					JSONObject obj = new JSONObject(cursorMostrarUno.next().toJson());
					datos.add(obj.getString("alias"));
					datos.add(obj.getString("nombreCompleto"));
					datos.add(obj.getString("fechaNacimiento"));
					datos.add(obj.getString("nacionalidad"));
					datos.add(obj.getString("foto"));
					web = webUno(datos);
				}
			} else {
				web = web404();
			}

		} else {
			web = web404();
		}
		return web;
	}

	/**
	 * Envia la peticio. 
	 * @param httpExchange
	 * @param requestParamValue pagina web amb les dades. 
	 */
	private void handleGETResponse(HttpExchange httpExchange, String requestParamValue) {
		OutputStream outputStream = httpExchange.getResponseBody();
		String htmlResponse = requestParamValue;
		try {
			byte[] bytes = htmlResponse.getBytes(StandardCharsets.UTF_8);
			httpExchange.sendResponseHeaders(200, bytes.length);
			outputStream.write(htmlResponse.getBytes());
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//  POST  //
	// Metodes de les peticions POST//
	
	/**
	 * Afegir delincuent a la base de dades MongoDB.
	 * @param document
	 */
	private void addDelincuente(ArrayList<String> document) {

		Document doc = new Document();
		doc.append("alias", document.get(0));
		doc.append("nombreCompleto", document.get(1));
		doc.append("fechaNacimiento", document.get(2));
		doc.append("nacionalidad", document.get(3));
		doc.append("foto", document.get(4));
		coleccion.insertOne(doc);
		mongoClient.close();
	}

	
	/**
	 * Metode que prepara el mail per a enviar. 
	 * @param datos del delincuent pero enviar les dades al body del mail.  
	 * @throws MessagingException
	 * @throws IOException
	 */
	public void prepararMail(ArrayList<String> datos) throws MessagingException, IOException {
		Scanner sc = new Scanner(System.in);
		String asunto = "Nuevo delincuente añadido";
		String mensaje = "Se ha añadido un nuevo delincuente con los siguientes datos:\n";
		
		mensaje += "Alias: " + datos.get(0) + "\n";
		mensaje += "Nombre Completo: " + datos.get(1) + "\n";
		mensaje += "Fecha de nacimiento: " + datos.get(2) + "\n";
		mensaje += "Nacionalidad: " + datos.get(3) + "\n";
		
		System.out.println("Correo remitente: ");
		String email_remitente = sc.next();
		System.out.println("Contraseña remitente: "); //Contrasenya de autoritzacion de g mail. 
		String email_remitente_pass = sc.next();
		String host_email = "smtp.gmail.com";
		String port_email = "587";
		System.out.println("Correo destino: ");
		String e_dest = sc.next();
		String[] email_destino = { e_dest};
		
		String pathFoto = convertirFoto(datos.get(4), datos.get(0));
		String[] anexo = { pathFoto };

		envioMail(mensaje, asunto, email_remitente, email_remitente_pass, host_email, port_email, email_destino, anexo);
	}

	
	/**
	 * Envia el mail amb la imatge a partir del absolute path. 
	 * @param mensaje
	 * @param asunto
	 * @param email_remitente
	 * @param email_remitente_pass
	 * @param host_email
	 * @param port_email
	 * @param email_destino
	 * @param anexo
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	public static void envioMail(String mensaje, String asunto, String email_remitente, String email_remitente_pass,
			String host_email, String port_email, String[] email_destino, String[] anexo)
			throws UnsupportedEncodingException, MessagingException {
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host_email);
		props.put("mail.smtp.user", email_remitente);
		props.put("mail.smtp.clave", email_remitente_pass);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", port_email);
		Session session = Session.getDefaultInstance(props);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(email_remitente));
		message.addRecipients(Message.RecipientType.TO, email_destino[0]);
		message.setSubject(asunto);
		BodyPart messageBodyPart1 = new MimeBodyPart();
		messageBodyPart1.setText(mensaje);
		BodyPart messageBodyPart2 = new MimeBodyPart();
		DataSource src = new FileDataSource(anexo[0]);
		messageBodyPart2.setDataHandler(new DataHandler(src));
		messageBodyPart2.setFileName(anexo[0]);
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart1);
		multipart.addBodyPart(messageBodyPart2);
		message.setContent(multipart);
		Transport transport = session.getTransport("smtp");
		transport.connect(host_email, email_remitente, email_remitente_pass);
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
		//Borrem la foto. 
		borrarFoto(anexo[0]);
	}

	
	// *** POST *** //
	// Metodes de les peticions POST //
	/**
	 * Llegim la peticio POST que rebem de POSTMAN, afegir un nou delincuent extraent les dades i enviant el mail.  
	 * @param httpExchange
	 * @return String JSON de la peticio. 
	 * @throws MessagingException
	 * @throws IOException
	 */
	private String handlePostRequest(HttpExchange httpExchange) throws MessagingException, IOException {

		InputStream is = httpExchange.getRequestBody();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String line;
		//Llegim el JSON. 
		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Parseem el JSON per extrauer les dades y afegir nou delincuent.  
		JSONObject jObject = new JSONObject(sb.toString());
		ArrayList<String> document = new ArrayList<>();
		document.add(jObject.getString("alias"));
		document.add(jObject.getString("nombreCompleto"));
		document.add(jObject.getString("fechaNacimiento"));
		document.add(jObject.getString("nacionalidad"));
		document.add(jObject.getString("foto"));
		addDelincuente(document);
		try {
			//NOTA!!: Al enviar la petició al POSTMAN, aquest es quedará procesant la petición perque 
			//tindrem que introduir les dades del mail. Una vegada el mail senvie la peticio terminara. 
			prepararMail(document);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * Envia la peticio. 
	 * @param httpExchange
	 * @param requestParamValue
	 */
	private void handlePOSTResponse(HttpExchange httpExchange, String requestParamValue) {
		System.out.println(requestParamValue);
		OutputStream outputStream = httpExchange.getResponseBody();
		try {
			httpExchange.sendResponseHeaders(204, -1);
			String request = outputStream.toString();
			System.out.println("Insercion realizada correctamente");
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
