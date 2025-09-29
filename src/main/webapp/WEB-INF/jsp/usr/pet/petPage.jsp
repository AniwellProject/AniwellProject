<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
  <title>우리 아이 페이지</title>
  <link href="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.css" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/fullcalendar@6.1.8/index.global.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <style>
    body {
      margin: 0;
      font-family: 'Arial';
      background: #f4f4f4;
      display: flex;
    }

    .sidebar {
      width: 240px;
      background: linear-gradient(to bottom, #d2e6b8, #f0f8da);
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px 10px;
      box-shadow: 2px 0 8px rgba(0,0,0,0.1);
    }

    .sidebar img.pet-photo {
      width: 100%;
      height: 30%;
      border-radius: 20px;
      object-fit: cover;
      margin-bottom: 20px;
    }

    .pet-info-card {
      background: #ffffff;
      width: 90%;
      border-radius: 16px;
      padding: 15px;
      text-align: center;
      margin-bottom: 20px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      font-size: 14px;
    }

    .pet-info-card h3 {
      margin-top: 0;
      font-size: 16px;
    }

    .sidebar button {
      width: 90%;
      padding: 12px;
      margin: 8px 0;
      background: #f4fbe5;
      border: none;
      border-radius: 10px;
      font-weight: bold;
      cursor: pointer;
      transition: background 0.2s;
    }

    .sidebar button:hover {
      background: #e4f2c9;
    }

    .content {
      flex: 1;
      padding: 30px;
    }

    .top-section {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
    }

    .chart-box {
      background: #fff;
      border-radius: 16px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);
      flex: 1;
      margin-right: 20px;
    }

    .horizontal-buttons {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }

    .horizontal-buttons button {
      padding: 12px 18px;
      border: none;
      border-radius: 12px;
      background: #f0f8da;
      font-weight: bold;
      cursor: pointer;
      transition: background 0.2s;
      width: 200px;
    }

    .horizontal-buttons button:hover {
      background: #dceec4;
    }

    canvas {
      width: 100% !important;
      height: 200px !important;
    }

    .calendar-box {
      margin-top: 30px;
      background: #ffffff;
      border-radius: 16px;
      padding: 10px;
      width: 50%;
      height: 50%;
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);
      position: relative;
    }

    #calendar {
      height: 90%;
    }

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
    }
  </style>
</head>
<body>

<!-- 📌 사이드바 -->
<div class="sidebar">
  <c:choose>
    <c:when test="${not empty pet.photo}">
      <img src="${pet.photo}" class="pet-photo" alt="펫 사진" />
    </c:when>
    <c:otherwise>
      <img src="/img/default-pet.png" class="pet-photo" alt="기본 사진" />
    </c:otherwise>
  </c:choose>

  <div class="pet-info-card">
    <h3>반려동물등록증</h3>
    <div style="display: flex; align-items: center; gap: 10px;">
      <div style="flex: 1; text-align: left;">
        이름: ${pet.name}<br>
        품종: ${pet.breed}<br>
        생일: ${pet.birthDate}<br>
        성별: ${pet.gender}
      </div>
      <div>
        <img src="${not empty pet.photo ? pet.photo : '/img/default-pet.png'}" style="width: 60px; height: 60px; object-fit: cover; border-radius: 8px;" />
      </div>
    </div>
  </div>

  <button onclick="location.href='/usr/pet/petPlace'">주변 장소 검색</button>
  <!-- 갤러리 버튼 -->
  <button id="btn-open-gallery" data-pet-id="${pet.id}">감정 갤러리</button>

  <!-- 갤러리 팝업 (1개만) -->
  <div id="galleryPopup" style="
  position: fixed;
  top: 0;
  right: -160%;
  width: 900px;
  height: 100%;
  background: white;
  box-shadow: -4px 0 12px rgba(0,0,0,0.2);
  transition: right 0.4s ease;
  z-index: 9999;
">
    <button onclick="closeGalleryPopup()" style="position:absolute; width: 80px; top:12px; right:12px;">❌ 닫기</button>
    <div id="galleryContent"></div>
  </div>

  <button onclick="location.href='/usr/pet/daily?petId=${pet.id}'">감정 일기</button>

</div>

<!-- 📌 본문 콘텐츠 -->
<div class="content">
  <!-- 🎯 차트 + 버튼 -->
  <div class="top-section">
    <div class="chart-box">
      <h3>활동 분석 차트</h3>
      <canvas id="activityChart"></canvas>
    </div>
    <div class="horizontal-buttons">
      <!-- 🔘 감정 분석하기 버튼 -->
      <button id="btn-analyze-emotion" data-pet-id="${pet.id}">감정 분석하기</button>

      <!-- 🔲 슬라이딩 팝업 영역 -->
      <div id="emotionPopup" style="
    position: fixed;
    top: 0;
    right: -80%;
    width: 80%;
    height: 100%;
    background: white;
    box-shadow: -4px 0 10px rgba(0,0,0,0.2);
    z-index: 1000;
    transition: right 0.8s ease;
    overflow-y: auto;">
        <button onclick="closeEmotionPopup()" style="position:absolute; width: 80px; top:12px; right:12px;">❌ 닫기</button>
        <div id="emotionContent"></div>
      </div>
      <button>행동 분석 결과 보기</button>
    </div>
  </div>
  <div style="display:flex; align-items: flex-start; gap: 20px;">
  <!-- 📅 백신 일정 캘린더 -->
  <div class="calendar-box">
    <button id="btn-vaccine-add" style="margin-bottom:10px;">➕ 백신 등록</button>
    <div id="calendar"></div>
    <div class="vaccine-detail-box" id="vaccineDetailBox"></div>
  </div>

  <!-- 캘린더 오른쪽에 위치할 예정일 목록 -->
  <div id="upcomingEventsBox" style="margin-top: 30px; margin-left: 20px; background: #ffffff; border-radius: 16px; padding: 20px; width: 20%; height: 500px; overflow-y: auto; box-shadow: 0 2px 8px rgba(0,0,0,0.08);">
    <h4 style="margin-top:0;">📌 예정된 일정</h4>
    <ul id="upcomingEventsList" style="list-style:none; padding-left:0;"></ul>
  </div>
</div>
</div>
<!-- 📊 Chart.js -->
<script>
  const ctx = document.getElementById('activityChart');
  const chart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['수면', '식사량', '물섭취'],
      datasets: [
        { label: '어제', data: [6, 200, 80], backgroundColor: '#d6eebb' },
        { label: '오늘', data: [7.5, 210, 95], backgroundColor: '#8ecfbb' }
      ]
    },
    options: {
      scales: { y: { beginAtZero: true } }
    }
  });
</script>

<!-- 📅 FullCalendar + 백신 모달 -->
<script>
  const eventsData = JSON.parse('${eventsJson}');

  document.addEventListener('DOMContentLoaded', function () {
    const calendarEl = document.getElementById('calendar');
    const calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: 'dayGridMonth',
      locale: 'ko',
      events: eventsData,
      eventClick: function (info) {
        const id = info.event.id;
        if (id) {
          fetch('/usr/pet/vaccination/detail?vaccinationId=' + id)
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

  // 백신 등록 모달 열기
  document.getElementById('btn-vaccine-add').addEventListener('click', () => {
    const box = document.getElementById('vaccineDetailBox');
    fetch('/usr/pet/vaccination/registration?petId=${param.petId}')
            .then(res => res.text())
            .then(html => {
              box.innerHTML = html;
              box.style.display = 'block';
            });
  });

  // 백신 모달 내부 처리
  document.addEventListener('click', function (e) {
    const box = document.getElementById('vaccineDetailBox');
    const card = e.target.closest('.vaccine-card');
    if (!card) return;

    const id = card.dataset.id;

    if (e.target.classList.contains('btn-modify')) {
      card.querySelector('.detail-view').style.display = 'none';
      card.querySelector('.edit-form').style.display = 'block';
    }

    if (e.target.classList.contains('btn-cancel')) {
      card.querySelector('.edit-form').style.display = 'none';
      card.querySelector('.detail-view').style.display = 'block';
    }

    if (e.target.classList.contains('btn-delete')) {
      if (confirm('정말 삭제할까요?')) {
        fetch('/usr/pet/vaccination/delete?vaccinationId=' + id)
                .then(() => location.reload());
      }
    }

    if (e.target.classList.contains('btn-close')) {
      box.innerHTML = '';
      box.style.display = 'none';
    }
  });

  // 등록/수정 폼 처리
  document.addEventListener('submit', function (e) {
    if (e.target.id === 'addForm' || e.target.id === 'modifyForm') {
      e.preventDefault();
      const form = e.target;
      const formData = new FormData(form);
      const petId = formData.get('petId');

      const url = form.id === 'addForm'
              ? '/usr/pet/vaccination/doRegistration?petId=' + petId
              : '/usr/pet/vaccination/doModify?vaccinationId=' + form.closest('.vaccine-card')?.dataset.id;

      fetch(url, {
        method: 'POST',
        body: formData
      })
              .then(res => res.json())
              .then(data => {
                if (data.resultCode?.startsWith('S-')) {
                  alert('✅ 처리 완료!');
                  location.reload();
                } else {
                  alert('❌ 실패: ' + (data.msg || '오류 발생'));
                }
              });
    }
  });
</script>

<script>
  // 버튼 클릭 시 감정 분석 JSP 불러오기
  document.getElementById('btn-analyze-emotion').addEventListener('click', function () {
    const petId = this.getAttribute('data-pet-id'); // 👉 버튼에 넣은 petId 꺼내기

    fetch('/usr/pet/analysis?petId=' + petId)
            .then(res => res.text())
            .then(html => {
              document.getElementById('emotionContent').innerHTML = html;
              document.getElementById('emotionPopup').style.right = '0';

              // 💡 삽입된 html의 이벤트 바인딩 실행
              bindEmotionPopupEvents();
            });
  });

  // 닫기 함수 (emotion.jsp 내부에서 이 함수 호출 가능)
  function closeEmotionPopup() {
    document.getElementById('emotionPopup').style.right = '-160%';
  }

  document.getElementById('btn-open-gallery').addEventListener('click', function () {
    const petId = this.getAttribute('data-pet-id');

    if (!petId) {
      alert("petId가 비어있습니다!");
      return;
    }

    fetch('/usr/pet/gallery?petId=' + petId)
            .then(res => res.text())
            .then(html => {
              document.getElementById('galleryContent').innerHTML = html;
              document.getElementById('galleryPopup').style.right = '0';

              if (typeof bindGalleryEvents === "function") {
                bindGalleryEvents();
              }
            });
  });

  // 닫기 버튼 함수
  function closeGalleryPopup() {
    document.getElementById('galleryPopup').style.right = '-160%';
  }

  function closeEmotionPopup() {
    document.getElementById('emotionPopup').style.right = '-160%';
  }

</script>

<script>
  function filterByEmotion(emotion) {
    const buttons = document.querySelectorAll('.emotion-btn');
    buttons.forEach(btn => btn.classList.remove('active'));

    const selectedBtn = [...buttons].find(btn => btn.textContent.toLowerCase().includes(emotion));
    if (selectedBtn) selectedBtn.classList.add('active');

    const items = document.querySelectorAll('.result-item');
    items.forEach(item => {
      if (item.dataset.emotion === emotion) {
        item.classList.remove('hidden');
      } else {
        item.classList.add('hidden');
      }
    });
  }

  document.getElementById('btn-analyze-emotion').addEventListener('click', function () {
    const petId = this.getAttribute('data-pet-id');

    fetch('/usr/pet/analysis?petId=' + petId)
            .then(res => res.text())
            .then(html => {
              document.getElementById('emotionContent').innerHTML = html;
              document.getElementById('emotionPopup').style.right = '0';

              // 여기서 emotion.jsp 내부 요소 바인딩
              attachEmotionEventHandlers();
            });
  });

  function attachEmotionEventHandlers() {
    let emotionChart = null;

    document.querySelectorAll('.species-btn').forEach(btn => {
      btn.addEventListener('click', function () {
        document.querySelectorAll('.species-btn').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
        document.getElementById('speciesInput').value = this.dataset.species;
      });
    });

    document.getElementById('imageFile').addEventListener('change', function () {
      const file = this.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
          document.getElementById('preview').innerHTML = '<img src="' + e.target.result + '" style="max-width:100%; border-radius:12px; border:3px solid #f3df87;" />';

        };
        reader.readAsDataURL(file);
      }
    });

    document.getElementById('analysisForm').addEventListener('submit', function (e) {
      e.preventDefault();
      const formData = new FormData(this);

      fetch('/usr/pet/analysis/do', {
        method: 'POST',
        body: formData
      })
              .then(res => res.json())
              .then(data => {
                document.getElementById('preview').innerHTML =
                        '<img src="' + data.imagePath + '" style="max-width:100%; border-radius:12px; border:3px solid #f3df87;" />';


                const probs = data.probabilities;
                const labels = Object.keys(probs);
                const values = Object.values(probs).map(v => parseFloat((v * 100).toFixed(2)));

                const labelMap = {
                  "happy": "😊 행복",
                  "relaxed": "😌 평온",
                  "angry": "😠 화남",
                  "sad": "😿 슬픔",
                  "scared": "😨 두려움"
                };

                const maxIdx = values.indexOf(Math.max(...values));
                const displayLabel = labelMap[labels[maxIdx]] || labels[maxIdx];

                document.getElementById('resultText').textContent = '가장 높은 감정: ' + displayLabel + ' (' + values[maxIdx] + '%)';


                if (emotionChart) emotionChart.destroy();
                const ctx = document.getElementById('emotionChart').getContext('2d');
                emotionChart = new Chart(ctx, {
                  type: 'pie',
                  data: {
                    labels: labels.map(l => labelMap[l] || l),
                    datasets: [{
                      data: values,
                      backgroundColor: ['#f9c74f', '#90be6d', '#f8961e', '#43aa8b', '#577590']
                    }]
                  },
                  options: {
                    plugins: {
                      legend: { position: 'bottom' },
                      title: { display: true, text: '감정 비율 분석' }
                    }
                  }
                });
              })
              .catch(err => {
                alert("❌ 분석 실패");
                console.error(err);
              });
    });
  }


  document.addEventListener('DOMContentLoaded', function () {
    const calendarEl = document.getElementById('calendar');
    const calendar = new FullCalendar.Calendar(calendarEl, {
      initialView: 'dayGridMonth',
      locale: 'ko',
      events: eventsData,
      eventClick: function (info) {
        const id = info.event.id;
        if (id) {
          fetch('/usr/pet/vaccination/detail?vaccinationId=' + id)
                  .then(res => res.text())
                  .then(html => {
                    const box = document.getElementById('vaccineDetailBox');
                    box.innerHTML = html;
                    box.style.display = 'block';
                  });
        }
      },
      datesSet: function () {
        // 이벤트 다시 필터링
        showUpcomingEvents(calendar.getEvents());
      }
    });
    calendar.render();

    showUpcomingEvents(calendar.getEvents()); // 초기 출력
  });

  function showUpcomingEvents(events) {
    const upcomingBox = document.getElementById('upcomingEventsList');
    upcomingBox.innerHTML = '';

    const today = new Date();
    const upcoming = events
            .filter(e => new Date(e.start) >= today)
            .sort((a, b) => new Date(a.start) - new Date(b.start));

    if (upcoming.length === 0) {
      upcomingBox.innerHTML = '<li>😺 예정된 일정이 없어요!</li>';
      return;
    }

    upcoming.forEach(e => {
      const li = document.createElement('li');
      const dateStr = new Date(e.start).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        weekday: 'short'
      });
      li.textContent = "🗓️ " + dateStr + ": " + e.title;
      li.style.marginBottom = '8px';
      upcomingBox.appendChild(li);
    });
  }
</script>





</body>
</html>
