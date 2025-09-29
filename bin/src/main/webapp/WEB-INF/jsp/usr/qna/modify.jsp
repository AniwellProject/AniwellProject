<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>질문 수정</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/tailwindcss/2.1.4/tailwind.min.css">
</head>
<body class="bg-gray-100 p-8">

<div class="max-w-3xl mx-auto bg-white p-6 rounded shadow">
    <h1 class="text-2xl font-bold mb-6">✏️ 질문 수정</h1>

    <form action="/usr/qna/doModify" method="post">
        <!-- ID 전달 -->
        <input type="hidden" name="id" value="${qna.id}" />

        <!-- 제목 -->
        <div class="mb-4">
            <label class="block font-semibold mb-1">제목</label>
            <input type="text" name="title" value="${qna.title}" required class="w-full border p-2 rounded" />
        </div>

        <!-- 내용 -->
        <div class="mb-4">
            <label class="block font-semibold mb-1">내용</label>
            <textarea name="body" rows="8" required class="w-full border p-2 rounded">${qna.body}</textarea>
        </div>

        <!-- 비공개 여부 -->
        <div class="mb-4">
            <label>
                <input type="checkbox" name="isSecret" value="true"
                       <c:if test="${qna.secret}">checked</c:if> />
                비공개로 설정
            </label>
        </div>

        <!-- 버튼 -->
        <div class="mt-6 flex justify-between">
            <button type="submit" class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">수정 완료</button>
            <a href="/usr/qna/detail?id=${qna.id}" class="text-sm text-blue-600 hover:underline">← 돌아가기</a>
        </div>
    </form>
</div>

</body>
</html>
