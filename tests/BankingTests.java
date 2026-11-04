/**
 * Часть 5: проверки без внешних библиотек.
 * Проверки работают всегда и не требуют параметра JVM -ea.
 */
public final class BankingTests {
    private static int passed;

    private BankingTests() {
    }

    public static void main(String[] args) throws Exception {
        run("Конструктор сохраняет и очищает строки", () -> {
            BankAccount account = new BankAccount("  ACC001  ", "  Aida  ", 50000);
            check("ACC001".equals(account.getAccountNumber()), "Номер не очищен");
            check("Aida".equals(account.getOwner()), "Имя не очищено");
            balance(account, 50000);
        });
        run("Нулевой начальный баланс разрешён", () ->
                balance(new BankAccount("ZERO", "Dana", 0), 0));

        String[] invalidTexts = {null, "", "   ", "\t\n"};
        for (int index = 0; index < invalidTexts.length; index++) {
            String value = invalidTexts[index];
            run("Некорректный номер счёта, вариант " + (index + 1), () ->
                    expectThrows(IllegalArgumentException.class,
                            () -> new BankAccount(value, "Aida", 0)));
            run("Некорректное имя владельца, вариант " + (index + 1), () ->
                    expectThrows(IllegalArgumentException.class,
                            () -> new BankAccount("ACC001", value, 0)));
        }

        double[] invalidBalances = {-1, Double.NaN,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double value : invalidBalances) {
            run("Некорректный начальный баланс " + value, () ->
                    expectThrows(IllegalArgumentException.class,
                            () -> new BankAccount("ACC001", "Aida", value)));
        }

        run("Успешное пополнение", () -> {
            BankAccount account = sender();
            account.deposit(125.50);
            balance(account, 50125.50);
        });
        run("Успешное снятие", () -> {
            BankAccount account = sender();
            account.withdraw(125.50);
            balance(account, 49874.50);
        });
        run("Снятие всего остатка", () -> {
            BankAccount account = sender();
            account.withdraw(50000);
            balance(account, 0);
        });
        run("Недостаток средств при снятии сохраняет баланс", () -> {
            BankAccount account = sender();
            expectThrows(InsufficientFundsException.class,
                    () -> account.withdraw(100000));
            balance(account, 50000);
        });

        double[] invalidAmounts = {0, -0.0, -500, Double.NaN,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double value : invalidAmounts) {
            run("Некорректное пополнение " + value + " сохраняет баланс", () -> {
                BankAccount account = sender();
                expectThrows(IllegalArgumentException.class, () -> account.deposit(value));
                balance(account, 50000);
            });
            run("Некорректное снятие " + value + " сохраняет баланс", () -> {
                BankAccount account = sender();
                expectThrows(IllegalArgumentException.class, () -> account.withdraw(value));
                balance(account, 50000);
            });
            run("Некорректный перевод " + value + " сохраняет оба баланса", () -> {
                BankAccount from = sender();
                BankAccount to = receiver();
                expectThrows(IllegalArgumentException.class,
                        () -> new TransferService().transfer(from, to, value));
                balances(from, to, 50000, 10000);
            });
        }

        run("Переполнение при пополнении сохраняет баланс", () -> {
            BankAccount account = new BankAccount("MAX", "Aida", Double.MAX_VALUE);
            expectThrows(IllegalArgumentException.class,
                    () -> account.deposit(Double.MAX_VALUE));
            balance(account, Double.MAX_VALUE);
        });
        run("Обязательный сценарий 5000", () -> {
            BankAccount from = sender();
            BankAccount to = receiver();
            new TransferService().transfer(from, to, 5000);
            balances(from, to, 45000, 15000);
        });
        run("Обязательный сценарий 100000 сохраняет оба баланса", () -> {
            BankAccount from = sender();
            BankAccount to = receiver();
            expectThrows(InsufficientFundsException.class,
                    () -> new TransferService().transfer(from, to, 100000));
            balances(from, to, 50000, 10000);
        });
        run("Перевод всего остатка", () -> {
            BankAccount from = sender();
            BankAccount to = receiver();
            new TransferService().transfer(from, to, 50000);
            balances(from, to, 0, 60000);
        });
        run("Перевод дробной суммы", () -> {
            BankAccount from = sender();
            BankAccount to = receiver();
            new TransferService().transfer(from, to, 125.50);
            balances(from, to, 49874.50, 10125.50);
        });
        run("Отсутствующий отправитель", () -> {
            BankAccount to = receiver();
            expectThrows(IllegalArgumentException.class,
                    () -> new TransferService().transfer(null, to, 100));
            balance(to, 10000);
        });
        run("Отсутствующий получатель", () -> {
            BankAccount from = sender();
            expectThrows(IllegalArgumentException.class,
                    () -> new TransferService().transfer(from, null, 100));
            balance(from, 50000);
        });
        run("Оба счёта отсутствуют", () ->
                expectThrows(IllegalArgumentException.class,
                        () -> new TransferService().transfer(null, null, 100)));
        run("Перевод на тот же объект сохраняет баланс", () -> {
            BankAccount account = sender();
            expectThrows(IllegalArgumentException.class,
                    () -> new TransferService().transfer(account, account, 100));
            balance(account, 50000);
        });
        run("Разные объекты с одинаковым номером сохраняют оба баланса", () -> {
            BankAccount from = sender();
            BankAccount to = new BankAccount(" ACC001 ", "Dana", 10000);
            expectThrows(IllegalArgumentException.class,
                    () -> new TransferService().transfer(from, to, 100));
            balances(from, to, 50000, 10000);
        });
        run("Переполнение получателя проверяется до списания", () -> {
            BankAccount from = new BankAccount("FROM", "Aida", Double.MAX_VALUE);
            BankAccount to = new BankAccount("TO", "Dana", Double.MAX_VALUE);
            expectThrows(IllegalArgumentException.class,
                    () -> new TransferService().transfer(from, to, Double.MAX_VALUE));
            balances(from, to, Double.MAX_VALUE, Double.MAX_VALUE);
        });
        System.out.println("Успешно пройдено проверок: " + passed);
    }

    private static BankAccount sender() {
        return new BankAccount("ACC001", "Aida", 50000);
    }

    private static BankAccount receiver() {
        return new BankAccount("ACC002", "Dana", 10000);
    }

    private static void run(String name, CheckedAction action) throws Exception {
        try {
            action.run();
        } catch (Exception | AssertionError failure) {
            throw new AssertionError("Проверка не пройдена: " + name, failure);
        }
        passed++;
        System.out.println("OK: " + name);
    }

    private static void expectThrows(Class<? extends Exception> type,
                                     CheckedAction action) throws Exception {
        try {
            action.run();
        } catch (Exception exception) {
            check(type.isInstance(exception), "Ожидалось " + type.getSimpleName()
                    + ", получено " + exception.getClass().getSimpleName());
            check(exception.getMessage() != null && !exception.getMessage().isBlank(),
                    "У исключения должно быть содержательное сообщение");
            return;
        }
        throw new AssertionError("Не выброшено ожидаемое " + type.getSimpleName());
    }

    private static void balance(BankAccount account, double expected) {
        check(Double.compare(account.getBalance(), expected) == 0,
                "Баланс " + account.getAccountNumber() + ": ожидалось " + expected
                        + ", получено " + account.getBalance());
    }

    private static void balances(BankAccount from, BankAccount to,
                                 double expectedFrom, double expectedTo) {
        balance(from, expectedFrom);
        balance(to, expectedTo);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }
}
