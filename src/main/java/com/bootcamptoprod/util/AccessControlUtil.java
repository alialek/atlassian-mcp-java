package com.bootcamptoprod.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccessControlUtil {

    @Value("${mcp.read-only:false}")
    private boolean readOnlyMode;

    public void checkWriteAccess(String operation) {
        if (readOnlyMode) {
            throw new SecurityException("Cannot perform " + operation + " in read-only mode");
        }
    }

    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    public boolean isWriteOperation(String toolName) {
        return toolName != null && (
            toolName.contains("create") ||
            toolName.contains("update") ||
            toolName.contains("delete") ||
            toolName.contains("add") ||
            toolName.contains("remove")
        );
    }
} 