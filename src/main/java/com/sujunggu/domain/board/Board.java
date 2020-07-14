package com.sujunggu.domain.board;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@NoArgsConstructor
@Entity
public class Board {

    @Id
    private int boardNo;

    private String name;
    private String address;

    @Builder
    public Board(int boardNo, String name, String address) {
        this.boardNo = boardNo;
        this.name = name;
        this.address = address;
    }
}
