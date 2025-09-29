package com.example.RSW.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.QnaRepository;
import com.example.RSW.vo.Qna;

import java.util.List;

@Service
public class QnaService {

    @Autowired
    private QnaRepository qnaRepository;

    public List<Qna> getPublicFaqList() {
        return qnaRepository.getFaqList();
    }

    public Qna getQnaById(int id) {
        Qna qna = qnaRepository.getQnaById(id);
        if (qna == null) {
            System.out.println("DEBUG: Qna is null");
        } else {
            System.out.println("DEBUG: qna.isAnswered = " + qna.isAnswered());
        }
        return qna;
    }


    public void writeUserQna(int loginedMemberId, String title, String body, boolean isSecret) {
        Qna qna = new Qna();
        qna.setMemberId(loginedMemberId);
        qna.setTitle(title);
        qna.setBody(body);
        qna.setSecret(isSecret);
        qna.setFromUser(true);
        qna.setAnswered(false);
        qna.setActive(true);
        qnaRepository.saveUserQna(qna);
    }

    public List<Qna> getUserQnaByMemberId(int memberId) {
        return qnaRepository.getUserQnaByMemberId(memberId);
    }

    public void deleteQna(int id) {
        qnaRepository.markQnaAsDeleted(id);
    }

    public void modifyQna(int id, String title, String body, boolean isSecret) {
        qnaRepository.updateQna(id, title, body, isSecret);
    }

    public void markAsAnswered(int qnaId) {
        qnaRepository.updateIsAnswered(true, qnaId);
    }


    public List<Qna> getAllQuestions() {
        return qnaRepository.findAll();
    }

    public void markAsAnsweredFalse(int qnaId) {
        qnaRepository.updateIsAnswered(false, qnaId);
    }

    public List<Qna> findAll() {
        List<Qna> list = qnaRepository.findAll();
        for (Qna q : list) {
            System.out.println("DEBUG isSecret: " + q.isSecret()); // ← 여기서 오류 나면 EL도 터짐
        }
        return list;
//        return qnaRepository.findAll();
    }

    public Qna findById(int id) {
        return qnaRepository.findById(id);
    }

    public void update(int id, String title, String body) {
        qnaRepository.update(id, title, body);
    }

    public void delete(int id) {
        qnaRepository.delete(id);
    }

    public void modify(int id, String title, String body) {
        qnaRepository.modify(id, title, body);
    }

    public void writeFaq(int memberId, String title, String body) {
        qnaRepository.insert(memberId, title, body, false, true); // isSecret = false, isFaq = true
    }

    public List<Qna> getSelectedQna() {
        return qnaRepository.getSelectedQna();
    }

    public List<Qna> getMyAnsweredQna(int loginedMemberId) {
        return qnaRepository.getMyAnsweredQna(loginedMemberId);
    }

    public List<Qna> findWithoutAnswer() {
        return qnaRepository.findWithoutAnswer();
    }

    public void setFalse(int qnaId) {
        qnaRepository.setFalse(qnaId);
    }
}