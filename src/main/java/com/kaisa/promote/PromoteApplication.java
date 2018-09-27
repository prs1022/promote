package com.kaisa.promote;

import com.api.collect.Lord;
import com.api.user.Login;
import com.bean.*;
import com.cons.BaseVariable;
import com.exception.ExceptionEnum;
import com.exception.MyException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
@SpringBootApplication
@EnableScheduling
@Configuration
@Scope("prototype")
public class PromoteApplication implements ApplicationRunner {

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Login login = new Login();//登录的时候，同步

    private ShowInfo showInfo = new ShowInfo();

    private boolean flag = false;//定时任务开关

    private ThreadLocal<String> token = new ThreadLocal();//登录后所发的token

    private String phoneCookie = "phone_cookie";
    private String tokenCookie = "token_cookie";
    private String pwdCookie = "pwd_cookie";

    private Map<String, String> uNamePwd = new HashMap();//key->手机号,val->pwd

    @GetMapping("/login")
    String loginForm(Model model) {
        model.addAttribute("loginObj", new LoginObj());
        model.addAttribute("showInfo", showInfo);
        return "login";
    }

    @PostMapping("/login")
    String loginRs(@ModelAttribute("loginObj") LoginObj loginObj, RedirectAttributes redirectAttributes, HttpServletRequest request, HttpServletResponse response) throws Exception {
        redirectAttributes.addFlashAttribute("phoneNum", loginObj.getUName().trim());
        redirectAttributes.addFlashAttribute("pwd", loginObj.getPwd().split(",")[0].trim());//URL隐藏参数
//        removeAllCookie(request, response);
        Cookie cookie = new Cookie(phoneCookie, loginObj.getUName().trim());
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7);//设置一周的失效时间
        Cookie cookie2 = new Cookie(pwdCookie, loginObj.getPwd().split(",")[0].trim());
        cookie2.setPath("/");
        cookie2.setMaxAge(60 * 60 * 24 * 7);//设置一周的失效时间
        response.addCookie(cookie);
        response.addCookie(cookie2);
        return "redirect:result";
    }

    /**
     * 清除所有之前登录过的cookie
     *
     * @param request
     * @param response
     */
    private void removeAllCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return;
        }
        List<String> cName = new ArrayList<>();
        cName.add(phoneCookie);
        cName.add(pwdCookie);
        cName.add(tokenCookie);
        for (Cookie cookie : cookies) {
            if (cName.contains(cookie.getName())) {
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
    }

    @RequestMapping("/result")
    String getRs(Model model, HttpServletRequest request, @ModelAttribute("phoneNum") String phoneNum, @ModelAttribute("pwd") String pwd, HttpServletResponse response) {
        if (phoneNum.equals("") || pwd.equals("")) {
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(phoneCookie)) {
                    phoneNum = cookie.getValue();
                } else if (cookie.getName().equals(pwdCookie)) {
                    pwd = cookie.getValue();
                } else if (cookie.getName().equals(tokenCookie)) {
                    token.set(cookie.getValue());//读取token
                }
            }
        }
       try{
           login.setPhoneNum(phoneNum);
           login.setPwd(pwd);
           String tokenWrite = login();//登录
           Cookie cookie = new Cookie(tokenCookie, tokenWrite);
           cookie.setPath("/");
           response.addCookie(cookie);//写入token
           UserInfo.UserData userData = login.userInfo(token.get()).getData();
           Lord lord = new Lord();
           MineAccount.MineAccountData accountData = lord.mineAccount(token.get()).getData();
           if (accountData == null) {
               token.remove();
               System.err.println("accountData获取为null，token失效");
               throw new MyException(ExceptionEnum.SESSION_TIME_OUT);
           }
           showInfo.setCapacity(accountData.getCapacity());
           showInfo.setInviteCode(userData.getInvite_code());
           showInfo.setInviteNum(login.getInviteUserNum(token.get()));
           showInfo.setIp(userData.getIp());
           showInfo.setLordCount(accountData.getCredit());
           showInfo.setMoneyLeft(login.getMoney(token.get()));
           String realName = userData.getReal_name();
           showInfo.setRealName(realName.substring(0, 1) + "*" + ((realName.length() == 2) ? "" : "*"));
           showInfo.setRefresh("刷新时间:" + dateFormat.format(BaseVariable.getCurrentTime()));
//            showInfo.setPartition(lord.partitionInfo(token.get()).get("total"));
           //收取lord
           for (Mine.MineItem mineItem : lord.mines(token.get()).getData().getMines()) {
               lord.collect(mineItem.getKey(), token.get());//收集
               showInfo.getLordCollect().add("大吉大利,喜提lord:" + mineItem.getCredit() + ",    产出时间:" + dateFormat.format(new Date(mineItem.getTime() * 1000)));
           }

           //收取附近的红包
           lord.collectRedPacket(token.get()).forEach(e -> {
               showInfo.getMoneyCollect().add("收取附近的红包，" + e);
           });
       }catch (Exception e){
           System.err.println("login to result ERROR=>"+e.getMessage());
           throw new MyException(ExceptionEnum.TIME_OUT);
       }
        if (this.showInfo == null) {
            throw new MyException(ExceptionEnum.SESSION_TIME_OUT);
        }
        model.addAttribute("showInfo", this.showInfo);
        return "result";
    }

    /**
     * 统一异常处理
     *
     * @param exception exception
     * @return
     */
    @ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.OK)
    public ModelAndView processException(RuntimeException exception) {
        ModelAndView m = new ModelAndView();
        m.addObject("exception", exception.getMessage());
        m.setViewName("error");
        return m;
    }

/*
//todo 目前先自己用
    //每天1点登录一次
    @Scheduled(cron = "0 0 1 * * *")
    public void loginEveryday() {
        login();
        System.out.println("每次定时登录成功!!");
    }
*/

    public String login() {
        if (token.get() != null) {
            flag = true;
            return token.get();
        }
        //登录
        try {
            token.set(login.getToken());
            flag = true;
        } catch (Exception e) {
            flag = false;
            System.err.println("登录失败，uName:" + login.getPhoneNum());
            throw new MyException(ExceptionEnum.LOGIN_ERROR);
        }
        showInfo.getLordCollect().clear();
        System.out.println(login.getPhoneNum() + "登陆了！！token:" + token.get());
        System.out.println("领取分红:" + new Lord().collectPartition(token.get()));
        return token.get();
    }

    //每隔5分钟执行一次
    @Scheduled(cron = "0 0/5 * * * *")
    public void run() {
        if (!flag) {//不登录是不会执行的
            return;
        }
        token.set(BaseVariable.token);
        if (token.get() == null) {//无效token或者重新登陆一次导致cookie中的token失效
            System.out.println("进入到重登录方法" + dateFormat.format(BaseVariable.getCurrentTime()));
            //重新触发一次登录 //todo
            if (BaseVariable.tryTimes > 2) {
                //尝试三次以上就不登录了
                throw new MyException(ExceptionEnum.THREE_TRY_OUT);
            }
            String tokenStr = login();
            if (tokenStr == null) {
                System.err.println("第" + (BaseVariable.tryTimes) + "次重试失败!");
                return;
            }
            BaseVariable.tryTimes++;
        }
        System.out.println("十分钟刷新一次----tryTIme:" + BaseVariable.tryTimes + ",phone:" + login.getPhoneNum() + ",刷新时间:" + dateFormat.format(BaseVariable.getCurrentTime()) + ",token:" + token.get());
        BaseVariable.tryTimes = 0;//登录成功置trytime = 0
        try {
            UserInfo.UserData userData = null;
            try {
                userData = login.userInfo(token.get()).getData();
            } catch (Exception e) {
                token.remove();//如果token失效，则当天重新登录一次获取
                System.err.println("登录失败，再接再厉...");
                throw new MyException(ExceptionEnum.LOGIN_ERROR);
            }
            Lord lord = new Lord();
            MineAccount.MineAccountData accountData = lord.mineAccount(token.get()).getData();
            if (accountData == null) {
                token.remove();
                System.err.println("token失效");
                throw new MyException(ExceptionEnum.SESSION_TIME_OUT);
            }
            showInfo.setCapacity(accountData.getCapacity());
            showInfo.setInviteCode(userData.getInvite_code());
            showInfo.setInviteNum(login.getInviteUserNum(token.get()));
            showInfo.setIp(userData.getIp());
            showInfo.setLordCount(accountData.getCredit());
            showInfo.setMoneyLeft(login.getMoney(token.get()));
            String realName = userData.getReal_name();
            showInfo.setRealName(realName.substring(0, 1) + "*" + ((realName.length() == 2) ? "" : "*"));
            showInfo.setRefresh("刷新时间:" + dateFormat.format(Calendar.getInstance().getTime()));
//            showInfo.setPartition(lord.partitionInfo(token.get()).get("total"));
            //收取lord
            for (Mine.MineItem mineItem : lord.mines(token.get()).getData().getMines()) {
                lord.collect(mineItem.getKey(), token.get());//收集
                try {
                    Thread.sleep(1000);//休息一秒再收集lord
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showInfo.getLordCollect().add("大吉大利,喜提lord:" + mineItem.getCredit() + ",    产出时间:" + dateFormat.format(new Date(mineItem.getTime() * 1000)));
            }

            //收取附近的红包
            lord.collectRedPacket(token.get()).forEach(e -> {
                showInfo.getMoneyCollect().add("收取附近的红包，" + e);
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            System.out.println(showInfo.toString());
            showInfo.getLordCollect().forEach(i -> {
                System.out.println(i);
            });
            showInfo.getMoneyCollect().forEach(e -> {
                System.out.println(e);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            });
            System.out.println("===============刷新时间:" + dateFormat.format(BaseVariable.getCurrentTime()) + "========================");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(PromoteApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        System.out.println("===程序启动时进入====");
        flag = true;
        login.setPhoneNum("15251710379");
        login.setPwd("prs1022flw");
        String tokenstr = login.getToken();
        BaseVariable.token = tokenstr;
        token.set(tokenstr);
        run();
    }
}
