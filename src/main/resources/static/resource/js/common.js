const paw = document.getElementById('cat-paw');
const btn = document.getElementById('hamburger-btn');

let isVisible = false;

btn.addEventListener('click', () => {
    isVisible = !isVisible;
    if (isVisible) {
        paw.classList.remove('left-[-100%]');
        paw.classList.add('left-0');
    } else {
        paw.classList.remove('left-0');
        paw.classList.add('left-[-100%]');
    }
});


function App_app() {
    const [pets, setPets] = React.useState([null])
    const [loginedMember, setLoginedMember] = React.useState(null)
    const [crew, setCrew] = React.useState(null)


    React.useEffect(() => {
        fetch(`/usr/member/myPage`)
            .then(res => res.json())
            .then((memberData) => {
                console.log("로그인 멤버:", memberData);
                setLoginedMember(memberData);
                window.localStorage.setItem("loginedMemberId", memberData.id);
            });
    }, []);

    React.useEffect(() => {
        fetch(`/api/pet/list?memberId=${loginedMember?.id}`)
            .then(res => res.json())
            .then((data) => {
                console.log(data)
                console.log("petlist: ", data.pets)
                setPets(data.pets || []); // ← 정확히 'pets'를 받아야 함
                setCrew(data.crews || []);
            });
    }, [loginedMember])
    // sidebar.js

};

const mId = localStorage.getItem("loginedMember");
const memberPhotoDiv = document.getElementById('memberPhoto');
const defaultPhoto = "/img/default-pet.png";


function applyUserUI(data) {
    const level = Number(data.authLevel);
    const adminEl = document.getElementById('adminPage');
    const vetEl = document.getElementById('vetPage');

    // 7이면 관리자만 보이기, 3이면 수의사만 보이기
    adminEl.classList.toggle('hidden', level !== 7);
    vetEl.classList.toggle('hidden', level !== 3);
}

function e() {

    $.ajax({
        type: "GET",
        url: `/api/member/getUsrInfo`,
        data: {memberId: mId},
        success: function (data) {
            authLevel = data.authLevel;
            applyUserUI(data);
            const photoUrl = typeof data.photo === 'string' && data.photo.trim() !== ""
                ? data.photo
                : defaultPhoto;


            const img = document.createElement('img');
            img.src = photoUrl;
            img.alt = "프로필";
            img.className = "w-full h-full object-cover";

            // 이전 내용 초기화 후 삽입
            memberPhotoDiv.innerHTML = "";
            memberPhotoDiv.appendChild(img);

            f = true;
        },
        error: function (err) {
            console.error("getUsrInfo 실패", err);
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    // 🔹 사용자 정보 불러오기
    e();
    if (mId > 0) {
        connectWebSocket(mId);
        updateNotificationBadge();  // ✅ 초기에 숫자 표시
    }

    // 🔹 메뉴 클릭 이벤트
    document.querySelectorAll('.menu-item').forEach((item) => {
        item.addEventListener('click', () => {
            const page = item.dataset.page;
            let url = '';
            const petId = window.localStorage.getItem('selectedPetId');
            const loginedMemberId = window.localStorage.getItem('loginedMember');

            switch (page) {
                case 'pet':
                    if (!petId) {
                        alert("🐾 반려동물을 등록해주세요!");
                        return;
                    }
                    url = `/usr/pet/petPage?petId=${petId}`;
                    break;
                case 'my':
                    url = `/usr/pet/list?memberId=${loginedMemberId}`;
                    break;
                case 'crew':
                    url = `/usr/walkCrew/list`;
                    break;
                case 'qna':
                    url = `/usr/qna/list`;
                    break;
                case 'admin':
                    url = `/adm/article/list`;
                    break;
                case 'vet':
                    url = `/usr/vetAnswer/vetList`;
                    break;
            }
            window.parent.location.href = url;
        });
    });

    // 🔹 고양이 발 이미지 슬라이드 설정
    document.querySelectorAll('.menu-item').forEach((item) => {
        const container = item.querySelector('#cat_hand');

        if (container) {
            container.innerHTML = `
				<img src="https://res.cloudinary.com/decrm0hhf/image/upload/h_90,c_fill,q_auto,f_auto/v1752334976/cat_hand_w9zkku.png"
					 alt="고양이 발"
					 class="cat-paw w-full h-full object-contain rotate-90" />
			`;

            container.classList.add(
                "absolute", "top-[-27px]", "left-[-100px]",
                "group-hover:left-[-20px]",
                "transition-all", "duration-500",
                "z-20", "pointer-events-none"
            );

            item.classList.add("relative", "group");
        }
    });

});


let comStompClient = null;

function updateNotificationBadge() {
    fetch('/usr/notifications/unreadCount')
        .then(res => res.json())
        .then(json => {
            if (json.resultCode !== "S-1") {
                console.warn("❗ 알림 수 조회 실패", json.msg);
                return;
            }

            const badge = document.getElementById('notiCountBadge');
            const count = json.data1 ?? 0;

            if (count > 0) {
                badge.textContent = count;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        })
        .catch(err => {
            console.error("❌ 알림 수 가져오기 실패", err);
        });

}

function connectWebSocket() {
    const socket = new SockJS('/ws');
    comStompClient = Stomp.over(socket);

    comStompClient.connect({}, function () {
        comStompClient.subscribe('/topic/notifications/' + mId, function (msg) {
            // 실시간 알림 수신
            console.log("실시간 알림 도착:", msg.body);
            updateNotificationBadge();  // ✅ 뱃지 숫자만 갱신
        });
    });
}

function memberOpenModal() {
    document.getElementById("myModal").classList.remove("hidden");
}

function memberCloseModal() {
    document.getElementById("myModal").classList.add("hidden");
}

function logout() {
    Swal.fire({
        title: '로그아웃 하시겠습니까?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '네, 로그아웃',
        cancelButtonText: '취소',
        reverseButtons: true
    }).then((result) => {
        if (result.isConfirmed) {
            // ✅ 확인 버튼 누르면 진행
            Swal.fire({
                title: '로그아웃 중...',
                timer: 500,
                didOpen: () => {
                    Swal.showLoading();
                },
                allowOutsideClick: false,
                showConfirmButton: false
            });

            setTimeout(() => {
                fetch('/usr/member/doLogout', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    }
                })
                    .then(res => {
                        if (!res.ok) throw new Error("서버 응답 오류");
                        return res.text();
                    })
                    .then(() => {
                        memberCloseModal();
                        location.href = '/';
                    })
                    .catch(err => {
                        Swal.fire({
                            icon: 'error',
                            title: '❌ 로그아웃 실패',
                            text: err.message
                        });
                    });
            }, 500);
        }
    });
}

function doWithDraw() {
    Swal.fire({
        title: '정말로 회원 탈퇴하시겠습니까?',
        text: '탈퇴 후에는 계정을 복구할 수 없습니다.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '네, 탈퇴할게요',
        cancelButtonText: '취소',
        reverseButtons: true
    }).then((result) => {
        if (result.isConfirmed) {
            // 진행 중 알림
            Swal.fire({
                title: '회원 탈퇴 중...',
                timer: 800,
                didOpen: () => {
                    Swal.showLoading();
                },
                allowOutsideClick: false,
                showConfirmButton: false
            });

            setTimeout(() => {
                fetch('/usr/member/doWithdraw', {
                    method: 'POST'
                })
                    .then(res => res.json())
                    .then(json => {
                        if (json.resultCode && json.resultCode.startsWith('S-')) {
                            Swal.fire({
                                icon: 'success',
                                title: '탈퇴 완료',
                                text: '그동안 이용해주셔서 감사합니다.',
                                timer: 2000,
                                showConfirmButton: false
                            }).then(() => {
                                window.location.href = '/';
                            });
                        } else {
                            throw new Error(json.msg || '회원 탈퇴에 실패했습니다.');
                        }
                    })
                    .catch(err => {
                        Swal.fire({
                            icon: 'error',
                            title: '❌ 탈퇴 실패',
                            text: err.message
                        });
                    });
            }, 800);
        }
    });
}



function submitCertificate() {
    Swal.fire({
        icon: 'info',
        title: '인증서 제출 페이지로 이동합니다',
        showConfirmButton: false,
        timer: 500,
        timerProgressBar: true,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    memberCloseModal(); // 모달 닫기

    setTimeout(() => {
        window.location.href = "/usr/member/myCert";
    }, 500);
}


document.getElementById("myModal").addEventListener("click", (e) => {
    if (e.target.id === "myModal") memberCloseModal();
});





