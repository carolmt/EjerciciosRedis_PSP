import redis.clients.jedis.Jedis;

public class Cliente {
    public static void main(String[] args) {
        /*e pedirá al usuario uno de los
siguientes 3 comandos:
- “shorten URL”, donde URL es la dirección a acortar. Al realizarlo el programa
escribirá en un listado de Redis la URL a acortar para que el otro servicio la
tramite. La clave de este listado será “DAVID:URLS_TO_SHORT”, cada uno
con su nombre.
- “exit”, saldrá de la aplicación.
- url “SHORTEDURL”, donde SHORTEDURL es la dirección acortada. Al
realizarlo el programa buscará la dirección URL original en una tabla HASH
en Redis y la imprimirá. La clave de la tabla HASH será:
“DAVID:SHORTED_URLS”.
*/
        Jedis jedis = new Jedis("192.168.1.203", 6379 );
        jedis.set("foo", "bar");
        String value = jedis.get("foo");
        System.out.println(value);
    }
}
