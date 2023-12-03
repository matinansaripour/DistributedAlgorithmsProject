package cs451;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ConfigParser {
    private static final String SPACES_REGEX = "\\s+";
    private int m;
    private String path;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();

        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            String[] splits = line.split(SPACES_REGEX);
            if (splits.length != 1) {
                System.err.println("Problem with the configs file!");
                return false;
            }

            m = Integer.parseInt(splits[0]);
        } catch (IOException e) {
            System.err.println("Problem with the configs file!");
            return false;
        }

        return true;
    }

    public String getPath() {
        return path;
    }

    public int getM(){
        return m;
    }

}
