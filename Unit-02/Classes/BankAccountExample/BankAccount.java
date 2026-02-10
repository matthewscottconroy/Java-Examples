public class BankAccount {
    private double balance;

    public BankAccount(double initial) {
        balance = initial;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public double getBalance() {
        return balance;
    }
}

