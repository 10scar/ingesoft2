public class CalculadoraComision implements Calculadora {
    @Override
    public double calcular(double monto, TipoTransaccion tipo) {
        return tipo.calcularComision(monto);
    }
}