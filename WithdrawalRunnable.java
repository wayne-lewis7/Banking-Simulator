import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class WithdrawalRunnable implements Runnable{
    private final Lock lock;
    private final Condition canWithdraw;
    private static final Random generator = new Random();
    private final int agentNum;
    private final BankAccount myAccount;
    private final File transactionLog;

    public WithdrawalRunnable(int agentNum, BankAccount myAccount, Lock lock, Condition canWithdraw, File transactionLog) {
        this.agentNum = agentNum;
        this.myAccount = myAccount;
        this.lock = lock;
        this.canWithdraw = canWithdraw;
        this.transactionLog = transactionLog;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // If multicore processor, thread will sleep for 3 to 4 milliseconds
                if(Runtime.getRuntime().availableProcessors() > 1) {
                    Thread.sleep(generator.nextInt(400 - 300) + 300);
                }
                
                lock.lock();
                int withdrawAmount = generator.nextInt(99 - 1) + 1;     // Withdrawal range: $1-$99

                if (withdrawAmount > myAccount.getBalance()) {
                    System.out.printf("\t\t\t\t\tAgent WT%d withdraws $%d\t\t\t\t(******) WITHDRAWAL BLOCKED - INSUFFICIENT FUNDS!!!\n",
                            agentNum, withdrawAmount);

                    canWithdraw.await();
                }
                else {
                    myAccount.withdrawFunds(withdrawAmount);
                    System.out.printf("\t\t\t\t\tAgent WT%d withdraws $%d\t\t\t\t(-) Balance is $%d\n",
                            agentNum, withdrawAmount, myAccount.getBalance());

                    if(withdrawAmount >= 75) {
                        System.out.printf("\n* * * Flagged Transaction - Withdrawal Agent WT%d Made A Withdrawal In Excess Of $75.00 USD - See Flagged Transaction Log.\n\n",
                                agentNum);

                        writeToLog(transactionLog, agentNum, withdrawAmount);   // Calling method to record flagged transaction
                    }
                }
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            } finally {
                lock.unlock();

                // If single-core processor, thread will yield to processor
                if(Runtime.getRuntime().availableProcessors() == 1)
                    Thread.yield();
            }
        }
    }
    public void writeToLog(File transactionLog, int agentNum, int withdrawAmount) {
        // Getting current date, time, and creating formats
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a z");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        PrintWriter logWriter = null;

        // Opening transaction log file to write to it
        try {
            FileWriter fw = new FileWriter(transactionLog, true);
            BufferedWriter bw = new BufferedWriter(fw);
            logWriter = new PrintWriter(bw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logWriter.println("\tWithdrawal Agent WT" + agentNum + " issued withdrawal of $" + withdrawAmount + ".00 at: "
                + dateFormat.format(currentDate) + " " + time.format(currentDate) + "\n");
        logWriter.close();
    }
}
