# commandLineRunner

## 问题描述：

当写spring-boot的控制台程序，或者为web程序增加了CommandLineRunner之后，在写单元测试时，会自动执行CommandLineRunner中的代码，导致单元测试无法正常进行，严重影响开发进度和效率。

## 解决方案

在主类中

```java
@SpringBootApplication
@Profile("!test")
public class App implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void run(String... arg0) throws Exception {
        System.out.println("run!");
    }

}
```

“!test” 表示该CommandLineRunner中的程序会在除了名为test的profile之外的地方执行，即排除掉test。

```java
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class AppTest {
    @Test
    public void contextLoads() {
    }
}
```

这里将该测试文件标记为test

## 但有时也没有用
直接用maven命令行：mvn install -Dmaven.test.skip=true
