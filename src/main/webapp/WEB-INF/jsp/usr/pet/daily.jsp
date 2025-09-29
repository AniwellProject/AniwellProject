<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>감정일기</title>
    <link href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.css" rel="stylesheet">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.js"></script>
    <style>
        body {
            font-family: 'SUIT', sans-serif;
            background: #fffbea;
            padding: 20px;
        }
        h1 { text-align: center; }
        #calendar {
            max-width: 900px;
            margin: 20px auto;
            background: white;
            padding: 10px;
            border-radius: 12px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
        }
        .btn-register {
            display: block;
            margin: 0 auto;
            padding: 10px 20px;
            background: #ffd6e0;
            border-radius: 10px;
            border: none;
            cursor: pointer;
        }
        .modal { display: none; position: fixed; z-index: 100; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); }
        .modal-content {
            background: white;
            width: 400px;
            margin: 5% auto;
            padding: 20px;
            border-radius: 15px;
            max-height: 500px;
            overflow-y: auto;
        }
        .emotion-btn {
            font-size: 20px; padding: 10px; margin: 5px;
            border-radius: 50px; border: 2px solid #ffc0cb;
            background: #fff8f8; cursor: pointer;
        }
        .emotion-btn.selected { background: #ffb3c1; }
        textarea {
            width: 100%; height: 100px; padding: 10px; border-radius: 10px;
            font-family: 'SUIT';
        }
        .modal-footer { text-align: right; margin-top: 10px; }
    </style>
</head>
<body>

<h1>🐾 감정 일기</h1>
<button class="btn-register" onclick="openModal()">+ 등록</button>
<div id="calendar"></div>

<!-- 등록 형식 -->
<div id="diaryModal" class="modal">
    <div class="modal-content">
        <h3>오늘의 감정 일기</h3>
        <label>날짜: <input type="date" id="diaryDate"></label>
        <div>
            <p>감정:</p>
            <button class="emotion-btn" data-emotion="happy">😊 행복</button>
            <button class="emotion-btn" data-emotion="surprised">😮 놀람</button>
            <button class="emotion-btn" data-emotion="sad">😿 슬프름</button>
            <button class="emotion-btn" data-emotion="relaxed">😌 평온</button>
        </div>
        <textarea id="diaryContent" placeholder="일기 내용을 입력해주세요..."></textarea>
        <div class="modal-footer">
            <button onclick="saveDiary()">등록</button>
            <button onclick="closeModal()">닫기</button>
        </div>
    </div>
</div>

<!-- 상세보기 목록 -->
<div id="viewModal" class="modal">
    <div class="modal-content">
        <h3>📘 상세보기</h3>
        <div class="modal-body" id="diaryDetail"></div>
        <div class="modal-footer">
            <button onclick="startEditFromView()">✏️ 수정</button>
            <button onclick="deleteDiaryFromView()">🗑 삭제</button>
            <button onclick="$('#viewModal').hide();">닫기</button>
        </div>
    </div>
</div>


<!-- 수정 모달 -->
<div id="editModal" class="modal">
    <div class="modal-content">
        <h3>✏️ 감정일기 수정</h3>
        <input type="hidden" id="editEventId">
        <label>날짜: <input type="date" id="editDiaryDate"></label>
        <div>
            <p>감정:</p>
            <button class="emotion-btn" data-emotion="happy">😊 행복</button>
            <button class="emotion-btn" data-emotion="surprised">😮 놀람</button>
            <button class="emotion-btn" data-emotion="sad">😿 슬프름</button>
            <button class="emotion-btn" data-emotion="relaxed">😌 평온</button>
        </div>
        <textarea id="editDiaryContent"></textarea>
        <div class="modal-footer">
            <button onclick="updateDiary()">수정 완료</button>
            <button onclick="$('#editModal').hide();">닫기</button>
        </div>
    </div>
</div>

<script>
    const petId = ${petId};
    const emotionIcons = { happy: '😊', surprised: '😮', sad: '😿', relaxed: '😌' };
    let currentDiaryData = null;
    let calendar = null;

    document.addEventListener('DOMContentLoaded', function () {
        const calendarEl = document.getElementById('calendar');

        calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            locale: 'ko',
            eventClick: function (info) {
                const id = info.event.id;
                openViewModal(id);
            },
            events: [
                <c:forEach var="e" items="${events}">
                {
                    id: '${e.id}',
                    title: '${e.title}',
                    start: '${e.eventDate}',
                    allDay: true,
                    display: 'auto'
                },
                </c:forEach>
            ]
        });

        calendar.render();
    });

    function openModal() {
        $('#diaryModal').show();
        $('#diaryDate').val(new Date().toISOString().split('T')[0]);
    }

    function closeModal() {
        $('#diaryModal').hide();
        $('.emotion-btn').removeClass('selected');
        $('#diaryContent').val('');
    }

    $(document).on('click', '.emotion-btn', function () {
        $(this).siblings().removeClass('selected');
        $(this).addClass('selected');
    });

    function saveDiary() {
        const date = $('#diaryDate').val();
        const content = $('#diaryContent').val();
        const emotion = $('#diaryModal .emotion-btn.selected').data('emotion');

        if (!emotion || !date || !content) {
            alert('모든 항목을 입력하세요.');
            return;
        }

        $.post('/usr/pet/daily/write', {
            petId: petId,
            eventDate: date,
            title: emotion,
            content: content
        }, function (data) {
            if (data.resultCode && data.resultCode.startsWith('S-')) {
                location.reload();
            } else {
                alert('등록 실패: ' + data.msg);
            }
        });
    }

    function openViewModal(id) {
        $.get('/usr/pet/daily/detail', { id: id }, function (res) {
            if (res.resultCode === 'S-1') {
                const e = res.calendarEvent;
                currentDiaryData = e;

                const html =
                    "<div><b>" + emotionIcons[e.title] + " " + e.title + "</b></div>" +
                    "<div>" + e.content + "</div>" +
                    "<div>" + e.eventDate + "</div>";

                $('#diaryDetail').html(html);
                $('#viewModal').show();
            } else {
                alert('일기 정보를 불러오지 못했습니다.');
            }
        });
    }

    function deleteDiaryFromView() {
        if (!confirm('정말 삭제하시겠습니까?')) return;

        const id = currentDiaryData.id;
        $.post('/usr/pet/daily/delete', { id: id }, function (data) {
            if (data.resultCode && data.resultCode.startsWith('S-')) {
                calendar.getEventById(id)?.remove();
                $('#viewModal').hide();
                alert('삭제 완료!');
            } else {
                alert('삭제 실패: ' + data.msg);
            }
        });
    }

    function startEditFromView() {
        const e = currentDiaryData;
        $('#editEventId').val(e.id);
        $('#editDiaryDate').val(e.eventDate);
        $('#editDiaryContent').val(e.content);
        $('#editModal .emotion-btn').removeClass('selected');
        $('#editModal .emotion-btn[data-emotion="' + e.title + '"]').addClass('selected');
        $('#viewModal').hide();
        $('#editModal').show();
    }

    function updateDiary() {
        const id = $('#editEventId').val();
        const date = $('#editDiaryDate').val();
        const content = $('#editDiaryContent').val();
        const emotion = $('#editModal .emotion-btn.selected').data('emotion');

        if (!id || !date || !content || !emotion) {
            alert('모든 항목을 입력하세요.');
            return;
        }

        $.post('/usr/pet/daily/domodify', {
            id: id,
            eventDate: date,
            title: emotion,
            content: content
        }, function (res) {
            if (res.resultCode && res.resultCode.startsWith('S-')) {
                location.reload();
            } else {
                alert('수정 실패: ' + res.msg);
            }
        });
    }
</script>


</body>
</html>
