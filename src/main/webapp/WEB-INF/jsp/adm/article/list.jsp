<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <title>게시글 관리</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css">
</head>
<body class="bg-gray-100">
<div class="max-w-6xl mx-auto mt-10 bg-white shadow p-6 rounded-lg">
    <h1 class="text-2xl font-bold mb-6">게시글 관리</h1>

    <!-- 검색 -->
    <form method="get" action="/adm/article/list" class="mb-4 flex gap-2">
        <input type="text" name="searchKeyword" placeholder="제목 검색"
               value="${param.searchKeyword}" class="border px-3 py-2 w-1/2 rounded">
        <button class="bg-blue-600 text-white px-4 py-2 rounded">검색</button>
    </form>

    <!-- 게시글 테이블 -->
    <table class="w-full table-auto border-collapse border">
        <thead class="bg-gray-100 text-left">
        <tr>
            <th class="border px-4 py-2">번호</th>
            <th class="border px-4 py-2">제목</th>
            <th class="border px-4 py-2">작성자</th>
            <th class="border px-4 py-2">작성일</th>
            <th class="border px-4 py-2">조회수</th>
            <th class="border px-4 py-2">관리</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="article" items="${articles}">
            <tr>
                <td class="border px-4 py-2">${article.id}</td>
                <td class="border px-4 py-2">
                    <a href="/usr/article/detail?id=${article.id}" class="text-blue-600 hover:underline">
                            ${article.title}
                    </a>
                </td>
                <td class="border px-4 py-2">${article.extra__writer}</td>
                <td class="border px-4 py-2">${article.regDate}</td>
                <td class="border px-4 py-2">${article.hitCount}</td>
                <td class="border px-4 py-2">
                    <form method="post" action="/adm/article/doDelete" onsubmit="return confirm('삭제하시겠습니까?')">
                        <input type="hidden" name="id" value="${article.id}">
                        <button class="text-red-500 hover:underline">삭제</button>
                    </form>
                </td>
            </tr>
        </c:forEach>

        <c:if test="${empty articles}">
            <tr>
                <td colspan="6" class="text-center py-6">게시글이 없습니다.</td>
            </tr>
        </c:if>
        </tbody>
    </table>

    <!-- 페이징 -->
    <div class="mt-6 flex justify-center space-x-2">
        <c:forEach var="i" begin="1" end="${totalPage}">
            <a href="?page=${i}&searchKeyword=${param.searchKeyword}" class="px-3 py-1 border rounded
               <c:if test='${i == page}'> bg-blue-500 text-white </c:if>">
                    ${i}
            </a>
        </c:forEach>
    </div>
</div>
</body>
</html>