<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<html>
<head>
<title>크루 모집 리스트</title>

<script>
	// ✅ 로그인 여부에 따라 분기 처리
	function goToCreate(isLogined) {
		if (isLogined) {
			location.href = '/usr/walkCrew/create';
		} else {
			alert('로그인 후 이용해주세요.');
			location.href = '/usr/member/login';
		}
	}
</script>

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

a.button, button.button {
	display: inline-block;
	padding: 6px 12px;
	background-color: #4CAF50;
	color: white;
	text-decoration: none;
	border-radius: 4px;
	cursor: pointer;
}
</style>
</head>
<body>

	<h2 style="text-align: center;">🚶‍♀️ 크루 모집 리스트</h2>

	<!-- ✅ 로그인 여부에 따라 동작 달라지는 버튼 -->
	<div style="text-align: center; margin-bottom: 20px;">
		<button class="button"
			onclick="goToCreate(${rq != null && rq.logined})">크루 등록</button>
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
					<td><c:choose>
							<c:when test="${not empty crew.city}">
								${crew.city} ${crew.district} ${crew.dong}
							</c:when>
							<c:otherwise>-</c:otherwise>
						</c:choose></td>
					<td><c:out value="${crew.nickname}" default="알 수 없음" /></td>
					<td>${crew.createdAt.toLocalDate()}</td>
					<td><a href="/usr/walkCrew/detail/${crew.id}" class="button">보기</a>
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
