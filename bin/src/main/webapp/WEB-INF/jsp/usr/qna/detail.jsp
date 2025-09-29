<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ include file="/WEB-INF/jsp/usr/common/head.jspf" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>질문 상세</title>
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
</head>
<body class="bg-gray-100 p-8">

<div class="max-w-3xl mx-auto bg-white p-6 rounded shadow">

    <h1 class="text-2xl font-bold mb-2">🙋 ${qna.title}</h1>

    <!-- 등록일 + 비공개 여부 + 답변 여부 -->
    <div class="mb-4 text-sm text-gray-500">
        등록일: ${qna.regDate}
        <c:if test="${qna.secret}">
            <span class="ml-4 text-red-500 font-semibold">🔒 비공개 질문</span>
        </c:if>
        <c:if test="${!qna.secret}">
            <span class="ml-4 text-green-500 font-semibold">🌐 공개 질문</span>
        </c:if>

        <c:choose>
            <c:when test="${qna.answered}">
                <span class="ml-4 text-green-600 font-semibold">✔️ 답변 완료</span>
            </c:when>
            <c:otherwise>
                <span class="ml-4 text-yellow-600 font-semibold">⏳ 답변 대기 중</span>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- 질문 내용 -->
    <div class="mb-6 whitespace-pre-line text-gray-800">
        ${qna.body}
    </div>

    <!-- 수의사 답변 -->
    <c:if test="${not empty vetAnswers}">
        <c:forEach var="vetAnswer" items="${vetAnswers}">
            <div class="bg-green-50 p-4 border-l-4 border-green-400 rounded mb-4 relative">
                <strong class="text-green-700">📢 수의사 답변 - ${vetAnswer.vetName}</strong>

                <!-- 답변 텍스트와 수정 폼 영역 -->
                <div class="answer-view" id="answer-view-${vetAnswer.id}">
                    <p class="mt-2 text-gray-800 whitespace-pre-line">
                            ${vetAnswer.answer}
                    </p>
                </div>

                <div class="answer-edit hidden" id="answer-edit-${vetAnswer.id}">
                    <form action="/usr/vetAnswer/doModify" method="post"
                          onsubmit="return submitVetAnswerModify(event, ${vetAnswer.id});">
                        <input type="hidden" name="id" value="${vetAnswer.id}"/>
                        <textarea name="answer" rows="4"
                                  class="w-full p-2 border rounded">${vetAnswer.answer}</textarea>
                        <div class="mt-2">
                            <button type="submit" class="bg-blue-600 text-white px-3 py-1 rounded mr-2">저장</button>
                            <button type="button" class="bg-gray-400 text-white px-3 py-1 rounded"
                                    onclick="toggleEditForm(${vetAnswer.id}, false)">취소
                            </button>
                        </div>
                    </form>
                </div>

                <p class="mt-2 text-sm text-gray-500">
                    작성일: ${vetAnswer.answerAt}
                </p>

                <c:if test="${rq.loginedMember != null && rq.loginedMember.id == vetAnswer.memberId}">
                    <div class="mt-2">
                        <button type="button"
                                class="text-blue-600 hover:underline mr-4 bg-transparent border-none p-0 cursor-pointer"
                                onclick="toggleEditForm(${vetAnswer.id}, true)">
                            수정
                        </button>

                        <form action="/usr/vetAnswer/doDelete" method="post" style="display:inline;">
                            <input type="hidden" name="id" value="${vetAnswer.id}"/>
                            <button type="submit" onclick="return confirm('정말 삭제하시겠습니까?');"
                                    class="text-red-600 hover:underline bg-transparent border-none p-0 cursor-pointer">
                                삭제
                            </button>
                        </form>
                    </div>
                </c:if>
            </div>
        </c:forEach>
    </c:if>
    <!-- 수의사 로그인 시 답변 폼 표시 (아직 본인이 답변 안 한 경우만) -->
    <c:if test="${rq.loginedMember != null && rq.loginedMember.authLevel == 3 && qna.isFaq == 0}">

        <c:set var="alreadyAnswered" value="false"/>
        <c:forEach var="va" items="${vetAnswers}">
            <c:if test="${va.memberId == rq.loginedMemberId}">
                <c:set var="alreadyAnswered" value="true"/>
            </c:if>
        </c:forEach>

        <c:if test="${!alreadyAnswered}">
            <div class="mt-8 bg-gray-50 p-4 rounded border">
                <h2 class="font-bold text-lg mb-2">✏️ 수의사 답변 작성</h2>
                <form method="post" action="/usr/vetAnswer/doWrite">
                    <input type="hidden" name="qnaId" value="${qna.id}"/>
                    <textarea name="answer" rows="5" class="w-full p-2 border rounded"
                              placeholder="답변을 입력해주세요."></textarea>
                    <button type="submit" class="mt-2 bg-blue-600 text-white px-4 py-2 rounded">등록</button>
                </form>
            </div>
        </c:if>
    </c:if>


    <c:if test="${rq.loginedMemberId == qna.memberId}">
        <div class="mt-6 space-x-2">
            <a href="/usr/qna/modify?id=${qna.id}"
               class="text-sm text-white bg-yellow-500 px-3 py-1 rounded hover:bg-yellow-600">수정</a>
            <a href="/usr/qna/doDelete?id=${qna.id}"
               class="text-sm text-white bg-red-500 px-3 py-1 rounded hover:bg-red-600"
               onclick="return confirm('정말 삭제하시겠습니까?');">삭제</a>
        </div>
    </c:if>

    <div class="mt-6">
        <a href="/usr/qna/list" class="text-blue-600 hover:underline">← 목록으로 돌아가기</a>
    </div>

</div>

<script>
    function toggleEditForm(id, show) {
        const viewDiv = document.getElementById('answer-view-' + id);
        const editDiv = document.getElementById('answer-edit-' + id);

        if (show) {
            viewDiv.classList.add('hidden');
            editDiv.classList.remove('hidden');
        } else {
            viewDiv.classList.remove('hidden');
            editDiv.classList.add('hidden');
        }
    }

    function submitVetAnswerModify(event, id) {
        event.preventDefault();

        const form = event.target;
        const formData = new FormData(form);

        fetch(form.action, {
            method: 'POST',
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
            .then(res => res.text())
            .then(html => {
                // <script> 태그 제거하고 eval
                const scriptContent = html.replace(/<script[^>]*>([\s\S]*?)<\/script>/gi, '$1');
                eval(scriptContent);
            })
            .catch(err => {
                alert('수정 처리 중 오류가 발생했습니다.');
                console.error(err);
            });

        return false;
    }
</script>

</body>
</html>
