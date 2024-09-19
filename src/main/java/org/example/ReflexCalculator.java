package org.example;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Arrays;

public class ReflexCalculator {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8082)) {
            System.out.println("Calculadora escuchando en el puerto 8082...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String line = in.readLine();

                    if (line == null || !line.startsWith("GET")) {
                        out.println("HTTP/1.1 400 Bad Request\r\n");
                        continue;
                    }

                    // Extraer el comando de la URL
                    try {
                        String comando = line.split(" ")[1].split("=")[1];
                        comando = URLDecoder.decode(comando, "UTF-8");
                        System.out.println("Comando recibido: " + comando);

                        // Calcular el resultado
                        String resultado = calcular(comando);

                        // Enviar la respuesta en formato JSON
                       out.println("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + resultado);
                    } catch (Exception e) {
                        out.println("HTTP/1.1 500 Internal Server Error\r\n\r\n{\"error\": \"Error en el procesamiento de la solicitud\"}");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para realizar el cálculo basado en reflexión
    public static String calcular(String comando) {
        try {
            // Verificar si el comando es "bbl" para bubble sort
            if (comando.startsWith("bbl")) {
                String[] params = comando.substring(comando.indexOf('(') + 1, comando.indexOf(')')).split(",");
                double[] numbers = Arrays.stream(params).mapToDouble(Double::parseDouble).toArray();

                // Implementación del algoritmo bubble sort
                bubbleSort(numbers);

                return "{\"resultado\": " + Arrays.toString(numbers) + "}";
            }

            // Si no es bbl, invocar un método de Math usando reflexión
            String methodName = comando.substring(0, comando.indexOf('('));
            String[] params = comando.substring(comando.indexOf('(') + 1, comando.indexOf(')')).split(",");

            double[] arguments = Arrays.stream(params).mapToDouble(Double::parseDouble).toArray();

            // Obtener el método de la clase Math con los parámetros correctos
            if (arguments.length == 1) {
                Method method = Math.class.getMethod(methodName, double.class);
                Object result = method.invoke(null, arguments[0]);
                return "{\"resultado\": " + result.toString() + "}";
            } else if (arguments.length == 2) {
                Method method = Math.class.getMethod(methodName, double.class, double.class);
                Object result = method.invoke(null, arguments[0], arguments[1]);
                return "{\"resultado\": " + result.toString() + "}";
            } else {
                return "{\"resultado\": \"Error: número incorrecto de parámetros\"}";
            }

        } catch (Exception e) {
            return "{\"resultado\": \"Error: " + e.getMessage() + "\"}";
        }
    }

    // Algoritmo bubble sort
    public static void bubbleSort(double[] array) {
        int n = array.length;
        boolean swapped;

        // Bucle para recorrer todo el array
        for (int i = 0; i < n - 1; i++) {
            swapped = false;

            // Bucle interno para comparar e intercambiar elementos
            for (int j = 0; j < n - i - 1; j++) {
                // Si el elemento actual es mayor que el siguiente, los intercambiamos
                if (array[j] > array[j + 1]) {
                    double temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    swapped = true;  // Marcamos que hubo un intercambio
                }
            }

            // Si no se hizo ningún intercambio, el array ya está ordenado
            if (!swapped) {
                break;
            }
        }
    }
}
