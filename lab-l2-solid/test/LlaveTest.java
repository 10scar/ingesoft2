import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LlaveTest {
    @Test
    public void transferenciaPorLlaveNoCobraComision() {
        CuentaTransaccional origen = new CuentaAhorros("123", "Juan", 100000);
        CuentaTransaccional destino = new CuentaAhorros("124", "Pedro", 100000);
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        TransaccionService transaccionService = new TransaccionService(repositorio, new NotificadorFalso());

        transaccionService.transferir(origen, destino, 50000, new Llave());

        assertEquals("123 -> 124 50000.0 0.0", repositorio.transacciones.get(0));
        assertEquals(50000, origen.getSaldo());
        assertEquals(150000, destino.getSaldo());
    }
}
