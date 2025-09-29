document.addEventListener("DOMContentLoaded", () => {
    const joinForm = document.querySelector('.login-form--register form');
    if (!joinForm) return;

    const pwInput = joinForm.querySelector("#loginPw");
    const pwConfirmInput = joinForm.querySelector("#loginPwConfirm");
    const pwMessage = joinForm.querySelector("#pwMessage");
    const pwWarning = joinForm.querySelector("#pwWarning");

    function preventKoreanInput(input) {
        let isComposing = false;

        input.addEventListener("compositionstart", () => {
            isComposing = true;
        });

        input.addEventListener("compositionend", (e) => {
            isComposing = false;
            if (/[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(e.data)) {
                input.value = input.value.replace(/[ㄱ-ㅎㅏ-ㅣ가-힣]/g, "");
            }
            if (input === pwInput) validatePassword();
        });

        input.addEventListener("input", () => {
            if (!isComposing) {
                input.value = input.value.replace(/[ㄱ-ㅎㅏ-ㅣ가-힣]/g, "");
                if (input === pwInput) validatePassword();
            }
        });

        input.addEventListener("keydown", (e) => {
            if (/^[ㄱ-ㅎㅏ-ㅣ가-힣]$/.test(e.key)) {
                e.preventDefault();
            }
        });

        input.addEventListener("beforeinput", (e) => {
            if (e.data && /[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(e.data)) {
                e.preventDefault();
            }
        });

        input.addEventListener("paste", (e) => {
            const pasteData = (e.clipboardData || window.clipboardData).getData('text');
            if (/[ㄱ-ㅎㅏ-ㅣ가-힣]/.test(pasteData)) {
                e.preventDefault();
            }
        });
    }

    preventKoreanInput(pwInput);
    preventKoreanInput(pwConfirmInput);

    // ✅ 유효성 검사 함수
    function validatePassword() {
        const pw = pwInput.value.trim();
        pwInput.classList.remove("border-red-500", "border-green-500");
        pwWarning.classList.add("hidden");
        pwWarning.textContent = "";
        pwWarning.classList.remove("text-red-600", "text-green-600");

        if (pw === "") return;

        if (/(012|123|234|345|456|567|678|789|890)/.test(pw)) {
            pwInput.classList.add("border-red-500");
            pwWarning.textContent = "연속된 숫자는 사용할 수 없습니다.";
            pwWarning.classList.add("text-red-600");
            pwWarning.classList.remove("hidden");
            return;
        }

        if (/(\w)\1\1/.test(pw)) {
            pwInput.classList.add("border-red-500");
            pwWarning.textContent = "같은 문자를 3번 이상 반복할 수 없습니다.";
            pwWarning.classList.add("text-red-600");
            pwWarning.classList.remove("hidden");
            return;
        }

        if (pw.length < 8 || pw.length > 16) {
            pwInput.classList.add("border-red-500");
            pwWarning.textContent = "비밀번호는 8자리 이상 16자리 이하로 입력해주세요.";
            pwWarning.classList.add("text-red-600");
            pwWarning.classList.remove("hidden");
            return;
        }

        if (!/[A-Za-z]/.test(pw)) {
            pwInput.classList.add("border-red-500");
            pwWarning.textContent = "영문자가 포함되어야 합니다.";
            pwWarning.classList.add("text-red-600");
            pwWarning.classList.remove("hidden");
            return;
        }

        if (!/\d/.test(pw)) {
            pwInput.classList.add("border-red-500");
            pwWarning.textContent = "숫자가 포함되어야 합니다.";
            pwWarning.classList.add("text-red-600");
            pwWarning.classList.remove("hidden");
            return;
        }

        if (!/[!@#$%^&*(),.?":{}|<>]/.test(pw)) {
            pwInput.classList.add("border-red-500");
            pwWarning.textContent = "특수문자가 포함되어야 합니다.";
            pwWarning.classList.add("text-red-600");
            pwWarning.classList.remove("hidden");
            return;
        }

        pwInput.classList.add("border-green-500");
        pwWarning.textContent = "사용 가능한 비밀번호입니다.";
        pwWarning.classList.add("text-green-600");
        pwWarning.classList.remove("hidden");
    }

    function checkPasswordMatch() {
        const pw = pwInput.value.trim();
        const pwConfirm = pwConfirmInput.value.trim();

        pwConfirmInput.classList.remove("border-red-500", "border-green-500");
        pwMessage.classList.add("hidden");
        pwMessage.textContent = "";
        pwMessage.classList.remove("text-red-600", "text-green-600");

        if (pwConfirm === "") return;

        if (pw === pwConfirm) {
            pwConfirmInput.classList.add("border-green-500");
            pwMessage.textContent = "비밀번호가 일치합니다.";
            pwMessage.classList.add("text-green-600");
            pwMessage.classList.remove("hidden");
        } else {
            pwConfirmInput.classList.add("border-red-500");
            pwMessage.textContent = "비밀번호가 일치하지 않습니다.";
            pwMessage.classList.add("text-red-600");
            pwMessage.classList.remove("hidden");
        }
    }

    pwInput.addEventListener("keyup", checkPasswordMatch);
    pwConfirmInput.addEventListener("keyup", checkPasswordMatch);

    function togglePassword(input, btn, openIcon, closedIcon) {
        btn.addEventListener("click", () => {
            const isPw = input.type === "password";
            input.type = isPw ? "text" : "password";
            openIcon.classList.toggle("hidden", !isPw);
            closedIcon.classList.toggle("hidden", isPw);
        });
    }

    togglePassword(
        pwInput,
        joinForm.querySelector("#togglePw"),
        joinForm.querySelector("#iconOpenPw"),
        joinForm.querySelector("#iconClosedPw")
    );
    togglePassword(
        pwConfirmInput,
        joinForm.querySelector("#togglePwConfirm"),
        joinForm.querySelector("#iconOpenPwConfirm"),
        joinForm.querySelector("#iconClosedPwConfirm")
    );
});
