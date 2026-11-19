package com.nie.mentalserver.controller;

import com.nie.mental.common.exception.BusinessException;
import com.nie.mentalserver.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class FileRedirectController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 兼容历史 /upload/... 数据，以及未配置 CDN 时给图片提供一个匿名可访问入口
     */
    @GetMapping("/upload/**")
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();
        String key = uri.substring("/upload/".length());
        try {
            response.sendRedirect(fileStorageService.resolveUrl(key));
        } catch (BusinessException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
