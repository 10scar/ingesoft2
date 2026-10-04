public class TransaccionService {
    private final Validador validador;
    private final Calculadora calculadora;
    private final RepositorioTransacciones repositorio;
    private final Comprobante comprobante;
    private final Notificador notificador;
    private final RegistroTransaccion registro;

    public TransaccionService(Validador validador, Calculadora calculadora, RepositorioTransacciones repositorio,
            Comprobante comprobante, Notificador notificador, RegistroTransaccion registro) {
        this.validador = validador;
        this.calculadora = calculadora;
        this.repositorio = repositorio;
        this.comprobante = comprobante;
        this.notificador = notificador;
        this.registro = registro;
    }
    
    public void transferir(CuentaTransaccional origen, Cuenta destino, double monto, TipoTransaccion tipo) {
        ejecutarTransaccion(origen, destino.getNumero(), monto, tipo, 
                () -> destino.depositar(monto),
                "Transferiste $" + monto + " a la cuenta " + destino.getNumero());
    }

    public void pagarServicio(CuentaTransaccional origen, String referenciaFactura, double monto) {
        TipoTransaccion tipo = new ServicioPublico();
        ejecutarTransaccion(origen, referenciaFactura, monto, tipo, 
                () -> {},
                "Pagaste $" + monto + " de la factura " + referenciaFactura);
    }

    private void ejecutarTransaccion(CuentaTransaccional origen, String destino, double monto,
                                     TipoTransaccion tipo, Runnable accionDestino, String mensajeNotificacion) {
        // 1. Validación
        validador.validar(monto, tipo);

        // 2. Cálculo de la comisión
        double comision = calculadora.calcular(monto, tipo);

        // 3. Movimiento del dinero
        origen.retirar(monto + comision);
        accionDestino.run();

        // 4. Persistencia
        repositorio.guardarTransaccion(origen.getNumero(), destino, monto, comision);

        // 5. Comprobante
        comprobante.imprimir(origen, destino, monto, comision);

        // 6. Notificación
        notificador.enviar(origen.getTitular(), mensajeNotificacion);

        // 7. Auditoría y antifraude
        registro.registrar(tipo.getNombre(), origen, destino, monto);
    }
}
