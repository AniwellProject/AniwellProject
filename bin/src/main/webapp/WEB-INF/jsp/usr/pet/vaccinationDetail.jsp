<%@ page contentType="text/html;charset=UTF-8" %>
<div class="vaccine-card" data-id="${petVaccination.id}">

  <!-- ❌ 닫기 버튼 -->
  <button class="btn-close">❌</button>

  <!-- ✅ 상세 보기 영역 -->
  <div class="detail-view">
    <h3>🩺 백신 상세 정보</h3>
    <ul class="vaccine-info">
      <li><strong>백신 이름:</strong> ${petVaccination.vaccineName}</li>
      <li><strong>접종 날짜:</strong> ${petVaccination.injectionDate}</li>
      <li><strong>다음 접종 예정일:</strong> ${petVaccination.nextDueDate}</li>
      <li><strong>비고:</strong> ${petVaccination.notes}</li>
    </ul>
    <div class="vaccine-actions">
      <button class="btn-modify">✏️ 수정</button>
      <button class="btn-delete">❌ 삭제</button>
    </div>
  </div>

  <!-- 📝 수정 폼 영역 (초기에 숨김) -->
  <div class="edit-form" style="display:none;">
    <h3>📝 백신 정보 수정</h3>
    <form id="modifyForm">
      <input type="hidden" name="vaccinationId" value="${petVaccination.id}" />
      <ul class="vaccine-info">
        <li><strong>백신 이름:</strong> <select name="vaccineName" required>
          <option value="">선택하세요</option>
          <option value="Rabies">Rabies</option>
          <option value="Parvovirus">Parvovirus</option>
          <option value="Distemper">Distemper</option>
          <option value="Feline Distemper">Feline Distemper</option>
          <option value="Feline Leukemia">Feline Leukemia</option>
          <option value="Leptospirosis">Leptospirosis</option>
          <option value="Bordetella">Bordetella</option>
          <option value="Feline Panleukopenia">Feline Panleukopenia</option>
          <option value="FIP">FIP</option>
        </select></li>
        <li><strong>접종 날짜:</strong> <input type="date" name="injectionDate" value="${petVaccination.injectionDate}" required /></li>
        <li><strong>다음 접종 예정일:</strong> <input type="date" name="nextDueDate" value="${petVaccination.nextDueDate}" /></li>
        <li><strong>비고:</strong> <textarea name="notes">${petVaccination.notes}</textarea></li>
      </ul>
      <div class="vaccine-actions">
        <button type="submit" class="btn-save">💾 저장</button>
        <button type="button" class="btn-cancel">❌ 취소</button>
      </div>
    </form>
  </div>
</div>
