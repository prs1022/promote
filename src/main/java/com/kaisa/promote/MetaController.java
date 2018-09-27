package com.kaisa.promote;

import com.util.Shell;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetaController {
    @RequestMapping("/log")
    public String getLog() {
        return Shell.executeShell("tail -f /home/log.txt");
    }
}
