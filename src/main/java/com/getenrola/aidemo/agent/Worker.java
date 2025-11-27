package com.getenrola.aidemo.agent;

import com.getenrola.aidemo.model.WorkerResponse;

public interface Worker {
    WorkerResponse handle(String input, ConversationState state) throws Exception;
}