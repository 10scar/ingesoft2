import java.util.List;

public class RegistroCompuesto implements RegistroTransaccion {
    private final List<RegistroTransaccion> registros;

    public RegistroCompuesto(List<RegistroTransaccion> registros) {
        this.registros = List.copyOf(registros);
    }

    @Override
    public void registrar(String tipo, Cuenta origen, String destino, double monto) {
        for (RegistroTransaccion registro : registros) {
            registro.registrar(tipo, origen, destino, monto);
        }
    }
}
