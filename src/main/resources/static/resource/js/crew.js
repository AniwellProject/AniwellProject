
function openModal(contentHTML) {
	const modal = document.getElementById('modal');
	const content = document.getElementById('modalContent');

	if (!content) {
		console.error("모달 컨텐츠 요소가 없습니다.");
		return;
	}
	content.innerHTML = `
			${contentHTML}
		`;

	modal.classList.remove('hidden');
}

function closeModal() {
	document.getElementById('modal').classList.add('hidden');
}


function openCommentModal() {
	const modal = document.getElementById("commentModal");
	modal.classList.remove("translate-y-full");  // 위로 올라오게
}

function closeCommentModal() {
	const modal = document.getElementById("commentModal");
	modal.classList.add("translate-y-full");  // 아래로 다시 내려감
}

// 📝 게시글 상세보기모달
function detailModal(e) {
	const free = {
		title: e.dataset.title,
		body: e.dataset.body,
		imageUrl: e.dataset.imageUrl,
		writer: e.dataset.extra__writer,
		regDate: e.dataset.regDate
	};


	const html = `
	<div class="flex h-full">
		  <!-- 왼쪽 이미지 영역 -->
		  <div class="w-1/2 bg-gray-100">
		    <img src=${free.imageUrl} alt="product" class="object-cover w-full h-full" />
		  </div>

		  <!-- 오른쪽 텍스트 영역 -->
		  <div class="w-1/2 p-6 flex flex-col justify-between text-gray-800 space-y-4 relative">
		    <!-- 게시글 본문 -->
		    <div class="flex-1 flex flex-col justify-between shadow p-4 overflow-auto">
		      <div class="overflow-y-auto h-[300px] text-sm leading-relaxed mb-4">
					${free.body}
		      </div>
		      <div class="flex justify-between text-xs text-gray-500 mt-2">
		        <span class="font-bold>${free.writer}</span>
		        <span>${free.regDate}</span>
		      </div>
		    </div>

		    <!-- 댓글 버튼 -->
		    <div class="shadow w-[100%] p-4 text-sm rounded cursor-pointer hover:bg-gray-100" onclick="openCommentModal()">
		      <p class="flex text-gray-500">여기누르기기</p>
		    </div>

		    <!-- ✅ 오른쪽 영역 내부에서 슬라이드되는 댓글 모달 -->
		    <div id="commentModal"
		         class="absolute bottom-0 left-0 w-full bg-white		ease-in-out
			            shadow-[0_-4px_10px_rgba(0,0,0,0.1)] rounded-t-2xl p-4 z-50 transform translate-y-full transition-transform duration-300 ease-in-out">
		      <div class="flex justify-between items-center mb-2">
		        <h2 class="text-lg font-semibold">댓글</h2>
		        <button onclick="closeCommentModal()" class="text-gray-500 hover:text-black text-sm">닫기 ✕</button>
		      </div>

		      <div class="overflow-y-auto max-h-60 space-y-2">
		        <div class="text-sm border-b pb-2">닉네임1: 123123</div>
		        <div class="text-sm border-b pb-2">닉네임2: 123123123</div>
		      </div>

		      <div class="mt-4 flex gap-2">
		        <input type="text" placeholder="댓글 입력..." class="flex-1 border px-3 py-2 rounded-md text-sm" />
		        <button class="bg-green-200 px-4 py-2 rounded-md text-sm">작성</button>
		      </div>
		    </div>
		  </div>
		</div>
    `;
	openModal(html);
}


function openModal(contentHTML) {
	const modal = document.getElementById('modal');
	const content = document.getElementById('modalContent');

	if (!content) {
		console.error("모달 컨텐츠 요소가 없습니다.");
		return;
	}

	content.innerHTML = `
		${contentHTML}
	`;

	modal.classList.remove('hidden');
}

function openComModal(contentHTML) {
	const modal = document.getElementById('comModal');
	modal.innerHTML = `
		<div class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
			<div class="bg-white p-6 rounded-lg shadow-lg relative max-w-md w-full">
				<button onclick="closeComModal()" class="absolute top-2 right-4 text-xl text-gray-500 hover:text-black">&times;</button>
				${contentHTML}
			</div>
		</div>
	`;
	modal.classList.remove('hidden');
}

function openComNobgModal(contentHTML) {
	const modal = document.getElementById('comNobgModal');
	modal.innerHTML = `
		<div class="fixed flex flex-col inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
			<button onclick="closeComNobgModal()" class="pl-[50%] content-center text-xl hover:text-black">&times;</button>
			<div class="flex">	
				<!-- 좌측 화살표 -->
				<button onclick="prevImage()"
				        class="">
				  ◀
				</button>

				
				<div class="p-6 rounded-lg max-w-md w-full">
				${contentHTML}
				</div>
			
				<!-- 우측 화살표 -->
				  <button onclick="nextImage()"
			          class="">
			   		 ▶
			  	</button>	
			
				</div>	
		</div>
	`;
	modal.classList.remove('hidden');
}

function closeModal() {
	document.getElementById('modal').classList.add('hidden');
}

function closeComModal() {
	document.getElementById('comModal').classList.add('hidden');
	document.getElementById('comModal').innerHTML = ''; // 내용도 초기화
}

function closeComNobgModal() {
	document.getElementById('comNobgModal').classList.add('hidden');
}

function memberModal() {
	const html = `
		<h2 class="text-lg font-bold mb-4">멤버 정보</h2>
		<div class="flex items-center gap-4">
			<div class="w-16 h-16 bg-gray-300 rounded-full"></div>
			<div>
				<p class="font-semibold">닉네임</p>
				<p class="text-sm text-gray-500">간단한 소개</p>
			</div>
		</div>
	`;
	openComModal(html);
}


// 📅 일정 보기 모달
function scModal(el) {
	const schedule = {
		title: el.dataset.title,
		body: el.dataset.body,
		scheduleDate: el.dataset.scheduledate, // ⚠️ 주의: HTML에서는 소문자로 바뀜!
		writer: el.dataset.writer,
		regDate: el.dataset.regDate,
		id: el.dataset.scheduleId  // data-schedule-id 속성 사용
	};

	const html = `
		<h2 class="text-lg font-bold mb-4">일정 정보</h2>
		<div>${schedule.scheduleDate}</div>
		<p class="text-sm">${schedule.title}</p>
		<p class="text-sm text-gray-500">${schedule.body}</p>
		<div class="flex justify-end">
			<button id="scJoinBtn" class="mt-4 px-6 py-2 text-black font-semibold rounded-xl shadow-md bg-gradient-to-r from-green-200 to-yellow-100 hover:shadow-lg transition">
				참가하기
			</button>
			<button id="scViewParticipantsBtn"
				class="mt-4 px-6 py-2 text-black font-semibold rounded-xl shadow-md bg-gradient-to-r from-green-200 to-yellow-100 hover:shadow-lg transition">
				참가자 보기
			</button>
		</div>
	`;

	openComModal(html);

	setTimeout(() => {
		$('#scJoinBtn').on('click', function() {
			const scheduleId = schedule.id;

			$.post("/usr/article/doJoinSchedule", { scheduleId }, function(res) {
				if (res.success) {
					alert("✅ 참가 완료!");
					el.classList.remove('shadow');
					el.classList.add('shadow-yellow-400');

					// 필요시 참가 버튼 숨기기 or 참가자 수 갱신 등 추가
				} else {
					alert(res.msg);
				}
			});
		});

		// ✅ 참가자 보기 버튼 클릭 이벤트 추가
		$('#scViewParticipantsBtn').on('click', function() {
			viewParticipants(schedule.id); // 👈 참가자 목록 요청
		});
	}, 0);
}

// ✅ 일정 참가자 목록 불러오기 함수 (전역에 위치)
function viewParticipants(scheduleId) {
	$.get("/usr/article/getParticipants", { scheduleId }, function(res) {
		if (res.success) {
			const participants = res.data1;

			let html = `
				<h2 class="text-lg font-bold mb-2">👥 참가자 목록</h2>
				<ul class="list-disc pl-5 space-y-1 text-sm">
					${participants.map(p => `<li>${p.nickname}</li>`).join('')}
				</ul>
			`;

			openComModal(html); // ✅ 기존 공용 모달 사용
		} else {
			alert("⚠ 참가자 목록 불러오기 실패");
		}
	});
}


// 📸 사진 보기 
// 모달
function photoModal(e) {
	const photo = {
		imageUrl: e.dataset.url,
	};

	const html = `
	<div class="w-full max-w-xl mx-auto flex">
	  
	  <!-- 이미지 -->
	  <div class="flex-1 overflow-hidden rounded-lg">
	  	<div class="w-full object-cover transition duration-300">
		<img src=${photo.imageUrl} alt="사진" class="object-cover w-full h-full rounded-lg" />
		</div>
	  </div>


	</div>

    `;
	openComNobgModal(html);

}

// 아래는 add 로직
//공지사항
function noti_btn() {
	const html = `
	<div class="flex h-full">
	  <div class="w-full p-3 flex flex-col justify-between text-gray-800 space-y-4 relative">
	    <div class="flex-1 flex flex-col justify-between shadow p-4 rounded bg-white">
	      <input type="hidden" id="crewIdInput" value="${crewId}">
	      <input type="hidden" id="boardIdInput" value="1">

	      <!-- 제목 입력 -->
	      <div class="mb-4">
	        <label class="block text-sm font-bold mb-1">제목</label>
	        <input type="text" id="titleInput" placeholder="제목을 입력하세요"
	          class="w-full border rounded px-3 py-2 text-sm shadow-sm" required />
	      </div>

	      <!-- 내용 입력 -->
	      <div class="mb-4 flex-1">
	        <label class="block text-sm font-bold mb-1">내용</label>
	        <textarea id="bodyInput" rows="20" placeholder="내용을 입력하세요"
	          class="w-full border rounded px-3 py-2 text-sm shadow-sm resize-none" required></textarea>
	      </div>

	      <!-- 등록 버튼 -->
	      <div class="text-right mt-4">
	        <button id="submitArticleBtn"
	          class="bg-gradient-to-r from-green-200 to-yellow-200 px-6 py-2 rounded-full shadow hover:shadow-md">
	          등록
	        </button>
	      </div>
	    </div>
	  </div>
	</div>
	`
	openComModal(html);
	setTimeout(() => {
		$('#submitArticleBtn').on('click', function(e) {
			e.preventDefault();

			const crewId = $('#crewIdInput').val();
			const boardId = $('#boardIdInput').val();
			const title = $('#titleInput').val();
			const body = $('#bodyInput').val();

			const formData = new FormData();
			formData.append("crewId", crewId);
			formData.append("boardId", boardId);
			formData.append("title", title);
			formData.append("body", body);

			$.ajax({
				url: '/usr/article/doWrite',
				type: 'POST',
				data: formData,
				contentType: false,
				processData: false,
				success: function(data) {
					if (data.resultCode === "S-1") {
						alert('게시글이 작성되었습니다.');
						window.location.href = data.data.redirectUrl;
					} else {
						alert("⚠️ " + data.msg);
					}
				},
				error: function(err) {
					console.error("❌ 등록 실패:", err);
					alert('등록 중 오류가 발생했습니다.');
				}
			});
		});
	}, 0);
}


function crewArtAdd() {
	const html = `
	<div>
	  <div class="flex h-full">
	    <!-- 숨겨진 입력들 -->
	    <input type="hidden" id="crewIdInput" value="${crewId}">
	    <input type="hidden" id="boardIdInput" value="3">

	    <!-- 왼쪽 이미지 영역 -->
	    <label for="imageUpload" class="w-1/2 bg-gray-100 cursor-pointer">
	      <img id="previewImage" src="https://via.placeholder.com/500" alt="preview"
	        class="object-cover w-full h-full" />
	      <input type="file" id="imageUpload" name="imageFile" accept="image/*"
	        class="hidden" onchange="previewImage(event)" />
	    </label>

	    <!-- 오른쪽 입력 영역 -->
	    <div class="w-1/2 p-6 flex flex-col justify-between text-gray-800 space-y-4 relative">
	      <div class="flex-1 flex flex-col justify-between shadow p-4 rounded bg-white">
	        <!-- 제목 -->
	        <div class="mb-4">
	          <label class="block text-sm font-bold mb-1">제목</label>
	          <input type="text" id="titleInput" placeholder="제목을 입력하세요"
	            class="w-full border rounded px-3 py-2 text-sm shadow-sm" required />
	        </div>

	        <!-- 내용 -->
	        <div class="mb-4 flex-1">
	          <label class="block text-sm font-bold mb-1">내용</label>
	          <textarea id="bodyInput" rows="20" placeholder="내용을 입력하세요"
	            class="w-full border rounded px-3 py-2 text-sm shadow-sm resize-none" required></textarea>
	        </div>

	        <!-- 등록 버튼 -->
	        <div class="text-right mt-4">
	          <button id="submitArticleBtn"
	            class="bg-gradient-to-r from-green-200 to-yellow-200 px-6 py-2 rounded-full shadow hover:shadow-md">
	            등록
	          </button>
	        </div>
	      </div>
	    </div>
	  </div>
	</div>
	`;

	openModal(html);

	setTimeout(() => {
		$('#submitArticleBtn').on('click', function(e) {
			e.preventDefault();

			const crewId = parseInt($('#crewIdInput').val(), 10);
			const boardId = parseInt($('#boardIdInput').val(), 10);
			const title = $('#titleInput').val();
			const body = $('#bodyInput').val();
			const imageFile = $('#imageUpload')[0].files[0];

			if (!title || !body) {
				alert("제목과 내용을 모두 입력해주세요.");
				return;
			}

			const formData = new FormData();
			formData.append("crewId", crewId);
			formData.append("boardId", boardId);
			formData.append("title", title);
			formData.append("body", body);
			if (imageFile) {
				formData.append("imageFile", imageFile);
			}

			$.ajax({
				url: '/usr/article/doWrite',
				type: 'POST',
				data: formData,
				contentType: false,
				processData: false,
				success: function(data) {
					if (data.resultCode === "S-1") {
						alert('게시글이 작성되었습니다.');
						window.location.href = data.data.redirectUrl;
					} else {
						alert("⚠️ " + data.msg);
					}
				},
				error: function(err) {
					console.error("❌ 등록 실패:", err);
					alert('등록 중 오류가 발생했습니다.');
				}
			});
		});
	}, 0);
}

//사진미리보기
function previewImage(event) {
	const input = event.target;
	if (input.files && input.files[0]) {
		const reader = new FileReader();
		reader.onload = function(e) {
			document.getElementById('previewImage').src = e.target.result;
		};
		reader.readAsDataURL(input.files[0]);
	}
}

function scAdd() {
	const html = `
		<div class="w-full h-full">
		  <div class="flex content-center bg-white p-6 rounded-2xl shadow-md w-full h-full">
			<div class="flex-1 grid grid-cols-2 gap-4 w-full h-full flex content-center">   
			<div class="span-col-1 shadow-xl p-3 w-[360px] h-[400px] flex flex-col text-base"> <!-- 높이 + 폰트 -->
			  <!-- 📅 캘린더 헤더 -->
			  <div class="flex justify-between items-center mb-4">
			    <button type="button" onclick="prevMonth()" class="text-3xl text-yellow-200 hover:scale-110">←</button>
			    <div id="calendarHeader" class="font-semibold text-lg text-center">2025년 7월</div>
			    <button type="button" onclick="nextMonth()" class="text-3xl text-yellow-200 hover:scale-110">→</button>
			  </div>

			  <!-- 📆 캘린더 본문 -->
			  <table class="w-full text-base"> <!-- 폰트 크게 -->
			    <thead>
			      <tr class="text-gray-600">
			        <th>일</th><th>월</th><th>화</th><th>수</th><th>목</th><th>금</th><th>토</th>
			      </tr>
			    </thead>
			    <tbody id="calendarBody" class="text-black font-medium"></tbody>
			  </table>

				<!-- 🕒 선택된 날짜 -->
				<input type="hidden" id="selectedDate" />
			  </div>

			  <div class="span-col-1 space-y-2">
				<label class="block text-sm font-bold">제목</label>
				<input type="text" id="scheduleTitle" class="border rounded w-full p-1 text-sm" placeholder="일정 제목 입력" />

				<label class="block text-sm font-bold mt-2">내용</label>
				<textarea id="scheduleBody" rows="5" class="border rounded w-full p-1 text-sm" placeholder="간단한 메모"></textarea>

				<div class="pt-2 text-center">
				  <button id="submitScheduleBtn" class="bg-gradient-to-r from-green-200 to-yellow-200 px-4 py-2 rounded-full shadow hover:shadow-lg">
					일정 등록
				  </button>
				</div>
			  </div>
			</div>
		  </div>
		</div>
		`;

	openModal(html);

	setTimeout(() => {
		renderCalendar(); // 달력 렌더링

		// 등록 버튼 클릭 이벤트
		$('#submitScheduleBtn').on('click', function(e) {
			e.preventDefault();

			const scheduleDate = $('#selectedDate').val();  // yyyy-MM-dd
			const scheduleTitle = $('#scheduleTitle').val();
			const scheduleBody = $('#scheduleBody').val();

			if (!scheduleDate) {
				alert("📆 날짜를 선택해주세요.");
				return;
			}

			if (!scheduleTitle) {
				alert("📌 제목을 입력해주세요.");
				return;
			}
			console.log(crewId);
			console.log(scheduleDate);
			console.log(scheduleTitle);
			console.log(scheduleBody);

			$.ajax({
				url: '/usr/article/doWriteSchedule',
				type: 'POST',
				data: {
					crewId: crewId,
					scheduleDate: scheduleDate,
					scheduleTitle: scheduleTitle,
					scheduleBody: scheduleBody
				},
				success: function(data) {
					console.log(data);
					if (data.resultCode === "S-1") {
						alert("✅ 일정이 등록되었습니다!");
						const redirectUrl = data.data1.redirectUrl;
						window.location.href = redirectUrl
					} else {
						alert("⚠️ " + data.msg);
					}
				},
				error: function(err) {
					console.error("❌ 일정 등록 실패", err);
				}
			});
		});
	}, 0);
}

// 전역 상태
let selectedDate = null;
let currentDate = new Date();

// 📅 달력 렌더링
function renderCalendar() {
	const calendarBody = document.getElementById("calendarBody");
	const calendarHeader = document.getElementById("calendarHeader");

	// ⛔ DOM이 없다면 재시도 (최대 10번까지)
	if (!calendarBody || !calendarHeader) {
		console.warn("⛔ 캘린더 요소를 찾을 수 없습니다. 100ms 후 재시도합니다.");
		let retryCount = 0;
		const interval = setInterval(() => {
			const calBody = document.getElementById("calendarBody");
			const calHeader = document.getElementById("calendarHeader");
			if (calBody && calHeader) {
				clearInterval(interval);
				renderCalendar(); // 재실행
			}
			retryCount++;
			if (retryCount > 10) {
				clearInterval(interval);
				console.error("❌ 캘린더 DOM을 찾을 수 없습니다. 렌더링 포기.");
			}
		}, 100);
		return;
	}

	calendarBody.innerHTML = "";

	const year = currentDate.getFullYear();
	const month = currentDate.getMonth();

	calendarHeader.textContent = `${year}년 ${month + 1}월`;

	const firstDay = new Date(year, month, 1).getDay();
	const lastDate = new Date(year, month + 1, 0).getDate();

	let html = "<tr>";
	for (let i = 0; i < firstDay; i++) {
		html += "<td></td>";
	}

	for (let day = 1; day <= lastDate; day++) {
		const dateStr = `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
		html += `
      <td onclick="selectDate('${dateStr}', this)"
          class="hover:bg-yellow-200 text-center py-2 rounded cursor-pointer">
        ${day}
      </td>
    `;

		if ((firstDay + day) % 7 === 0) {
			html += "</tr><tr>";
		}
	}
	html += "</tr>";
	calendarBody.innerHTML = html;
}

// ✅ 날짜 선택
function selectDate(dateStr, element) {
	selectedDate = dateStr;

	// hidden input에 값 설정
	$('#selectedDate').val(dateStr);

	// 모든 셀에서 강조 제거
	$('#calendarBody td').removeClass('bg-yellow-300');

	// 현재 클릭한 셀 강조
	$(element).addClass('bg-yellow-300');
}


// 🔁 이전/다음 달 이동
function prevMonth() {
	currentDate.setMonth(currentDate.getMonth() - 1);
	renderCalendar();
}

function nextMonth() {
	currentDate.setMonth(currentDate.getMonth() + 1);
	renderCalendar();
}


// sideModal////////////////////////////////////////////////

function modal_btn() {
	const modal = document.getElementById("sideModal");
	const contentHtml = `
  <div class="relative p-6 w-50% h-full bg-white shadow-lg rounded-tl-3xl rounded-bl-3xl">
    <!-- 닫기 버튼 (오른쪽 상단) -->
    <button onclick="closeSideModal()"
      class="absolute top-4 right-4 text-gray-500 hover:text-black text-xl font-bold">
      &times;
    </button>

	<div class="relative p-6 w-80 h-full bg-white flex flex-col">
	  <!-- 타이틀 -->
	  <h2 class="text-xl font-bold mb-6">📁 메뉴</h2>

	  <!-- 메뉴 항목 -->
	  <div class="space-y-4 mb-8">
	    <!-- 참가 신청서 (방장만 노출) -->
	    <button onclick="handleCrewJoin()" class="w-full text-left text-sm font-medium text-gray-800 hover:text-yellow-500 transition">
	      참가 신청
	    </button>

	    <!-- 내가 쓴 글 -->
	    <button onclick="handleArticleList()" class="w-full text-left text-sm font-medium text-gray-800 hover:text-yellow-500 transition">
	     내가 쓴 글
	    </button>

	  </div>

	  <!-- 멤버 목록 -->
	  <div class="flex-1 border-t pt-4 overflow-y-auto">
	    <h3 class="text-sm font-semibold text-gray-600 mb-3">멤버 목록</h3>
	       <ul id="memberList" class="space-y-2 text-sm text-gray-700">
	    </ul>
	  </div>
	</div>


  `;
	modal.innerHTML = contentHtml;
	modal.classList.remove("translate-x-full");
	modal.classList.add("translate-x-0");

	requestAnimationFrame(() => {
		renderMemberList();
	});
}


function closeSideModal() {
	const modal = document.getElementById("sideModal");
	modal.classList.remove("translate-x-0");
	modal.classList.add("translate-x-full");
}


// 사이드메뉴 팝업
function crewjoy() {
	const html = `
<div class="flex h-screen">
  <!-- 왼쪽 신청 리스트 -->
  <div class="w-1/3 border-r p-4 overflow-y-auto">
    <h2 class="text-lg font-semibold mb-4">신청 리스트</h2>
    <ul id="requestList" class="space-y-2">
     
    </ul>
  </div>

  <!-- 오른쪽 상세 정보 -->
  <div class="w-2/3 p-6">
    <h2 class="text-xl font-bold mb-4">신청자 정보</h2>
    
    <div id="requestDetail" class="space-y-2 bg-white p-4 rounded shadow flex flex-col justify-center">
      <p>좌측에서 신청자를 선택하세요.</p>
    </div>

    <div class="mt-6 space-x-4" id="actionButtons" style="display: none;">
      <button onclick="acceptRequest()" class="px-4 py-2 bg-green-200 rounded hover:bg-green-300 shadow">수락</button>
      <button onclick="rejectRequest()" class="px-4 py-2 bg-red-200 rounded hover:bg-red-300 shadow">거절</button>
    </div>
  </div>
</div>

    `;
	openModal(html);

	setTimeout(() => renderRequestList(), 0);

}
function handleCrewJoin() {
	closeSideModal(); // 사이드바 닫기
	crewjoy();        // 참가 신청 로직 실행
}


// 신청자 정보 전역변수
let applicants = [];

// 신청자 리스트 보기
function renderRequestList() {
	$.ajax({
		url: "/usr/walkCrewMember/requestList",
		type: "GET",
		data: { crewId },
		success: function(response) {
			console.log(response);
			// 응답 결과는 response.data 형태로 가정
			applicants = response.data1.applicants;

			const list = document.getElementById("requestList");
			list.innerHTML = applicants.map(r =>
				`<li class="cursor-pointer hover:bg-yellow-100 p-2 rounded" onclick="showDetail(${r.memberId})">${r.memberName}</li>`
			).join('');
		},
		error: function(xhr, status, error) {
			console.error("🚨 요청 실패:", status, error);
			alert("요청 목록 불러오기에 실패했습니다.");
		}
	});
}

// 참가동의 해주는 로직
function acceptRequest() {
	const slelctMemberId = document.getElementById("requestDetail").dataset.userId;

	$.ajax({
		url: "/usr/walkCrewMember/approve",
		type: "POST",
		data: {
			crewId: crewId,
			memberId: slelctMemberId
		},
		success: function(res) {
			console.log("✅ 요청 성공:", res);
			// ✅ 1. applicants 배열에서 삭제
			applicants = applicants.filter(app => app.memberId != slelctMemberId);

			// ✅ 2. 리스트 다시 렌더링
			const list = document.getElementById("requestList");
			list.innerHTML = applicants.map(r =>
				`<li class="cursor-pointer hover:bg-yellow-100 p-2 rounded" onclick="showDetail(${r.memberId})">${r.memberName}</li>`
			).join('');

			// ✅ 3. 디테일 초기화
			const detail = document.getElementById("requestDetail");
			const buttons = document.getElementById("actionButtons");
			detail.innerHTML = `<p>좌측에서 신청자를 선택하세요.</p>`;
			delete detail.dataset.userId;
			buttons.style.display = "none";

			renderCrewMemberSection();

		},
		error: function(xhr, status, error) {
			console.error("🚨 요청 실패:", status, error);
			alert("요청 처리에 실패했습니다.");
		}
	});
}

// 클릭 시 상세 정보 표시
function showDetail(id) {
	const user = applicants.find(u => u.memberId === id);
	const detail = document.getElementById("requestDetail");
	const buttons = document.getElementById("actionButtons");
	const memberId = user.memberId;
	$.ajax({
		url: "/usr/walkCrewMember/requestDetail",
		type: "GET",
		data: {
			crewId: crewId,
			memberId: memberId
		},
		success: function(res) {
			console.log("✅ 요청 성공:", res);
			const selectusr = res.data1.applicant;

			detail.innerHTML = `
			    <p><strong>닉네:</strong> ${selectusr.memberNickname}</p>
			    <p><strong>주소:</strong> ${selectusr.memberAddress}</p>
			  `;
		},
		error: function(xhr, status, error) {
			console.error("🚨 요청 실패:", status, error);
			alert("요청 처리에 실패했습니다.");
		}
	});

	buttons.style.display = "block";
	detail.dataset.userId = user.memberId; // 다음 처리를 위한 저장
}



function rejectRequest() {
	const selectedMemberId = document.getElementById("requestDetail").dataset.userId;
	consol.log(`❌ ID ${selectedMemberId} 거절 처리`);

	// 1. applicants 배열에서 해당 멤버 삭제
	applicants = applicants.filter(app => app.memberId != selectedMemberId);

	// 2. 리스트 다시 렌더링
	const list = document.getElementById("requestList");
	list.innerHTML = applicants.map(r =>
		`<li class="cursor-pointer hover:bg-yellow-100 p-2 rounded" onclick="showDetail(${r.memberId})">${r.memberName}</li>`
	).join('');

	// 3. 디테일 영역 초기화
	const detail = document.getElementById("requestDetail");
	const buttons = document.getElementById("actionButtons");
	detail.innerHTML = `<p>좌측에서 신청자를 선택하세요.</p>`;
	delete detail.dataset.userId;
	buttons.style.display = "none";
}

window.onload = renderRequestList;
///////

function handleCrewMember() {
	closeSideModal(); // 사이드바 닫기
	crewMember();

}



function crewMember() {
	const html = `
	<div class="flex h-screen">
	  <!-- 왼쪽 회원 리스트 -->
	  <div class="w-1/3 border-r p-4 overflow-y-auto">
	    <h2 class="text-lg font-semibold mb-4">멤버 리스트</h2>
	    <ul id="memberList" class="space-y-2">
	      <!-- JS로 렌더링 -->
	    </ul>
	  </div>

	  <!-- 오른쪽 회원 상세 정보 -->
	  <div class="w-2/3 p-6">
	    <h2 class="text-xl font-bold mb-4">멤버 정보</h2>

	    <div id="memberDetail" class="space-y-2 bg-white p-4 rounded shadow">
	      <p>좌측에서 회원을 선택하세요.</p>
	    </div>
		
	    <div class="mt-6 space-x-4" id="memberActionButtons" style="display: none;">
	      <button onclick="kickMember()" class="px-4 py-2 bg-red-200 rounded hover:bg-red-300 shadow">강퇴</button>
		  <button onclick="transLeader()" class="px-4 py-2 bg-yellow-200 rounded hover:bg-yellow-300 shadow">위임</button>
		</div>
	  </div>
	</div>

	
	    `;
	openModal(html);

	setTimeout(() => renderMemberList(), 0);

}
let members = [];

// 리스트 렌더링 크루에 저장된 크루멤버 리스트를 뿌리는 메서드 
function renderMemberList() {
	$.ajax({
		type: "get",
		url: `/usr/walkCrewMember/usr/walkCrew/memberList`,
		data: { crewId },
		success: function(data) {
			members = data.data1;
			const list = document.getElementById("memberList");
			list.innerHTML = members.map(m =>
				`<li class="cursor-pointer hover:bg-yellow-100 p-2 rounded" onclick="showMemberDetail(${m.memberId})">${m.crew_member_name}</li>`
			).join('');
		},
		error: function(err) {
			console.error("err list", err);
		}
	});

}

// 상세 보기
function showMemberDetail(id) {
	const member = members.find(m => m.memberId === id);
	const detail = document.getElementById("memberDetail");
	const buttons = document.getElementById("memberActionButtons");
	console.log(members);
	$.ajax({
		type: "GET",
		url: `/api/member/getUsrInfo`,
		data: { memberId: member.memberId },
		success: function(data) {
			console.log(data);
			detail.innerHTML = `
				  <p>${data.nickname}</p>
				  <p>${data.address}</p>
				`;

			detail.dataset.usrId = member.memberId;

			buttons.style.display = "block";
		},
		error: function(err) {
			console.error("참가등록실패", err);
		}
	});

}
//위임 처리
function transLeader() {
	const id = document.getElementById("memberDetail").dataset.usrId;
	console.log(id);
	if (!confirm(`정말로 ID ${id} 회원을 위임하시겠습니까?`)) return;
	$.ajax({
		url: "/usr/walkCrewMember/transferLeadership",
		method: "POST",
		data: {
			crewId: crewId,
			newLeaderId: id
		},
		success: function(data) {
			if (data.resultCode.startsWith("S-")) {
				alert("위임 완료");
				renderMemberList(); // 성공 후 목록 다시 렌더링
				handleCrewMember()
			} else {
				alert(`❌ 실패: ${data.msg}`);
			}
		},
		error: function(xhr, status, error) {
			console.error("❌ 위 요청 실패", error);
			alert("서버 오류로 위임에 실패했습니다.");
		}
	});

}

// 강퇴 처리
function kickMember() {
	const id = document.getElementById("memberDetail").dataset.userId;

	if (!confirm(`정말로 ID ${id} 회원을 강퇴하시겠습니까?`)) return;
	$.ajax({
		url: "/usr/walkCrewMember/expel",
		method: "POST",
		data: {
			crewId: crewId,
			memberId: id
		},
		success: function(data) {
			if (data.resultCode.startsWith("S-")) {
				alert("강퇴 완료");
				renderMemberList(); // 성공 후 목록 다시 렌더링
				handleCrewMember()
			} else {
				alert(`❌ 실패: ${data.msg}`);
			}
		},
		error: function(xhr, status, error) {
			console.error("❌ 강퇴 요청 실패", error);
			alert("서버 오류로 강퇴에 실패했습니다.");
		}
	});

}
//////



////참가신청로직
function crewJoin(crewId) {
	$.ajax({
		type: "POST",
		url: `/usr/walkCrewMember/doJoin`,
		data: { crewId },
		success: function(data) {

			console.log(data.msg);
			// ✅ 참가 수락 후 멤버 목록도 다시 렌더링
			renderMemberList();
		},
		error: function(err) {
			console.error("참가등록실패", err);
		}
	});
}


function handleArticleList() {
	closeSideModal(); // 사이드바 닫기
	myArticle();        // 참가 신청 로직 실행
}
// 내가 쓴글
function myArticle() {
	const memberId = localStorage.getItem("loginedMember");

	$.ajax({
		type: "GET",
		url: `/usr/article/list`,
		data: {
			crewId: crewId,
			boardId: 3,
			memberId: memberId
		},
		success: function(data) {
			console.log(data.msg);
			console.log(data.data1);

			// ✅ 기존 멤버 목록 다시 렌더링 유지
			renderMemberList();

			// ✅ article 리스트 출력 처리 추가
			const articles = data.data1.articles || [];

			const html = `
				<div class="space-y-4 p-4 max-h-[500px] overflow-y-auto">
					<h2 class="text-lg font-bold">📋 내가 쓴 글</h2>
					${articles.length === 0
					? `<p class="text-sm text-gray-500">작성한 글이 없습니다.</p>`
					: articles.map(article => `
							<div class="p-4 shadow rounded bg-white">
								<h3 class="font-semibold text-base">${article.title}</h3>
								<p class="text-sm text-gray-700">${article.body}</p>
								<p class="text-xs text-right text-gray-400">${article.regDate}</p>
							</div>
						`).join('')}
				</div>
			`;

			openComModal(html);

			// ✅ 원래 있던 renderMemberList 재호출도 그대로 유지 (필요 시 제거 가능)
			setTimeout(() => renderMemberList(), 0);
		},
		error: function(err) {
			console.error("가져오기실패", err);
		}
	});
}


