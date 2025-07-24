
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

function closeModal() {
	document.getElementById('modal').classList.add('hidden');
}

function closeComModal() {
	document.getElementById('comModal').classList.add('hidden');
	document.getElementById('comModal').innerHTML = ''; // 내용도 초기화
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
function scModal() {
	const html = `
      <h2 class="text-lg font-bold mb-4">일정 정보</h2>
      <p>7월 7일</p>
      <p class="text-sm text-gray-500">오후 2시 / 장소: OO공원</p>
	  <div class="flex justify-end">
	  <button class="mt-4 px-6 py-2 text-black font-semibold rounded-xl shadow-md bg-gradient-to-r from-green-200 to-yellow-100 hover:shadow-lg transition">
	    참가하기
	  </button>
	  </div>
    `;
	openComModal(html);
}

// 📸 사진 추가 모달
function photoModal(photo) {
	const html = `
	<div class="w-full max-w-xl mx-auto flex">

	  <!-- 좌측 화살표 -->
	  <button onclick="prevImage()"
	          class="ml-[-20%]">
	    ◀
	  </button>
	  
	  <!-- 이미지 -->
	  <div class="flex-1 overflow-hidden rounded-lg">
	  	<div class="w-full object-cover h-96 transition duration-300">
		<img th:src="${photo.imageUrl}" alt="사진" class="object-cover w-full h-full rounded-lg" />
		</div>
	  </div>

	  <!-- 우측 화살표 -->
	  <button onclick="nextImage()"
	          class="mr-[-20%]">
	    ▶
	  </button>

	</div>

    `;
	openComModal(html);


}

// 아래는 add 로직
function crewArtAdd() {
	const html = `
	<div class="flex h-full">
	<!-- 왼쪽 이미지 영역 (클릭 시 업로드) -->
	  <label for="imageUpload" class="w-1/2 bg-gray-100 cursor-pointer">
	    <img id="previewImage" src="https://via.placeholder.com/500" alt="preview"
	      class="object-cover w-full h-full" />
	    <input type="file" id="imageUpload" name="imageFile" accept="image/*" class="hidden" onchange="previewImage(event)" />
	  </label>

	  <!-- 오른쪽 입력 영역 -->
	  <div class="w-1/2 p-6 flex flex-col justify-between text-gray-800 space-y-4 relative">
	    <!-- 게시글 입력 폼 -->
	    <form action="/usr/article/doWrite" method="post" class="flex-1 flex flex-col justify-between shadow p-4 rounded bg-white">
	      <!-- 제목 입력 -->
	      <div class="mb-4">
	        <label class="block text-sm font-bold mb-1">제목</label>
	        <input type="text" name="title" placeholder="제목을 입력하세요"
	          class="w-full border rounded px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring focus:border-yellow-300" required />
	      </div>

	      <!-- 내용 입력 -->
	      <div class="mb-4 flex-1">
	        <label class="block text-sm font-bold mb-1">내용</label>
	        <textarea name="body" rows="20" placeholder="내용을 입력하세요"
	          class="w-full border rounded px-3 py-2 text-sm shadow-sm resize-none focus:outline-none focus:ring focus:border-yellow-300" required></textarea>
	      </div>

	      <!-- 작성자 및 날짜 (예시) -->
	      <div class="flex justify-between text-xs text-gray-500 mt-2">
	        <span class="font-bold">admin</span>
	        <span>2025.07.20</span>
	      </div>

	      <!-- 등록 버튼 -->
	      <div class="text-right mt-4">
	        <button type="submit"
	          class="bg-gradient-to-r from-green-200 to-yellow-200 px-6 py-2 rounded-full shadow hover:shadow-md">등록</button>
	      </div>
	    </form>
	  </div>
	</div>

	    `;
	openModal(html);
}

function scAdd() {
	const html = `
	<form action="/usr/schedule/doAdd" method="post" class="flex flex-col content-between bg-white p-6 rounded-2xl shadow-md w-[360px] h-[400px]">
	<div class="flex-1">    
	<!-- 캘린더 헤더 -->
	    <div class="flex justify-between items-center mb-4">
	      <button type="button" onclick="prevMonth()" class="text-2xl text-yellow-200 hover:scale-110">←</button>
	      <div id="calendarHeader" class="font-semibold text-lg text-center">2025년 7월</div>
	      <button type="button" onclick="nextMonth()" class="text-2xl text-yellow-200 hover:scale-110">→</button>
	    </div>

	    <!-- 캘린더 본문 -->
	    <table class="w-full text-sm">
	      <thead>
	        <tr class="text-gray-600">
	          <th>일</th><th>월</th><th>화</th><th>수</th><th>목</th><th>금</th><th>토</th>
	        </tr>
	      </thead>
	      <tbody id="calendarBody" class="text-black font-medium"></tbody>
	    </table>

	    <!-- 숨겨진 날짜 필드 -->
	    <input type="hidden" name="date" id="selectedDate" required />
		</div>
	    <!-- 제출 버튼 -->
	    <div class="pt-6 text-center">
	      <button type="submit" class="bg-gradient-to-r from-green-200 to-yellow-200 px-6 py-2 rounded-full shadow hover:shadow-lg">
	        일정 등록
	      </button>
	    </div>
	  </form>
	`;
	openComModal(html);
	setTimeout(() => {
		renderCalendar();
	}, 0);
}

// 전역 상태
let selectedDate = null;
let currentDate = new Date();

// 📅 달력 렌더링
function renderCalendar() {
	const calendarBody = document.getElementById("calendarBody");
	const calendarHeader = document.getElementById("calendarHeader");

	// DOM이 없으면 중단 (방어코드)
	if (!calendarBody || !calendarHeader) {
		console.warn("⛔ 캘린더 요소를 찾을 수 없습니다.");
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

	// hidden input에 값 세팅
	const input = document.getElementById("selectedDate");
	if (input) input.value = dateStr;

	// 기존 선택 스타일 제거
	document.querySelectorAll("#calendarBody td").forEach(td => td.classList.remove("bg-yellow-300"));

	// 현재 선택된 날짜 강조
	if (element) element.classList.add("bg-yellow-300");
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
	    <button onclick="location.href='/usr/walkCrew/crewarticle'" class="w-full text-left text-sm font-medium text-gray-800 hover:text-yellow-500 transition">
	     내가 쓴 글
	    </button>

	    <!-- 멤버 관리 (방장만 노출) -->
	    <button onclick="handleCrewMember()" class="w-full text-left text-sm font-medium text-gray-800 hover:text-yellow-500 transition">
	      멤버 관리
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
			applicants = response.data1;

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
	    </div>
	  </div>
	</div>

	
	    `;
	openModal(html);

	setTimeout(() => renderMemberList(), 0);

}
let members = [];

// 리스트 렌더링
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
				  <p>${data.photo}</p>
				  <p>${data.nickname}</p>
				`;
			detail.dataset.userId = member.id;
			buttons.style.display = "block";
		},
		error: function(err) {
			console.error("참가등록실패", err);
		}
	});


}

// 강퇴 처리
function kickMember() {
	const id = document.getElementById("memberDetail").dataset.userId;
	alert(`❌ ID ${id} 회원 강퇴 처리`);

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
			renderCrewMemberSection();
		},
		error: function(err) {
			console.error("참가등록실패", err);
		}
	});
}








