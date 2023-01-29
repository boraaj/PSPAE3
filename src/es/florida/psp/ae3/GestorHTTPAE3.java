package es.florida.psp.ae3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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

public class GestorHTTPAE3 implements HttpHandler {
	static MongoClient mongoClient = null;
	static MongoDatabase database = null;
	static MongoCollection<Document> coleccion = null;

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		obrirConexio();
		String requestParamValue = null;
		if ("GET".equals(httpExchange.getRequestMethod())) {
			requestParamValue = handleGetRequest(httpExchange);
			handleGETResponse(httpExchange, requestParamValue);
		} else if ("POST".equals(httpExchange.getRequestMethod())) {
			requestParamValue = handlePostRequest(httpExchange);
			handlePOSTResponse(httpExchange, requestParamValue);
		}
		tancarConexio();
	}

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

	private String handleGetRequest(HttpExchange httpExchange) {

		String web = "";
		String tipo = httpExchange.getRequestURI().toString();
		ArrayList<String> aliasList = new ArrayList<String>();
		System.out.println(tipo.contains("mostrarTodos"));
		System.out.println(tipo.contains("mostrarUno"));
		if (tipo.contains("mostrarTodos")) {
			MongoCursor<Document> cursor = coleccion.find().iterator();
			while (cursor.hasNext()) {
				JSONObject obj = new JSONObject(cursor.next().toJson());
				aliasList.add(obj.getString("alias"));
			}
			web = webTodos(aliasList);
			System.out.println(">>>>"+web);
			return web;
		} else if (tipo.contains("mostrarUno")) {
			if (httpExchange.getRequestURI().toString().split("\\?")[1].contains("alias")) {
				System.out.println("HOLA");
				ArrayList<String> datos = new ArrayList<String>();
				String alias = httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];
				System.out.println(alias);
				Bson query = eq("alias", alias);
				MongoCursor<Document> cursorMostrarUno = coleccion.find(query).iterator();
				while (cursorMostrarUno.hasNext()) {
					JSONObject obj = new JSONObject(cursorMostrarUno.next().toJson());
					datos.add(obj.getString("alias"));
					datos.add(obj.getString("nombreCompleto"));
					datos.add(obj.getString("fechaNacimiento"));
					datos.add(obj.getString("nacionalidad"));
					web = webUno(datos);
				}
			} else {
				web404();
			}

		} else {
			web404();
		}
		// String valor =
		// httpExchange.getRequestURI().toString().split("\\?")[1].split("=")[1];

		// System.out.println(tipo);

		return web;
	}

	private String web404() {

		String web = "";
		web = "<html><body> Error de Busqueda, intentalo de nuevo con otra direccion. </body></html>";

		return web;
	}

	private String webTodos(ArrayList<String> datos) {

		String web = "";
		web = "<html><body>  <h1>Lista de delincuentes:</h1>";
		for (int i = 0; i < datos.size(); i++) {
			web += datos.get(i) + "<br>";
		}
		web += "<p>localhost:7777/servidor/mostrarTodo para mostrar la lista de delincuentes.<br>";
		web += "localhost:7777/servidor/mostrarUno?=(alias) para mostrar información del delincuente por su alias.</p>";
		web += "</body></html>";

		return web;
	}

	private String webUno(ArrayList<String> datos) {

		String web = "";
		web = "<html><body><h1>Datos del Delincuente:</h1>";
		web += "<b>Alias</b>: " + datos.get(0) + "<br>";
		web += "<b>Nombre Completo</b>: " + datos.get(1) + "<br>";
		web += "<b>Fecha Nacimiento</b>: " + datos.get(2) + "<br>";
		web += "<b>Nacionalidad</b>: " + datos.get(3) + "<br>";
		web += "<p>servidor/mostrarTodo para mostrar la lista de delincuentes.<br>";
		web += "servidor/mostrarUno para mostrar el buscado.</p>";
		web += "</body></html>";

		return web;
	}

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

	private String handlePostRequest(HttpExchange httpExchange) {
		InputStream inputStream = httpExchange.getRequestBody();

		String postRequest = null;
		return postRequest;
	}

	private void handlePOSTResponse(HttpExchange httpExchange, String requestParamValue) {
		OutputStream outputStream = httpExchange.getResponseBody();
		String htmlResponse = "Respuesta a la petición POST";
		try {
			httpExchange.sendResponseHeaders(200, htmlResponse.length());
			outputStream.write(htmlResponse.getBytes());
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
