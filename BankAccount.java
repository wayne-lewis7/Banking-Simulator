public class BankAccount {
    private int balance;

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int depositFunds(int depositAmount) {
        balance += depositAmount;
        return balance;
    }

    public int withdrawFunds(int withdrawAmount) {
        balance -= withdrawAmount;
        return balance;
    }
}
