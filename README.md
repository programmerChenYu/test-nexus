

# test-nexus 🚀  

![TestNG-Compatible](https://img.shields.io/badge/TestNG-Compatible-brightgreen) ![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

---

## 🌟 项目简介  
**test-nexus** 是一个基于 TestNG 深度封装的轻量级 IOC 框架，旨在为测试领域提供简洁高效的依赖管理能力。通过注解驱动和三级缓存机制，无缝集成 TestNG 测试流程，让复杂测试场景的依赖管理化繁为简，助力开发者聚焦核心测试逻辑。  

📌 **核心理念**：  
**"测试世界的技术枢纽，让复杂测试化繁为简"**  

---

## 🎯 核心特性  

### 📦 **注解驱动的 IOC 容器**  
- **核心注解**：`@Component`, `@Configuration`, `@Bean`, `@ComponentScan`, `@Autowired` , `@TestNexus`  
- **生命周期控制**：`@InitBefore`, `@InitAfter`, `@Destroy`  
- **支持作用域**：单例（Singleton） / 原型（Prototype）  

### 🔄 **三种依赖注入方式**  
1. **属性注入**：直接通过 `@Autowired` 注入字段  
2. **方法注入**：支持任意方法参数自动装配  
3. **构造器注入**：自动识别构造函数参数  

### ⚙️ **全生命周期管理**  
```java
@Component
public class DemoBean {
    @InitBefore
    public void init() { /* 初始化前逻辑 */ }

    @InitAfter
    public void postInit() { /* 初始化后逻辑 */ }

    @Destroy
    public void cleanup() { /* 销毁前逻辑 */ }
}
```

### 🧠 **循环依赖解决方案**  
使用 **三级缓存机制** 确保稳定性，解决循环依赖问题：  
`BeanDefinitions` → `EarlySingletonObjects` → `SingletonObjects`

### 🛠️ **设计模式加持**  
- **工厂方法模式**：Bean 实例化过程标准化  
- **责任链模式**：Bean 生命周期阶段化处理  

---

## 🚀 快速开始  

### 1. 添加依赖  
```xml
<dependency>
    <groupId>io.github.programmerchenyu</groupId>
    <artifactId>test-nexus</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 2. 基础用法示例  
**配置类**  
```java
@Configuration
@ComponentScan("com.example")
public class AppConfig {
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource();
    }
}
```

**组件类**  
```java
@Component
public class UserService {
    @Autowired
    private DataSource dataSource;
}
```

**测试类**  
```java
@TestNexus(classes={AppConfig.class})
public class UserTest implements ITestNexusContext {
    @Autowired
    private UserService userService;

    @Test
    public void testUserOperation() {
        // 测试逻辑
    }
}
```

### 3. 最佳实践

开源项目 BaseTestFramework 使用 test-nexus 作为基础测试框架整合了 Selenium 和 Appium，使其能以更简洁的代码风格，更少量的对象创建，来完成复杂场景的更高性能的自动化测试。

- BaseTestFramework 项目地址：https://github.com/programmerChenYu/BaseTestFramework

---

## 📚 详细文档  
### 注解详解  
| 注解             | 作用域           | 说明                           |
| ---------------- | ---------------- | ------------------------------ |
| `@Component`     | 类               | 标记为可被扫描的组件           |
| `@Autowired`     | 字段/方法/构造器 | 自动装配依赖项                 |
| `@ComponentScan` | 配置类           | 指定组件扫描路径               |
| `@Comfiguration` | 配置类           | 指定配置类                     |
| `@Bean`          | 生成 bean 的方法 | 在配置类中生命引入的 bean      |
| `@InitBefore`    | 生命周期方法     | 提供 bean 对象初始化前的入口   |
| `@InitAfter`     | 生命周期方法     | 提供 bean 对象初始化后的入口   |
| `@Destroy`       | 生命周期方法     | 提供 bean 对象销毁前的入口     |
| `@TestNexus`     | 测试类           | 标记测试类使用 test-nexus 框架 |

### 作用域控制  
```java
// 单例
@Autowired(singleton=true)
private UserService userService;

// 原型
@Autowired(singleton=false)
private UserService userService;
```

---

## 🌈 未来规划  
- **AOP 特性支持**：通过注解实现切面编程  
- **条件化 Bean 注册**：`@ConditionalOnProperty` 等条件注解  
- **扩展生态**：接入 Mock 工具链支持  

---

## 🤝 参与贡献  
欢迎提交 Issue 或 PR！  
---

## 📜 许可证  

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 

---

**⭐ 如果这个项目帮助了你，欢迎 Star 支持！**  
**让测试变得更优雅，是我们永恒的追求~**
