package com.kaisa.promote;

import com.util.RemoteShellExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class MetaController {
    @RequestMapping("/log")
    public String getLog(Model model, HttpServletRequest request) throws Exception {
        RemoteShellExecutor executor = new RemoteShellExecutor("138.128.207.84", 29954, "root", "123321");
        String num = "100";
        if (request.getParameter("num") != null) {
            num = request.getParameter("num").toString();
        }
        Map<String, Object> res = executor.exec("tail -" + num + " /home/log.txt");
        model.addAttribute("out", res.get("out"));
        model.addAttribute("error", res.get("error"));
        return "log";
    }
}
