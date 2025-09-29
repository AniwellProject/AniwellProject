package com.example.RSW.service;

import com.example.RSW.dto.ExplainRequest;
import com.example.RSW.repository.MedicalDocumentRepository;
import com.example.RSW.repository.PetRepository;
import com.example.RSW.repository.VisitRepository;
import com.example.RSW.vo.MedicalDocument;
import com.example.RSW.vo.Pet;
import com.example.RSW.vo.Visit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VetDocAssembler {

    private final MedicalDocumentRepository docRepo;
    private final VisitRepository visitRepo;
    private final PetRepository petRepo;
    private final OcrNormalizer normalizer;

    /** 문서 id 1건을 표준 ExplainRequest로 만든다(펫 합침). */
    public ExplainRequest fromDocumentId(int documentId) {
        MedicalDocument doc = docRepo.selectById(documentId);
        if (doc == null) throw new IllegalArgumentException("document not found: " + documentId);

        // 1) 문서 → 표준 구조(펫 제외)
        ExplainRequest base = normalizer.fromDocument(doc);

        // 2) Visit/Pet 조회
        Pet pet = null;
        Visit visit = null;
        if (doc.getVisitId() != 0) {
            visit = visitRepo.selectVisitById(doc.getVisitId());
            if (visit != null && visit.getPetId() != 0) {
                pet = petRepo.getPetsById(visit.getPetId());
            }
        }

        // 3) 펫 → DTO 변환 (문서 날짜를 기준일로 사용해 나이 계산)
        LocalDate refDate = parseIsoDate(base.getDocument()!=null ? base.getDocument().getDate() : null);
        ExplainRequest.Pet petDto = PetInfoAdapter.toExplainPet(pet, refDate);

        // 4) provider/date는 base 그대로 사용(visit.provider가 있으면 필요 시 교체 가능)
        String provider = base.getDocument() != null ? base.getDocument().getProvider() : null;
        String date     = base.getDocument() != null ? base.getDocument().getDate()     : null;
        ExplainRequest.Document docDto = new ExplainRequest.Document(provider, date);

        // 5) 최종 병합
        return new ExplainRequest(
                docDto,
                petDto,
                base.getLabs(),
                base.getPrescriptions(),
                base.getMeta()
        );
    }

    private LocalDate parseIsoDate(String s){
        try { return (s==null || s.isBlank()) ? LocalDate.now() : LocalDate.parse(s); }
        catch(Exception e){ return LocalDate.now(); }
    }
}
