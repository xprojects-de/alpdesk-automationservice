package x.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecCommander {

    private AtomicBoolean globalState = new AtomicBoolean(true);
    private final RetVal globalRetVal = new RetVal();
    public static int OS_WIN = 1;
    public static int OS_LINUX = 2;
    public static int OS_MAC = 3;
    public static int OS_SOLARIS = 4;

    public static class RetVal {

        private final Object mutex = new Object();
        private boolean ready = false;
        private boolean error = false;
        private boolean killed = false;
        private boolean timeout = false;
        private int PID = 0;
        private final ArrayList<String> returnvalues = new ArrayList<>();

        public boolean isReady() {
            return ready;
        }

        public void setReady(boolean ready) {
            synchronized (mutex) {
                this.ready = ready;
            }
        }

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            synchronized (mutex) {
                this.error = error;
            }
        }

        public int getPID() {
            return PID;
        }

        public void setPID(int PID) {
            synchronized (mutex) {
                this.PID = PID;
            }
        }

        public boolean isKilled() {
            return killed;
        }

        public void setKilled(boolean killed) {
            synchronized (mutex) {
                this.killed = killed;
            }
        }

        public boolean isTimeout() {
            return timeout;
        }

        public void setTimeout(boolean timeout) {
            synchronized (mutex) {
                this.timeout = timeout;
            }
        }

        public void addItem(String item) {
            synchronized (mutex) {
                returnvalues.add(item);
            }
        }

        public void clearItems() {
            synchronized (mutex) {
                returnvalues.clear();
            }
        }

        public ArrayList<String> getReturnvalues() {
            return returnvalues;
        }
    }

    private void initGlobalRetVal() {
        globalRetVal.setReady(false);
        globalRetVal.setError(true);
        globalRetVal.setPID(0);
        globalRetVal.setKilled(false);
        globalRetVal.setTimeout(false);
        globalRetVal.clearItems();
    }

    private Thread runCommand(ArrayList<String> commands) throws Exception {
        Thread processThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessBuilder commandProcessBuilder = new ProcessBuilder(commands);
                    commandProcessBuilder.redirectErrorStream(true);
                    Process commandProcess = commandProcessBuilder.start();
                    globalRetVal.setPID(getProcessID(commandProcess));
                    BufferedReader inpCommandReader = new BufferedReader(new InputStreamReader(commandProcess.getInputStream()));
                    while (globalState.get()) {
                        String ret = inpCommandReader.readLine();
                        if (ret != null) {
                            globalRetVal.addItem(ret);
                        }
                        if (!processIsRunning(commandProcess) && ret == null) {
                            globalState.set(false);
                        }
                        Thread.sleep(10);
                    }
                    commandProcess.waitFor();
                    commandProcess.destroy();
                    globalRetVal.setReady(true);
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(ExecCommander.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        processThread.start();
        return processThread;
    }

    public RetVal executeCommand(String command, String params, boolean setWriteable, int timeoutMS) throws Exception {

        File f = new File(command);
        if (!f.exists()) {
            globalRetVal.setError(true);
            return globalRetVal;
        }
        if (setWriteable) {
            f.setReadable(true, true);
            f.setWritable(true, true);
            f.setExecutable(true, true);
        }

        globalState.set(true);
        initGlobalRetVal();

        ArrayList<String> commands = new ArrayList<>();
        if (params.length() > 0) {
            command = command + " " + params;
        }
        String[] commandList = command.split(" ");
        for (String s : commandList) {
            commands.add(s);
        }
        long startTime = System.currentTimeMillis();
        Thread processThread = runCommand(commands);

        boolean loop = true;
        while (loop) {
            if (globalRetVal.isReady() || !globalState.get()) {
                loop = false;
                if (!globalRetVal.isTimeout() && !globalRetVal.isKilled()) {
                    globalRetVal.setError(false);
                }
            }
            if (System.currentTimeMillis() > (startTime + timeoutMS)) {
                globalState.set(false);
                globalRetVal.setTimeout(true);
                if (!globalRetVal.isReady()) {
                    try {
                        if (getOS() == OS_LINUX && globalRetVal.getPID() != 0) {
                            Runtime.getRuntime().exec("kill -9 " + globalRetVal.getPID());
                            globalRetVal.setKilled(true);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    globalRetVal.setError(true);
                }
            }
            Thread.sleep(10);
        }

        processThread.join();

        return globalRetVal;
    }

    private int getProcessID(Process p) {
        int pid = 0;
        if (getOS() == OS_LINUX) {
            if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                try {
                    Field f = p.getClass().getDeclaredField("pid");
                    f.setAccessible(true);
                    pid = f.getInt(p);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return pid;
    }

    protected boolean processIsRunning(Process process) {
        try {
            if (getOS() == OS_LINUX) {
                process.exitValue();
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    private int getOS() {
        int retval = -1;
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("win") >= 0) {
            retval = OS_WIN;
        } else if (OS.indexOf("mac") >= 0) {
            retval = OS_MAC;
        } else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
            retval = OS_LINUX;
        } else if (OS.indexOf("sunos") >= 0) {
            retval = OS_SOLARIS;
        }
        return retval;
    }

    public static void main(String[] args) {
        ExecCommander ap = new ExecCommander();
        System.out.println("OS:" + ap.getOS());
        try {
            for (int k = 0; k < 2000; k++) {
                System.out.println("NR.: " + k);
                ExecCommander.RetVal value = ap.executeCommand("/home/pi/XHomeautomation/dht22", "22 4", true, 15000);
                for (String s : value.getReturnvalues()) {
                    System.out.println(s);
                }
                if (value.isError() || value.isKilled()) {
                    System.out.println("READY:" + value.isReady());
                    System.out.println("ERROR:" + value.isError());
                    System.out.println("KILLED:" + value.isKilled());
                    System.out.println("PID = " + value.getPID());
                    //break;
                }
                Thread.sleep(2500);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
