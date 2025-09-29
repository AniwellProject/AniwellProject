<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>인증서 상태</title>
</head>
<body>
<h1>수의사 인증서 상태</h1>

<c:choose>
    <c:when test="${cert != null}">
        <p><strong>제출한 파일명:</strong>
            <a href="/gen/file/download?path=vet_certificates/${cert.filePath}" target="_blank">[다운로드]</a>

        </p>
        <p><strong>업로드 날짜:</strong> ${cert.uploadedAt}</p>
        <p><strong>승인 상태:</strong>
            <c:choose>
                <c:when test="${cert.approved == 1}">✅ 승인됨</c:when>
                <c:when test="${cert.approved == 2}">❌ 거절됨</c:when>
                <c:otherwise>⏳ 승인 대기 중</c:otherwise>
            </c:choose>
        </p>

        <form method="post" action="/usr/member/deleteVetCert" onsubmit="return confirm('정말 삭제하시겠습니까?');">
            <button type="submit">❌ 인증서 삭제</button>
        </form>

        <form method="post" action="/usr/member/doVetCertUpload" enctype="multipart/form-data">
            <input type="file" name="file" accept=".pdf,.jpg,.jpeg,.png" required/>
            <button type="submit">🔄 새 인증서 업로드</button>
        </form>

    </c:when>
    <c:otherwise>
        <p>제출한 인증서가 없습니다.</p>

        <form method="post" action="/usr/member/doVetCertUpload" enctype="multipart/form-data">
            <input type="file" name="file" accept=".pdf,.jpg,.jpeg,.png" required/>
            <button type="submit">업로드</button>
        </form>
    </c:otherwise>
</c:choose>

<br>
<a href="myPage">← 마이페이지로 돌아가기</a>
</body>
</html>
