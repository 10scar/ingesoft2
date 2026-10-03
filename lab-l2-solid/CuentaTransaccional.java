public abstract class CuentaTransaccional extends Cuenta {
    public CuentaTransaccional(String numero, String titular, double saldoInicial) {
        super(numero, titular, saldoInicial);
    }

    public void retirar(double monto) {
        if (monto > saldo) throw new IllegalStateException("Saldo insuficiente");
        saldo -= monto;
    }
}