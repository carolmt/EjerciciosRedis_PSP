import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// hace funcion de subscriber
public class CarSimulator {
    static int velocidadAleatoria = (int) (Math.random() * 140) + 60;
    static int codigoAscii = (int)Math.floor(Math.random()*(90 - 65)+65);
    static int numerosMatricula = (int) (Math.random() * 9999) + 1000;
    static String matricula = String.valueOf(codigoAscii + numerosMatricula);

    public CarSimulator(int velocidadAleatoria, String matricula){
        CarSimulator.velocidadAleatoria = velocidadAleatoria;
        CarSimulator.matricula = matricula;
    }

    public static int getVelocidadAleatoria() {
        return velocidadAleatoria;
    }

    public static void setVelocidadAleatoria(int velocidadAleatoria) {
        CarSimulator.velocidadAleatoria = velocidadAleatoria;
    }

    public static int getCodigoAscii() {
        return codigoAscii;
    }

    public static void setCodigoAscii(int codigoAscii) {
        CarSimulator.codigoAscii = codigoAscii;
    }

    public static int getNumerosMatricula() {
        return numerosMatricula;
    }

    public static void setNumerosMatricula(int numerosMatricula) {
        CarSimulator.numerosMatricula = numerosMatricula;
    }

    public static String getMatricula() {
        return matricula;
    }

    public static void setMatricula(String matricula) {
        CarSimulator.matricula = matricula;
    }


    public static void main(String[] args) throws MqttException {

        CarSimulator car = new CarSimulator(velocidadAleatoria, matricula);

        String broker = "tcp://192.168.56.1:1883";

        int id = 1;
        String topic = "/VEHICULOS/velocidad/"+getMatricula();
        String publisherId = "CarSimulator"+id;

        IMqttClient publisher = null;
        try {
            publisher = new MqttClient(broker,publisherId);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        try {
            publisher.connect(options);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

        Scanner reader = new Scanner(System.in);
        String message = String.valueOf(getVelocidadAleatoria());

        try {
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            Future<Boolean> result = executorService.submit(new MQTTPublisher(publisher, message, topic));

            if (result.get()) {
                System.out.printf("MQTT velocidad enviada.");
            } else {
                System.out.printf("MQTT velocidad no enviada.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            publisher.disconnect();
            publisher.close();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

    }
}
