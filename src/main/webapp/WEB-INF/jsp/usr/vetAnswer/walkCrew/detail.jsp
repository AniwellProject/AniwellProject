<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<title>크루 상세보기</title>
<style>
.container {
	width: 60%;
	margin: 30px auto;
	border: 1px solid #ccc;
	padding: 20px;
	border-radius: 10px;
}

h2 {
	margin-top: 0;
}

.field {
	margin: 15px 0;
}

.label {
	font-weight: bold;
}

.back-link {
	margin-top: 20px;
	display: block;
}

button[type="submit"] {
	padding: 8px 16px;
	background-color: #4CAF50;
	color: white;
	border: none;
	border-radius: 5px;
	cursor: pointer;
	margin-top: 10px;
}
</style>
</head>
<body>

	<div class="container">

		<!-- ✅ 참가 메시지 출력 영역 -->
		<c:if test="${not empty param.msg}">
			<div
				style="background-color: #e0ffe0; border: 1px solid #4CAF50; padding: 10px; margin-bottom: 20px; text-align: center; color: #2e7d32; font-weight: bold;">
				${param.msg}</div>
		</c:if>


		<h2>📌 크루 상세정보</h2>

		<div class="field">
			<div class="label">제목:</div>
			<div>${crew.title}</div>
		</div>

		<div class="field">
			<div class="label">설명:</div>
			<div>${crew.description}</div>
		</div>

		<div class="field">
			<div class="label">지역:</div>
			<div>${crewLocation}</div>
			<!-- ✔️ 예: 서울특별시 종로구 청운동 -->
		</div>


		<div class="field">
			<div class="label">작성일:</div>
			<div>
				<fmt:formatDate value="${createdDate}" pattern="yyyy-MM-dd HH:mm:ss" />
			</div>
		</div>

		<!-- 참가 버튼 -->
		<c:if test="${rq.loginedMemberId ne crew.leaderId}">
			<form method="post" action="/usr/walkCrew/join">
				<input type="hidden" name="crewId" value="${crew.id}" />
				<button type="submit">참가하기</button>
			</form>
		</c:if>

		<a href="/usr/walkCrew/list" class="back-link">← 목록으로 돌아가기</a>
	</div>

</body>
</html>
