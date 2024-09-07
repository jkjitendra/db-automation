package org.demo.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PropertyVerifyService {

    void verifyCurrentDBData(List<String> properties, String beforeAfter) throws InterruptedException;
}
