package de.neofonie.common.cinderella.example;

import de.neofonie.cinderella.core.RequestUtil;
import de.neofonie.cinderella.core.counter.MemoryCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HelloWorld {

    @Autowired
    private MemoryCounter counter;

    @RequestMapping("/*")
    public String helloWorld(HttpServletRequest request, Model model) {
        model.addAttribute("ip", RequestUtil.getClientIpAddr(request));
        model.addAttribute("statistic", counter.getStatistic());
        return "helloWorld";
    }
}
