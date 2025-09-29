<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>건강 로그</title>
    <style>
        body { font-family: 'SUIT', sans-serif; padding: 20px; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ccc; padding: 10px; text-align: center; }
        th { background-color: #ffe0f0; }
    </style>
</head>
<body>

<h2>🐾 건강 로그 (petId=${petId})</h2>

<table>
    <thead>
    <tr>
        <th>날짜</th>
        <th>사료(g)</th>
        <th>물(g)</th>
        <th>배변 횟수</th>
        <th>소리값</th>
        <th>비고</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="log" items="${logs}">
        <tr>
            <td>${log.logDate}</td>
            <td>${log.foodWeight}</td>
            <td>${log.waterWeight}</td>
            <td>${log.litterCount}</td>
            <td>${log.soundLevel}</td>
            <td>${log.notes}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>

</body>
</html>
