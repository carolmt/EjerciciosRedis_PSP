import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedisManager {

    public RedisManager() {
    }

    public static void createKeyValue(Jedis jedis, String clave, String valor) {
        jedis.set(clave, valor);
        System.out.println("Se ha guardado en Redis la clave: " + clave + " y el valor: " + valor);
    }
    public static void redisList(Jedis jedis, String clave, String valor) {
        jedis.rpush(clave, valor);
        System.out.printf("Se ha guardado en Redis la clave: %s y el valor: %s\n", clave, valor);
    }
    public List<String> devolverListaVehiculos(Jedis jedis, String clave) {
        List<String> values = jedis.lrange(clave, 0, -1);
        return values;
    }
    public static void deleteKey(Jedis jedis, String clave) {
        jedis.del(clave);
        System.out.println("Se ha borrado la clave: " + clave);
    }
    public List<String> readKeys(Jedis jedis, String inicioClave) {
        Set<String> keys = jedis.keys(inicioClave);
        List<String> valores = new ArrayList<>();

        for (String key : keys) {
            System.out.println("Se ha leído la clave: " + key + " y el valor es: " + jedis.get(key));
            valores.add(jedis.get(key));
        }
        return valores;
    }
    public List<String> devolverKeys(Jedis jedis, String inicioClave) {
        Set<String> keys = jedis.keys(inicioClave);
        List<String> claves = new ArrayList<>();

        for (String key : keys) {
            System.out.println("Se ha leído la clave: " + key);
            claves.add(key);
        }
        return claves;
    }
}
