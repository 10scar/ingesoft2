import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CuentaInfantilTest {
    @Test
    public void retiroQueSuperaElLimiteDiarioSeRechazaSinCambiarSaldo() {
        CuentaInfantil cuenta = new CuentaInfantil("INF-1", "Sofía", 500000);
        cuenta.retirar(150000);

        assertThrows(IllegalStateException.class, () -> cuenta.retirar(60000));
        assertEquals(350000, cuenta.getSaldo());
    }

    @Test
    public void retiroDentroDelLimiteDiarioSePermite() {
        CuentaInfantil cuenta = new CuentaInfantil("INF-1", "Sofía", 500000);
        cuenta.retirar(150000);
        cuenta.retirar(50000);

        assertEquals(300000, cuenta.getSaldo());
    }

    @Test
    public void recibeDepositosSinLimite() {
        CuentaInfantil cuenta = new CuentaInfantil("INF-1", "Sofía", 0);
        cuenta.depositar(10000000);

        assertEquals(10000000, cuenta.getSaldo());
    }

    @Test
    public void sePuedeUsarComoOrigenDeTransferencias() {
        CuentaInfantil origen = new CuentaInfantil("INF-1", "Sofía", 500000);
        CuentaTransaccional destino = new CuentaAhorros("124", "Pedro", 100000);
        RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
        TransaccionService transaccionService = new TransaccionService(repositorio, new NotificadorFalso());

        transaccionService.transferir(origen, destino, 100000, new MismoBanco());

        assertEquals("INF-1 -> 124 100000.0 0.0", repositorio.transacciones.get(0));
        assertEquals(400000, origen.getSaldo());
        assertEquals(200000, destino.getSaldo());
    }

    @Test
    public void seLeCobraLaCuotaDeManejo() {
        CuentaInfantil cuenta = new CuentaInfantil("INF-1", "Sofía", 500000);

        new CobroCuotaManejo().cobrarMensual(List.of(cuenta));

        assertEquals(487100, cuenta.getSaldo());
    }
}
