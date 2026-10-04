import java.time.LocalDate;

public class CuentaInfantil extends CuentaTransaccional {
    private static final double LIMITE_DIARIO = 200_000;

    private LocalDate fechaUltimoRetiro;
    private double retiradoHoy;

    public CuentaInfantil(String numero, String titular, double saldoInicial) {
        super(numero, titular, saldoInicial);
    }

    @Override
    public void retirar(double monto) {
        LocalDate hoy = LocalDate.now();
        if (!hoy.equals(fechaUltimoRetiro)) {
            fechaUltimoRetiro = hoy;
            retiradoHoy = 0;
        }
        if (retiradoHoy + monto > LIMITE_DIARIO) {
            throw new IllegalStateException("Supera el límite diario de retiros de la cuenta infantil");
        }
        super.retirar(monto);
        retiradoHoy += monto;
    }
}
