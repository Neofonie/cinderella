# cinderella

![travis-ci.org Build-Status](https://travis-ci.org/Neofonie/cinderella.svg?branch=master)
[ ![Download](https://api.bintray.com/packages/moeth/maven/Cinderella/images/download.svg) ](https://bintray.com/moeth/maven/Cinderella/_latestVersion)

cinderella is a java-framework to reject bad web-request (accordingly to the equal named fairy tale). 
Its possible to define rules, which requests should be avoided and which not. 
If a bad request is found, the client could enter a captcha to confirm him as a human user.
 
* The code is based on Spring-MVC and should work with everything based on this. 
* The rules can be changed at compile time or at runtime (without restarting the application server)

Table of Contents
=================

  * [cinderella](#cinderella)
    * [Getting started](#getting-started)
      * [Example](#example)
      * [From scratch](#from-scratch)
    * [Way of working](#way-of-working)
    * [Config-File](#config-file)
      * [Location](#location)
      * [Overview](#overview)
      * [Rule](#rule)
      * [Whitelist](#whitelist)
      * [Conditions](#conditions)
        * [ip](#ip)
        * [hostName](#hostname)
        * [session](#session)
        * [requestPath](#requestPath)
        * [param](#param)
        * [header](#header)
        * [userAgent](#useragent)
        * [attribute](#attribute)
        * [and](#and)
        * [or](#or)
        * [not](#not)
    * [Example Config](#example-config)
    * [Extension](#extension)
    * [Todo](#todo)

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
  <version>1.2.1</version>
</dependency>
```

#### add bintray mvn repository
```XML
<repositories>
    <repository>
        <id>bintray</id>
        <url>http://jcenter.bintray.com/</url>
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
This checked, which rules matched the current request. For every matched rule, a Counter will be increased.
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
* noResponseThreshould: the amount of requests, which where redirected to the captcha-site. 
After this, there will be no result rendered. Only the HTTP-Status will be sent. 
To disable this feature, omit the attribute or set it to 0.
* a list of conditions, which should be ever whitelisted (that should contain google-IPs, Client-Names, ...)
* a list of rules

```XML
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cinderellaXmlConfig xmlns="http://www.neofonie.de/xsd/cinderella.xsd"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www.neofonie.de/xsd/cinderella.xsd https://raw.githubusercontent.com/Neofonie/cinderella/master/cinderella-core/src/main/resources/xsd/cinderella.xsd"
                     whitelistMinutes="3" blacklistMinutes="3" noResponseThreshould="10">
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
- The Rule themselve contains a list of conditions, these could be and, or, ip, requestPath, header, session available and so on. All conditions must match to apply the rule.

for Details, look at [cinderella.xsd](https://raw.githubusercontent.com/Neofonie/cinderella/master/cinderella-core/src/main/resources/xsd/cinderella.xsd)

### Whitelist
 
Here the same conditions as for Rules can be used. The difference is, that only one rule must match to identifiy the request as whitelisted one. 

### Conditions

Conditions are used in Rules and the Whitelist. There exists some predefined rules.

#### ip
 
Checks, if the request-ip is a defined IP or IP-Range.

Examples:
```XML
<ip>127.0.0.1</ip>
<ip>127.0.0.1-127.0.0.255</ip>
<ip>2001:0db8:85a3:08d3:1319:8a2e:0370:7344-2001:0db8:85a3:09d3:1319:8a2e:0370:7344</ip>
```
The Request-IP will be extracted from:

- Request-Header "X-Forwarded-For"
- Request-Header "Proxy-Client-IP"
- Request-Header "WL-Proxy-Client-IP"
- Request-Header "HTTP_CLIENT_IP"
- Request-Header "HTTP_X_FORWARDED_FOR"
- [request.getRemoteAddr()](http://tomcat.apache.org/tomcat-7.0-doc/servletapi/javax/servlet/ServletRequest.html#getRemoteAddr())

The first value in this order will be used.

#### hostName
 
Checks, if a reverse lookup of the request-ip matched a hostname.

Examples:
```XML
<hostName>googlebot.com$</hostName>
```
The Request-IP will be extracted from [ip](#ip)

#### session

Checks, if the request sends a valid session id. 

Examples:
```XML
<session session="true"/>
<session session="false"/>
```

#### requestPath

checks, if the request (see [request.getRequestURI()](https://tomcat.apache.org/tomcat-7.0-doc/servletapi/javax/servlet/http/HttpServletRequest.html#getRequestURI()))
 matches a specific [regex-pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).

The request-Uri doesnt contain protocol, host, port and querystring.
 
The search is a regex - if you want a full-text search, use \Q<<TextToSearch>>\E. If you want that the request path must start with this text - use circumflex (^). For the ending use dollar ($).

Examples:
```XML
<requestPath>oink</requestPath>
<requestPath>^/foobar/[0-9]+/?</requestPath>
<requestPath>^\Q/foobar\E$</requestPath>
```

#### param
checks, if the request contains a request-param with a specific value. If more than one param with the name exists, all will be checked (for example in the querystring ?a=1&a=2&b=4).

The param-name is a id, but the value is any [regex-pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).

Examples:
```XML
<param name="a">^[0-9]+$</param>
<param name="a">^\Q/foobar\E$</param>
```

#### header
checks, if the request contains a request-header with a specific value. If more than one header with the name exists, all will be checked.

The param-name is a id, but the value is any [regex-pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).

Examples:
```XML
<header name="a">^[0-9]+$</param>
<header name="a">^\Q/foobar\E$</param>
```

#### userAgent
checks, if the request-header "User-Agent" matched a [regex-pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).

Examples:
```XML
<userAgent>Googlebot</userAgent>
<userAgent>\QYahoo! Slurp\E</userAgent>
```

This is a shorthand for

Examples:
```XML
<header name="User-Agent">Googlebot</header>
<header name="User-Agent">\QYahoo! Slurp\E</header>
```

#### attribute

checks, if the request contains a request-attribute with a specific value. This can only occur, if a previous filter set this attribute.
So this is a option to implement own extentions. There Request-Attribute should be a String or implement a proper toString-Method.

The attribute-name is a id, but the value is any [regex-pattern](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).

Examples:
```XML
<attribute name="a">^[0-9]+$</param>
<attribute name="a">^\Q/foobar\E$</param>
```

#### and

A logical Operation - doesnt match, if any contained condition doesnt match, otherwise this condition match too. Conditions directly in a rule behaves equal to a and-list. 

Examples:
```XML
<and>
    <param name="a">^[0-9]+$</param>
    <header name="a">^[0-9]+$</header>
</and>
```

#### or

A logical Operation - match, if any contained condition match, otherwise this condition dont match. This is the same behaviour in the whitelist-conditions.

Examples:
```XML
<or>
    <param name="a">^[0-9]+$</param>
    <header name="a">^[0-9]+$</header>
</or>
```

#### not

A logical Operation - doesnt match, if any contained condition match, otherwise this condition matches.

Examples:
```XML
<not>
    <param name="a">^[0-9]+$</param>
</not>
```
Not can contain multiple elements

```XML
<not>
    <param name="a">^[0-9]+$</param>
    <header name="a">^[0-9]+$</header>
</not>
```

is equal to

```XML
<not>
    <or>
        <param name="a">^[0-9]+$</param>
        <header name="a">^[0-9]+$</header>
    </or>
</not>
```

### Example Config

```XML
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cinderellaXmlConfig xmlns="http://www.neofonie.de/xsd/cinderella.xsd"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www.neofonie.de/xsd/cinderella.xsd ../../../../cinderella-core/src/main/resources/xsd/cinderella.xsd"
                     whitelistMinutes="30" blacklistMinutes="60" noResponseThreshould="10">
    <whitelist>
        <requestPath>^/favicon.ico$</requestPath>
        <requestPath>^/whitelist$</requestPath>
        <!-- see https://support.google.com/webmasters/answer/80553?hl=de -->
        <and>
            <userAgent>Googlebot</userAgent>
            <or>
                <hostName>googlebot.com$</hostName>
                <hostName>google.com$</hostName>
            </or>
        </and>

        <!-- see http://www.spanishseo.org/how-to-identify-user-agents-and-ip-addresses-for-bot-blocking -->
        <!--Yahoo crawlers will end with crawl.yahoo.net like in llf520064.crawl.yahoo.net.-->
        <and>
            <userAgent>\QYahoo! Slurp\E</userAgent>
            <hostName>crawl.yahoo.net$</hostName>
        </and>

        <!-- https://www.bing.com/webmaster/help/verifying-that-bingbot-is-bingbot-3905dc26 -->
        <and>
            <userAgent>bingbot</userAgent>
            <hostName>search.msn.com$</hostName>
        </and>

    </whitelist>
    <rules>
        <rule id="ip" identifierType="IP" requests="3" minutes="5">
            <session session="false"/>
        </rule>
        <rule id="session" identifierType="SESSION" requests="90" minutes="5">
            <session session="true"/>
        </rule>

        <!-- Blacklist user which claims to be a whitelisted search spider -->
        <rule id="noCrawler" identifierType="IP" requests="3" minutes="5">
            <or>
                <userAgent>Googlebot</userAgent>
                <userAgent>\QYahoo! Slurp\E</userAgent>
                <userAgent>bingbot</userAgent>
            </or>
        </rule>
    </rules>
</cinderellaXmlConfig>
```

## Extension

For production use, the datastore should be exchanged from an in-memory model to something else - for example a (NoSQL) DB or any distributed cache. 
To do this, you must implement an own de.neofonie.cinderella.core.counter.Counter Service and declare this as spring-bean.

## Changes

### 1.2.0

- after a defined number of requests, every request will render no response (and return only the status code)
 This is to avoid too penetrant clients.
- Now you can define 2 files in CinderellaXmlConfigLoaderFactory
- [hostName](#hostname) and [userAgent](#useragent) Conditions added

### 1.1.0

- change config for requestHeader
- status code 429 will be sent when blacklisted
- fix blacklist-Bug
- additional conditions (query, attribute, not)
 
## Todo

- Implement Counter for various storage systems
- Statistic Overview to view the most frequently requests
- Its currently not easyliy possible to write own conditions. I'm sorry - any ideas to implement this are welcome ;-)