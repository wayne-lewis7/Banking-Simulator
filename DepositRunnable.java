import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class DepositRunnable implements Runnable{
    private final Lock lock;
    private final Condition canWithdraw;
    private static final Random generator = new Random();
    private final int agentNum;
    private final BankAccount myAccount;
    private final File transactionLog;

    public DepositRunnable(int agentNum, BankAccount myAccount, Lock lock, Condition canWithdraw, File transactionLog) {
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
                Thread.sleep(generator.nextInt(1800 - 300) + 300);
                lock.lock();

                int depositAmount = generator.nextInt(500 - 1) + 1;     // Deposit range: $1-$500
                myAccount.depositFunds(depositAmount);

                System.out.printf("Agent DT%d deposits $%d\t\t\t\t\t\t\t\t\t(+) Balance is $%d\n",
                                agentNum, depositAmount, myAccount.getBalance());

                if(depositAmount >= 350) {
                    System.out.printf("\n* * * Flagged Transaction - Depositor Agent DT%d Made A Deposit In Excess Of $350.00 USD - See Flagged Transaction Log.\n\n",
                            agentNum);

                    writeToLog(transactionLog, agentNum, depositAmount);    // Calling method to record flagged transaction
                }
                canWithdraw.signalAll();
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    public void writeToLog(File transactionLog, int agentNum, int depositAmount) {
        // Getting current date, time, and creating formats
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a z");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        PrintWriter logWriter = null;

        try {
            FileWriter fw = new FileWriter(transactionLog, true);
            BufferedWriter bw = new BufferedWriter(fw);
            logWriter = new PrintWriter(bw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logWriter.println("Deposit Agent DT" + agentNum + " issued deposit of $" + depositAmount + ".00 at: "
                                + dateFormat.format(currentDate) + " " + time.format(currentDate) + "\n");
        logWriter.close();
    }
}
