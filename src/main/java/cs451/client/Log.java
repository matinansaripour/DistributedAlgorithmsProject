package cs451.client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Log {
    private StringBuilder stringLog = new StringBuilder();
    private int chunkSize = 20 * 1024 * 1024;
    private FileOutputStream out;

    public Log(String outputPath) throws FileNotFoundException {
        out = new FileOutputStream(outputPath);
    }
    public synchronized void add(StringBuilder stringBuilder) {
        stringLog.append(stringBuilder);
        if (stringLog.length() > chunkSize) {
            try {
                out.write(stringLog.toString().getBytes());
            } catch (Exception e) {}
            stringLog.setLength(0);
        }
    }

    public void close() {
        try {
            out.write(stringLog.toString().getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {}
    }
}
