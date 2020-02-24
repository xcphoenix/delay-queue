package top.xcphoenix.delayqueue.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author      xuanc
 * @date        2019/12/29 下午5:37
 * @version     1.0
 */
@ConfigurationProperties(prefix = "delayqueue.cluster")
public class ClusterConfigurationProperties {

    private List<String> nodes;
    private Integer maxRedirects;

    public List<String> getNodes() {
        return this.nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public Integer getMaxRedirects() {
        return this.maxRedirects;
    }

    public void setMaxRedirects(Integer maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

}
