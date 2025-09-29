package com.example.RSW.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.RSW.repository.BoardRepository;
import com.example.RSW.vo.Board;

import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public Board getBoardById(int boardId) {

        return boardRepository.getBoardById(boardId);
    }

    public List<Board> getBoards() {
        return boardRepository.getBoards();
    }


}