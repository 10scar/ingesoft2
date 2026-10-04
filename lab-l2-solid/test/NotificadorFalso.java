import java.util.List;
import java.util.ArrayList;

public class NotificadorFalso implements Notificador {
    public final List<String> mensajes = new ArrayList<>();

    @Override
    public void enviar(String destino, String mensaje) {
        mensajes.add(destino + " " + mensaje);
    }
}