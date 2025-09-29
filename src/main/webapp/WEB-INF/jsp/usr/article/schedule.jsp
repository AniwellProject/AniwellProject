<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<html>
<head>
<title>일정 목록</title>
<style>
body {
	font-family: Arial, sans-serif;
	margin: 20px auto;
	max-width: 900px;
}

h1 {
	text-align: center;
	margin-bottom: 30px;
}

table {
	width: 100%;
	border-collapse: collapse;
	margin-top: 20px;
}

th, td {
	padding: 12px;
	border: 1px solid #ccc;
	text-align: center;
}

th {
	background-color: #f4f4f4;
}

.no-data {
	text-align: center;
	margin-top: 50px;
	font-size: 18px;
	color: #888;
}
</style>
</head>
<body>

	<h1>📅 일정 목록</h1>

	<c:choose>
		<c:when test="${not empty scheduleList}">
			<table>
				<thead>
					<tr>
						<th>날짜</th>
						<th>제목</th>
						<th>내용</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="schedule" items="${scheduleList}">
						<tr>
							<td>
								<fmt:formatDate value="${schedule.scheduleDate}" pattern="yyyy-MM-dd" />
							</td>
							<td>${schedule.title}</td>
							<td>${schedule.body}</td>

						</tr>
					</c:forEach>
				</tbody>
			</table>
		</c:when>
		<c:otherwise>
			<div class="no-data">등록된 일정이 없습니다.</div>
		</c:otherwise>
	</c:choose>

</body>
</html>
