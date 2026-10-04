import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostgresRepositorioTest {
    private final PrintStream salidaOriginal = System.out;
    private final ByteArrayOutputStream consola = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(consola));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(salidaOriginal);
    }

    private long lineasQueEmpiezanCon(String prefijo) {
        return consola.toString().lines().filter(linea -> linea.startsWith(prefijo)).count();
    }

    @Test
    public void transferenciaSeGuardaEnPostgres() {
        CuentaTransaccional origen = new CuentaAhorros("123", "Juan", 100000);
        CuentaTransaccional destino = new CuentaAhorros("124", "Pedro", 100000);
        TransaccionService transaccionService = new TransaccionService(new ValidadorTransaccion(), new CalculadoraComision(),
                new PostgresRepositorio(), new ImpresoraComprobante(), new NotificadorFalso(), new Auditoria());

        transaccionService.transferir(origen, destino, 1000, new MismoBanco());

        assertEquals(1, lineasQueEmpiezanCon("[POSTGRES] INSERT INTO transacciones VALUES ('123', '124', 1000.0, 0.0)"));
        assertEquals(0, lineasQueEmpiezanCon("[ORACLE]"));
    }
}
