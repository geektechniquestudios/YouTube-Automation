package com.geektechnique.notificationserivce;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocalService {
    private String service;
    private String url;
}
