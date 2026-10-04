public class ImpresoraComprobante implements Comprobante {
    @Override
    public void imprimir(Cuenta origen, String destino, double monto, double comision) {
        System.out.println("===== BANCO ANDINO - COMPROBANTE =====");
        System.out.println("Origen: " + origen.getNumero());
        System.out.println("Destino: " + destino);
        System.out.println("Monto: $" + monto);
        System.out.println("Comisión: $" + comision);
        System.out.println("======================================");
    }
}
