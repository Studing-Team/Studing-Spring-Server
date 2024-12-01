package studing.studing_server.notices.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import studing.studing_server.common.BaseTimeEntity;
import studing.studing_server.member.entity.Member;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private  String title;

    @Column(columnDefinition = "TEXT")
    private String content;
    private  String tag;
    private Long noticeLike = 0L;
    private Long viewCount = 0L;
    private Long saveCount = 0L;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeImage> noticeImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeView> noticeViews;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaveNotice> saveNotices;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeLike> noticeLikes;

    @Builder
    public Notice(String title, String content, Member member, String tag) {
        this.title = title;
        this.content = content;
        this.member = member;
        this.tag=tag;
        this.noticeLike = 0L;
        this.viewCount = 0L;
        this.saveCount=0L;
    }
    public void setNoticeLike(Long noticeLike) {
        this.noticeLike = noticeLike;
    }
    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void addNoticeImage(NoticeImage noticeImage) {
        noticeImages.add(noticeImage);
        noticeImage.setNotice(this);
    }


}
