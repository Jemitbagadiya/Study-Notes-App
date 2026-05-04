package com.example.studynotes;

public class NoteModel {

    String id;
    String topic;
    String content;
    String date;

    public NoteModel(String id,
                     String topic,
                     String content,
                     String date) {

        this.id = id;
        this.topic = topic;
        this.content = content;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }
}