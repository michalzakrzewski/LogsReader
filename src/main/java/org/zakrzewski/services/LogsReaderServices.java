package org.zakrzewski.services;

import org.zakrzewski.interfaces.LogsReaderInterface;
import org.zakrzewski.utils.FormatterDateTimeUtil;
import org.zakrzewski.utils.PatternRegexUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogsReaderServices implements LogsReaderInterface {
    @Override
    public boolean checkIfPathExists(String path) {
        return Files.exists(Paths.get(path));
    }

    @Override
    public boolean checkIfFileIsDirectory(File file) {
        return file.isDirectory();
    }

    @Override
    public List<String> getLogMatches(String fileLogContent, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(fileLogContent);
        List<String> allMatchesList = new ArrayList<>();
        while (matcher.find()){
            allMatchesList.add(matcher.group());
        }
        return allMatchesList;
    }

    @Override
    public List<File> getLogFiles(File logFile) {
        File[] files = logFile.listFiles();
        List<File> fileList = Arrays.stream(files)
                .filter(file -> file.getName().endsWith(".log"))
                .sorted(Comparator.comparingLong(File::lastModified))
                .collect(Collectors.toList());
        return fileList;
    }

    @Override
    public void frameworkUniqueCount(List<String> frameworkLog) {
        Map<String, Long> counts =
                frameworkLog.stream().collect(Collectors.groupingBy(Object::toString, Collectors.counting()));
        Long minValues = Collections.min(counts.values());
        System.out.println("Unique framework: ");
        for (Map.Entry<String, Long> entry : counts.entrySet()){
            if (entry.getValue() <= minValues){
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
        }
    }

    @Override
    public void severityCount(List<String> severityLogs) {
        Map<String, Long> counts =
                severityLogs.stream().collect(Collectors.groupingBy(Object::toString, Collectors.counting()));
        System.out.println("Severity counts: ");
        counts.forEach((key, value) -> System.out.println(key + ":" + value));
    }

    @Override
    public void severityRatioLogs(List<String> severityLogs) {
        List<String> listOfSeverityPriority = List.of("ERROR", "FATAL");
        List<String> listOfOtherSeverity = List.of("DEBUG", "INFO", "WARN", "OFF", "TRACE");
        Map<String, Long> counts =
                severityLogs.stream().collect(Collectors.groupingBy(Object::toString, Collectors.counting()));
        int sumOfHighPriority = 0;
        int sumOfLowerPriority = 0;
        for (Map.Entry<String, Long> entry : counts.entrySet()){
            if (listOfSeverityPriority.contains(entry.getKey())){
                sumOfHighPriority += entry.getValue();
            }
            if (listOfOtherSeverity.contains(entry.getKey())){
                sumOfLowerPriority += entry.getValue();
            }
        }
        System.out.println("Ratio of high priority to low priority:\n" + sumOfHighPriority + " to " + sumOfLowerPriority + ".");
        System.out.println((double) sumOfHighPriority/sumOfLowerPriority);
    }

    @Override
    public long getDifferenceTime(List<String> datesDifferenceList) {
        List<LocalDateTime> localDateTimeList = datesDifferenceList.stream()
                .map(s -> LocalDateTime.parse(s, FormatterDateTimeUtil.DATE_TIME_FORMATTER))
                .sorted(LocalDateTime::compareTo)
                .collect(Collectors.toList());

        LocalDateTime firstLog = localDateTimeList.get(0);
        LocalDateTime lastLog = localDateTimeList.get(localDateTimeList.size() - 1);
        return firstLog.until(lastLog, ChronoUnit.DAYS);
    }

    @Override
    public void readLog(String logPath) {
        if (!checkIfPathExists(logPath)){
            System.out.println("Log path doesn't exist");
            System.exit(1);
        }

        File logFile = new File(logPath);
        if (!checkIfFileIsDirectory(logFile)){
            System.out.println("Check correctly directory");
            System.exit(1);
        }

        List<File> logFiles = getLogFiles(logFile);
        for (File file : logFiles){
            long startTime = System.nanoTime();

            try{
                String fileLogContent = new String(Files.readAllBytes(Paths.get(file.getPath())));
                List<String> datesDifferenceList = getLogMatches(fileLogContent, PatternRegexUtil.DATE_REGEX);
                System.out.println("File: " + file.getName());
                System.out.println("Difference between first and last timestamp log: " + getDifferenceTime(datesDifferenceList) + " days");
                List<String> severityLogsLeveL = getLogMatches(fileLogContent, PatternRegexUtil.LEVE_REGEX);
                severityCount(severityLogsLeveL);
                List<String> severityRatio = getLogMatches(fileLogContent, PatternRegexUtil.LEVE_REGEX);
                severityRatioLogs(severityRatio);
                List<String> frameworkClassUniqueList = getLogMatches(fileLogContent, PatternRegexUtil.CLASS_REGEX);
                frameworkUniqueCount(frameworkClassUniqueList);
            }catch (IOException e){
                e.printStackTrace();
            }

            long elapsedTime = System.nanoTime() - startTime;
            double seconds = (double)elapsedTime / 1_000_000_000.0;
            System.out.println("Open file time: " + seconds + " seconds");
            System.out.println();
        }
    }
}
