
package arep;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class BackendServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(36000);
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
                if (inputLine.startsWith("GET /compreflex?comando=")) {
                    String comando = inputLine.split("=")[1].split(" ")[0];
                    String resultado = procesarComando(comando);
                    out.println(createHttpResponse(resultado));
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String procesarComando(String comando) {
        try {
            String[] parts = comando.split("\\(");
            String operation = parts[0];
            String paramsString = parts[1].replace(")", "");
            switch (operation) {
                case "bbl":
                    List<Double> numbers = parseNumbers(paramsString);
                    return createSuccessResponse(bubbleSort(numbers));
                default:
                    return invokeMathMethod(operation, paramsString);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"data\": \"" + e.getMessage() + "\"}";
        }
    }

    private static String invokeMathMethod(String methodName, String paramsString) throws Exception {
        Class<?> mathClass = Math.class;
        String[] paramsArray = paramsString.split(",");
        Class<?>[] paramTypes = new Class<?>[paramsArray.length];
        Object[] args = new Object[paramsArray.length];

        for (int i = 0; i < paramsArray.length; i++) {
            String param = paramsArray[i].trim();
            paramTypes[i] = double.class;
            args[i] = Double.parseDouble(param);
        }
        Method method = mathClass.getMethod(methodName, paramTypes);
        Object result = method.invoke(null, args);
        return createSuccessResponse(result);
    }

    private static List<Double> parseNumbers(String paramsString) {
        List<Double> numbers = new ArrayList<>();
        String[] paramsArray = paramsString.split(",");
        for (String param : paramsArray) {
            numbers.add(Double.parseDouble(param.trim()));
        }
        return numbers;
    }
    private static List<Double> bubbleSort(List<Double> numbers) {
        for (int i = numbers.size()-1; i > 0; i--){
            for (int j = 0; j < numbers.size()-1; j++){
                if (numbers.get(j) > numbers.get(j+1)){
                    Double izq = numbers.get(j);
                    numbers.set(j, numbers.get(j+1));
                    numbers.set(j+1, izq);
                }
            }
        }
        return numbers;
    }
    private static String createHttpResponse(String body) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Content-Length: " + body.length() + "\r\n" +
                "\r\n" +
                body;
    }
    private static String createSuccessResponse(Object data) {
        return "{\"data\": \"" + data + "\"}";
    }

}
