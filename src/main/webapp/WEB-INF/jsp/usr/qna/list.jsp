<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/jsp/usr/common/head.jspf" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>QnA 목록</title>
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
</head>

<body class="bg-gray-100 min-h-screen flex justify-center items-start p-10">

<div class="flex w-full max-w-7xl space-x-6">

    <!-- 왼쪽: 자주 묻는 질문 -->
    <div class="flex flex-col space-y-6 w-3/4">

        <c:if test="${rq.loginedMember != null && rq.loginedMember.authLevel == 7}">
            <div class="mb-4">
                <a href="/adm/qna/write"
                   class="inline-block bg-blue-700 text-white px-4 py-2 rounded hover:bg-blue-800">
                    ✍️ 자주 묻는 질문 등록
                </a>
            </div>
        </c:if>

        <!-- 1. 자주 묻는 질문 제목 목록 -->
        <div class="bg-white p-6 rounded-xl shadow">
            <h2 class="text-xl font-bold mb-4">📌 자주 묻는 질문 목록</h2>
            <ol class="list-decimal list-inside space-y-2">
                <c:forEach var="qna" items="${qnas}">
                    <li class="text-blue-800 font-semibold">${qna.title}</li>
                </c:forEach>
            </ol>
        </div>


        <!-- 2. 전체 질문/답변 내용 -->
        <div class="bg-white p-6 rounded-xl shadow space-y-6">
            <h2 class="text-xl font-bold mb-4">📖 질문과 답변</h2>
            <c:forEach var="qna" items="${qnas}">
                <div class="bg-gray-50 p-4 rounded-md shadow">
                    <h3 class="text-blue-700 font-semibold mb-2">Q. ${qna.title}</h3>
                    <p class="text-gray-800">A. ${qna.body}</p>
                </div>
            </c:forEach>
        </div>
    </div>

    <!-- 내 질문 목록 위에 버튼 추가 -->
    <div class="w-1/4 bg-white p-6 rounded-xl shadow h-[500px] overflow-y-auto">
        <div class="flex justify-between items-center mb-4">
            <h2 class="text-lg font-bold">🙋 내 질문 목록</h2>
            <c:set var="isLogined" value="${isLogined}"/>
            <button id="askBtn" type="button"
                    class="text-sm bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600">
                질문 등록
            </button>
        </div>

        <ul class="space-y-3 text-sm text-gray-700">
            <c:forEach var="qna" items="${myQnas}">
                <li class="bg-gray-50 p-3 rounded shadow">
                    <a href="/usr/qna/detail?id=${qna.id}" class="text-blue-700 hover:underline">
                            ${qna.title}
                    </a>
                    <c:choose>
                        <c:when test="${qna.answered}">
                            <span class="ml-2 text-green-600 font-semibold">[답변 완료]</span>
                        </c:when>
                        <c:otherwise>
                            <span class="ml-2 text-yellow-500 font-semibold">[답변 대기]</span>
                        </c:otherwise>
                    </c:choose>
                </li>
            </c:forEach>
        </ul>

    </div>

</div>

<script>
    document.getElementById('askBtn').addEventListener('click', function () {
        const isLogined = "${isLogined}" === "true";

        if (!isLogined) {
            alert("로그인 후 이용해주세요.");
            location.href = "/usr/member/login?afterLoginUri=" + encodeURIComponent("/usr/qna/ask");
        } else {
            location.href = "/usr/qna/ask";
        }
    });
</script>


</body>
</html>
