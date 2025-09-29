<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>수의사 질문 리스트</title>
  <style>
    .answered { background-color: #d4edda; }   /* 답변된 질문 초록 */
    .unanswered { background-color: #f8d7da; } /* 미답변 질문 빨강 */
    table { width: 100%; border-collapse: collapse; }
    th, td { padding: 8px; border: 1px solid #ccc; text-align: left; }
    th { background-color: #f2f2f2; }
    a { text-decoration: none; color: #007bff; }
    a:hover { text-decoration: underline; }
  </style>
</head>
<body>
<h1>수의사용 질문 리스트</h1>

<table>
  <thead>
  <tr>
    <th>번호</th>
    <th>제목</th>
    <th>작성자</th>
    <th>등록일</th>
    <th>답변상태</th>
  </tr>
  </thead>
  <tbody>
  <c:forEach var="qna" items="${questions}">
    <c:choose>
      <c:when test="${qna.answered}">
        <tr class="answered">
      </c:when>
      <c:otherwise>
        <tr class="unanswered">
      </c:otherwise>
    </c:choose>
    <td>${qna.id}</td>
    <td><a href="/usr/qna/detail?id=${qna.id}">${qna.title}</a></td>
    <td>${qna.memberName}</td>
    <td>${qna.regDate}</td>
    <td>
      <c:choose>
        <c:when test="${qna.answered}">
          답변 완료
        </c:when>
        <c:otherwise>
          미답변
        </c:otherwise>
      </c:choose>
    </td>
    </tr>
  </c:forEach>
  <c:if test="${empty questions}">
    <tr><td colspan="5">등록된 질문이 없습니다.</td></tr>
  </c:if>
  </tbody>
</table>
</body>
</html>
