public class SistemaAntifraude implements RegistroTransaccion {
    @Override
    public void registrar(String tipo, Cuenta origen, Cuenta destino, double monto) {
        System.out.println("[ANTIFRAUDE] Transacción enviada a revisión: " + tipo + " " + origen.getNumero() + " -> " + destino.getNumero() + " $" + monto);
    }
}
