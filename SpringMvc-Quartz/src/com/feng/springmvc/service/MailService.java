package com.feng.springmvc.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StreamUtils;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Configuration freemarkerConfig; //freeMarker configuration

    public void test() {
    	try {
    		System.err.println("MailService.mailSender=>" + mailSender);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * 发送简单邮件
     * @return
     */
    public Map<String, Object> sendSimpleMail() {
        Map<String, Object> ret = new HashMap<>();
        try {
        	System.err.println("开始发送简单邮件....");
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("445121408@qq.com");
            message.setTo("405727062@qq.com");
            message.setSubject("主题：简单邮件");
            message.setText("测试邮件内容");
            mailSender.send(message);
            ret.put("code", 200);
            System.err.println("发送简单邮件结束");
        }
        catch (Exception e) {
            e.printStackTrace();
            ret.put("code", 500);
            ret.put("error", e.getMessage());
        }
        return ret;
    }

    /**
     * 发送带附件邮件
     * @return
     */
    public Map<String, Object> sendAttachmentsMail() {
        Map<String, Object> ret = new HashMap<>();
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("445121408@qq.com");
            helper.setTo("405727062@qq.com");
            helper.setSubject("主题：有附件");
            helper.setText("有附件的邮件");
            FileSystemResource file = new FileSystemResource(new File("/Users/baby/work/upload/test/b3962bcc-66d4-4f4e-aa52-52b54a81785e.jpeg"));
            helper.addAttachment("美女-1.jpg", file);
            helper.addAttachment("美女-2.jpg", file);
            mailSender.send(mimeMessage);
            ret.put("code", 200);
        }
        catch (Exception e) {
            e.printStackTrace();
            ret.put("code", 500);
            ret.put("error", e.getMessage());
        }
        return ret;
    }

    /**
     * 包含静态html内容发送
     * @return
     * @throws Exception
     * @description 这里需要注意的是addInline函数中资源名称meinv需要与正文中cid:meinv对应起来
     */
    public Map<String, Object> sendInlineMail() {
        Map<String, Object> ret = new HashMap<>();
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("445121408@qq.com");
            helper.setTo("405727062@qq.com");
            helper.setSubject("主题：嵌入静态资源");
            helper.setText("<html><body><img src=\"cid:meinv\" ></body></html>", true);
            FileSystemResource file = new FileSystemResource(new File("/Users/baby/work/upload/test/b3962bcc-66d4-4f4e-aa52-52b54a81785e.jpeg"));
            // 这里需要注意的是addInline函数中资源名称meinv需要与正文中cid:meinv对应起来
            helper.addInline("meinv", file);
            mailSender.send(mimeMessage);
            ret.put("code", 200);
        }
        catch (Exception e) {
            e.printStackTrace();
            ret.put("code", 500);
            ret.put("error", e.getMessage());
        }
        return ret;

    }

    /**
     * 包含静态html内容发送
     * @return
     * @throws Exception
     * @see <a href="https://www.cnblogs.com/wjf0/p/9440775.html">参考</a>
     */
    public Map<String, Object> sendExcel() {
        Map<String, Object> ret = new HashMap<>();
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeBodyPart mbp1 = new MimeBodyPart();
            FileSystemResource file = new FileSystemResource(new File(
                    "/Users/baby/work/upload/test/aaa.xlsx"));
            byte[] bytes = StreamUtils.copyToByteArray(file.getInputStream());
            DataSource fds = new ByteArrayDataSource(bytes, "application/vnd.ms-excel");
            mbp1.setText("Everything will be fine.");
            MimeBodyPart mbp2 = new MimeBodyPart();
            mbp2.setDataHandler(new DataHandler(fds));

            mbp2.setFileName("aaa.xlsx");
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);
            mimeMessage.setContent(mp);
            mimeMessage.saveChanges();
//            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
//            helper.setFrom("445121408@qq.com");
//            helper.setTo("405727062@qq.com");
//            helper.setSubject("主题：发送Excel");
//            helper.setText("附件带Excel");
//            helper.addAttachment("aaa.xlsx", file);
            mimeMessage.addFrom(InternetAddress.parse("445121408@qq.com"));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse("405727062@qq.com,445121408@qq.com"));
            mimeMessage.setSubject("主题：发送Excel");
            mailSender.send(mimeMessage);
            ret.put("code", 200);
        }
        catch (Exception e) {
            e.printStackTrace();
            ret.put("code", 500);
            ret.put("error", e.getMessage());
        }
        return ret;

    }

    /**
     * 根据模版发送邮件
     * @return
     * @throws Exception
     * @see <a href="https://blog.csdn.net/tuoni123/article/details/79867939">Freemarker 加载模板目录的方法</a>
     */
    public Map<String, Object> sendTemplateMail() {
        Map<String, Object> ret = new HashMap<>();
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("445121408@qq.com");
            helper.setTo("405727062@qq.com");
            helper.setSubject("主题：模板邮件");
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("time", new Date());

            model.put("toUserName", "大哥");
            model.put("fromUserName", "小弟");

            List<String> strList = new ArrayList<>();
            for(int i=0; i<5; i++){
                strList.add("循环信息"+i);
            }
            model.put("messages", strList);

            List<Map<String,String>> mapList = new ArrayList<>();
            for(int i=0; i<5; i++){
                Map<String,String> m = new HashMap<>();
                m.put("title", "列表信息标题"+i);
                m.put("content", "详细内容"+i);
                mapList.add(m);
            }
            model.put("mapMsg", mapList);

            freemarkerConfig.setClassForTemplateLoading(MailService.class, "/templates");
            Template t = freemarkerConfig.getTemplate("freemarker.ftl", Locale.CHINA); // freeMarker template

            // @see <a href="https://blog.csdn.net/tuoni123/article/details/79867939">Freemarker 加载模板目录的方法</a>
            // 加载方式1
//            Configuration cfg = new Configuration();
//            cfg.setClassForTemplateLoading(FreemarkerUtil.class, "/template");
//            cfg.getTemplate("Base.ftl");

            // 加载方式2
//            Configuration cfg = new Configuration();
//            cfg.setDirectoryForTemplateLoading(new File("/home/user/template"));
//            cfg.getTemplate("Base.ftl");

            t.setOutputEncoding("utf-8");
            //File path = new File("C:\\opt\\files\\html\\");
//            File path = new File("/opt/files/html/");
//            if(!path.exists()){
//                path.mkdirs();
//            }
//
//            String p = path.getPath() + File.separator;
//
//            String fullPath = p + "freemarker.html";
//
//            Writer file = new FileWriter(new File(fullPath));
//            t.process(model, file);

            String content = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
            System.err.println("内容为：\n\t" + content);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
            ret.put("code", 200);
        }
        catch (Exception e) {
            e.printStackTrace();
            ret.put("code", 500);
            ret.put("error", e.getMessage());
        }
        return ret;
    }


}
