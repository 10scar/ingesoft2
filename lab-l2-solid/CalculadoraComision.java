public class CalculadoraComision {
    public double calcular(double monto, String tipo) {
        double comision;
        
        switch (tipo) {
            case "MISMO_BANCO" -> comision = 0;
            case "OTRO_BANCO" -> comision = 7_500;
            case "INTERNACIONAL" -> comision = monto * 0.03 + 25_000;
            default -> throw new IllegalArgumentException("Tipo de transferencia desconocido");
        }
        return comision;
    }
}