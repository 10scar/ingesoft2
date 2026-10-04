import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TransaccionServiceTest {
    private CuentaTransaccional origen;
    private CuentaTransaccional destino;
    private RepositorioEnMemoria repositorio;
    private NotificadorFalso notificador;
    private TransaccionService transaccionService;

    @BeforeEach
    public void setUp() {
        origen = new CuentaAhorros("123", "Juan", 100000);
        destino = new CuentaAhorros("124", "Pedro", 100000);
        repositorio = new RepositorioEnMemoria();
        notificador = new NotificadorFalso();
        transaccionService = new TransaccionService(new ValidadorTransaccion(), new CalculadoraComision(),
                repositorio, new ImpresoraComprobante(), notificador, new Auditoria());
    }
    @Test
    public void transferenciaMismoBancoNoCobraComision() {
        transaccionService.transferir(origen, destino, 1000, new MismoBanco());
        assertEquals("123 -> 124 1000.0 0.0", repositorio.transacciones.get(0));
        assertEquals(99000, origen.getSaldo());
        assertEquals(101000, destino.getSaldo());
    }
    @Test
    public void transferenciaOtroBancoCobra7500DeComision() {
        transaccionService.transferir(origen, destino, 1000, new OtroBanco());
        assertEquals("123 -> 124 1000.0 7500.0", repositorio.transacciones.get(0));
        assertEquals(91500, origen.getSaldo());
        assertEquals(101000, destino.getSaldo());
    }
    @Test
    public void saldoInsuficienteRechazaSinGuardarNiNotificar() {
        origen = new CuentaAhorros("123", "Juan", 0);
        assertThrows(IllegalStateException.class, () -> { 
            transaccionService.transferir(origen, destino, 1000, new OtroBanco());
        });
        assertEquals(0, repositorio.transacciones.size());
        assertEquals(0, notificador.mensajes.size());
    }
    @Test
    public void transferenciaExitosaSeGuardaYNotificaUnaSolaVez() {
        transaccionService.transferir(origen, destino, 1000, new OtroBanco());
        assertEquals(1, repositorio.transacciones.size());
        assertEquals(1, notificador.mensajes.size());
    }
    @Test
    public void tipoDesconocidoSeRechazaSinCambiarSaldo() {
        assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.transferir(origen, destino, 1000, null);
        });
        assertEquals(100000, origen.getSaldo());
        assertEquals(100000, destino.getSaldo());
        assertEquals(0, repositorio.transacciones.size());
        assertEquals(0, notificador.mensajes.size());
    }
}