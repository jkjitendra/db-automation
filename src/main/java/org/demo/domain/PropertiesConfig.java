package org.demo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "properties_config")
public class PropertiesConfig {

    @Id
    @Column(length = 50)
    private String appProfile;

    @Lob
    private String properties;

    @Column(name = "Create_TimeStamp", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createTimestamp;

    @Column(name = "Last_Update_TimeStamp", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime lastUpdateTimestamp;

}
