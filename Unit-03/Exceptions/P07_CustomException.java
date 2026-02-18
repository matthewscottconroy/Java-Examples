public class P07_CustomException {

    // Custom exception type (checked or unchecked — this one is checked).
    static class NotEnoughFundsException extends Exception {
        public NotEnoughFundsException(String message) {
            super(message);
        }
    }

    static class Account {
        private int cents;

        public Account(int cents) {
            this.cents = cents;
        }

        public void withdraw(int amountCents) throws NotEnoughFundsException {
            if (amountCents > cents) {
                throw new NotEnoughFundsException(
                    "Need " + amountCents + " cents but only have " + cents + " cents."
                );
            }
            cents -= amountCents;
        }

        public int getCents() {
            return cents;
        }
    }

    public static void main(String[] args) {
        Account a = new Account(500);

        try {
            a.withdraw(700);
            System.out.println("Balance: " + a.getCents());
        } catch (NotEnoughFundsException e) {
            System.out.println("Withdrawal failed: " + e.getMessage());
        }
    }
}
