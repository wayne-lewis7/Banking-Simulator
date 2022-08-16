

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankingSimulator {
    public static void main(String[] args) {
        // Creating file for transaction log
        File transactionLog = new File("transactions.txt");

        BankAccount myAccount = new BankAccount();      // My bank account... money printer go brrrrr
        myAccount.setBalance(0);

        ExecutorService executor = Executors.newFixedThreadPool(15);

        System.out.println("Deposit Agents\t\t\t\tWithdrawal Agents\t\t\t\tBalance");
        System.out.println("______________\t\t\t\t_________________\t\t\t\t_________________");

        for(int i = 0; i < 10; i++) {
            if(i < 5)   // Ensuring 5 deposit threads are created
            {
                DepositRunnable makeDeposit = new DepositRunnable(i, myAccount, lock, canWithdraw, transactionLog);
                executor.execute(makeDeposit);
            }

            WithdrawalRunnable makeWithdrawal = new WithdrawalRunnable(i, myAccount, lock, canWithdraw, transactionLog);
            executor.execute(makeWithdrawal);
        }
    }
    private static final Lock lock = new ReentrantLock();
    private static final Condition canWithdraw = lock.newCondition();
}