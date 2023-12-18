import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*- Cada vez que haya una clave del estilo “EXCESO:*” lee la clave, lee el valor de
velocidad y envía una multa por MQTT. Una vez realizado el envío borra la clave de
Redis correspondiente (para no multar dos veces) y añade la matrícula a un grupo
de redis llamado “VEHICULOSDENUNCIADOS”.
- Cada segundo aproximadamente mostrará por pantalla el número total de vehículos
y el porcentaje de vehículos que han sido multados.
*/
public class PoliceStation {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Jedis jedis = new Jedis("127.0.0.1", 6379);
        RedisManager redisManager = new RedisManager();
        List<String> excedidos = new ArrayList<>();
        List<String> matriculasDenunciadas = new ArrayList<>();

        Scanner reader = new Scanner(System.in);
        String publisherId = "policeStation";
        IMqttClient publisher = null;
        String broker = "tcp://192.168.56.1:1883";

        try {
            publisher = new MqttClient(broker, publisherId);
        } catch (
                MqttException e) {
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

        excedidos = redisManager.readKeys(jedis, "EXCESO:*");
        matriculasDenunciadas = redisManager.readKeys(jedis, "EXCESO:*");
        String multa;

        for(String claveRedis : matriculasDenunciadas) {
            String topic = "/VEHICULOS/velocidad/" + claveRedis.substring("EXCESO:".length());

            for (String excedido : excedidos) {
                int velocidad = Integer.parseInt(excedido);

                if ((velocidad >= 80) && (velocidad <= 96)) {
                    multa = "100€";
                    enviarMulta(publisher, multa, topic);
                }

                else if ((velocidad >= 97) && (velocidad <= 104)) {
                    multa = "200€";
                    enviarMulta(publisher, multa, topic);
                }
                else {
                    multa = "500€";
                    enviarMulta(publisher, multa, topic);
                }
            }

        }

        try {
            publisher.disconnect();
            publisher.close();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

    }
    public static void enviarMulta(IMqttClient publisher, String multa, String topic) throws ExecutionException, InterruptedException {
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            Future<Boolean> result = executorService.submit(new MQTTPublisher(publisher, multa, topic));

            if (result.get()) {
                System.out.print("MQTT multa enviada.");
            } else {
                System.out.print("MQTT multa no enviada.");
            }
    }
}

