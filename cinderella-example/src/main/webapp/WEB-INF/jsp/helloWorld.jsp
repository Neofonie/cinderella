<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<body>

<div>Hello World!</div>
<div>IP: ${ip}</div>

<table>
    <tr>
        <th colspan="3">Counter</th>
    </tr>
    <c:forEach items="${statistic.counter}" var="counter">
        <tr>
            <th>${counter.key}</th>
            <td>${counter.validUntil}</td>
            <td>${counter.count}</td>
        </tr>
    </c:forEach>

    <tr>
        <th colspan="3">Whitelist</th>
    </tr>
    <c:forEach items="${statistic.whitelist}" var="counter">
        <tr>
            <th>${counter.key}</th>
            <td>${counter.validUntil}</td>
            <td>${counter.count}</td>
        </tr>
    </c:forEach>

    <tr>
        <th colspan="3">Blacklist</th>
    </tr>
    <c:forEach items="${statistic.blacklist}" var="counter">
        <tr>
            <th>${counter.key}</th>
            <td>${counter.validUntil}</td>
            <td>${counter.count}</td>
        </tr>
    </c:forEach>
</table>
<div>Count: ${count}</div>

</body>
</html>
