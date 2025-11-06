package io.data.chain.fx.concurrent;

//import com.dtflys.forest.Forest;
//import com.dtflys.forest.config.ForestConfiguration;
//import com.dtflys.forest.converter.json.ForestFastjsonConverter;
//import io.data.chain.fx.concurrent.process.ProcessChainDemo;
import io.data.chain.fx.concurrent.process.PublishingTaskDemo;
import io.data.chain.fx.concurrent.task.ImageLoaderDemo;
import io.data.chain.fx.concurrent.task.InfinitiveProgressChainDemo;
import io.data.chain.fx.concurrent.task.ObservableExecutorDemo;
import io.data.chain.fx.concurrent.ui.ImageViewerTest;
import javafx.application.Application;

/**
 * @author lstar
 * @create 2025-11
 * @description:
 */
public class AppStart {

    public static void main(String[] args) {
//        // 获取全局配置
//        ForestConfiguration configuration = Forest.config();
//        configuration.setMaxConnections(5000);
//        configuration.setConnectTimeout(7000);
//        configuration.setReadTimeout(7000);
//        configuration.setBackendName("httpclient");
//        configuration.setJsonConverter(new ForestFastjsonConverter());

        Application.launch(ImageViewerTest.class, args);
    }
}
