# Руководство по интеграции ReportPortal

Данное руководство является сокращенной адаптацией официальной документации.
C оригиналом можно ознакомиться [https://reportportal.io/docs](здесь).

ReportPortal разворачивается с помощью Docker, Docker-Compose.

### Установка c помощью Docker, Docker-Compose

1. Установить [https://www.docker.com/get-started](Docker)
2. Загрузить последнюю версию docker-compose.yml c помощью команды в терминале 
`curl -LO https://raw.githubusercontent.com/reportportal/reportportal/master/docker-compose.yml`
Также его можно скачать [https://github.com/reportportal/reportportal/blob/master/docker-compose.yml](отсюда).
   
При развертывании ReportPortal на Windows необходимо внести следующие корректировки в docker-compose.yml:
a) Для контейнера postgres установить значение **volumes**`- postgres:/var/lib/postgresql/data`
b) Раскомментить следующую запись
`# Docker volume for Windows host
volumes:
postgres:`

3.  Запустить сервис командой в терминале:
`docker-compose -p reportportal up -d --force-recreate`
  
4. Открыть в браузере IP-адрес, по которому развернут сервис, с указанием порта 8080 ([http://localhost:8080](по умолчанию))
Узнать текущий IP адрес хоста можно с помощью следующих команд для Docker:

  Mac/Windows: 
`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' CONTAINER_ID_OR_NAME`

   Linux: 
`curl ifconfig.co`

ReportPortal address:
`http://IP_ADDRESS:8080  ` 

Для авторизации используются следующая пара логин/пароль:
   Пользователь по-умолчанию: default\1q2w3e
   Администратор: superadmin\erebus
   
В целях безопасности после первой авторизации следует сменить пароль администратора.

### Создание проекта

Для создания проекта необходимо

- залогиниться под учеткой администратора
- открыть выпадающий список, кликнув по иконке пользователя в левом нижнем углу
- Нажать кнопку 'Administrative'
- В правом верхнем углу нажать кнопку 'Add Project'
- Указать наименование проекта и нажать кнопку 'Submit'

### Интеграция

Для осуществления интеграции с проектом необходимо внесение клиентской части кода ReportPortal 
в автоматизированные тесты проекта. Они состоят из:

   - `client-*` - интеграция API. Клиенты HTTP, которые используют протокол HTTP для отправки запросов. 
     В том числе для `Java client-java-*`
   - `agent-*` - интеграция фреймворков. Кастомные репортеры/листенеры, которые отслеживают тестовые события/триггеры, 
     обращаясь через `client-*`
   - `logger-*` - интеграция логов. Логгеры, собирающие логи, обращаются к тест-кейсам через `agent-*` 
     и отправляют данные на сервер через `client-*`


## Интеграция ReportPortal c фреймворком JUnit5
Для использования ReportPortal c фреймворком JUnit5 необходимо:
- Создать папки **_/META-INF/services_** в папке **_resources_**
- Добавить в них файл с именем **_org.junit.jupiter.api.extension.Extension_**
- Добавить исполнение по-умолчанию в файл    **_com.epam.reportportal.junit5.ReportPortalExtension_**

Например:
__/META-INF/services/org.junit.jupiter.api.extension.Extension__
```none
com.epam.reportportal.junit5.ReportPortalExtension
```
### Maven

```xml
<dependency>
   <groupId>com.epam.reportportal</groupId>
   <artifactId>agent-java-junit5</artifactId>
   <version>5.1.0-RC-1</version>
</dependency>
```

#### Автоматическая регистрация дополнения (опционально)

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0-M5</version>
            <configuration>
                <properties>
                    <configurationParameters>
                        junit.jupiter.extensions.autodetection.enabled = true
                    </configurationParameters>
                </properties>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Gradle

```groovy
repositories {
    mavenLocal()
    mavenCentral()
}

testCompile 'com.epam.reportportal:agent-java-junit5:5.1.0-RC-1'
```

#### Автоматическая регистрация дополнения (опционально)

```groovy
test {
    useJUnitPlatform()
    systemProperty 'junit.jupiter.extensions.autodetection.enabled', true
}
```

# Пошаговое руководство по настройке существующего проекта Maven

Во-первых, проверьте, что у вас установлен Report Portal, инструкция по установке [тут](http://reportportal.io/docs/Installation-steps)

Предположим, что Report Portal установлен и работает по адресу <http://localhost:8080>

## Шаг 1. Конфигурация pom.xml

#### 2.1 Добавьте следующую зависимость:
```xml
<dependency>
    <groupId>com.epam.reportportal</groupId>
    <artifactId>agent-java-junit5</artifactId>
    <version>5.1.0-RC-1</version>
</dependency>
```
> Последнюю версию агента можно найти [здесь](https://search.maven.org/search?q=g:%22com.epam.reportportal%22%20AND%20a:%22agent-java-junit5%22)

#### 2.2 Добавьте оболочку логгера Report Portal
Report Portal предоставляет свою реализацию для крупнейших фреймворков типа *log4j* и *logback*

*Добавление зависимости ReportPortal logback logger *
```xml
<dependency>
    <groupId>com.epam.reportportal</groupId>
    <artifactId>logger-java-logback</artifactId>
    <version>5.0.3</version>
</dependency>
```
> Последнюю версию берем [тут](https://search.maven.org/search?q=g:%22com.epam.reportportal%22%20AND%20a:%22logger-java-logback%22)

*Оригинальный logback*
```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.2.3</version>
</dependency>
```

*Добавление зависимости ReportPortal log4j logger *
```xml
<dependency>
    <groupId>com.epam.reportportal</groupId>
    <artifactId>logger-java-log4j</artifactId>
    <version>5.0.3</version>
</dependency>
```
> Последнюю версию берем [тут](https://search.maven.org/search?q=g:%22com.epam.reportportal%22%20AND%20a:%22logger-java-log4j%22)

*Оригинальный log4j*
```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>2.11.2</version>
</dependency>

<dependency>
     <groupId>org.apache.logging.log4j</groupId>
     <artifactId>log4j-core</artifactId>
     <version>2.11.2</version>
</dependency>
```

## Шаг 3. Добавление тестов с логгированием.

#### 3.1 Метод добавления простых тестов

Создайте тестовый класс `MyTests` в тестовой директории и добавьте в него тестовый метод, использующий JUnit 5.

```java
package com.mycompany.tests;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

public class MyTests {

    private static final Logger LOGGER = LogManager.getLogger(MyTests.class);

    @Test
    void testMySimpleTest() {
        LOGGER.info("Hello from my simple test");
    }
}
```

#### 3.2 Add `log4j2.xml` файл в папку `resources` 
*Пример:*
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="ConsoleAppender" target="SYSTEM_OUT">
            <PatternLayout
                    pattern="%d [%t] %-5level %logger{36} - %msg%n%throwable"/>
        </Console>
        <ReportPortalLog4j2Appender name="ReportPortalAppender">
            <PatternLayout
                    pattern="%d [%t] %-5level %logger{36} - %msg%n%throwable"/>
        </ReportPortalLog4j2Appender>
    </Appenders>
    <Loggers>
        <Root level="DEBUG">
            <AppenderRef ref="ConsoleAppender"/>
            <AppenderRef ref="ReportPortalAppender"/>
        </Root>
    </Loggers>
</Configuration>
```
Необходимо добавить `ReportPortalAppender` (как в примере)

С этого момента, структура проекта должна выглядеть примерно так:

![Структура проекта](integration_manual_files/step_project_structure.png)

## Шаг 4. Конфигурация Report Portal

#### 4.1 Открываем ReportPortal UI

Открываем *http:$IP_ADDRESS_OF_REPORT_PORTAL:8080* (по-умолчанию *http://localhost:8080*)

Логинимся как **Admin** и создаем новый проект(подробно [тут](http://reportportal.io/docs/Deploy-ReportPortal) и [тут](http://reportportal.io/docs/Creation-of-project))

![RP. Добавление проекта 1](integration_manual_files/step_add_project.png)

![RP. Добавление проекта 2](integration_manual_files/step_add_project2.png)

#### 4.2 Добавьте пользователей проекта:

Перейти в *Administrative* -> *My Test Project* -> *Members* -> *Add user*
> Пример ссылки *http://localhost:8080/ui/#administrate/project-details/my_test_project/members*

![RP. Добавление пользователя](integration_manual_files/step_add_user.png)

## Шаг 5. Связать ReportPortal с имеющимися автотестами. 

#### 5.1 - Добавление `reportportal.properties`

После создания нового пользователя проекта вы можете получить файл `reportportal.properties` со страницы профиля пользователя.

В категории *Configuration Examples* находится пример файла `reportportal.properties` для этого пользователя.

![RP. Профиль пользователя](integration_manual_files/step_user_profile.png)

Вернитесь в код проекта и создайте файл`reportportal.properties` в папке `resources` и скопируйте в нее
содержимое со страницы пользователя из предыдущего пункта.

*Пример:*
```properties
[reportportal.properties]
rp.endpoint = http://localhost:8080
rp.uuid = d50810f1-ace9-44fc-b1ba-a0077fb3cc44
rp.launch = jack_TEST_EXAMPLE
rp.project = my_test_project
rp.enable = true
```

> Более подробно о файле `reportportal.properties` [здесь](https://github.com/reportportal/client-java)

#### 5.2 - Регистрация агента  Report Portal в JUnit 5
Есть два пути поключить расширешие ReportPortal к вашим тестам:
- С помощью аннотации `@ExtendWith` 
- С указанием локации сервиса

##### Регистрация расширения ReportPortal через аннотации
Каждый тест, аннотированный как `@ExtendWith(ReportPortalExtension.class)` будет залоггирован на ReportPortal
Это наследуемая аннотация, таким образом ее можно разместить в родительском классе, а все дочерние классы также будут
ее использовать.

Например:
```java
import com.epam.reportportal.junit5.ReportPortalExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(ReportPortalExtension.class)
public class EnumParametersTest {

	public enum TestParams {
		ONE,
		TWO
	}

	@ParameterizedTest
	@EnumSource(TestParams.class)
	public void testParameters(TestParams param) {
		System.out.println("Test: " + param.name());
	}

}
```

##### Регистрация расширения ReportPortal через сервисную локацию
[https://github.com/reportportal/agent-java-junit5/blob/develop/README.md#register-reportportal-extension-through-service-location][Подробное описание здесь]

## Шаг 6. Получение результатов тестового прогона. 

После того как агент JUnit 5 для Report Portal будет привязан, после запуска тестов будут доступны результаты
в интерфейсе Report Portal.
Для этого нужно залогиниться на ReportPortal и перейти  во вкладку *Launches*

*Пример:*

![RP. Launches](integration_manual_files/step_launches.png)

![RP. Test Results](integration_manual_files/step_test_results.png)


    

   
[здесь]: https://reportportal.io/docs/Deploy-with-Docker

[отсюда]: https://github.com/reportportal/reportportal/blob/master/docker-compose.yml

[адрес]: http://localhost:8080

[Подробное описание здесь]: https://github.com/reportportal/agent-java-junit5/blob/develop/README.md#register-reportportal-extension-through-service-location
