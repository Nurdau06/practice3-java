import java.util.Locale;

/** Часть 3: перевод между двумя счетами в однопоточном приложении. */
public final class TransferService {
    public void transfer(BankAccount from, BankAccount to, double amount)
            throws InsufficientFundsException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Оба счёта должны существовать.");
        }
        // == сравнивает ссылки, equals — содержимое строк с номерами счетов.
        if (from == to || from.getAccountNumber().equals(to.getAccountNumber())) {
            throw new IllegalArgumentException(
                    "Отправитель и получатель должны быть разными счетами.");
        }
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                    "Сумма перевода должна быть конечным числом больше нуля.");
        }

        // Проверяем возможность зачисления ДО списания.
        // Иначе при переполнении получателя деньги уже ушли бы у отправителя.
        if (!Double.isFinite(to.getBalance() + amount)) {
            throw new IllegalArgumentException(
                    "Перевод приведёт к переполнению баланса получателя.");
        }

        // При нехватке средств withdraw бросит исключение без изменения баланса.
        from.withdraw(amount);
        // Все причины отказа deposit уже проверены. В одном потоке он выполнится.
        to.deposit(amount);

        System.out.printf(Locale.ROOT,
                "Перевод выполнен успешно: %.2f со счёта %s на счёт %s.%n",
                amount, from.getAccountNumber(), to.getAccountNumber());
    }
}
