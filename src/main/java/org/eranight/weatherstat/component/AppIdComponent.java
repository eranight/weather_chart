package org.eranight.weatherstat.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppIdComponent {

    @Value("${APPID}")
    private String appId;

    public String getAppId() {
        return appId;
    }
}
