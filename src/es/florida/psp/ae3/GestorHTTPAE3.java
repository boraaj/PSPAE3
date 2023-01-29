package es.florida.psp.ae3;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
public class GestorHTTPAE3 implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		
		//Peticiones GET 
		 	//  /servidor/mostrarTodos mostrara todos los alias de delincuentes. 
			// /servidor/mostrarUno?alias=XXX mostrar toda la info de ese delincuente si existe. 
		
		
		
		
		
		
		
	}

}
