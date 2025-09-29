<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<html>
<head>
<title>참가 신청자 상세 정보</title>
<style>
.container {
	width: 60%;
	margin: 30px auto;
	border: 1px solid #ccc;
	padding: 20px;
	border-radius: 10px;
	background-color: #f9f9f9;
}

h2 {
	text-align: center;
	margin-bottom: 20px;
}

.field {
	margin: 15px 0;
}

.label {
	font-weight: bold;
	margin-bottom: 5px;
}

.back-link {
	margin-top: 20px;
	display: block;
	text-align: center;
	text-decoration: none;
	color: #333;
}

.back-link:hover {
	text-decoration: underline;
}

.button-group {
	text-align: center;
	margin-top: 30px;
}

.button-group form {
	display: inline-block;
	margin: 0 10px;
}

.button-group button {
	padding: 8px 16px;
	font-size: 14px;
	border: none;
	border-radius: 5px;
	cursor: pointer;
}

.accept-btn {
	background-color: #4CAF50;
	color: white;
}

.reject-btn {
	background-color: #f44336;
	color: white;
}
</style>
</head>
<body>

	<div class="container">
		<h2>🙋 참가 신청자 상세정보</h2>

		<div class="field">
			<div class="label">닉네임:</div>
			<div>${applicant.memberNickname}</div>
		</div>

		<div class="field">
			<div class="label">주소:</div>
			<div>${applicant.memberAddress}</div>
		</div>

		<div class="field">
			<div class="label">권한:</div>
			<div>
				<c:choose>
					<c:when test="${applicant.authLevel == 7}">관리자</c:when>
					<c:when test="${applicant.authLevel == 3}">수의사</c:when>
					<c:otherwise>일반</c:otherwise>
				</c:choose>
			</div>
		</div>

		<div class="field">
			<div class="label">신청일:</div>
			<div>${fn:replace(fn:substring(applicant.joinedAt, 0, 16), 'T', ' ')}</div>
		</div>

		<!-- ✅ 수락 / 거절 버튼 -->
		<div class="button-group">
			<form action="/usr/walkCrew/approveApplicant" method="post">
				<input type="hidden" name="crewId" value="${crewId}" />
				<input type="hidden" name="memberId" value="${applicant.memberId}" />
				<button type="submit">✅ 수락</button>
			</form>

			<form action="/usr/walkCrew/rejectApplicant" method="post">
				<input type="hidden" name="crewId" value="${crewId}" />
				<input type="hidden" name="memberId" value="${applicant.memberid}" />
				<button type="submit" class="reject-btn">거절</button>
			</form>
		</div>

		<a class="back-link" href="/usr/walkCrew/requestList?crewId=${crewId}">← 신청자 목록으로 돌아가기</a>
	</div>

</body>
</html>
