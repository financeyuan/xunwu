package com.yaoyao.online.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/29 15:27
 * @Description:
 */
@Component
public class ESMonitor {

    private static final String URL = "http://118.190.208.148:9200/_cluster/health";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JavaMailSender mailSender;

    @Scheduled(fixedDelay = 10000)
    public void healthCheck(){
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet(URL);
        try {
            HttpResponse httpResponse = httpClient.execute(get);
            if(httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK){
            }
            String body = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            JsonNode jsonNode = objectMapper.readTree(body);
            String status = jsonNode.get("status").asText();
            switch (status){
                case "green":break;
                case "yellow":break;
                case  "red": break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void  sendAlertMessage(String message){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("fastboot1001@126.com");
        mailMessage.setTo("2478412512@qq.com");
        mailMessage.setSubject("测试");
        mailMessage.setText(message);
        mailSender.send(mailMessage);
    }
}
