public class PushGateway implements Notificador {
    @Override
    public void enviar(String destinatario, String mensaje) {
        System.out.println("[PUSH] Para " + destinatario + ": " + mensaje);
    }
}
