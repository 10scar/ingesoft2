public class Llave implements TipoTransaccion {
    @Override
    public String getNombre() {
        return "LLAVE";
    }

    @Override
    public double calcularComision(double monto) {
        return 0;
    }
}
