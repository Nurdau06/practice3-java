import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;


public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        BankAccount account1 = new BankAccount("ACC001", "Aida", 50000);
        BankAccount account2 = new BankAccount("ACC002", "Dana", 10000);
        TransferService transferService = new TransferService();

        System.out.println("Перевод со счёта ACC001 (Aida) на счёт ACC002 (Dana).");
        System.out.println("Введите сумму перевода (например, 5000 или 5000.50):");

        // try-with-resources закроет Scanner по завершении этого запуска.
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
            if (!scanner.hasNextLine()) {
                throw new IllegalArgumentException("Сумма перевода не введена.");
            }
            // nextLine + parseDouble позволяют обработать именно NumberFormatException.
            double amount = Double.parseDouble(scanner.nextLine().strip());
            transferService.transfer(account1, account2, amount);
        } catch (NumberFormatException exception) {
            // Этот catch должен идти раньше IllegalArgumentException:
            // NumberFormatException является его подклассом.
            System.out.println("Ошибка ввода числа: введите число, например 5000 или 5000.50.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Некорректные данные: " + exception.getMessage());
        } catch (InsufficientFundsException exception) {
            System.out.println("Перевод отклонён: " + exception.getMessage());
        } finally {
            // Выполняется и при успехе, и после обработанной ошибки.
            System.out.println("Операция перевода завершена");
        }

        printBalance(account1);
        printBalance(account2);
    }

    private static void printBalance(BankAccount account) {
        System.out.printf(Locale.ROOT, "Счёт %s (%s): %.2f%n",
                account.getAccountNumber(), account.getOwner(), account.getBalance());
    }
}
