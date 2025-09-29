<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>감정일기</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.js"></script>
    <link href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.css" rel="stylesheet">
    <style>
        body {
            margin: 0;
            font-family: 'SUIT', sans-serif;
            background: #b7b7b7;
            display: flex;
            justify-content: end;
            align-items: center;
            height: 100vh;
        }

        .main-container {
            display: flex;
            background: white;
            width: 90%;
            height: 100%;
            border-radius: 20px 0 0 20px;
            box-shadow: 0 0 30px rgba(0, 0, 0, 0.15);
            overflow: hidden;
        }

        .content-area {
            flex-grow: 1;
            padding: 30px;
            display: flex;
            flex-direction: column;
            gap: 40px;
            background: white;
        }

        /* 달력 전체 */
        #calendar {
            font-family: 'SUIT', sans-serif;
            background: white;
            border-radius: 20px;
            padding: 10px;
            box-shadow: none;
            width: 100%;
            border: none;
        }

        .fc .fc-daygrid-day-frame {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100%;
        }

        .fc .fc-button:focus {
            outline: none !important;
            box-shadow: none !important;
            border: none !important;
        }

        /* 헤더: 2025년 7월 + 좌우 화살표 */
        .fc-toolbar-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 10px;
        }
        .fc .fc-button {
            background: transparent;
            border: none;
            color: #d3cfc2;
            font-size: 20px;
            transition: 0.2s ease;
        }
        .fc .fc-button:hover {
            color: #b4ae9c;
            background: transparent;
        }


        /* 요일 (일~토) */
        .fc-col-header-cell {
            font-weight: bold;
            font-size: 15px;
            color: #333;
            padding: 10px 0;
            border: none !important;
            background: none !important;
        }

        .fc {
             background: transparent !important;
             box-shadow: none !important;
             border: none !important;
         }



        /* 날짜 셀 - 테두리 제거 + 숫자 크게 */
        .fc-daygrid-day {
            border: none !important;
            background: none;
            text-align: center;
            font-size: 20px;
            color: #333;
            height: 50px;
            vertical-align: middle;
            padding: 0;
            position: relative;
        }

        /* 지난달/다음달 날짜 흐리게 */
        .fc-day-other {
            color: #ccc !important;
        }

        /* 오늘 날짜 하이라이트 (동그란 배경) */
        .fc-day-today {
            background: #e3e8b7 !important;
            border-radius: 50% !important;
            font-weight: bold;
        }

        /* 격자 테두리 완전히 제거 */
        .fc-scrollgrid,
        .fc-scrollgrid-section,
        .fc-scrollgrid-sync-table,
        .fc-col-header,
        .fc-daygrid-body {
            border: none !important;
        }

        .fc-theme-standard td,
        .fc-theme-standard th {
            border: none !important;
        }

        .calendar-event {
            position: absolute;
            top: -30px;
            left: 4px;
            font-size: 18px;
        }

        .calendar-event-icon {
            position: absolute;
            top: 4px;
            left: 4px;
            width: 20px;
            height: 20px;
        }

        .fc-daygrid-event {
            background: transparent !important;
            border: none !important;
            padding: 0;
        }

        .diary-panel {
            flex-grow: 1;
            background: white;
            border-radius: 15px;
            padding: 20px;
            overflow-y: auto;
        }

        .diary-container {
            border-radius: 15px;
            padding: 20px;
            box-shadow: inset 0 0 5px rgba(232, 240, 193);
            overflow-y: auto;
            height: 80%;
        }

        .diary-panel h2 {
            margin-top: 0;
        }

        .entry {
            background: #e8f0c1;
            padding: 15px 20px;
            margin: 15px auto;
            border-radius: 20px;
            font-family: 'SUIT', sans-serif;
            color: #333;
            max-width: 80%;
            box-shadow: 0 2px 6px rgba(0,0,0,0.05);
        }

        .entry-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }

        .entry-title {
            font-size: 16px;
            font-weight: bold;
            display: flex;
            align-items: center;
            gap: 4px;
        }

        .entry-date {
            font-size: 13px;
            color: #555;
            border-bottom: 1px solid #aaa;
            padding-top: 3px;
        }

        .entry-content {
            font-size: 14px;
            white-space: pre-wrap;
        }


        .btn-register {
            padding: 10px 15px;
            background: #e8f0c1;
            border: none;
            border-radius: 10px;
            cursor: pointer;
            font-weight: bold;
            margin: 30px auto 10px auto;
            max-width: 80%;

        }

        .modal {
            display: none;
            position: fixed;
            z-index: 100;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.3);
            font-family: 'SUIT', sans-serif;
        }

        /* 🌿 말랑한 모달 박스 */
        .modal-content {
            background: white;
            width: 90%;
            max-width: 460px;
            margin: 15% auto;
            padding: 30px;
            border-radius: 25px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
            border: 4px double #e0f2c2;
        }

        /* 제목 */
        .modal-content h3 {
            text-align: center;
            font-size: 22px;
            color: #444;
            margin-bottom: 25px;
        }

        /* 날짜 input */
        .modal-content label {
            font-size: 14px;
            font-weight: bold;
            color: #777;
            display: block;
            margin-bottom: 12px;
        }

        /* 감정 버튼 영역 */
        .emotion-btn-group {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 12px;
            margin-bottom: 20px;
        }

        /* 감정 버튼 - 연초록 테마 */
        .emotion-btn {
            font-size: 16px;
            padding: 10px 18px;
            background: #f0f9e8;
            border: 2px solid #c7e9b0;
            border-radius: 40px;
            cursor: pointer;
            transition: 0.2s ease;
            font-weight: 500;
            box-shadow: 1px 1px 5px rgba(0,0,0,0.05);
        }

        .emotion-btn:hover {
            background: #e3f6c9;
        }

        .emotion-btn.selected {
            background: #c5e6a6;
            color: #2e2e2e;
        }

        /* 일기 입력창 */
        textarea#diaryContent {
            width: 100%;
            height: 100px;
            padding: 12px 14px;
            font-size: 14px;
            border: 1.5px solid #d0e4c1;
            border-radius: 15px;
            background: #fcfff9;
            resize: vertical;
            margin-top: 5px;
        }

        /* 하단 버튼 */
        .modal-footer {
            margin-top: 20px;
            display: flex;
            justify-content: space-between;
            gap: 10px;
        }

        .modal-footer button {
            flex: 1;
            padding: 10px 0;
            border-radius: 15px;
            border: none;
            font-size: 15px;
            font-weight: bold;
            cursor: pointer;
            transition: 0.2s;
        }

        .modal-footer button:first-child {
            background: #e0f3c5;
            color: #333;
        }

        .modal-footer button:last-child {
            background: #f1f1f1;
            color: #333;
        }

        .modal-footer button:hover {
            opacity: 0.9;
        }



    </style>
</head>
<body>
<div class="main-container">
    <div class="content-area">
        <button class="btn-register" onclick="openModal()">+ 등록</button>
        <div id="calendar"></div>
    </div>
        <div class="diary-panel">
            <h2>📓 감정일기</h2>
            <div class="diary-container">
            <c:forEach var="e" items="${events}">
                <div class="entry">
                    <div class="entry-header">
                        <span class="entry-title"><b>${e.title}</b> 🐱</span>
                        <span class="entry-date">${e.eventDate}</span>
                    </div>
                    <div class="entry-content">${e.content}</div>
                </div>

            </c:forEach>
            </div>
        </div>
</div>

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
            <button class="emotion-btn" data-emotion="sad">😿 슬픔</button>
            <button class="emotion-btn" data-emotion="relaxed">😌 평온</button>
        </div>
        <textarea id="diaryContent" ></textarea>
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
            headerToolbar: {
                left: 'prev',
                center: 'title',
                right: 'next'
            },
            titleFormat: { month: 'long' }, // "7월"
            dayMaxEventRows: true,

            // "1일" → "1"로 표시
            dayCellContent: function (arg) {
                return { html: '<div>' + arg.date.getDate() + '</div>' };
            },
            eventContent: function(arg) {
                return {
                    html: '<img src="/img/paw_active.png" class="calendar-event-icon" alt="고양이">'
                };
            },
            events: [
                <c:forEach var="e" items="${events}">
                {
                    id: '${e.id}',
                    title: '${e.title}',
                    start: '${e.eventDate}',
                    allDay: true
                },
                </c:forEach>
            ],
            dateClick: function(info) {
                // 해당 날짜에 이벤트가 있는지 확인
                const dateStr = info.dateStr;

                const eventsOnDate = calendar.getEvents().filter(function(event) {
                    return event.startStr === dateStr;
                });

                if (eventsOnDate.length > 0) {
                    // 첫 번째 이벤트 상세 보기
                    openViewModal(eventsOnDate[0].id);
                }
            },

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
