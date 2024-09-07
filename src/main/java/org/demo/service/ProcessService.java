package org.demo.service;

import java.sql.SQLException;

public interface ProcessService {

    void startDBAutomationProcess() throws SQLException, InterruptedException;
}