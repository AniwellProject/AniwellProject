package com.example.RSW.repository;

import org.apache.ibatis.annotations.Mapper;

import com.example.RSW.vo.Board;

import java.util.List;

@Mapper
public interface BoardRepository {

    public Board getBoardById(int id);

    public List<Board> getBoards();
}