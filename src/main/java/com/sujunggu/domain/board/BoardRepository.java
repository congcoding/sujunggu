package com.sujunggu.domain.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.OrderBy;
import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Integer> {

    @OrderBy("college")
    List<Board> findAll();

    Board findOneByBoardNo(int boardNo);
}
