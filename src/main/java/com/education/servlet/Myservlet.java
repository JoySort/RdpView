package com.education.servlet;

import com.education.config.GuacamoleConfig;
import com.education.config.RdpViewConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.soap.Addressing;
import java.util.Arrays;
import java.util.List;

/**
 * @author: ruoniao@gmail.com
 * @date 2019/3/16 21:43
 * Explain:
 */
@Slf4j
public class Myservlet extends GuacamoleHTTPTunnelServlet {

    @Autowired
    private GuacamoleConfig guacamoleConfig;

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request)
            throws GuacamoleException {
        try {
            List<String> info = Arrays.asList(request.getRequestURI().split("/rdpview/")[1].split("&"));
            // Create our configuration
            GuacamoleConfiguration config = new GuacamoleConfiguration();
            config.setProtocol("rdp");
            config.setParameter("hostname", info.get(0));
            config.setParameter("port", info.get(1));
            config.setParameter("username", info.get(2));
            config.setParameter("password", info.get(3));
            config.setParameter("width",info.get(4));
            config.setParameter("height",info.get(5));
            String gateway=info.get(6);
            if(gateway==null || gateway.equals("")){
                gateway="127.0.0.1";
            }
            // 在windows 机器下，默认的安全远程连接方式:仅允许运行使用网路级别身份验证的远程桌面的计算机连接(更安全) 若不将认证模式改为NLA
            //网络级验证（network level authentication NLA）是提供给远程桌面连接的一种新安全验证机制，可以在终端桌面连接及登录画面出现前预先完成用户验证程序，由于提前验证部分仅需要使用到较少的网络资源，因此可以有效防范黑客与恶意程序的攻击，同时降低阻断服务攻击(Dos)的机会。

            config.setParameter("security","nla");
            // 忽略证书验证，否则guaced 会报错
            config.setParameter("ignore-cert","true");
            config.setParameter("color-depth","8");

            // Connect to guacd - everything is hard-coded here.
            GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                    // Todo 学习Servlet 整合springboot 将配置放在yml中，之前一直未成功
                    new InetGuacamoleSocket(gateway, 4822),
                    config
            );

            // Return a new tunnel which uses the connected socket
            return new SimpleGuacamoleTunnel(socket);
        } catch (GuacamoleException e){
            log.error("启动rdp错误,无法启动GuacamoleTunnel",e.getMessage());
            //暂时不处理
            return null;
        }

    }
}
