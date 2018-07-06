package com.yaoyao.online.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Auther: yuanpb
 * @Date: 2018/6/13 10:42
 * @Description:ElasticSearch配置
 */
@Configuration
public class ElasticSearchConfig {

    /**
     * elk集群地址
     */
    @Value("${elasticsearch.ip}")
    private String hostName;
    /**
     * 端口
     */
    @Value("${elasticsearch.port}")
    private String port;
    /**
     * 集群名称
     */
    @Value("${elasticsearch.cluster.name}")
    private String clusterName;

    /**
     * 连接池
     */
    @Value("${elasticsearch.pool}")
    private String poolSize;

    @Bean
    public TransportClient init() {

        TransportClient transportClient = null;

        try {
            // 配置信息
            Settings esSetting = Settings.builder().put("cluster.name", clusterName)
                    .put("client.transport.ignore_cluster_name", true) // 如果集群名不对，也能连接
                     //.put("client.transport.sniff", true)// 增加嗅探机制，找到ES集群,如果未设置集群不要设置
                    .put("thread_pool.search.size", Integer.parseInt(poolSize))// 增加线程池个数，暂时设为10
                    .build();
            transportClient = new PreBuiltTransportClient(esSetting);
            TransportAddress inetSocketTransportAddress = new TransportAddress(
                    InetAddress.getByName(hostName), Integer.valueOf(port));
            transportClient.addTransportAddress(inetSocketTransportAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return transportClient;
    }

}
