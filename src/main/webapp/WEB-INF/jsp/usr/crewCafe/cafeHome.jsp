<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<title>${crew.title}-크루 전용 카페</title>
<style>
.container {
	width: 80%;
	margin: 30px auto;
}

.header {
	text-align: center;
	margin-bottom: 30px;
}

.menu {
	display: flex;
	justify-content: center;
	gap: 20px;
	margin-bottom: 20px;
}

.menu a {
	text-decoration: none;
	font-weight: bold;
	color: #333;
}

.content-box {
	border: 1px solid #ccc;
	padding: 20px;
	border-radius: 10px;
	background-color: #f9f9f9;
}
</style>
</head>
<body>

	<div class="container">
		<div class="header">
			<h1>🏠 [${crew.title}] 전용 크루 공간</h1>
			<p class="subtitle">📌 소개: ${crew.description}</p>
		</div>


		<div class="menu">
			<a href="/usr/crewCafe?crewId=${crew.id}">🏠 홈</a> <a
				href="/usr/crewCafe/articles?crewId=${crew.id}&type=notice">📢
				공지사항</a> <a href="/usr/crewCafe/articles?crewId=${crew.id}&type=free">📝
				자유게시판</a> <a href="/usr/crewCafe/gallery?crewId=${crew.id}">📸 사진첩</a> <a
				href="/usr/crewCafe/schedule?crewId=${crew.id}">📅 일정</a>
		</div>

		<div class="content-box">
			<p>이곳은 ${crew.title} 크루만을 위한 전용 공간입니다.</p>
			<ul>
				<li>✔ 자유롭게 소통하고 사진을 공유하세요.</li>
				<li>✔ 크루 일정을 등록하고 함께 계획을 세워보세요.</li>
			</ul>
		</div>
	</div>

</body>
</html>
