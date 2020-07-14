package com.sujunggu.web.dto;

import com.sujunggu.domain.post.Post;
import com.sujunggu.domain.post.PostPK;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCrawlingDto {

    private PostPK postPK;
    private String title;
    private String address;

    @Builder
    public PostCrawlingDto(PostPK postPK, String title, String address) {
        this.postPK = postPK;
        this.title = title;
        this.address = address;
    }

    public Post toEntity() {
        return Post.builder()
                .postPK(postPK)
                .title(title)
                .address(address)
                .build();
    }
}
