public class Main {
    public static void main(String[] args) {
        BankAccount acct = new BankAccount(100);
        acct.deposit(50);

        System.out.println(acct.getBalance());
    }
}

