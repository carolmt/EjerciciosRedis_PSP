    import org.eclipse.paho.client.mqttv3.*;
    import redis.clients.jedis.Jedis;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Scanner;

    /*La aplicación Radar.java lee los mensajes MQTT con las velocidades. El límite de velocidad
    es 80, cada vez que alguien supera esa velocidad se crea una entrada en Redis tipo cadena
    cuya clave es del estilo “EXCESO:80:1234ABC” y su valor la velocidad. Cada vez que
    alguien no supera la velocidad se añade a un grupo de Redis para saber cuánta gente ha
    pasado por el radar y sacar estadísticas, el grupo se llamará “VEHICULOS”.
    */
    public class Radar {

        public static void main(String[] args) throws MqttException {
            List<String>vehiculos;

            Jedis jedis = new Jedis("127.0.0.1.203", 6379);
            RedisManager redisManager = new RedisManager();

            Scanner reader = new Scanner(System.in);
            String publisherId = "radar";
            IMqttClient publisher = null;
            String broker = "tcp://192.168.56.1:1883";

            try {
                publisher = new MqttClient(broker, publisherId);
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

            publisher.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    int velocidad = Integer.parseInt(new String(message.getPayload()));

                    System.out.println("\n¡Velocidad recivida!" +
                            "\n\tHora:    " + LocalDateTime.now() +
                            "\n\tTopic:   " + topic +
                            "\n\tVelocidad: " + velocidad +
                            "\n\tQoS:     " + message.getQos() + "\n");

                    // Verificar si la velocidad es mayor a 80
                    if (velocidad > 80) {
                        String clave = "EXCESO:80:" + CarSimulator.getMatricula();
                        String valor = String.valueOf(velocidad);

                        RedisManager.createKeyValue(jedis, clave, valor);
                        System.out.println("Se ha guardado en Redis la clave: " + clave + " y el valor: " + valor);
                    } else {
                        String clave = "VEHICULOS";
                        String valor = String.valueOf(velocidad);

                        RedisManager.redisList(jedis, clave, valor);
                        System.out.println("Se ha guardado en Redis la clave: " + clave + " y el valor: " + valor);
                    }
                }

                public void connectionLost(Throwable cause) {
                    System.out.println("¡Conexión con el broker de Solace perdida!" + cause.getMessage());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                }

            });

            vehiculos = redisManager.devolverListaVehiculos(jedis, "VEHICULOS");

            try {
                publisher.subscribe("/VEHICULOS/velocidad/"+CarSimulator.getMatricula(), 0);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }

        }
    }