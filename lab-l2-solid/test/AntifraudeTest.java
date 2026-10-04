import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AntifraudeTest {
    private final PrintStream salidaOriginal = System.out;
    private final ByteArrayOutputStream consola = new ByteArrayOutputStream();
    private CuentaTransaccional destino;
    private TransaccionService transaccionService;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(consola));
        destino = new CuentaAhorros("124", "Pedro", 100000);
        RegistroTransaccion registro = new RegistroCompuesto(List.of(new Auditoria(), new SistemaAntifraude()));
        transaccionService = new TransaccionService(new ValidadorTransaccion(), new CalculadoraComision(),
                new RepositorioEnMemoria(), new ImpresoraComprobante(), new NotificadorFalso(), registro);
    }

    @AfterEach
    public void tearDown() {
        System.setOut(salidaOriginal);
    }

    private long lineasQueEmpiezanCon(String prefijo) {
        return consola.toString().lines().filter(linea -> linea.startsWith(prefijo)).count();
    }

    @Test
    public void transferenciaExitosaGeneraAuditoriaYAntifraude() {
        CuentaTransaccional origen = new CuentaAhorros("123", "Juan", 100000);

        transaccionService.transferir(origen, destino, 1000, new MismoBanco());

        assertEquals(1, lineasQueEmpiezanCon("[AUDITORIA]"));
        assertEquals(1, lineasQueEmpiezanCon("[ANTIFRAUDE]"));
    }

    @Test
    public void transferenciaRechazadaNoGeneraAuditoriaNiAntifraude() {
        CuentaTransaccional sinSaldo = new CuentaAhorros("125", "Ana", 0);

        assertThrows(IllegalStateException.class,
                () -> transaccionService.transferir(sinSaldo, destino, 1000, new MismoBanco()));

        assertEquals(0, lineasQueEmpiezanCon("[AUDITORIA]"));
        assertEquals(0, lineasQueEmpiezanCon("[ANTIFRAUDE]"));
    }
}
