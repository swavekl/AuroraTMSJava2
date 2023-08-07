package com.auroratms.audit;

import lombok.Getter;
import lombok.ToString;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "audit", indexes = {
        @Index(name = "idx_auditentity", columnList = "eventIdentifier")
})
@Getter
@ToString
public class AuditEntity {

    // unique id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // profile id of user who generated this event
    private String profileId;

    // date and time when this event occurred
    private Date eventTimestamp;

    // type of event e.g. score entry, entry or withdrawal into/from tournament
    private String eventType;

    // some combination of ids which group several events together e.g. matchCardId-matchId
    private String eventIdentifier;

    // JSON string representing details of this event
    @Column(length = 4000)
    private String detailsJSON;

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public void setEventTimestamp(Date eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setEventIdentifier(String eventIdentifier) {
        this.eventIdentifier = eventIdentifier;
    }

    public void setDetailsJSON(String detailsJSON) {
        this.detailsJSON = detailsJSON;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        AuditEntity that = (AuditEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
