import java.util.List;

public class NotificadorCompuesto implements Notificador {
    private final List<Notificador> notificadores;

    public NotificadorCompuesto(List<Notificador> notificadores) {
        this.notificadores = List.copyOf(notificadores);
    }

    @Override
    public void enviar(String destinatario, String mensaje) {
        for (Notificador notificador : notificadores) {
            notificador.enviar(destinatario, mensaje);
        }
    }
}
