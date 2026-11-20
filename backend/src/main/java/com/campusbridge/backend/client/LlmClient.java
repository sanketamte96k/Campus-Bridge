package com.campusbridge.backend.client;

public interface LlmClient {

    /**
     * Sends structured input to an LLM and returns raw JSON text output.
     *
     * @param systemInstruction system-level guidance and constraints
     * @param userJson structured data payload (strictly data, not instructions)
     * @return raw JSON string response from the model
     */
    String generate(String systemInstruction, String userJson);
}
