<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>우리 아이 분석</title>
    <link href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.js"></script>
    <style>
        body {
            margin: 0;
            font-family: 'Arial';
            background: #fffdf5;
            display: flex;
            flex-direction: column;
            height: 100vh;
        }

        .main {
            display: flex;
            flex: 1;
        }

        .left-panel {
            flex: 1;
            background: #f7f9e7;
            padding: 20px;
        }

        .calendar-section {
            flex: 2;
            padding: 30px;
            background: #fffde6;
        }

        .calendar-box {
            display: flex;
            border-radius: 20px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }

        .calendar-container {
            flex: 1;
            background: #fff9d9;
            padding: 10px;
            position: relative;
        }

        #calendar {
            width: 100%;
        }

        .timeline-section {
            background: #f9f9f9;
            padding: 20px;
            height: 300px;
            overflow-y: auto;
            position: relative;
            margin-top: 20px;
            border-radius: 20px;
            box-shadow: inset 0 0 6px #ddd;
        }

        .timeline-line {
            position: absolute;
            left: 30px;
            top: 0;
            bottom: 0;
            width: 4px;
            background: #ffc0cb;
        }

        .paw {
            position: absolute;
            left: 15px;
            width: 30px;
            z-index: 2;
        }

        .timeline-entry {
            margin-left: 60px;
            margin-bottom: 20px;
            position: relative;
        }

        .timeline-entry .time {
            font-size: 12px;
            color: #888;
            margin-bottom: 4px;
        }

        .timeline-entry .card {
            background: #fff;
            border-radius: 10px;
            padding: 10px 15px;
            box-shadow: 0 1px 4px rgba(0,0,0,0.1);
        }

        .emotion-happy { background: #f2ffe6; }
        .emotion-sad { background: #fff2f2; }
        .emotion-relaxed { background: #e6f6ff; }
        .emotion-scared { background: #fff9d6; }

        /* 백신 상세정보 팝업 */
        .vaccine-detail-box {
            position: absolute;
            top: 30px;
            left: 30px;
            right: 30px;
            background: #fffefc;
            padding: 20px;
            border-radius: 16px;
            box-shadow: 0 8px 20px rgba(0,0,0,0.15);
            z-index: 10;
            display: none;
            animation: fadeIn 0.3s ease;
        }

        .vaccine-detail-box table {
            width: 100%;
            border-collapse: collapse;
        }

        .vaccine-detail-box th, .vaccine-detail-box td {
            padding: 10px;
            border-bottom: 1px solid #eee;
            text-align: left;
        }

        .vaccine-detail-box .btns {
            text-align: right;
            margin-top: 10px;
        }

        .vaccine-detail-box button {
            padding: 6px 12px;
            margin-left: 10px;
            border: none;
            background: #ffd3d3;
            border-radius: 6px;
            cursor: pointer;
        }

        .vaccine-detail-box button:hover {
            background: #ffbbbb;
        }

        @keyframes fadeIn {
            from {opacity: 0; transform: translateY(10px);}
            to {opacity: 1; transform: translateY(0);}
        }
    </style>
</head>
<body>

<div class="main">
    <!-- 🟨 왼쪽 영역 -->
    <div class="left-panel">
        <h3>우리 아이 감정 분석</h3>
        <p>어제보다 물을 더 마셨어요!</p>
        <div class="timeline-section" id="timeline">
            <div class="timeline-line"></div>
            <img src="/img/paw_active.png" class="paw" id="pawIcon">

            <div class="timeline-entry">
                <div class="time">08:00</div>
                <div class="card emotion-happy">사료 30g 섭취 😺</div>
            </div>
            <div class="timeline-entry">
                <div class="time">10:00</div>
                <div class="card emotion-relaxed">창가 일광욕 ☀️</div>
            </div>
            <div class="timeline-entry">
                <div class="time">14:00</div>
                <div class="card emotion-scared">청소기 소리 😿</div>
            </div>
        </div>
    </div>

    <!-- 📅 중앙 영역 -->
    <div class="calendar-section">
        <h2>백신 일정 캘린더</h2>
        <button id="btn-vaccine-add" style="margin-bottom:10px;">➕ 백신 등록</button>

        <div class="calendar-box">
            <div class="calendar-container">
                <div id="calendar"></div>
                <div class="vaccine-detail-box" id="vaccineDetailBox"></div>
            </div>
        </div>
    </div>
</div>

<!-- 🐾 스크립트 -->
<script>
    const eventsData = JSON.parse('${eventsJson}');

    document.addEventListener('DOMContentLoaded', function () {
        const calendarEl = document.getElementById('calendar');

        const calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            locale: 'ko',
            events: eventsData,
            eventClick: function(info) {
                const id = info.event.id;
                if (id) {
                    fetch('/usr/pet/vaccination/detail?vaccinationId='+id)
                        .then(res => res.text())
                        .then(html => {
                            const box = document.getElementById('vaccineDetailBox');
                            box.innerHTML = html;
                            box.style.display = 'block';
                        });
                }
            }
        });

        calendar.render();
    });

    // 🐾 발바닥 따라가기
    const timeline = document.getElementById('timeline');
    const paw = document.getElementById('pawIcon');

    timeline.addEventListener('scroll', function () {
        const top = timeline.scrollTop;
        paw.style.top = (top + 20) + 'px';
    });
</script>

<script>
    document.addEventListener('click', function (e) {
        const card = e.target.closest('.vaccine-card');
        if (!card) return;

        const id = card.dataset.id;

        // 상세 → 수정폼
        if (e.target.classList.contains('btn-modify')) {
            card.querySelector('.detail-view').style.display = 'none';
            card.querySelector('.edit-form').style.display = 'block';
        }

        // 수정폼 → 상세 (취소 시)
        if (e.target.classList.contains('btn-cancel')) {
            card.querySelector('.edit-form').style.display = 'none';
            card.querySelector('.detail-view').style.display = 'block';
        }

        // 삭제
        if (e.target.classList.contains('btn-delete')) {
            if (!confirm('정말 삭제할까요?')) return;
            fetch('/usr/pet/vaccination/delete?vaccinationId=' + id)
                .then(res => res.text())
                .then(() => location.reload()); // 전체 새로고침
        }


        // 닫기
        if (e.target.classList.contains('btn-close')) {
            const box = document.getElementById('vaccineDetailBox');
            box.innerHTML = '';
            box.style.display = 'none';
        }
    });

    // 저장 처리
    document.addEventListener('submit', function (e) {
        if (e.target.id === 'modifyForm') {
            e.preventDefault();
            const id = e.target.closest('.vaccine-card')?.dataset.id;
            const formData = new FormData(e.target);

            fetch('/usr/pet/vaccination/doModify?vaccinationId=' + id, {
                method: 'POST',
                body: formData
            })
                .then(res => res.text())
                .then(() => location.reload()) // 수정 후 전체 새로고침
                .catch(() => alert('수정에 실패했습니다.'));
        }
    });


    const petId = '${param.petId}'

    document.getElementById('btn-vaccine-add').addEventListener('click', () => {
        const box = document.getElementById('vaccineDetailBox');
        fetch('/usr/pet/vaccination/registration?petId='+petId) // 👉 JSP에서 등록 폼 반환
            .then(res => res.text())
            .then(html => {
                box.innerHTML = html;
                box.style.display = 'block';
            });
    });
    // 등록 폼 제출 처리
    document.addEventListener('submit', function (e) {
        if (e.target.id === 'addForm') {
            e.preventDefault();

            const form = e.target;
            const formData = new FormData(form);

            if (!petId || petId === 'undefined') {
                alert('petId가 유효하지 않습니다!');
                return;
            }

            fetch('/usr/pet/vaccination/doRegistration?petId=' + petId, {
                method: 'POST',
                body: formData
            })
                .then(res => res.json()) // ← JSON 응답
                .then(data => {
                    if (data.resultCode.startsWith('S-')) {
                        location.href = '/usr/pet/vaccination?petId=' + petId;
                    } else {
                        alert(data.msg);
                    }
                })
                .catch(err => {
                    console.error('등록 실패:', err);
                    alert('등록 중 오류가 발생했습니다.');
                });
        }
    });



</script>




</body>
</html>
