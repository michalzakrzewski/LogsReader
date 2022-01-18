package org.zakrzewski.interfaces;

import java.io.File;
import java.util.List;

public interface LogsReaderInterface {

    boolean checkIfPathExists(String path);
    boolean checkIfFileIsDirectory(File file);
    List<String> getLogMatches(String fileLogContent, String regex);
    List<File> getLogFiles(File logFile);
    void frameworkUniqueCount(List<String> frameworkLog);
    void severityCount(List<String> severityLogs);
    void severityRatioLogs(List<String> severityLogs);
    long getDifferenceTime(List<String> datesDifferenceList);
    void readLog(String logPath);








}
