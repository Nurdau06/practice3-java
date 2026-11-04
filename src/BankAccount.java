
public final class BankAccount {
    private final String accountNumber;
    private final String owner;
    private double balance;

    public BankAccount(String accountNumber, String owner, double balance) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Номер счёта не может быть пустым.");
        }
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("Имя владельца не может быть пустым.");
        }
        
        if (!Double.isFinite(balance) || balance < 0) {
            throw new IllegalArgumentException(
                    "Начальный баланс должен быть конечным неотрицательным числом.");
        }
        this.accountNumber = accountNumber.strip();
        this.owner = owner.strip();
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwner() {
        return owner;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        validatePositiveAmount(amount);
        double newBalance = balance + amount;
        // Даже сумма двух конечных double может дать Infinity.
        if (!Double.isFinite(newBalance)) {
            throw new IllegalArgumentException("Пополнение приведёт к переполнению баланса.");
        }
        balance = newBalance;
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        validatePositiveAmount(amount);
        if (amount > balance) {
            // Данные корректны, но выполнить операцию не позволяет остаток.
            throw new InsufficientFundsException(
                    "На счёте " + accountNumber + " недостаточно средств.");
        }
        balance -= amount;
    }

    private static void validatePositiveAmount(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                    "Сумма операции должна быть конечным числом больше нуля.");
        }
    }
}
