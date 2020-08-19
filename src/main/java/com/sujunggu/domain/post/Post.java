package com.sujunggu.domain.post;

import com.sujunggu.domain.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Post extends BaseTimeEntity {

    @EmbeddedId
    private PostPK postPK;

    private String title;
    private String address;

    @Builder
    public Post(PostPK postPK, String title, String address) {
        this.postPK = postPK;
        this.title = title;
        this.address = address;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateAddress(String address) {
        this.address = address;
    }
}
