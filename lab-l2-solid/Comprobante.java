public interface Comprobante {
    default void imprimir(Cuenta origen, Cuenta destino, double monto, double comision) {
        imprimir(origen, destino.getNumero(), monto, comision);
    }

    void imprimir(Cuenta origen, String destino, double monto, double comision);
}
