package com.vastpro.sphinx.dto;

public class TopicDTO {
	private String topicId;
	private String topicName;
	public String getTopicId() {
		return topicId;
	}
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
	public TopicDTO(String topicId, String topicName) {
		super();
		this.topicId = topicId;
		this.topicName = topicName;
	}
	
	public TopicDTO() {
	}

}
