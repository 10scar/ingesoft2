public interface RegistroTransaccion {
    default void registrar(String tipo, Cuenta origen, Cuenta destino, double monto) {
        registrar(tipo, origen, destino.getNumero(), monto);
    }

    void registrar(String tipo, Cuenta origen, String destino, double monto);
}
