<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

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
</style>
</head>
<body>

	<div class="container">
		<h2>📌 크루 상세정보</h2>

		<div class="field">
			<div class="label">제목:</div>
			<div>${crew.title}</div>
		</div>

		<div class="field">
			<div class="label">설명:</div>
			<div>${crew.descriptoin}</div>
		</div>

		<div class="field">
			<div class="label">지역:</div>
			<div>${crew.districtId}</div>
		</div>

		<div class="field">
			<div class="label">작성자 ID:</div>
			<div>${crew.leaderId}</div>
		</div>

		<div class="field">
			<div class="label">작성일:</div>
			<div>
				<fmt:formatDate value="${createdDate}" pattern="yyyy-MM-dd HH:mm:ss" />

			</div>
		</div>

		<a href="/usr/walkCrew/list" class="back-link">← 목록으로 돌아가기</a>
	</div>

</body>
</html>
