<p align="center">
  <a href="https://github.com/lxwise/aurora-fx/">
    <img src="./doc/aurora-fx.png" alt="FXUpdater">
  </a>
</p>

<p align="center">
Aurora-FX 是一款高性能 JavaFX 自定义 UI 组件库，以"曙光"为名，致力于为开发者提供优雅、高效的界面开发体验。
</p>

<p align="center">
   <a target="_blank" href="https://github.com/lxwise/aurora-fx">
      <img src="https://img.shields.io/hexpm/l/plug.svg"/>
      <img src="https://img.shields.io/badge/build-maven-green"/>
      <img src="https://img.shields.io/badge/java-9%2B-%23F27E3F"/>
      <img src="https://img.shields.io/badge/javafx-21%2B-%23F27E3F"/>
   </a>
</p>

## 项目地址

**Gitee 地址：** [https://gitee.com/lxwise/aurora-fx](https://gitee.com/lxwise/aurora-fx)

**Github 地址：** [https://github.com/lxwise/aurora-fx](https://github.com/lxwise/aurora-fx)



## Star

ps: 虽然我知道，大部分人和作者菌一样喜欢白嫖，都是看了直接下载源代码后就潇洒的离开。但我还是想请各位喜欢本项目的小伙伴：**Star**，**Star**，**Star**。只有你们的**Star**本项目才能有更多的人看到，才有更多志同道合的小伙伴一起加入完善本项目。请小伙伴们动动您可爱的小手，给本项目一个**Star**。**同时也欢迎大家提交pr，一起改进项目** 。



## 安装和使用

### 1.依赖安装

您可以使用 Maven [下载](https://repo1.maven.org/maven2/io/github/lxwise/aurora-fx/)或安装：

- Maven:

```xml
<dependency>
    <groupId>io.github.lxwise</groupId>
    <artifactId>aurora-fx</artifactId>
    <version>0.0.1</version>
</dependency>
```

- Gradle:

```Groovy
dependencies {
    implementation group: 'io.github.lxwise', name: 'aurora-fx', version: '0.0.1'
}
```

### 2.代码使用

1.任务链(javafx UI执行接口查询并更新UI界面操作)

```java
  ProcessChain.create()
       .addRunnableInPlatformThread(() -> {
       // 第一步：在 JavaFX 主线程执行 UI 更新逻辑
              button.setDisable(true);
      })
      .addSupplierInExecutor(() -> {
          // 第二步：后台线程执行耗时操作 例如请求接口
          return "后台任务结果";
      })
      .addConsumerInPlatformThread(result -> {
          // 第三步：拿到接口结果 在 JavaFX 主线程更新 UI
          label.setText(result);
      })
      .onException(e -> {
          // 异常处理
          System.err.println("执行出错：" + e.getMessage());
      })
      .withFinal(() -> System.out.println("任务链结束"))
      .run();
```

2.任务链(javafx UI执行保存更新删除等操作)

```java
    ProcessChain.create()
        .addRunnableInExecutor(() -> System.err.println("执行操作请求"))
        .addRunnableInPlatformThread(() -> {
            //在 JavaFX 主线程执行 UI 更新逻辑/例如关闭弹窗,重新请求加载列表
            dialog.close();
            query();
         }).onException(e -> e.pr intStackTrace())
        .run();
```



最后，我希望我的项目能够为你带来帮助与收获。如果你有任何建议或意见，欢迎随时联系我。让我们一起分享知识，共同成长！