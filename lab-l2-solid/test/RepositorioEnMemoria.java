import java.util.List;
import java.util.ArrayList;

public class RepositorioEnMemoria implements RepositorioTransacciones {
    public final List<String> transacciones = new ArrayList<>();

    @Override
    public void guardarTransaccion(String origen, String destino, double monto, double comision) {
        transacciones.add(origen + " -> " + destino + " " + monto + " " + comision);
    }

}