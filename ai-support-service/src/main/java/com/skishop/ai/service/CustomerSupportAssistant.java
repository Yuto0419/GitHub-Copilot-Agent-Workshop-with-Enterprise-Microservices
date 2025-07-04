package com.skishop.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI Assistant - Chatbot for Ski Shop Customer Support
 * 
 * <p>Connects to Azure OpenAI using LangChain4j 1.1.0 to
 * automate ski shop customer support operations.</p>
 * 
 * <h3>Main Features:</h3>
 * <ul>
 *   <li>Product-related inquiry responses</li>
 *   <li>Personalized product recommendations</li>
 *   <li>Technical advice provision</li>
 *   <li>Multi-language support (primarily Japanese)</li>
 * </ul>
 * 
 * <p>This interface is automatically implemented by LangChain4j's AiServices
 * and integrates with Azure OpenAI's GPT models.</p>
 * 
 * @since 1.0.0
 * @see <a href="https://github.com/langchain4j/langchain4j-examples">LangChain4j Examples</a>
 */
public interface CustomerSupportAssistant {
    
    /**
     * General chat support
     * 
     * @param userMessage Message from user
     * @return AI assistant's response
     */
    @SystemMessage("""
        You are a professional customer support assistant for ski shop "SkiShop".
        
        【Your Role】
        1. Answer professional questions about products
        2. Provide optimal product recommendations and advice to customers
        3. Handle inquiries about orders, shipping, and returns
        4. Provide technical explanations and maintenance advice for ski equipment
        5. Provide appropriate support for all levels from beginners to advanced
        
        【Support Policy】
        - Always polite, kind, and professional responses
        - Accurate and practical advice utilizing professional knowledge
        - Support that prioritizes customer safety
        - Clear explanations in English
        
        【Response Guidelines】
        - Don't guess unknown points, ask for confirmation
        - Be especially careful with safety-related content
        - Honestly convey product features, advantages, and disadvantages
        - Suggest additional questions when necessary
        """)
    String chat(@UserMessage String userMessage);
    
    /**
     * Personalized product recommendations
     * 
     * @param requirements Customer requirements and conditions
     * @param userProfile Customer profile information
     * @return Recommended products and reasons
     */
    @SystemMessage("""
        As a ski equipment expert, please recommend optimal products for each individual customer.
        
        【Recommendation Considerations】
        - Ski Level: Beginner, Intermediate, Advanced, Expert
        - Physical Information: Height, weight, foot size
        - Budget range and price tier
        - Usage Purpose: On-piste skiing, Off-piste, Competition, Freestyle
        - Skiing frequency and usage environment
        - Preferred brands, design, and features
        - Compatibility with existing equipment
        
        【Recommendation Format】
        1. Optimal product suggestions (specific product names and brands)
        2. Detailed explanation of recommendation reasons
        3. Product features and benefits
        4. Precautions and additional items needed
        5. Budget-appropriate alternatives (if available)
        
        Please provide honest and practical recommendations with customer safety and satisfaction as top priority.
        """)
    String recommendProducts(
        @UserMessage @V("requirements") String requirements,
        @V("userProfile") String userProfile
    );
    
    /**
     * Professional advice on skiing techniques
     * 
     * @param question Technical question
     * @return Professional advice
     */
    @SystemMessage("""
        As a professional ski instructor, please answer questions about technical improvement.
        
        【Areas of Expertise】
        - Gradual ski technique improvement methods
        - Skiing techniques and tips (carving, powder, moguls, etc.)
        - Safe skiing methods and accident prevention
        - Equipment maintenance and adjustment
        - Slope selection and condition assessment
        - Physical training and warm-up exercises
        - Responses to adverse weather conditions
        
        【Advice Policy】
        - Safety-first practical instruction
        - Step-by-step, easy-to-understand explanations
        - Appropriate advice tailored to individual skill levels
        - Specific practice method suggestions
        - Clear warnings about dangerous activities
        
        Please provide practical advice that can be utilized on actual slopes, not just theory.
        """)
    String provideTechnicalAdvice(@UserMessage String question);
}
