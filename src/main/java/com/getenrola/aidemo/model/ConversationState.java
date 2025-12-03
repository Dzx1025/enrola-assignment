package com.getenrola.aidemo.model;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
    ConversationState maintains the state of a conversation including message history,
    slots for storing key-value pairs, and an interest score to track user engagement.

    Shared across workers to maintain context.
 */
public class ConversationState {
    private final List<String> history = new ArrayList<>();
    private final Map<String, Object> slots = new ConcurrentHashMap<>();
    private int interestScore = 5; // Default interest score
    private String currentStage;

    public void addMessage(String message) {
        history.add(message);
    }

    public List<String> getHistory() {
        return history;
    }

    public List<String> getHistory(int lastN) {
        if (lastN <= 0 || lastN >= history.size()) {
            return history;
        }
        return history.subList(history.size() - lastN, history.size());
    }

    public void putSlot(String k, Object v) {
        slots.put(k, v);
    }

    public Object getSlot(String k) {
        return slots.get(k);
    }

    public Map<String, Object> getSlots() {
        return slots;
    }

    public int getInterestScore() {
        return interestScore;
    }

    public void setInterestScore(int interestScore) {
        this.interestScore = interestScore;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }
}