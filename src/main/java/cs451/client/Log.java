package cs451.client;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Log {
    public StringBuilder stringLog = new StringBuilder();
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private OutputStreamWriter writer;
    int chunkSize = 10 * 1024 * 1024;

    public Log(String outputPath) throws FileNotFoundException {
        fos = new FileOutputStream(outputPath);
        bos = new BufferedOutputStream(fos);
        writer = new OutputStreamWriter(bos);
    }
    public synchronized void add(StringBuilder stringBuilder, boolean isSent) {
        stringLog.append(stringBuilder);
        if (isSent && stringLog.length() > chunkSize) {
            try {
                writer.write(stringLog.toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            stringLog.setLength(0);
        }
    }

    public String getStringLog() {
        return stringLog.toString();
    }

    public void close() {
        try {
            writer.write(stringLog.toString());
            writer.flush();
            bos.flush();
            fos.flush();
            writer.close();
            bos.close();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
