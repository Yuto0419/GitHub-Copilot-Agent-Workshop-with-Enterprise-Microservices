package com.skishop.auth.service.compensation;

/**
 * Interface that defines a compensation action.
 */
public interface CompensationAction {
    
    /**
     * Executes the compensation process.
     * @param sagaId Saga ID
     * @param context Context information required for compensation processing
     * @return true if the compensation process succeeds, false if it fails
     */
    boolean compensate(String sagaId, CompensationContext context);
    
    /**
     * Determines whether this compensation action is applicable.
     * @param sagaType Type of Saga
     * @param status Current status
     * @return true if applicable
     */
    boolean isApplicable(String sagaType, String status);
    
    /**
     * Priority of the compensation action (lower value means higher priority)
     * @return Priority
     */
    int getPriority();
    
    /**
     * Name of the compensation action
     * @return Action name
     */
    String getActionName();
}
