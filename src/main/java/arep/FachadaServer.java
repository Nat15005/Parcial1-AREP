
package arep;

import java.net.*;
import java.io.*;

public class FachadaServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(35000);
        boolean running = true;
        while (running) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleRequest(clientSocket)).start();
        }
    }
    private static void handleRequest(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("GET /calculadora ")) {
                    String htmlResponse = getClienteHTML();
                    String httpResponse = "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: text/html\r\n"
                            + "Access-Control-Allow-Origin: *\r\n"
                            + "Content-Length: " + htmlResponse.length() + "\r\n"
                            + "\r\n"
                            + htmlResponse;
                    out.println(httpResponse);
                    break;
                }
                else if (inputLine.startsWith("GET /computar?comando=")) {
                    String comando = inputLine.split("=")[1].split(" ")[0];
                    String resultado = comunicacionBackend(comando);
                    String httpResponse = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/json\r\n" +
                            "Content-Length: " +
                            resultado.length() + "\r\n" +
                            "\r\n" +
                            resultado;
                    out.println(httpResponse);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static String getClienteHTML() {
        return "<!DOCTYPE html><html><head><title>Reflective Calculator</title><script>" +
                "function enviarComando() {" +
                "const comando = document.getElementById('comando').value;" +
                "const url = 'http://localhost:36000/compreflex?comando=' + comando;" +
                "fetch(url)" +
                ".then(response => {" +
                "    if (!response.ok) {" +
                "        throw new Error('Error en la solicitud');" +
                "    }" +
                "    return response.json();" +
                "})" +
                ".then(data => {" +
                "    document.getElementById('respuesta').innerHTML = JSON.stringify(data, null, 2);" +
                "})" +
                ".catch(error => {" +
                "    console.error('Error:', error);" +
                "    document.getElementById('respuesta').innerHTML = 'Error: ' + error.message;" +
                "});" +
                "}" +
                "</script></head><body><h1>Reflective Calculator</h1>" +
                "<input type='text' id='comando'>" +
                "<button onclick='enviarComando()'>Enviar</button>" +
                "<div id='respuesta'></div></body></html>";
    }
    private static String comunicacionBackend(String comando) {
        //Fallo al redirigir y no alcancé a corregir la implementacion
        return comando;
    }
}


