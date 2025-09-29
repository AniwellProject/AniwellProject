<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>백신 등록</title>
</head>
<body>


<div class="vaccine-card" id="addVaccineCard">
  <button class="btn-close">❌</button>
  <form id="addForm">

    <input type="hidden" name="petId" value="${param.petId}" />
    <ul class="vaccine-info">
      <li>
        <strong>백신 이름:</strong>
        <select name="vaccineName" required>
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
        </select>
      </li>
      <li>
        <strong>접종 날짜:</strong>
        <input type="date" name="injectionDate" required />
      </li>

      <li>
        <strong>비고:</strong>
        <textarea name="notes" rows="3"></textarea>
      </li>
    </ul>

    <div class="vaccine-actions">
      <button type="submit" class="btn-save">💾 등록</button>
      <button type="button" class="btn-cancel">❌ 취소</button>
    </div>
  </form>
</div>
</body>
</html>
