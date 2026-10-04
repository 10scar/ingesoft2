public class ServicioPublico implements TipoTransaccion {
    private static final double COMISION_FIJA = 1500.0;

    @Override
    public String getNombre() {
        return "SERVICIO_PUBLICO";
    }

    @Override
    public double calcularComision(double monto) {
        return COMISION_FIJA;
    }
}
