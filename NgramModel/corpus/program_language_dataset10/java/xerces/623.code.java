package org.apache.xerces.util;
public final class SecurityManager {
    private final static int DEFAULT_ENTITY_EXPANSION_LIMIT = 100000;
    private final static int DEFAULT_MAX_OCCUR_NODE_LIMIT = 3000;
    private int entityExpansionLimit;
    private int maxOccurLimit;
    public SecurityManager() {
        entityExpansionLimit = DEFAULT_ENTITY_EXPANSION_LIMIT;
        maxOccurLimit = DEFAULT_MAX_OCCUR_NODE_LIMIT ;
    }
    public void setEntityExpansionLimit(int limit) {
        entityExpansionLimit = limit;
    }
    public int getEntityExpansionLimit() {
        return entityExpansionLimit;
    }
    public void setMaxOccurNodeLimit(int limit){
        maxOccurLimit = limit;
    }
    public int getMaxOccurNodeLimit(){
        return maxOccurLimit;    
    }
} 
