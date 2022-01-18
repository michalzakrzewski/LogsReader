package org.zakrzewski;

import org.zakrzewski.services.LogsReaderServices;
import org.zakrzewski.utils.DirectoryPathUtil;

public class LogsReaderApp {
    public static void main(String[] args) {
        LogsReaderServices logsReaderServices = new LogsReaderServices();
        logsReaderServices.readLog(DirectoryPathUtil.DIRECTORY_NAME);
    }
}
