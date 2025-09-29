package com.example.RSW.service;

import com.example.RSW.repository.VetAnswerRepository;
import com.example.RSW.vo.Rq;
import com.example.RSW.vo.VetAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VetAnswerService {

    @Autowired
    private VetAnswerRepository vetAnswerRepository;

    @Autowired
    private Rq rq;

    public void write(int qnaId, int memberId, String answer, String vetName) {
        Map<String, Object> param = new HashMap<>();
        param.put("qnaId", qnaId);
        param.put("memberId", memberId);
        param.put("answer", answer);
        param.put("vetName", vetName);

        vetAnswerRepository.insert(param);
    }

    // 관리자용 (로그인 정보 활용)
    public void write(int qnaId, String answer, String vetName) {
        int memberId = rq.getLoginedMemberId();
        write(qnaId, memberId, answer, vetName);
    }




    public List<VetAnswer> getByQnaId(int qnaId) {
        return vetAnswerRepository.getByQnaId(qnaId);
    }

    public VetAnswer getById(int id) {
        return vetAnswerRepository.getById(id);
    }

    public void modify(int id, String answer) {
        vetAnswerRepository.update(id, answer);
    }

    public void delete(int id) {
        vetAnswerRepository.delete(id);
    }

    public int getQnaIdByAnswerId(int answerId) {
        return vetAnswerRepository.getQnaIdByAnswerId(answerId);
    }

    public VetAnswer findByQnaId(int qnaId) {
        return vetAnswerRepository.findByQnaId(qnaId);
    }


    public void update(int id, String answer) {
        vetAnswerRepository.update(id, answer);
    }


}

