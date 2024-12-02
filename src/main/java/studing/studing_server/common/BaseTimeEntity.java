package studing.studing_server.common;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;



    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime()
                .truncatedTo(ChronoUnit.SECONDS);
    }


    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                .toLocalDateTime()
                .truncatedTo(ChronoUnit.SECONDS);
    }

}
