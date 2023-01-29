package es.florida.psp.ae3;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetSocketAddress;import java.security.KeyStore.TrustedCertificateEntry;
import java.security.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpServer;

public class ServidorAE3 {
	
	private static void logIn(String ip) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("log.txt",true));
		LocalDateTime fecha = LocalDateTime.now();
		DateTimeFormatter fechaLog = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		String fechaFormateada = fecha.format(fechaLog);
		FileReader fr = new FileReader("log.txt");
		BufferedReader br = new BufferedReader(fr); 
		bw.write("IP: " + ip + " - "+" Hora de conexión: "+ fechaFormateada);	
		bw.newLine();
		bw.flush();	
		br.close();
	}

	public static void main(String[] args) throws IOException {
		
		//hacemos conexion y escribimos entrada en log.txt con ip y timestamp
		try {
			FileReader fr = new FileReader("config.txt");
			BufferedReader br = new BufferedReader(fr);
			String host = br.readLine();
			int puerto = Integer.parseInt(br.readLine());
			br.close();
			InetSocketAddress direccionTCPIP = new InetSocketAddress(host, puerto);
			int backlog = 0;
			HttpServer servidor = HttpServer.create(direccionTCPIP, backlog);
			GestorHTTPAE3 gestorHTTP = new GestorHTTPAE3();
			String rutaRespuesta = "/servidor";
			servidor.createContext(rutaRespuesta, gestorHTTP);

			ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
			servidor.setExecutor(threadPoolExecutor);
			servidor.start();
			System.out.println("Servidor HTTP arranca en el puerto " + puerto);
			logIn(host);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
