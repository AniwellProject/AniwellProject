/* =========================
   auth_verify.js  (최종 통합본)
========================= */

/* ---- 전역 안전 스텁 ---- */
window.sendEmailVerificationCode = window.sendEmailVerificationCode || function(){};
window.verifyEmailCode          = window.verifyEmailCode          || function(){};
window.sendVerificationCode     = window.sendVerificationCode     || function(){};
window.verifyPhoneCode          = window.verifyPhoneCode          || function(){};

/* ---- 공통 유틸 (프로젝트 스타일 통일) ---- */
// 경고(빨강) 표시 + 입력 테두리 빨강
function showError(warnEl, inputEl, msg) {
    if (warnEl) {
        warnEl.textContent = msg || '';
        warnEl.classList.remove('hidden', 'text-green-600');
        warnEl.classList.add('text-red-600');
    }
    if (inputEl) {
        inputEl.classList.remove('border-green-500');
        inputEl.classList.add('border-red-500');
    }
}
// 안내/성공(초록) 표시 (입력 테두리는 건드리지 않음)
function showInfo(warnEl, msg) {
    if (!warnEl) return;
    warnEl.textContent = msg || '';
    warnEl.classList.remove('hidden', 'text-red-600');
    warnEl.classList.add('text-green-600');
}
// 메시지/스타일 해제
function clearNotice(warnEl, inputEl) {
    if (warnEl) {
        warnEl.textContent = '';
        warnEl.classList.add('hidden');
        warnEl.classList.remove('text-red-600', 'text-green-600');
    }
    if (inputEl) {
        inputEl.classList.remove('border-red-500', 'border-green-500');
    }
}
// DOM 헬퍼
const byId  = (id) => document.getElementById(id);
const show  = (el) => el && el.classList.remove('hidden');
const hide  = (el) => el && el.classList.add('hidden');

/* =========================
   이메일 인증 (회원가입)
========================= */
function initEmailVerifyForSignup(){
    const emailInput = byId('email');
    if (!emailInput) return;

    const emailSendBtn  = byId('emailSendBtn');
    const emailCheckBtn = byId('emailCheckBtn');
    const emailBadge    = byId('emailVerifiedTag');
    hide(emailBadge);

    // 이메일 주소 경고(p) — 입력 아래
    let emailWarn = byId('emailWarning');
    if (!emailWarn) {
        emailWarn = document.createElement('p');
        emailWarn.id = 'emailWarning';
        emailWarn.className = 'block text-xs text-red-600 mt-1 hidden';
        (emailSendBtn?.parentElement || emailInput).insertAdjacentElement('afterend', emailWarn);
    }

    const emailBox = byId('email-verification-box');
    const emailCodeInput = byId('emailVerificationCode');

    // ✅ 코드 전용 경고(p) — 코드 입력칸 바로 아래
    let emailCodeWarn = byId('emailCodeWarning');
    if (!emailCodeWarn) {
        emailCodeWarn = document.createElement('p');
        emailCodeWarn.id = 'emailCodeWarning';
        emailCodeWarn.className = 'block text-xs text-red-600 mt-1 hidden';
        emailBox?.appendChild(emailCodeWarn);
    }

    const isValidEmail = (v)=>/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test((v||'').trim());
    let emailTxId = null;

    async function sendEmailVerificationCode(){
        if (!emailSendBtn) return;
        if (emailSendBtn.dataset.sending === '1') return;
        emailSendBtn.dataset.sending = '1';

        const email = (emailInput.value||'').trim();
        if (!isValidEmail(email)) { showError(emailWarn, emailInput, '유효한 이메일 주소를 입력해주세요.'); emailSendBtn.dataset.sending='0'; return; }
        clearNotice(emailWarn, emailInput);
        clearNotice(emailCodeWarn, emailCodeInput);
        emailSendBtn.disabled = true;

        try {
            const res = await fetch('/api/verify/email/send', {
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body: JSON.stringify({ email, purpose:'signup' })
            });
            let json = null; try { json = await res.json(); } catch(_){}
            if (!res.ok || !(json?.resultCode||'').startsWith('S-')) {
                showError(emailWarn, emailInput, json?.msg || '인증번호 전송에 실패했습니다.');
                emailSendBtn.disabled = false; emailSendBtn.dataset.sending='0';
                return;
            }
            // txId
            emailTxId = json?.txId ?? json?.data?.txId ?? json?.data1?.txId ?? (typeof json?.data1==='string'?json.data1:null);
            show(emailBox);
            showInfo(emailWarn, '인증번호를 이메일로 전송했습니다.');
        } catch (e) {
            showError(emailWarn, emailInput, '네트워크 오류로 전송에 실패했습니다.');
            emailSendBtn.disabled = false; emailSendBtn.dataset.sending='0';
        }
    }

    async function verifyEmailCode(){
        const code = (emailCodeInput?.value||'').trim();
        if (!emailTxId) { showError(emailCodeWarn, emailCodeInput, '먼저 인증번호를 요청해주세요.'); return; }
        if (!code)      { showError(emailCodeWarn, emailCodeInput, '인증번호를 입력해주세요.');   return; }

        try {
            const res = await fetch('/api/verify/email/check', {
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body: JSON.stringify({ txId: emailTxId, code, purpose:'signup' })
            });
            let json = null; try { json = await res.json(); } catch(_){}
            if (!res.ok || !(json?.resultCode||'').startsWith('S-')) {
                // ❗불일치/만료 → 코드 입력칸 아래 빨강
                showError(emailCodeWarn, emailCodeInput, json?.msg || '인증번호가 일치하지 않습니다.');
                return;
            }
            hide(emailBox);
            lockInputNoColor(emailInput);
            markButtonAsDone(emailSendBtn);
            clearNotice(emailCodeWarn, emailCodeInput);
            clearNotice(emailWarn, emailInput);
            alert('이메일 인증이 완료되었습니다.');
        } catch (e) {
            showError(emailCodeWarn, emailCodeInput, '네트워크 오류로 인증에 실패했습니다.');
        }
    }

    // 바인딩
    if (emailSendBtn && !emailSendBtn.dataset.bound) {
        emailSendBtn.dataset.bound = '1';
        emailSendBtn.addEventListener('click', (e)=>{
            e.preventDefault(); e.stopPropagation();
            clearTimeout(emailSendBtn._deb);
            emailSendBtn._deb = setTimeout(()=>sendEmailVerificationCode(), 120);
        });
    }
    if (emailCheckBtn && !emailCheckBtn.dataset.bound) {
        emailCheckBtn.dataset.bound = '1';
        emailCheckBtn.addEventListener('click', (e)=>{
            e.preventDefault(); e.stopPropagation();
            verifyEmailCode();
        });
    }

    // 입력 변경 시 초기화
    emailInput.addEventListener('input', ()=>{
        hide(emailBox);
        clearNotice(emailWarn, emailInput);
        clearNotice(emailCodeWarn, emailCodeInput);
        if (emailSendBtn) {
            emailSendBtn.disabled = false;
            emailSendBtn.textContent = '인증';
            emailSendBtn.classList.remove('opacity-60','cursor-not-allowed');
            emailSendBtn.dataset.sending = '0';
        }
        emailTxId = null;
    });
    emailCodeInput?.addEventListener('input', ()=> clearNotice(emailCodeWarn, emailCodeInput));

    // 전역 호환
    window.sendEmailVerificationCode = sendEmailVerificationCode;
    window.verifyEmailCode           = verifyEmailCode;
}

/* =========================
   전화번호(SMS) 인증 (회원가입/정보수정 공용)
========================= */
function initPhoneVerify(){
    const phoneInput = byId('cellphone');
    if (!phoneInput) return;

    const sendBtn   = byId('phoneSendBtn');
    const verifyBtn = byId('verifyPhoneBtn') || document.querySelector('[data-role="verify-phone"]');
    const phoneBox  = byId('phone-verification-box');
    const codeInput = byId('verificationCode');

    // 전화번호 경고(p) — 입력 아래
    let phoneWarn = byId('phoneWarning') || byId('cellphoneWarning');
    if (!phoneWarn) {
        phoneWarn = document.createElement('p');
        phoneWarn.id = 'phoneWarning';
        phoneWarn.className = 'block text-xs text-red-600 mt-1 hidden';
        (sendBtn?.parentElement || phoneInput).insertAdjacentElement('afterend', phoneWarn);
    }

    // ✅ 코드 전용 경고(p) — 코드 입력칸 바로 아래
    let codeWarn = byId('codeWarning');
    if (!codeWarn) {
        codeWarn = document.createElement('p');
        codeWarn.id = 'codeWarning';
        codeWarn.className = 'block text-xs text-red-600 mt-1 hidden';
        phoneBox?.appendChild(codeWarn);
    }

    const normalize   = (v)=> (v||'').replace(/\D/g,'');
    const isValidPhone= (raw)=> /^01[016789]\d{7,8}$/.test(normalize(raw));

    // 쿨다운
    let cdTimer = null, cdRemain = 0;
    const sendBtnLabel = (sendBtn && sendBtn.textContent) ? sendBtn.textContent : '인증';
    function startCooldown(sec){
        if (!sendBtn) return;
        clearInterval(cdTimer);
        cdRemain = Number(sec) || 60;
        sendBtn.disabled = true;
        sendBtn.textContent = `재전송(${cdRemain}s)`;
        cdTimer = setInterval(()=>{
            cdRemain--;
            if (cdRemain <= 0){
                clearInterval(cdTimer);
                sendBtn.disabled = false;
                sendBtn.textContent = sendBtnLabel;
            } else {
                sendBtn.textContent = `재전송(${cdRemain}s)`;
            }
        }, 1000);
    }

    async function sendVerificationCode(){
        if (!sendBtn) return;
        if (sendBtn.dataset.sending === '1') return;
        sendBtn.dataset.sending = '1';

        const raw = phoneInput.value || '';
        const phone = normalize(raw);

        if (!raw.trim())          { showError(phoneWarn, phoneInput, '전화번호를 입력하세요.'); sendBtn.dataset.sending='0'; return; }
        if (!isValidPhone(raw))   { showError(phoneWarn, phoneInput, '전화번호 형식이 올바르지 않습니다. 예: 000-0000-0000'); sendBtn.dataset.sending='0'; return; }

        clearNotice(phoneWarn, phoneInput);
        clearNotice(codeWarn,  codeInput);

        try {
            const res = await fetch('/api/verify/sms/send', {
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body: JSON.stringify({ phone })
            });
            let json = null; try { json = await res.json(); } catch(_){}
            if (res.ok && json?.resultCode === 'S-OK') {
                show(phoneBox);
                startCooldown(json?.data?.cooldownSec ?? 60);
                showInfo(phoneWarn, '인증번호를 전송했습니다.');
                return;
            }
            if (json?.resultCode === 'F-COOLDOWN') {
                show(phoneBox);
                startCooldown(json?.data?.retryAfterSec ?? 60);
                showInfo(phoneWarn, json?.msg || '재전송 대기 중입니다.');
                return;
            }
            showError(phoneWarn, phoneInput, json?.msg || '인증번호 전송에 실패했습니다.');
            sendBtn.dataset.sending = '0';
        } catch (e) {
            showError(phoneWarn, phoneInput, '네트워크 오류로 전송에 실패했습니다.');
            sendBtn.dataset.sending = '0';
        }
    }

    async function verifyPhoneCode(){
        const raw  = phoneInput.value || '';
        const phone= normalize(raw);
        const code = (codeInput?.value || '').trim();

        if (!raw.trim())        { showError(phoneWarn, phoneInput, '전화번호를 입력하세요.'); return; }
        if (!isValidPhone(raw)) { showError(phoneWarn, phoneInput, '전화번호 형식이 올바르지 않습니다. 예: 000-0000-0000'); return; }
        if (!code)              { showError(codeWarn,  codeInput,  '인증코드를 입력하세요.'); return; }

        try {
            const res = await fetch('/api/verify/sms/confirm', {
                method:'POST',
                headers:{'Content-Type':'application/json'},
                body: JSON.stringify({ phone, code })
            });
            let json = null; try { json = await res.json(); } catch(_){}
            if (res.ok && json?.resultCode === 'S-OK') {
                hide(phoneBox);
                lockInputNoColor(phoneInput);
                lockInputNoColor(codeInput);
                markButtonAsDone(sendBtn);
                if (sendBtn)  sendBtn.disabled = true;
                if (verifyBtn) verifyBtn.disabled = true;
                clearNotice(codeWarn,  codeInput); // 성공 시 해제
                clearNotice(phoneWarn, phoneInput);
                return;
            }
            // ❗불일치/만료 등 실패 → 코드 입력칸 아래 빨강
            showError(codeWarn, codeInput, json?.msg || '인증번호가 일치하지 않습니다.');
        } catch (e) {
            showError(codeWarn, codeInput, '네트워크 오류로 인증에 실패했습니다.');
        }
    }

    // 바인딩(중복 방지)
    if (sendBtn && !sendBtn.dataset.bound) {
        sendBtn.dataset.bound = '1';
        sendBtn.addEventListener('click', (e)=>{
            e.preventDefault(); e.stopPropagation();
            clearTimeout(sendBtn._deb);
            sendBtn._deb = setTimeout(()=>sendVerificationCode(), 120);
        });
    }
    if (verifyBtn && !verifyBtn.dataset.bound) {
        verifyBtn.dataset.bound = '1';
        verifyBtn.addEventListener('click', (e)=>{
            e.preventDefault(); e.stopPropagation();
            verifyPhoneCode();
        });
    }

    // 입력 변경 시 초기화
    phoneInput.addEventListener('input', ()=>{
        clearInterval(cdTimer);
        if (sendBtn){
            sendBtn.disabled = false;
            sendBtn.textContent = sendBtnLabel;
            sendBtn.dataset.sending = '0';
        }
        clearNotice(phoneWarn, phoneInput);
        clearNotice(codeWarn,  codeInput);
        show(phoneBox); // 사용자 경험상 코드칸 유지해도 OK; 숨기고 싶으면 hide(phoneBox)
    });
    codeInput?.addEventListener('input', ()=> clearNotice(codeWarn, codeInput));

    // 전역 호환
    window.sendVerificationCode = sendVerificationCode;
    window.verifyPhoneCode      = verifyPhoneCode;
}

/* =========================
   init
========================= */
document.addEventListener('DOMContentLoaded', () => {
    initEmailVerifyForSignup();
    initPhoneVerify();
});
