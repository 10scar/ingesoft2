public class CalculadoraComision {
    public double calcular(double monto, TipoTransaccion tipo) {
        return tipo.calcularComision(monto);
    }
}