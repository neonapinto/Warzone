package utils.logger;

import utils.Observer;

import java.io.*;

/**
 * A class implementing Observer which observes LogEntryBuffer and writes to log file
 *
 * @author Dhananjay Narayan
 * @author Surya Manian
 */
public class LogEntryWriter implements Observer, Serializable {
    /**
     * File name for logger
     */
    private String l_Filename = "demo";

    /**
     * log entry writer
     */
    public LogEntryWriter() {
        clearLogs();
    }

    /**
     * A function to receive the update from Subject. It then sends the message to be written to LogFile.
     *
     * @param p_s the message to be updated
     */
    public void update(String p_s) {
        writeLogFile(p_s);
    }

    /**
     * A function to write the actions of the game to a logfile named demolog.
     *
     * @param p_str The message to be written to the log file.
     */
    public void writeLogFile(String p_str) {
        PrintWriter l_WriteData = null;
        try {
            checkDirectory("logFiles");
            l_WriteData = new PrintWriter(new BufferedWriter(new FileWriter("logFiles/" + l_Filename + ".log", true)));
            l_WriteData.println(p_str);

        } catch (Exception p_Exception) {
            System.out.println(p_Exception.getMessage());
        } finally {
            l_WriteData.close();
        }
    }

    /**
     * Check if the path is a folder or not
     *
     * @param path the path to check
     */
    private void checkDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
            directory.mkdirs();
        }
    }

    /**
     * This method is used to clear the log file before a new game starts.
     */
    @Override
    public void clearLogs() {
        try {
            checkDirectory("logFiles");
            File l_File = new File("logFiles/" + l_Filename + ".log");
            if (l_File.exists()) {
                l_File.delete();
            }
        } catch (Exception ex) {

        }
    }
}

