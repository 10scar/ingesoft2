public class MismoBanco implements TipoTransaccion {
    @Override
    public String getNombre() {
        return "MISMO_BANCO";
    }

    @Override
    public double calcularComision(double monto) {
        return 0;
    }
}