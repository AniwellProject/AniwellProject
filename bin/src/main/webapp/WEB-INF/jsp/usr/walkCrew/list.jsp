<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<title>크루 모집 목록</title>
<style>
table {
	width: 80%;
	border-collapse: collapse;
	margin: 20px auto;
}

th, td {
	border: 1px solid #888;
	padding: 10px;
	text-align: center;
}

th {
	background-color: #f1f1f1;
}

a.button {
	display: inline-block;
	padding: 6px 12px;
	background-color: #4CAF50;
	color: white;
	text-decoration: none;
	border-radius: 4px;
}
</style>
</head>
<body>

	<h2 style="text-align: center;">🚶‍♀️ 크루 모집 리스트</h2>

	<div style="text-align: center; margin-bottom: 20px;">
		<a href="/usr/walkCrew/create" class="button">크루 등록</a>
	</div>

	<table>
		<thead>
			<tr>
				<th>ID</th>
				<th>제목</th>
				<th>지역</th>
				<th>작성자</th>
				<th>작성일</th>
				<th>상세보기</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="crew" items="${crews}">
				<tr>
					<td>${crew.id}</td>
					<td>${crew.title}</td>
					<td>${crew.districtId}</td>
					<td>${crew.leaderId}</td>
					<td>
						<fmt:formatDate value="${createdDate}" pattern="yyyy-MM-dd HH:mm:ss" />

					</td>
					<td>
						<a href="/usr/walkCrew/detail/${crew.id}" class="button">보기</a>
					</td>
				</tr>
			</c:forEach>
			<c:if test="${empty crews}">
				<tr>
					<td colspan="6">등록된 크루가 없습니다.</td>
				</tr>
			</c:if>
		</tbody>
	</table>

</body>
</html>
