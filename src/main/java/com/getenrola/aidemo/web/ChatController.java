package com.getenrola.aidemo.web;

import com.getenrola.aidemo.agent.ConversationState;
import com.getenrola.aidemo.agent.Orchestrator;
import com.getenrola.aidemo.model.AgentResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final Orchestrator orchestrator;
    private final Map<String, ConversationState> sessions = new ConcurrentHashMap<>();

    public ChatController(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/message")
    public AgentResult message(@RequestParam String sessionId, @RequestBody String message) throws Exception {
        ConversationState state = sessions.computeIfAbsent(sessionId, k -> new ConversationState());
        return orchestrator.route(message, state);
    }
}