public interface RegistroTransaccion {
    void registrar(String tipo, Cuenta origen, Cuenta destino, double monto);
}
