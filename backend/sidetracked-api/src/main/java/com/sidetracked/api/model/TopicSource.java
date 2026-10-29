package com.sidetracked.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/*
 * A structured starting point for researching a topic.
 *
 * Example:
 *
 * label:      "Big Ear Radio Observatory: The Wow! Signal"
 * url:        "https://www.bigear.org/wow.htm"
 * sourceType: "PRIMARY SOURCE"
 */
@Embeddable
public class TopicSource {

    private String label;

    @Column(length = 2000)
    private String url;

    private String sourceType;

    public TopicSource() {
    }

    public TopicSource(
        String label,
        String url,
        String sourceType
    ) {
        this.label = label;
        this.url = url;
        this.sourceType = sourceType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
