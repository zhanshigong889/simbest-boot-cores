package com.simbest.boot.security.auth.controller;

import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.security.IUser;
import com.simbest.boot.security.SimpleUser;
import com.simbest.boot.util.security.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * 用途：首页控制器
 * 作者: lishuyi
 * 时间: 2018/1/31  15:49
 */
@Api(description = "IndexController", tags = {"权限管理-首页控制器"})
@Slf4j
@Controller
public class IndexController {

    @ApiOperation(value = "匿名访问首页", notes = "互联网应用为主站点页面，企业应用为登录页面，调整以下welcome模板页面")
    @RequestMapping(value = {"/"}, method = {RequestMethod.POST, RequestMethod.GET})
    public String root(HttpServletRequest request) {
        return "redirect:/welcome";
    }

    @ApiOperation(value = "匿名访问首页", notes = "互联网应用welcome为主站点页面，企业应用welcome为登录页面")
    @GetMapping("/welcome")
    public ModelAndView welcome() {
        return new ModelAndView("welcome");
    }

    @ApiOperation(value = "需要SESSION信息的后台首页", notes = "支持SSO单点登录")
    @RequestMapping(value = {"/home", "/sso"}, method = {RequestMethod.POST, RequestMethod.GET})
    public String home(HttpServletRequest request) {
        return "redirect:/index";
    }

    @ApiOperation(value = "需要SESSION信息的后台首页")
    @RequestMapping(value = "/index", method = {RequestMethod.POST, RequestMethod.GET})
    public ModelAndView index(Model indexModel) {
        IUser iuser = SecurityUtils.getCurrentUser();
        indexModel.addAttribute("iuser", iuser);
        return new ModelAndView("index", "indexModel", indexModel);
    }

    @ApiOperation(value = "获取当前登陆人信息")
    @PostMapping(value={"/getCurrentUser","/getCurrentUser/sso","/getCurrentUser/api"})
    @ResponseBody
    public JsonResponse getCurrentUser() {
        IUser iuser = SecurityUtils.getCurrentUser();
        return JsonResponse.success(iuser);
    }


}
