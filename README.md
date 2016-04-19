# cinderella

![travis-ci.org Build-Status](https://travis-ci.org/Neofonie/cinderella.svg?branch=master)

cinderella is a java-framework to reject bad web-request (accordingly to the equal named fairy tale). 
Its possible to define rules, which requests should be avoided and which not. 
If a bad request is found, the client could enter a captcha to confirm him as a human user.
 
* The code is based on Spring-MVC and should work with everything based on this. 
* The rules can be changed at compile time or at runtime (without restarting the application server)
 
## Getting started

### Example

There is a full runable example under https://github.com/Neofonie/cinderella/tree/master/cinderella-example

1. Check it out via ```git clone https://github.com/Neofonie/cinderella.git```
2. build it with maven ```mvn clean install```
3. now under cinderella-example/target/ is a cinderella-example-*.war file which can be deployed at every Servlet-Container.

### From scratch

#### add a mvn dependency

```XML
<dependency>
  <groupId>de.neofonie.cinderella</groupId>
  <artifactId>cinderella-core</artifactId>
  <version>1.0.0</version>
</dependency>
```

#### add mvn repository
```XML
<repositories>
    <repository>
        <id>moeth-bintray</id>
        <url>https://dl.bintray.com/moeth/maven/</url>
    </repository>
</repositories>
```
    
#### add filter to web.xml

```XML
<filter>
    <filter-name>cinderellaFilter</filter-name>
    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
</filter>
<filter-mapping>
    <filter-name>cinderellaFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

#### add spring-beans to application-config

add following entries to your a application-config. 
Its always directly located under WEB-INF, but the name differs depending what name you have configured. 

```XML
<import resource="classpath*:spring-cinderella-common-config.xml"/>

<bean id="cinderellaFilter" class="de.neofonie.cinderella.core.CinderellaFilter"
      p:ddosJsp="/WEB-INF/jsp/cinderella.jsp"/>
<bean id="cinderellaXmlConfigLoader"
      class="de.neofonie.cinderella.core.config.loader.CinderellaXmlConfigLoaderFactory"
      p:xmlConfigPath="classpath:cinderellaConfig.xml"/>
<bean id="cinderellaMemoryCounter" class="de.neofonie.cinderella.core.counter.MemoryCounter"/>
```

#### Create a cinderella.jsp under /jsp/cinderella.jsp

Create a jsp which would be shown if a request would be abandon. If you dont like the path, you could change it in the application-config.

If you want the user to verify him as human, put the following html-fragment inside the jsp.

```HTML
<form action="${requestUrl}">
    <img src="jcaptcha.png"/> <input type="text" name="captcha" value=""/>
    <input type="submit"/>
</form>
```

#### Create a rules-config

Create a cinderellaConfig.xml inside the resource folder.

Here is an example, which can be used for the first run. But this should be adjusted for production-use. 

```XML
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cinderellaXmlConfig xmlns="http://www.neofonie.de/xsd/cinderella.xsd"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www.neofonie.de/xsd/cinderella.xsd https://raw.githubusercontent.com/Neofonie/cinderella/master/cinderella-core/src/main/resources/xsd/cinderella.xsd"
                     whitelistMinutes="3" blacklistMinutes="3">
    <whitelist>
        <requestPath>^/favicon.ico$</requestPath>
        <requestPath>^/whitelist$</requestPath>
    </whitelist>
    <rules>
        <rule id="ip" identifierType="IP" requests="3" minutes="5">
            <session session="false"/>
        </rule>
        <rule id="session" identifierType="SESSION" requests="7" minutes="5">
            <session session="true"/>
        </rule>
    </rules>
</cinderellaXmlConfig>
```

#### Summary

Now you should have added or modified following files (additional to your project-files).

```
├── pom.xml
└── src
    └── main
        ├── resources
        │   └── cinderellaConfig.xml
        └── webapp
            └── WEB-INF
                ├── jsp
                │   └── cinderella.jsp
                ├── spring-servlet.xml (name can differ)
                └── web.xml
```

## Way of working

Every Request will be tracked by the servlet-filter CinderellaFilter. 
This checked, which rules matched the current request. For every matched request, a Counter will be increased.
If a counter reached its limit within a certain time, the IP or SessionId will be blacklisted for a configurable period of time.

If the IP or SessionId is blacklisted, it will redirect the request to a error-site. 
On this site, its possible to enter a captcha, with what the IP/Sessionid will be whitelisted for a certain period of time.

## Config-File

### Location

The rules-file could be stored in the classpath or everywhere else in the filesystem. The location must be set in the cinderellaXmlConfigLoader.

```XML
<bean id="cinderellaXmlConfigLoader"
   class="de.neofonie.cinderella.core.config.loader.CinderellaXmlConfigLoaderFactory"
   p:xmlConfigPath="classpath:cinderellaConfig.xml"/>
```

If the file is not contained in a war/jar, changes will be detected automatically and the changed rules will be loaded immediately. 
So for production use "classpath:cinderellaConfig.xml" should be replaced with path outside of the classpath or a [spring-property placeholder](http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#beans-factory-extension-factory-postprocessors) sould be used.

### Overview

The rules config contains currently 3 parts.

* the time in minutes, for which IP/SessionIds are black/whitelisted
* a list of conditions, which should be ever whitelisted (that should contain google-IPs, Client-Names, ...)
* a list of rules

```XML
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cinderellaXmlConfig xmlns="http://www.neofonie.de/xsd/cinderella.xsd"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www.neofonie.de/xsd/cinderella.xsd https://raw.githubusercontent.com/Neofonie/cinderella/master/cinderella-core/src/main/resources/xsd/cinderella.xsd"
                     whitelistMinutes="3" blacklistMinutes="3">
    <whitelist>
        Whitelist section... 
    </whitelist>
    <rules>
        Rules ...
    </rules>
</cinderellaXmlConfig>
```

### Rule 

```XML
<rule id="ip" identifierType="IP" requests="3" minutes="5">
    <session session="false"/>
</rule>
```

- id: the Id is used as id for the counter. It should be unique and not changed, or the counter values will get lost or corrupt.
- identifierType: if requests should be counted/blacklisted/whitelisted by Ip or by Session. 
If the request doesnt contain a SessionId, these rules will be ignored.
- requests: This amount of requests is allowed, but not more.
- minutes: After this amount of minutes, the requests-Counter will be reseted.
- The Rule themselve contains a list of conditions, these could be 
-- and
-- or
-- ip
-- requestPath
-- header
-- ... to be continued

for Details, look at [cinderella.xsd](https://raw.githubusercontent.com/Neofonie/cinderella/master/cinderella-core/src/main/resources/xsd/cinderella.xsd)
 
### Whitelist
 
Here the same conditions as for Rules can be used. 

## Extension

For production use, the datastore should be exchanged from an in-memory model to something else - for example a (NoSQL) DB or any distributed cache. 
To do this, you must implement an own de.neofonie.cinderella.core.counter.Counter Service and declare this as spring-bean.

## Todo

- Implement Counter for various storage systems
- Statistic Overview to view the most frequently requests