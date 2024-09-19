package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.HttpURLConnection;

public class ServiceFacade {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8081)) {
            System.out.println("Fachada de servicios escuchando en el puerto 8081...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String line = in.readLine();

                    if (line == null || !line.startsWith("GET")) {
                        out.println("HTTP/1.1 400 Bad Request\r\n");
                        continue;
                    }

                    if (line.contains("/calculadora")) {
                        // Servir el cliente web
                        String html = "<html><body><h1>Calculadora Reflex</h1>" +
                                "<p>Ingrese el comando en el siguiente formato: max(5.0,7.2) o bbl(3.1,2,9.3)</p>" +
                                "<input id='comando' type='text' /><button onclick='compute()'>Calcular</button>" +
                                "<p>Resultado: <span id='resultado'></span></p>" +
                                "<script>function compute() {" +
                                "let comando = document.getElementById('comando').value;" +
                                "fetch('/computar?comando=' + comando)" +
                                ".then(response => response.json())" +
                                ".then(data => document.getElementById('resultado').innerText = data.resultado);" +
                                "}</script></body></html>";

                        out.println("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n" + html);
                    } else if (line.contains("/computar")) {
                        // Extraer el comando de la URL
                        String comando = line.split(" ")[1].split("=")[1];
                        comando = java.net.URLDecoder.decode(comando, "UTF-8");

                        // Hacer la solicitud al servicio de calculadora
                        String resultado = hacerSolicitudCalculadora(comando);

                        // Enviar la respuesta en formato JSON
                        out.println("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + resultado);
                    } else {
                        out.println("HTTP/1.1 404 Not Found\r\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para hacer la solicitud al servicio de calculadora
    private static String hacerSolicitudCalculadora(String comando) {
        try {
            URL url = new URL("http://localhost:8082/compreflex=" + comando);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
           connection.disconnect();

            return content.toString();
        } catch (Exception e) {
            return "{\"resultado\": \"Error: " + e.getMessage() + "\"}";
        }
    }
}
