// DOM 준비 후 시작
window.addEventListener("load", start);

function start() {
	/* ===========================
        Elements Selectors
    ============================ */
	const elm = {
		arrow: document.querySelector(".form-container__arrow"),
		overlay: document.querySelector(".overlay"),
		title: document.querySelector(".title"),
		signUpButton: document.querySelector(".buttons__signup"),
		loginButton: document.querySelector(".buttons__signup--login"),
		loginForm: document.querySelector(".login-form"),
		registerForm: document.querySelector(".login-form--register")
	};

	/* ===========================
        Properties Object
    ============================ */
	const props = {
		left: "left: 20px;",
		bottom: "bottom: -500px;",
		transition1: "transition: bottom 1s;",
		transition2: "transition: bottom 2s;",
		opacity0: "opacity: 0;",
		opacity1: "opacity: 1;",
		trnsDelay: "transition-delay: 1s;",
		zIndex: "z-index: 6;",
		left0: "left: 0;",
		trnsDelay0: "transition-delay: 0s;",
		zIndex0: "z-index: 0;",
		leftM120: "left: -120px;"
	};

	/* ===========================
        Elements Array
    ============================ */
	// null 요소는 제외해서 transition 시 에러 방지
	const elms = [
		elm.arrow,
		elm.overlay,
		elm.title,
		elm.signUpButton,
		elm.loginButton,
		elm.loginForm,
		elm.registerForm
	].filter(Boolean);

	function transition(elements, properties) {
		const len = Math.min(elements.length, properties.length);
		for (let i = 0; i < len; i++) {
			// setAttribute 전에 요소 존재 보장
			const node = elements[i];
			if (node) node.setAttribute("style", String(properties[i]));
		}
	}

	/* ===========================
        Events (존재할 때만 바인딩)
    ============================ */
	const signUpBtn = document.getElementById("signUp");
	if (signUpBtn) {
		signUpBtn.onclick = function () {
			const properties = [
				props.left,
				props.opacity0,
				props.opacity0,
				`${props.transition1} ${props.bottom}`,
				`${props.transition2} ${props.bottom}`,
				props.opacity0,
				`${props.opacity1} ${props.trnsDelay} ${props.zIndex}`
			];
			transition(elms, properties);
		};
	}

	const loginBtn = document.getElementById("login");
	if (loginBtn) {
		loginBtn.onclick = function () {
			const properties = [
				props.left,
				props.opacity0,
				props.opacity0,
				`${props.transition1} ${props.bottom}`,
				`${props.transition2} ${props.bottom}`,
				`${props.opacity1} ${props.trnsDelay} ${props.zIndex}`,
				props.opacity0
			];
			transition(elms, properties);
		};
	}

	const arrowClick = document.getElementById("arrowClick");
	if (arrowClick) {
		arrowClick.onclick = function () {
			const properties = [
				props.leftM120,
				props.opacity1,
				props.opacity1,
				props.opacity1,
				props.opacity1,
				`${props.opacity0} ${props.trnsDelay0} ${props.zIndex0}`,
				`${props.opacity0} ${props.trnsDelay0} ${props.zIndex0}`
			];
			transition(elms, properties);
		};
	}
}

/* ===========================
    Find ID / PW Modals
=========================== */

function openFindIdIframe() {
	const modal = document.getElementById("findModal");
	const iframe = document.getElementById("findIdIframe");
	if (!modal || !iframe) return;
	iframe.src = "/usr/member/findLoginId";
	modal.classList.remove("hidden");
}

function closeFindIdIframe() {
	const modal = document.getElementById("findModal");
	const iframe = document.getElementById("findIdIframe");
	if (!modal || !iframe) return;
	modal.classList.add("hidden");
	iframe.src = "";
}

function openFindPwIframe() {
	const modal = document.getElementById("findPwModal");
	const iframe = document.getElementById("findPwIframe"); // ⚠️ iframe ID 분리
	if (!modal || !iframe) return;
	iframe.src = "/usr/member/findLoginPw";
	modal.classList.remove("hidden");
}

function closeFindPwIframe() {
	const modal = document.getElementById("findPwModal");
	const iframe = document.getElementById("findPwIframe"); // ⚠️ iframe ID 분리
	if (!modal || !iframe) return;
	modal.classList.add("hidden");
	iframe.src = "";
}

/* ===========================
    Ajax: Find ID
=========================== */
$(function () {
	const $form = $("#findIdForm");
	if ($form.length === 0) return;

	$form.on("submit", function (e) {
		e.preventDefault();

		// 같은 페이지에 같은 ID가 여러 개일 위험을 줄이기 위해 폼 스코프 사용
		const name = ($form.find('[name="name"]').val() || "").trim() || ($("#name").val() || "").trim();
		const email = ($form.find('[name="email"]').val() || "").trim() || ($("#email").val() || "").trim();

		if (!name) {
			Swal.fire({ icon: "warning", title: "이름을 입력해주세요" });
			$form.find('[name="name"]').focus();
			return;
		}
		if (!email) {
			Swal.fire({ icon: "warning", title: "이메일을 입력해주세요" });
			$form.find('[name="email"]').focus();
			return;
		}

		$.ajax({
			url: "/usr/member/doFindLoginId",
			type: "POST",
			data: { name, email },
			success: function (res) {
				// ✅ 성공(S-)만 성공 알림
				if (res && typeof res.resultCode === "string" && res.resultCode.startsWith("S-")) {
					const getLoginId = res.data1 || res?.data?.loginId || "";
					Swal.fire({
						icon: "success",
						title: "아이디 찾기 결과",
						text: `아이디는 [${getLoginId}] 입니다`
					});
					closeFindIdIframe();
					return;
				}

				// ✅ 없음/불일치(F-1 또는 F-404)는 사용자 친화 문구로 통일
				if (res?.resultCode === "F-1" || res?.resultCode === "F-404") {
					Swal.fire({
						icon: "error",
						title: "일치하는 정보가 없습니다",
						text: "일치하는 이름 또는 이메일이 없습니다."
					});
					return;
				}

				// ✅ 그 외 서버 메시지 노출
				Swal.fire({
					icon: "error",
					title: "요청 실패",
					text: res?.msg || "서버 오류가 발생했습니다."
				});
			},
			error: function (err) {
				Swal.fire({
					icon: "error",
					title: "요청 실패",
					text: err?.responseText || "서버 오류가 발생했습니다."
				});
			}
		});
	});
});

/* ===========================
    바깥 클릭 시 닫기
=========================== */
document.addEventListener("click", function (event) {
	// ID 모달
	const idModal = document.getElementById("findModal");
	const idContent = document.getElementById("findModalContent");
	if (
		idModal &&
		idContent &&
		!idModal.classList.contains("hidden") &&
		idModal.contains(event.target) &&
		!idContent.contains(event.target)
	) {
		closeFindIdIframe();
	}

	// PW 모달 (ID 오타 수정: findPwModalContent)
	const pwModal = document.getElementById("findPwModal");
	const pwContent = document.getElementById("findPwModalContent");
	if (
		pwModal &&
		pwContent &&
		!pwModal.classList.contains("hidden") &&
		pwModal.contains(event.target) &&
		!pwContent.contains(event.target)
	) {
		closeFindPwIframe();
	}
});

/* ===========================
    Ajax: Find PW
=========================== */
$(function () {
	const $form = $("#findPwForm");
	if ($form.length === 0) return;

	$form.on("submit", function (e) {
		e.preventDefault();

		const loginId = ($form.find('[name="loginId"]').val() || $("#loginId").val() || "").trim();
		const emailRaw = ($form.find('[name="email"]').val() || $("#email").val() || "").trim();

		if (!loginId) {
			Swal.fire({ icon: "warning", title: "아이디를 입력해주세요" });
			$form.find('[name="loginId"]').focus();
			return;
		}
		if (!emailRaw) {
			Swal.fire({ icon: "warning", title: "이메일을 입력해주세요" });
			$form.find('[name="email"]').focus();
			return;
		}

		const email = emailRaw.toLowerCase(); // 🔹 프론트에서 정규화

		$.ajax({
			url: "/usr/member/doFindLoginPw",
			type: "POST",
			data: { loginId, email },
			success: function (res) {
				if (res?.resultCode?.startsWith("S-")) {
					Swal.fire({
						icon: "success",
						title: "임시 비밀번호 발송",
						text: "메일로 임시 비밀번호를 발송했습니다."
					});
					closeFindPwIframe();
					return;
				}

				// 🔹 아이디 없음/이메일 불일치/Not found → 동일 안내
				if (res?.resultCode === "F-1" || res?.resultCode === "F-2" || res?.resultCode === "F-404") {
					Swal.fire({
						icon: "error",
						title: "일치하는 정보가 없습니다",
						text: "해당하는 아이디 또는 이메일이 없습니다."
					});
					return;
				}

				// 기타 실패 메시지
				Swal.fire({
					icon: "error",
					title: "요청 실패",
					text: res?.msg || "요청을 처리하지 못했습니다."
				});
			},
			error: function (err) {
				Swal.fire({
					icon: "error",
					title: "서버 오류",
					text: err.responseText || "요청 중 문제가 발생했습니다."
				});
			}
		});
	});
});
