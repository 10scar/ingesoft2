import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PagoServiciosTest {
    private final PrintStream salidaOriginal = System.out;
    private final ByteArrayOutputStream consola = new ByteArrayOutputStream();
    private RepositorioEnMemoria repositorio;
    private NotificadorFalso notificador;
    private TransaccionService transaccionService;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(consola));
        repositorio = new RepositorioEnMemoria();
        notificador = new NotificadorFalso();
        RegistroTransaccion registro = new RegistroCompuesto(List.of(new Auditoria(), new SistemaAntifraude()));
        transaccionService = new TransaccionService(new ValidadorTransaccion(), new CalculadoraComision(),
                repositorio, new ImpresoraComprobante(), notificador, registro);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(salidaOriginal);
    }

    private long lineasQueEmpiezanCon(String prefijo) {
        return consola.toString().lines().filter(linea -> linea.startsWith(prefijo)).count();
    }

    @Test
    public void criterioDeAceptacionDescuentaMontoMasComisionYGuardaReferenciaComoDestino() {
        CuentaTransaccional cuenta = new CuentaAhorros("001-1", "Ana", 200_000);
        String referenciaFactura = "FAC-AGUA-9876";

        transaccionService.pagarServicio(cuenta, referenciaFactura, 184_300);

        assertEquals(14_200, cuenta.getSaldo());

        assertEquals(1, repositorio.transacciones.size());
        assertEquals("001-1 -> FAC-AGUA-9876 184300.0 1500.0", repositorio.transacciones.get(0));

        String salida = consola.toString();
        assertTrue(salida.contains("Origen: 001-1"));
        assertTrue(salida.contains("Destino: FAC-AGUA-9876"));
        assertTrue(salida.contains("Monto: $184300.0"));
        assertTrue(salida.contains("Comisión: $1500.0"));

        assertEquals(1, notificador.mensajes.size());
        assertEquals("Ana Pagaste $184300.0 de la factura FAC-AGUA-9876", notificador.mensajes.get(0));
    }

    @Test
    public void pagoExitosoPasaPorAuditoriaYAntifraude() {
        CuentaTransaccional cuenta = new CuentaAhorros("001-1", "Ana", 200_000);

        transaccionService.pagarServicio(cuenta, "FAC-LUZ-123", 50_000);

        assertEquals(1, lineasQueEmpiezanCon("[AUDITORIA]"));
        assertEquals(1, lineasQueEmpiezanCon("[ANTIFRAUDE]"));
        assertTrue(consola.toString().contains("SERVICIO_PUBLICO"));
    }

    @Test
    public void saldoInsuficienteRechazaPagoSinGuardarNiNotificarNiAuditar() {
        CuentaTransaccional cuenta = new CuentaAhorros("001-1", "Ana", 100_000);

        assertThrows(IllegalStateException.class, () -> {
            transaccionService.pagarServicio(cuenta, "FAC-GAS-456", 184_300);
        });

        assertEquals(100_000, cuenta.getSaldo());
        assertEquals(0, repositorio.transacciones.size());
        assertEquals(0, notificador.mensajes.size());
        assertEquals(0, lineasQueEmpiezanCon("[AUDITORIA]"));
        assertEquals(0, lineasQueEmpiezanCon("[ANTIFRAUDE]"));
    }

    @Test
    public void validacionesDeMontoAplicanAlPagoDeServicios() {
        CuentaTransaccional cuenta = new CuentaAhorros("001-1", "Ana", 10_000_000);

        assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.pagarServicio(cuenta, "FAC-INTERNET-789", 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.pagarServicio(cuenta, "FAC-INTERNET-789", -10_000);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.pagarServicio(cuenta, "FAC-INTERNET-789", 5_000_001);
        });

        assertEquals(10_000_000, cuenta.getSaldo());
        assertEquals(0, repositorio.transacciones.size());
    }

    @Test
    public void cdtNoPermitePagarServiciosPorSeguridadDeTiposLSP() {

        assertFalse(CuentaTransaccional.class.isAssignableFrom(CDT.class));
        assertTrue(Cuenta.class.isAssignableFrom(CDT.class));
    }
}
