public class TransaccionService {
    private final OracleRepositorio repositorio = new OracleRepositorio();
    private final SmsGateway sms = new SmsGateway();
    private final ValidadorTransaccion validador = new ValidadorTransaccion();
    private final CalculadoraComision calculadora = new CalculadoraComision();
    private final ImpresoraComprobante comprobante = new ImpresoraComprobante();
    private final Auditoria auditoria = new Auditoria();
    
    public void transferir(Cuenta origen, Cuenta destino, double monto, String tipo) {
        // 1. Validación
        validador.validar(monto);

        // 2. Cálculo de la comisión
        double comision = calculadora.calcular(monto, tipo);

        // 3. Movimiento del dinero
        origen.retirar(monto + comision);
        destino.depositar(monto);

        // 4. Persistencia
        repositorio.guardarTransaccion(origen.getNumero(), destino.getNumero(), monto, comision);

        // 5. Comprobante
        comprobante.imprimir(origen, destino, monto, comision);

        // 6. Notificación
        sms.enviar(origen.getTitular(), "Transferiste $" + monto + " a la cuenta " + destino.getNumero());

        // 7. Auditoría
        auditoria.registrar(tipo, origen, destino, monto);
    }
}
